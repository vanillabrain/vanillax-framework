package vanillax.framework.core.object

import groovy.util.logging.Log

/**
 * Created by gaedong on 2016. 8. 22..
 */

//@Log4j2
//@Slf4j
@Log
class GroovyObject01 {

    def main(){
        hello()
    }

    @vanillax.framework.core.db.Transactional(autoCommit = false)
    def hello(){
        println "hello world!"
        log.info "hahahaha"
        log.info "jajajajajaj"
        //logger.info "hahahahah"
    }

    @vanillax.framework.core.db.Transactional()
    def hello2(){
        println "hello world2!"
    }

}



