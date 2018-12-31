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
package vanillax.framework.batch.filter

import groovy.util.logging.Log
import vanillax.framework.webmvc.service.FilterBase

import javax.servlet.http.HttpServletRequest

/**
 * 인증정보 필터
 */
@Log
class LoginFilter extends FilterBase {

    @Override
    Map<String, Object> preprocess(Map<String, Object> param) throws Exception {
        HttpServletRequest request = (HttpServletRequest)param.get("_request")
        if(request != null){
            def userInfo = request.getSession(true).getAttribute("[VANILLA_USER_SESSION_INFO]")
            if(!userInfo){
                //throw new BaseException("ERR001","Not Authenticated.",null,null)
            }
        }
        return param
    }

}