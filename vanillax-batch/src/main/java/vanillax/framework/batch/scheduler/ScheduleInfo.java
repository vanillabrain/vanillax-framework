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

package vanillax.framework.batch.scheduler;

import vanillax.framework.batch.cron.CronExpression;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by gaedong on 2015-08-13.
 */
public class ScheduleInfo {
    private String script = null;
    private int interval = 1;
    private String idleTime = null;
    private String mainScript = null;
    private String preScript = null;
    private String postScript = null;
    private String errorScript = null;
    private boolean active = false;
    private Object inputParam = null;
    private ScheduleType scheduleType = ScheduleType.FIXED_DELAY;
    private CronExpression cronExpression = null;

    public ScheduleInfo(){

    }

    public ScheduleInfo(String script, int interval) {
        this.script = script;
        this.interval = interval;
        this.active = true;
    }

    public ScheduleInfo(String script, int interval, String idleTime, String preScript, String postScript, String errorScript, boolean active) {
        this.script = script;
        this.interval = interval;
        this.idleTime = idleTime;
        this.mainScript = script;
        this.preScript = preScript;
        this.postScript = postScript;
        this.errorScript = errorScript;
        this.active = active;
    }

    public ScheduleInfo(String script, int interval, String idleTime, String mainScript, String preScript, String postScript, String errorScript, boolean active) {
        this.script = script;
        this.interval = interval;
        this.idleTime = idleTime;
        if(mainScript == null){
            this.mainScript = script;
        }else {
            this.mainScript = mainScript;
        }
        this.preScript = preScript;
        this.postScript = postScript;
        this.errorScript = errorScript;
        this.active = active;
    }

    /**
     * 현재 시간이 작동가능한 시간인지 확인한다.
     * @return 미작동 시간에 있으면 false를 반환한다
     */
    public boolean canStartNow(){
        if(this.idleTime == null || this.idleTime.length() != 11)
            return true;
        SimpleDateFormat sdf = new SimpleDateFormat("HHmm");
        String[] arr = this.idleTime.split("\\-");
        String str = sdf.format(new Date());
        int now = Integer.parseInt(str);
        int startTime = Integer.parseInt(arr[0].replaceAll(":", ""));
        int endTime = Integer.parseInt(arr[1].replaceAll(":", ""));
        return now < startTime || now > endTime;
    }

    public String getMainScript() {
        return mainScript;
    }

    public void setMainScript(String mainScript) {
        this.mainScript = mainScript;
    }

    public Object getInputParam() {
        return inputParam;
    }

    public void setInputParam(Object inputParam) {
        this.inputParam = inputParam;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public String getIdleTime() {
        return idleTime;
    }

    public void setIdleTime(String idleTime) {
        this.idleTime = idleTime;
    }

    public String getPreScript() {
        return preScript;
    }

    public void setPreScript(String preScript) {
        this.preScript = preScript;
    }

    public String getPostScript() {
        return postScript;
    }

    public void setPostScript(String postScript) {
        this.postScript = postScript;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getErrorScript() {
        return errorScript;
    }

    public void setErrorScript(String errorScript) {
        this.errorScript = errorScript;
    }

    public ScheduleType getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(ScheduleType scheduleType) {
        this.scheduleType = scheduleType;
    }

    public CronExpression getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(CronExpression cronExpression) {
        this.cronExpression = cronExpression;
    }

    @Override
    public String toString() {
        return "ScheduleInfo{" +
                "script='" + script + '\'' +
                ", interval=" + interval +
                ", idleTime=" + nullify(idleTime) +
                ", preScript=" + nullify(preScript) +
                ", postScript=" + nullify(postScript) +
                ", active=" + active +
                '}';
    }

    private String nullify(Object obj){
        if(obj == null)
            return "null";
        return "'"+obj+"'";
    }
}
