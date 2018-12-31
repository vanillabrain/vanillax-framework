package vanillax.framework.batch.service.admin

import vanillax.framework.batch.scheduler.ScheduleInfoRepository
import vanillax.framework.batch.scheduler.ScheduleType
import vanillax.framework.webmvc.service.ServiceBase

class schedule extends ServiceBase {


    def find(data) {
        def paramScheduleType = data._param['scheduleType']
        def paramType = data._param['type']
        def paramScript = data._param['script']
        def paramSort = data._param['sort']
        if( !paramScheduleType && paramType){
            paramScheduleType = paramType
        }

        def scheduleType = null
        if(paramScheduleType){
            if(paramScheduleType.toLowerCase() in ['cr','cron']){
                scheduleType = ScheduleType.CRON
            }else if(paramScheduleType.toLowerCase() in ['fd','fixed_delay','fixeddelay','delay']){
                scheduleType = ScheduleType.FIXED_DELAY
            }
        }

        def keyList = ScheduleInfoRepository.instance.scheduleList
        def list = []
        keyList.each{
            list << ScheduleInfoRepository.instance.get(it)
        }

        list = list.findAll{ it ->
            !scheduleType || scheduleType == it.scheduleType
        }.findAll{ it ->
            !paramScript || it.script.toLowerCase().indexOf(paramScript.toLowerCase()) > -1
        }.sort{
            if(!paramSort) {
                it.script
            } else if(paramSort.endsWith('script')){
                it.script
            } else if(paramSort.endsWith('scheduleType') || paramSort.endsWith('type')){
                it.scheduleType
            } else {
                it.script
            }
        }
        if(paramSort && paramSort.startsWith("-")){
            list = list.reverse()
        }

        data.scheduleList = list
        return data
    }


}
