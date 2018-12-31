package vanillax.framework.batch.sql

import vanillax.framework.core.db.orm.Repository
import vanillax.framework.core.db.orm.Select
import vanillax.framework.core.db.script.Velocity

@Repository("vanillax_batch") //Spring Repository와 같은 개념. DataSource 이름을 value로 입력
interface ActionScheduleDAO {

    @Velocity
    @Select('''
        select
            A.script as "script",
            coalesce(A.intervalTime, B.intervalTime) as "intervalTime",
            coalesce(A.idleTime, B.idleTime) as "idleTime",
            A.mainScript as "mainScript",
            coalesce(A.preScript, B.preScript) as "preScript",
            coalesce(A.postScript, B.postScript) as "postScript",
            coalesce(A.errorScript, B.errorScript) as "errorScript",
            A.inputParam as "inputParam",
            A.active as "active",
            A.orderSeq as "orderSeq",
            A.scheduleType as "scheduleType",
            A.cron as "cron"
         from ActionSchedule A, ActionSchedule B
        where A.script != 'GLOBAL_CONFIG'
          and B.script = 'GLOBAL_CONFIG'
          #if($script)
          and A.script = :script
          #end
          #if($active)
          and A.active = :active
          #end
        order by A.orderSeq
        ''')
    List selectActiveActionSchedule(Map x)

}