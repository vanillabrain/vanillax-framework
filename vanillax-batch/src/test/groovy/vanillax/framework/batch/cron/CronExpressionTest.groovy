package vanillax.framework.batch.cron

import spock.lang.Specification

import java.time.ZoneId
import java.time.ZonedDateTime

class CronExpressionTest extends Specification {
    TimeZone original
    ZoneId zoneId

    def "cron 표현식 검증"(){
        setup:
        original = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"))
        CronExpression cronExpr = new CronExpression("* * * * * *")

        when:
        ZonedDateTime a = makeZonedDateTime(after)
        ZonedDateTime b = makeZonedDateTime(expected)

        then:
        cronExpr.nextTimeAfter(a).equals(b)
        cronExpr.isNowOnTime()
        TimeZone.setDefault(original)
        println "-----------------"
        println "OK"

        where:
        after                | expected
        [2012,4,10,13,0,1]   | [2012,4,10,13,0,2]
        [2012,4,10,13,59,59] | [2012,4,10,14,0,0]

    }
    def makeZonedDateTime(int y, int m, int d, int h, int mm, int s){
        zoneId = TimeZone.getDefault().toZoneId()
        return ZonedDateTime.of(y, m, d, h, mm, s, 0, zoneId)
    }
    def makeZonedDateTime(List arr){
        return makeZonedDateTime(arr[0], arr[1], arr[2], arr[3], arr[4], arr[5])
    }
}
