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

import groovy.json.JsonParserType
import groovy.json.JsonSlurper
import groovy.util.logging.Log
import vanillax.framework.batch.cron.CronExpression
import vanillax.framework.batch.scheduler.ScheduleInfo
import vanillax.framework.batch.scheduler.ScheduleType
import vanillax.framework.batch.sql.ActionScheduleDAO
import vanillax.framework.batch.sql.ScheduleLogDAO
import vanillax.framework.core.object.Autowired
import vanillax.framework.batch.scheduler.ScheduleInfoRepository
import vanillax.framework.core.db.Transactional

import java.sql.Timestamp

/**
 * 스케줄러가 가동될때 실댕되는 Action이다.
 * 스케줄러의 스케줄 정보를 DB에서 읽어서 ScheduleConfigLoader에게 전달한다.
 * 비정상적으로 수행중이던 Action을 TERMINATED상태로 변경한다.
 * Worker명령이 완료되지 않은 건도 TERMINATED상태로 변경한다.
 */
@Log
class StartupAction extends ActionBase{
    @Autowired
    ActionScheduleDAO actionScheduleDAO
    @Autowired
    ScheduleLogDAO scheduleLogDAO

    @Transactional(autoCommit = true)
    def process(obj){
        log.fine "I'm Startup Action ....."

        //schedule정보를 DB에서 읽어서 처리
        // 개발서버에서 처리할 스크립트(한국에서만 허용되는 서버들)
        // and A.script in ('crawler.community.dcinside.Dcinside01','crawler.community.dcinside.Dcinside02','crawler.community.inven.Inven01','crawler.community.inven.Inven02', 'crawler.rss.IamPeterRss')
        def list = actionScheduleDAO.selectActiveActionSchedule([active:'Y'])
        list.each { it ->
            def inputParam = null;
            String script = it['script'];
            ScheduleInfo info = new ScheduleInfo()
            info.setScript(script);
            info.setInterval((Integer) it['intervalTime']);
            if(it['idleTime']) {
                info.setIdleTime(it['idleTime']);
            }
            if(it['mainScript']) {
                info.setMainScript(it['mainScript']);
            }else{
                info.setMainScript(script);
            }
            if(it['postScript']) {
                info.setPostScript(it['postScript']);
            }
            if(it['preScript']) {
                info.setPreScript(it['preScript']);
            }
            if(it['active']) {
                info.setActive("Y".equals(it['active']));
            }
            if(it['scheduleType']) {
                if(it['scheduleType'] == 'FD'){
                    info.setScheduleType(ScheduleType.FIXED_DELAY)
                }else if(it['scheduleType'] == 'CR'){
                    info.setScheduleType(ScheduleType.CRON)
                    info.setCronExpression(new CronExpression(it['cron']))
                }
            }

            if(it['inputParam']){
                JsonSlurper json = new JsonSlurper().setType(JsonParserType.LAX)
                ByteArrayInputStream bin = new ByteArrayInputStream(it['inputParam'].getBytes('UTF-8'))
                def object = json.parse(bin,'UTF-8')
                if(!inputParam){
                    inputParam = [:]
                }
                object.each { key, val ->
                    inputParam[key] = val
                }
            }
            info.setInputParam(inputParam)
            ScheduleInfoRepository.instance.addSchedule(info)
        }
        def modDate = new Timestamp(System.currentTimeMillis())
        scheduleLogDAO.updateScheduleLogAsTerminated([statusCode:'DO', modDate:modDate])

        return null
    }

}
