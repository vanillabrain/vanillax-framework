package vanillax.framework.core.util

import spock.lang.Specification


class StringUtilTest extends Specification{
    def testMakeInString(){
        setup:
        def list = []
        list << [id:1, go:"haha"]
        list << [id:2, go:"haha1"]
        list << [id:3, go:"haha1"]
        def comparedPath = '/hello/gaga/abcdef.groovy'

        when:
        list.size() > 0

        then:
        println "-----------------"
        println( StringUtil.makeInString(list,"id") )

        println( StringUtil.match(comparedPath,"/hello/*/abc*f.gr*v*") )
        println( StringUtil.match(comparedPath,"*/abc*f.gr*vi*") )
    }


}
