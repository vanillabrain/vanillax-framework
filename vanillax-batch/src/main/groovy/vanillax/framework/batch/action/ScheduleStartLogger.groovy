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

import groovy.util.logging.Log
import vanillax.framework.batch.common.CommonSequence
import vanillax.framework.batch.sql.ScheduleLogDAO
import vanillax.framework.core.object.Autowired
import vanillax.framework.core.db.Transactional

import java.sql.Timestamp
import java.text.DecimalFormat


/**
 * Scheduler에 의해 작동되는 Action을 수행하기 전 호출되어 스케줄러 로그를 작성한다.
 */
@Log
class ScheduleStartLogger extends ActionBase{
    @Autowired
    ScheduleLogDAO scheduleLogDAO
    @Autowired
    CommonSequence commonSequence

    def process(obj){
        log.fine "I'm ScheduleLog ....."

        long start = System.currentTimeMillis()
        int id = commonSequence.nextval('scheduleLogSeq')//가끔 이게 문제를 일으킨다. Thread가 경합하면서 Dead Lock 문제를 일으키는 것 같다
        long duration = System.currentTimeMillis() - start
        if(duration > 3000){
            DecimalFormat formatter = new DecimalFormat("#,###")
            def s = formatter.format(duration)
            log.severe("Too long time to get sequence number : $s ms")
        }
        Timestamp nowDate = new Timestamp(System.currentTimeMillis())
        insertScheduleLog([id:id, script:obj.script, nowDate:nowDate])
        def newOne = [id:id, script:obj.script, startTime:System.currentTimeMillis()]
        return newOne
    }

    @Transactional(autoCommit = false)
    def insertScheduleLog(obj){
        scheduleLogDAO.insertScheduleLog(obj)
    }
}
