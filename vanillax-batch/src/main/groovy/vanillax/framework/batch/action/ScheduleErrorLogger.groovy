/*
 * Copyright (C) 2016 Vanilla Brain, Team - All Rights Reserved
 *
 * This file is part of 'VanillaTopic'
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Vanilla Brain Team and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Vanilla Brain Incorporated
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Vanilla Brain Team.
 */

package vanillax.framework.batch.action

import groovy.json.JsonBuilder
import groovy.util.logging.Log
import vanillax.framework.batch.sql.ScheduleLogDAO
import vanillax.framework.core.object.Autowired
import vanillax.framework.core.db.Transactional

import java.sql.Timestamp

/**
 * Scheduler에 의해 수행된 Action에서 오류가 발생할 경우 호출되는 후처리 Action이다.
 * 오류내용을 로그에 입력한다.
 */
@Log
class ScheduleErrorLogger extends ErrorActionBase{
    @Autowired
    ScheduleLogDAO scheduleLogDAO

    @Transactional(autoCommit = false)
    def process(obj, Throwable t){
        log.info "I'm ScheduleErrorLog ....."
        StringWriter sw = new StringWriter()
        t.printStackTrace(new PrintWriter(sw))
        obj.error = sw.toString()
        obj.duration = System.currentTimeMillis() - obj.startTime
//        sendSlackAlert(obj) //서버 Deploy 시에만 적용
        obj.statusCode = 'ERR'
        obj.nowDate = new Timestamp(System.currentTimeMillis())
        scheduleLogDAO.updateScheduleLog(obj)
        return obj
    }

    def sendSlackAlert(obj) {
        def emojiList = [":spider:",":ghost:",":monkey_face:",":ant:",":beetle:",":bug:",":bee:",":unicorn_face:"]
        //def slackUrl = "https://hooks.slack.com/services/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" //dev-vanillabrain-alert
        def slackUrl = "https://hooks.slack.com/services/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" //real-vanillabrain-alert
        def jsonBuilder = new JsonBuilder()
        def filterTxt = filterTrace(obj.error)
        def emojiIdx = new Random().nextInt(emojiList.size())
        def str = "script:${obj.script} \nduration: ${obj.duration} \n${filterTxt}"
        jsonBuilder {
            text str
            username "스파이더에러알림 - ${emojiIdx}"
            icon_emoji emojiList[emojiIdx]
        }
        filterTxt.size() > 0 ? vanillax.framework.batch.http.HttpHelper.postJson(slackUrl, jsonBuilder.toString()) : "fail"
    }

    def filterTrace(errorTrace) {
        def traceList = errorTrace.split("\\n")
        def errorList = traceList.take(1) + traceList.findAll {
            it.trim().startsWith("at crawler")
        }.take(3)
        errorList.take(1).join().findAll("connect timed out").size() > 0 ? "" : errorList.join("\n")
    }
}
