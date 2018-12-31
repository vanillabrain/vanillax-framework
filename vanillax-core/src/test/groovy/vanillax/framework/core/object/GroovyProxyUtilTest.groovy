package vanillax.framework.core.object

import spock.lang.Specification
import vanillax.framework.core.config.FrameworkConfig

class GroovyProxyUtilTest extends Specification {

    def "Transactional 객체를 로딩"(){
        setup:
        Properties prop = new Properties()
        prop.put("script.base.path","./src/test/groovy")
        prop.put("script.reload","true")
        FrameworkConfig.setProp(prop)

        when:
        GroovyObject object = ObjectLoader.load("GroovyObject01")

        then:
        println "-----------------"
        object.hello() == null
        object.hello2() == null
    }
}
