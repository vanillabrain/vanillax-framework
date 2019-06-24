package vanillax.framework.core.util.json

import spock.lang.Specification
import vanillax.framework.core.util.StringUtil


class JsonOutputTest extends Specification{
    def testJsonStringify(){
        setup:
        def vo = new ValueObject1("a","b","c","d","e")

        when:
        String s = JsonOutput.toJson(vo)

        then:
        println "-----------------"
        println( s )

    }


}
