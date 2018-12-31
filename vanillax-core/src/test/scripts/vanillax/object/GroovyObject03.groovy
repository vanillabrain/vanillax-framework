package vanillax.framework.object

import groovy.util.logging.Log
import vanillax.framework.core.db.Transactional
import vanillax.framework.core.object.ObjectLoader

/**
 * Created by gaedong on 2016. 8. 22..
 */

//@Log4j2
//@Slf4j
@Log
class GroovyObject03 {

    def main(){
        hello()
    }

    @Transactional(autoCommit = false)
    def hello(){
        println "hello world!"
        log.info "hahahaha"
        log.info "jajajajajaj"
        //logger.info "hahahahah"
    }

    @Transactional()
    def hello2(){
        println "hello world2!"
        def object = ObjectLoader.load("vanillax.framework.object.GroovyObject04")
        object.hello()
    }

}



