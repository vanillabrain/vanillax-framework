package vanillax.framework.core.object

import spock.lang.Specification
import vanillax.framework.core.config.FrameworkConfig

class GroovyObjectLoadingTest extends Specification {

    def "객체를 로딩"(){
        setup:
        Properties prop = new Properties()
        prop.put("script.base.path","./src/test/scripts")
        prop.put("script.reload","true")
        FrameworkConfig.setProp(prop)

        when:
        def object = ObjectLoader.load("vanillax.framework.object.GroovyObject04")

        then:
        println "-----------------"
        object.hello() == null

    }
}
