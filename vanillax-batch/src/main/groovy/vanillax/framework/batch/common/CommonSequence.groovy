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
 * Sequence 번호 채번한다
 *
 *
 *
 */
@Log
class CommonSequence{
    @Autowired
    CommonSequenceDataDAO commonSequenceDataDAO

    @Transactional
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
