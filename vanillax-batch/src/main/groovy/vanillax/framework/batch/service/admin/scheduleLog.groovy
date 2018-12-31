package vanillax.framework.batch.service.admin

import vanillax.framework.batch.sql.ScheduleLogDAO
import vanillax.framework.core.db.Transactional
import vanillax.framework.core.object.Autowired
import vanillax.framework.webmvc.service.ServiceBase

import java.sql.Timestamp

class scheduleLog extends ServiceBase {

    @Autowired
    ScheduleLogDAO scheduleLogDAO

    @Transactional()
    def find(data) {
        validate(data)
        def script = data._path
        def param = [script:script]
        param['startTime'] = new Timestamp(System.currentTimeMillis() - 30*1000)
        def map = scheduleLogDAO.selectScheduleLogLast(param)
        data.secheduleLog = map
        return data
    }

    private def validate(data){
        if(data._path){
            //OK
        }else{
            throw new Exception("required id : script")
        }
    }

    @Transactional()
    def insert(data) {
        def param = data._input
        return data
    }

}
