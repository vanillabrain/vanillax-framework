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

package vanillax.framework.batch.common

import groovy.util.logging.Log
import vanillax.framework.batch.action.ActionBase
import vanillax.framework.batch.sql.CommonSequenceDataDAO
import vanillax.framework.core.db.Transactional
import vanillax.framework.core.object.Autowired

/**
 * 스케줄러가 가동될때 실댕되는 Action이다.
 * 스케줄러의 스케줄 정보를 DB에서 읽어서 ScheduleConfigLoader에게 전달한다.
 * 비정상적으로 수행중이던 Action을 TERMINATED상태로 변경한다.
 * Worker명령이 완료되지 않은 건도 TERMINATED상태로 변경한다.
 */
@Log
class CommonSequence extends ActionBase{
    @Autowired
    CommonSequenceDataDAO commonSequenceDataDAO

    @Transactional(autoCommit = true)
    def curval(String sequenceName){
        def cur = commonSequenceDataDAO.selectCommonSequenceData([sequenceName:sequenceName])
        if(!cur)
            return null
        return cur['sequenceCurValue']
    }

    @Transactional(autoCommit = false)
    def nextval(String sequenceName){
        def cur = commonSequenceDataDAO.selectCommonSequenceData([sequenceName:sequenceName])
        if(!cur){
            commonSequenceDataDAO.insertCommonSequenceData([sequenceName:sequenceName])
            return 1
        }
        int next = cur['sequenceCurValue']+1
        commonSequenceDataDAO.updateCommonSequenceData([sequenceName:sequenceName, sequenceCurValue:next, sequenceCycle:0])
        return next
    }

}
