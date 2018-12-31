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

package vanillax.framework.webmvc.exception;

/**
 * Created by gaedong on 2015-10-06.
 */
public class BaseException extends Exception {
    private String detail;
    private String code;

    public BaseException() {
        super();
    }
    public BaseException(String msg) {
        super(msg);
    }
    public BaseException(String msg, Throwable t) {
        super(msg,t);
    }
    public BaseException(String code, String msg, String detail, Throwable t) {
        super(msg,t);
        this.detail = detail;
        this.code = code;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
