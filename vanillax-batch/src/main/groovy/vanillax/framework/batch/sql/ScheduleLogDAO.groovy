package vanillax.framework.batch.sql

import vanillax.framework.core.db.orm.Insert
import vanillax.framework.core.db.orm.Repository
import vanillax.framework.core.db.orm.Select
import vanillax.framework.core.db.orm.Update
import vanillax.framework.core.db.script.TimestampFields
import vanillax.framework.core.db.script.Velocity

@Repository("vanillax_batch") //Spring Repository와 같은 개념. DataSource 이름을 value로 입력
interface ScheduleLogDAO {

    @Update(''' 
        UPDATE VXBATCH_ActionScheduleLog
        SET
            statusCode='TERM',
            modUser='spider',
            modDate= :modDate
        WHERE statusCode = :statusCode
    ''')
    int updateScheduleLogAsTerminated(Map x)

    @Velocity
    @Update(''' 
        update VXBATCH_ActionScheduleLog
            set statusCode = :statusCode,
                #if($error) 
                    error = :error,
                #end
                endTime = :nowDate,
                duration = :duration,
                modUser = 'spider',
                modDate = :nowDate
        where id = :id
    ''')
    int updateScheduleLog(Map x)

    @Insert('''
        INSERT INTO VXBATCH_ActionScheduleLog(id, script, startTime, statusCode, actionCount, error, endTime, duration, regUser, regDate)
        VALUES(:id, :script, :nowDate, 'DO', 0, null, null, null, 'spider', :nowDate)
     ''')
    def insertScheduleLog(Map x) //단건입력

    @TimestampFields('startTime, endTime')
    @Select('''
        SELECT
            id as "id",
            script as "script",
            startTime as "startTime",
            statusCode as "statusCode",
            actionCount as "actionCount",
            error as "error",
            endTime as "endTime",
            duration as "duration"
        FROM VXBATCH_ActionScheduleLog 
        WHERE script = :script
          AND startTime > :startTime
        ORDER BY id DESC
        LIMIT 1
     ''')
    Map selectScheduleLogLast(Map x) //단건조회

}