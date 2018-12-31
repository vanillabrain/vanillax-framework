package vanillax.framework.core.object.proxy


import vanillax.framework.core.db.orm.Delete
import vanillax.framework.core.db.orm.Update

@vanillax.framework.core.db.orm.Repository("dataSourceName") //Spring Repository와 같은 개념. DataSource 이름을 value로 입력. 입력값이 없을 경우 기본 Data Source를 사용한다.
interface StudentSqlInterfaceSample {

    @vanillax.framework.core.db.script.Velocity //Velocity 문법처리
    @vanillax.framework.core.db.orm.Select('''
        SELECT a,b,c, '하하하' as d
        FROM Student
        WHERE 1=1
        #if($a) AND a = :a #end
        #in($names $x "my_name") '$x.l' #end
        #if($b) $b #end
        LIMIT 0,1
        ''')
    List selectList(Map x)// IN () 예제

    @vanillax.framework.core.db.script.Velocity //Velocity 문법처리
    @vanillax.framework.core.db.orm.Select('''
        SELECT a,b,c, '하하하' as d
        FROM Student
        WHERE 1=1
        #not_in($names $one "my_name") '$one.name' #end
        LIMIT 0,1
        ''')
    List selectListNotIn(Map x)// NOT IN() 예제

    @vanillax.framework.core.db.orm.Select('''
        SELECT a,b,c
        FROM Student
        WHERE 1=1
        AND a = :a
        ''')
    Map selectOne(Map x) // 단건조회

    @vanillax.framework.core.db.orm.Insert('''
        INSERT INTO Student
        (a,b,c) VALUES (:a, :b, :c)
        ''')
    def insertOne(Map x) //단건입력, 자동생성 PK가 있을 경우 그 값을 반환한다.

    @vanillax.framework.core.db.orm.Insert('''
        INSERT INTO Student
        (a,b,c) VALUES (:a, :b, :c)
        ''')
    List insertList(List list) //다건입력, 자동생성 PK가 있을 경우 그 값을 List에 담아 반환한다.

    @Update(''' 
        UPDATE Student 
        SET 
            studentName = :studentName, 
            email = :email, 
            userPhoto = :userPhoto, 
            providerType = :providerType, 
            visitCnt = :visitCnt, 
            modDate = NOW()
        WHERE id = :id
    ''')
    int updateOne(Map x) // 단건 갱신. 변경된 건수가 반환된다.

    @Update(''' 
        UPDATE Student  
        SET 
            studentName = :studentName, 
            email = :email, 
            userPhoto = :userPhoto, 
            providerType = :providerType, 
            visitCnt = :visitCnt, 
            modDate = NOW()
        WHERE id = :id
    ''')
    List updateList(List list)// 다건 갱신. 각 SQL의 실행에 따른 변경 건수가 List에 담아 반환된다.

    @Delete(''' 
        DELETE FROM Student 
        WHERE id = :id
    ''')
    boolean deleteOne(Map x)// 단건 삭제. 반환값은 의미가 없다.

    @Delete(''' 
        DELETE FROM zStudent 
        WHERE id = :id
    ''')
    List deleteList(List list)//다건 삭제. 반환값은 의미가 없다.
}
