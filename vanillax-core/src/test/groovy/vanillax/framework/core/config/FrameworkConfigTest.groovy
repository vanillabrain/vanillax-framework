package vanillax.framework.core.config

import spock.lang.Specification

class FrameworkConfigTest extends Specification {

    def "configTest"(){
        setup:
        Properties prop = new Properties()
        prop.put("script.base.path","./src/test/groovy")
        prop.put("script.reload","true")
        prop.put("int.value1","1231")
        prop.put("int.value2","1231 \r\n\t")
        prop.put("int.value3","a1231")
        prop.put("double.value1","1.2312")
        prop.put("double.value2","    1.2312    \r\n\t")
        prop.put("double.value3","a1.2312")
        prop.put("bool.value1","true")
        prop.put("bool.value2","false")
        prop.put("bool.value3","true     ")
        prop.put("bool.value4","     false    ")

        when:
        FrameworkConfig.setProp(prop)

        then:
        println "-----------------"
        FrameworkConfig.getInt('int.value1') == 1231
        FrameworkConfig.getInt('int.value2') == 1231
//        FrameworkConfig.getInt('int.value3') != 1231
        FrameworkConfig.getDouble('double.value1') == 1.2312D
        FrameworkConfig.getDouble('double.value2') == 1.2312D
//        FrameworkConfig.getDouble('double.value3') != 1.2312
        FrameworkConfig.getBoolean('bool.value1') == true
        FrameworkConfig.getBoolean('bool.value2') == false
        FrameworkConfig.getBoolean('bool.value3') == true
        FrameworkConfig.getBoolean('bool.value4') == false

    }
}