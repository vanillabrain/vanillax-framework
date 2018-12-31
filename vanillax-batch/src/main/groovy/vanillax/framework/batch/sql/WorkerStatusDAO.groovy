package vanillax.framework.batch.sql

import vanillax.framework.core.db.orm.Delete
import vanillax.framework.core.db.orm.Insert
import vanillax.framework.core.db.orm.Repository
import vanillax.framework.core.db.orm.Select
import vanillax.framework.core.db.orm.Update
import vanillax.framework.core.db.script.Velocity

@Repository("vanillax_batch") //Spring Repository와 같은 개념. DataSource 이름을 value로 입력
interface WorkerStatusDAO {


    @Update(''' 
        update WorkerCommand
            set resultCd = :resultCd
                , modDate = now()
        where resultCd = 'C'
    ''')
    int updateWorkerCommand(Map x)

    @Delete(''' 
        delete from WorkerStatus
    ''')
    boolean deleteWorkerStatusAll(Map x)

    @Velocity //Velocity 문법처리
    @Delete(''' 
        delete from WorkerStatus 
        where 1=1
        #not_in($workers $x "script") '$x.script' #end
    ''')
    boolean deleteWorkerStatusExclude(Map x)

    @Insert('''
        INSERT INTO WorkerStatus(script, statusCd, lastStartedTime, lastWorkedTime, executionTime, modDate)
            VALUES(:script, :statusCd, :lastStartedTime, :lastWorkedTime, :executionTime, now() )
        ON DUPLICATE KEY UPDATE
            statusCd = :statusCd, lastStartedTime = :lastStartedTime,
            lastWorkedTime = :lastWorkedTime, executionTime = :executionTime, modDate = now()
        ''')
    List insertWorkerStatusList(List list) //다건입력


    @Select('''

        ''')
    List selectWorkerStatus(Map x)

}