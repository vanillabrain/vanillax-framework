package vanillax.framework.batch.sql

import vanillax.framework.core.db.orm.Insert
import vanillax.framework.core.db.orm.Repository
import vanillax.framework.core.db.orm.Select
import vanillax.framework.core.db.orm.Update

@Repository("vanillax_batch") //Spring Repository와 같은 개념. DataSource 이름을 value로 입력
interface CommonSequenceDataDAO {

    @Update(''' 
        UPDATE CommonSequenceData 
        SET sequenceCurValue = :sequenceCurValue, 
            sequenceCycle = :sequenceCycle
        WHERE sequenceName=:sequenceName
    ''')
    int updateCommonSequenceData(Map x)

    @Insert('''
        INSERT INTO CommonSequenceData(
            sequenceName, sequenceIncrement, sequenceMinValue, 
            sequenceMaxValue, sequenceCurValue, sequenceCycle
        ) 
        VALUES(:sequenceName, 1, 1, 999999999, 1, 0)
     ''')
    def insertCommonSequenceData(Map x) //단건입력

    @Select('''
        SELECT
            sequenceName as "sequenceName",
            sequenceIncrement as "sequenceIncrement",
            sequenceMinValue as "sequenceMinValue",
            sequenceMaxValue as "sequenceMaxValue",
            sequenceCurValue as "sequenceCurValue",
            sequenceCycle as "sequenceCycle" 
        FROM CommonSequenceData 
        WHERE sequenceName = :sequenceName
     ''')
    Map selectCommonSequenceData(Map x) //단건조회

}