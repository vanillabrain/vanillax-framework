package vanillax.framework.core.db.script

import spock.lang.Specification

class VelocityFacadeTest extends Specification{

    def test1(){
        setup:
        def param = [a:"aaa",b:"order by hahahah"]
        def list = []
        list << [l:"haha1"]
        list << [l:"haha2"]
        list << [l:"haha3"]
        param.names = list

        def sql= ''' 
            SELECT a,b,c, '하하하' as d
            FROM Student
            WHERE 1=1
            #if($a) AND a = :a #end
            #in($names $x "my_name") '$x.l' #end
            #notin($names $x "my_name") '$x.l' #end
            #not_in($names $x "my_name") '$x.l' #end
            #or_in($names $x "my_name") '$x.l' #end
            #or_not_in($names $x "my_name") '$x.l' #end
            #if($b) $b #end
            LIMIT 0,1
        '''

        when:
        param

        then:
        println "-----------------"
        def vel = VelocityFacade.compile(sql,"velocity-name-01");
        def vel1 = VelocityFacade.compile(sql,"velocity-name-01");
        def out = VelocityFacade.apply(vel, param)
        println out
    }


}
