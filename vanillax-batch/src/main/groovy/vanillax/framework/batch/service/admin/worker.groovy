package vanillax.framework.batch.service.admin

import groovy.json.JsonParserType
import groovy.json.JsonSlurper
import vanillax.framework.batch.cron.CronExpression
import vanillax.framework.batch.sql.ActionScheduleDAO
import vanillax.framework.batch.scheduler.ScheduleInfo
import vanillax.framework.batch.scheduler.ScheduleType
import vanillax.framework.batch.scheduler.WorkerInfo
import vanillax.framework.batch.scheduler.WorkerScheduler
import vanillax.framework.core.db.Transactional
import vanillax.framework.core.object.Autowired
import vanillax.framework.batch.scheduler.ScheduleInfoRepository
import vanillax.framework.webmvc.service.ServiceBase

class worker extends ServiceBase {
    def statusCode = [C:"Cancelled", D:"Done", A:"Active", S:"Sleeping"]

    @Autowired
    ActionScheduleDAO actionScheduleDAO

    def find(data) {
        def paramStatus = data._param['status']
        def paramScript = data._param['script']
        def paramId = data._param['id']
        def paramSort = data._param['sort']
        WorkerScheduler scheduler = WorkerScheduler.getInstance()

        def list = scheduler.workerInfoList
        list.each {
            it.statusName = statusCode[it.statusCd]
        }
        list = list.findAll{ it ->
            !paramStatus || ( paramStatus && paramStatus.equalsIgnoreCase(it.statusCd) )
        }.findAll{ it ->
            !paramScript || ( paramScript && it.script.toLowerCase().indexOf(paramScript.toLowerCase()) > -1 )
        }.findAll{ it ->
            !paramId || ( paramId && it.id == paramId as Integer )
        }.sort{
            if(!paramSort) {
                if(it.lastStartedTime) {
                    -it.lastStartedTime.time
                }else{
                    it.id
                }
            } else if(paramSort.endsWith('script')){
                it.script
            } else if(paramSort.endsWith('id')){
                it.id
            } else if(paramSort.endsWith('statusCd')){
                it.statusCd
            } else if(paramSort.endsWith('delay')){
                it.delay
            } else if(it.lastStartedTime){
                -it.lastStartedTime.time
            } else {
                it.id
            }
        }
        if(paramSort && paramSort.startsWith("-")){
            list = list.reverse()
        }

        data.workerList = list
        return data
    }

    /**
     * Worker Scheduler를 핸들링한다.
     * Worker를 구동, 중지, 추가, 삭제 등을 수행한다.
     * @param data
     * @return
     */
    def post(data) {
        def param = data._input
        if(!param.command){
            throw new Exception("잘못된 요청입니다.")
        }
        WorkerScheduler workerScheduler = WorkerScheduler.instance
        ScheduleInfoRepository scheduleInfoRepository = ScheduleInfoRepository.instance
        if(param.command == "WO"){
            //Worker start Once
            //Worker를 일회성으로 실행한다.
            String script = param.script
            // Schedule 정보를 조회한다.
            ScheduleInfo scheduleInfo = selectActionSchedule([script:script])
            scheduleInfo.setActive(true) //Active가 아니더라도 강제로 active상태로 변경하여 실행케 한다.
            // Scheduler를 이용하여 실행시킨다.
            data.id = workerScheduler.startWorkerOnce(scheduleInfo, 1)
        }else if(param.command == "WH"){
            //Worker Halt
            //Worker를 종료시킨다.
            // 스케줄에 등록되어있는 경우 스케줄정보도 삭제한다. 더 이상 실행되지 않는다.
            // Cron 스케줄일 경우도 취소된다. --> 고민이 필요하다.
            int scriptId = param.id
            WorkerInfo workerInfo = workerScheduler.getWorkerInfo(scriptId)
            String script = null
            if(workerInfo){
                script = workerInfo.script
            }
            workerScheduler.shutdownWorker(scriptId)
            Thread.sleep(100) //잠시 기다려준다.
            workerScheduler.removeUselessWorkerHandle(scriptId)
            if(script && !workerScheduler.hasScheduledWorker(script)){
                scheduleInfoRepository.remove(script)
            }
        }else if(param.command == "WS"){
            //put Worker in Schedule
            //Worker를 Schedule에 등록한다.
            //Cron 스케줄일 경우에는 ScheduleInfoRepository에만 등록한다.
            String script = param.script
            if(workerScheduler.hasScheduledWorker(script) || scheduleInfoRepository.containsCron(script)){
                //WorkerScheduler이나 Cron 스케줄에 이미 등록되어있는 경우라면 오류발생
                throw new Exception("이미 등록되어 있는 스케줄입니다 : "+script)
            }
            // Schedule 정보를 조회한다.
            ScheduleInfo scheduleInfo = selectActionSchedule([script:script, active:"Y"])
            // Scheduler를 이용하여 Worker 구동 schedule에 등록한다.
            if(scheduleInfo.getScheduleType() == ScheduleType.FIXED_DELAY){
                data.id = workerScheduler.arrangeWorkerOnSchedule(scheduleInfo)
            }
            //Cron schedule일 경우에는 CronWatchDog이 해당시간이 맞춰서 실행시켜준다.
            scheduleInfoRepository.addSchedule(scheduleInfo)
        }else if(param.command == "WR"){
            //Worker Re-arrange
            //Worker를 종료시킨후 스케줄러에 다시 등록한다.
            String script = param.script
            // Schedule 정보를 조회한다.
            ScheduleInfo scheduleInfo = selectActionSchedule([script:script, active:"Y"])
            if(workerScheduler.hasScriptOnProcess(script)){
                throw new Exception("실행중인 스크립트는 재구동 할 수 없습니다 : $script")
            }
            workerScheduler.cancelWorker(script) //Scheduler에 등록된 worker를 작동중지 및 스케줄 취소시킨다.
            // Scheduler를 이용하여 Worker 구동 schedule에 등록한다.
            if(scheduleInfo.getScheduleType() == ScheduleType.FIXED_DELAY){
                data.id = workerScheduler.arrangeWorkerOnSchedule(scheduleInfo)
            }
            //Cron schedule일 경우에는 CronWatchDog이 해당시간이 맞춰서 실행시켜준다.
            scheduleInfoRepository.addSchedule(scheduleInfo)
            Thread.sleep(100)
            workerScheduler.removeUselessWorkerHandles()
        }else if(param.command == "WU"){
            //Worker Useless clear
            //Worker 사용하지 않는 정보 삭제
            //Cron 스케줄에서는 삭제하지 않는다.
            workerScheduler.removeUselessWorkerHandles()
        }else{
            throw new Exception("허용되지 않는 명령입니다 : $param.command")
        }
        return data
    }

    /**
     * ActionSchedule 정보를 조회한다.
     * @param param 조회조건
     * @return 조회조건에 맞지않으면 Exception이 발생한다.
     */
    @Transactional()
    private ScheduleInfo selectActionSchedule(param){
        ScheduleInfo scheduleInfo = new ScheduleInfo()
        def list = actionScheduleDAO.selectActiveActionSchedule(param)
        if(list && list.size()){
            Map map = list[0]
            scheduleInfo.setScript(param.script)
            scheduleInfo.setInterval(map['intervalTime'])
            if(map['mainScript']) {
                scheduleInfo.setMainScript(map['mainScript']);
            }else{
                scheduleInfo.setMainScript(map['script']);
            }
            if(map['idleTime']) {
                scheduleInfo.setIdleTime(map['idleTime'])
            }
            if(map['postScript']) {
                scheduleInfo.setPostScript(map['postScript'])
            }
            if(map['preScript']) {
                scheduleInfo.setPreScript(map['preScript'])
            }
            if(map['active']) {
                scheduleInfo.setActive('Y' == map['active'])
            }
            if(map['scheduleType']) {
                if(map['scheduleType'] == 'FD'){
                    scheduleInfo.setScheduleType(ScheduleType.FIXED_DELAY)
                }else if(map['scheduleType'] == 'CR'){
                    scheduleInfo.setScheduleType(ScheduleType.CRON)
                    scheduleInfo.setCronExpression(new CronExpression(map['cron']))
                }
            }
            def inputParam = null
            if(map['inputParam']){
                JsonSlurper json = new JsonSlurper().setType(JsonParserType.LAX)
                ByteArrayInputStream bin = new ByteArrayInputStream(map['inputParam'].getBytes('UTF-8'))
                def object = json.parse(bin,'UTF-8')
                if(!inputParam){
                    inputParam = [:]
                }
                object.each { key, val ->
                    inputParam[key] = val
                }
            }
            scheduleInfo.setInputParam(inputParam)
        }else{
            throw new Exception("대상 script의 정보가 DB에 존재하지 않거나 활성화 되어있지 않습니다 : $param.script")
        }
        return scheduleInfo
    }
}
