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

/**
 * Created by gaedong on 2015-08-11.
 */
@Log
class ConfigWatchDog extends ActionBase{

    def process(obj){
        //config 파일을 확인하여 파일이 변경됐을 경우 정보를 갱신한다.
        log.info "I'm ConfigWatchDog"
        return null;
    }

}
