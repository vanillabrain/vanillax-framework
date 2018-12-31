package vanillax.framework.object

import groovy.util.logging.Log
import vanillax.framework.core.object.ObjectLoader

/**
 * Created by gaedong on 2016. 8. 22..
 */

//@Log4j2
@Log
class GroovyObject04 {

    def hello(){
        GroovyObject03 object03 = ObjectLoader.load("vanillax.framework.object.GroovyObject03")
        object03.hello()
    }

}



