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
import vanillax.framework.batch.sql.ScheduleLogDAO
import vanillax.framework.core.object.Autowired
import vanillax.framework.core.db.Transactional

import java.sql.Timestamp

/**
 * Worker종료시점에서 로그기록 Action
 */
@Log
class ScheduleEndLogger extends ActionBase{
    @Autowired
    ScheduleLogDAO scheduleLogDAO

    @Transactional(autoCommit = false)
    def process(obj){
        log.fine "I'm ScheduleEndLogger ....."
        obj.duration = System.currentTimeMillis() - obj.startTime
        obj.statusCode = 'DONE'
        obj.nowDate = new Timestamp(System.currentTimeMillis())
        scheduleLogDAO.updateScheduleLog(obj)
        return obj
    }

}
