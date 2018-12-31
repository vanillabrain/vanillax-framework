package vanillax.framework.core.object


/**
 * Created by gaedong on 2016. 8. 22..
 */

class GroovyObject02 {

    def hello(){
        GroovyObject01 object01 = ObjectLoader.load("GroovyObject01")
        object01.hello()
    }

}



