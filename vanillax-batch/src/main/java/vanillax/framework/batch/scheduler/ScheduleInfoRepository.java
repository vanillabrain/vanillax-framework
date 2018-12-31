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

import java.util.*;
import java.util.stream.Collectors;

/**
 * Schedule 정보를 담고있는 singleton 클래스
 */
public class ScheduleInfoRepository {
    private static ScheduleInfoRepository ourInstance = new ScheduleInfoRepository();
    private Map<String, ScheduleInfo> scheduleInfoMap = null;
    private Set<String> cronScheduleSet = null;

    public static ScheduleInfoRepository getInstance() {
        return ourInstance;
    }

    private ScheduleInfoRepository() {
        scheduleInfoMap = new Hashtable<String, ScheduleInfo>();
        cronScheduleSet = new HashSet<>();
    }

    synchronized public void addSchedule(ScheduleInfo info){
        String script = info.getScript();
        if(script == null)
            return;
        this.scheduleInfoMap.put(info.getScript(), info);
        if(info.getScheduleType() == ScheduleType.CRON){
            this.cronScheduleSet.add(info.getScript());
        }else{
            //기존에 cron 스케줄이었다가 변경된 경우가 있을 수 있다. 이경우 cronSchedule정보는 삭제해준다.
            this.cronScheduleSet.remove(info.getScript());
        }
    }

    public boolean contains(String script){
        return this.scheduleInfoMap.containsKey(script);
    }
    public boolean containsCron(String script){
        return this.cronScheduleSet.contains(script);
    }
    public ScheduleInfo get(String script){
        return this.scheduleInfoMap.get(script);
    }
    public List<String> getScheduleList(){
        return this.scheduleInfoMap.keySet().stream().collect(Collectors.toList());
    }
    public boolean hasSchedule(String script){
        return this.scheduleInfoMap.containsKey(script);
    }

    public List<String> getCronScheduleList(){
        return this.cronScheduleSet.stream().collect(Collectors.toList());
    }

    public ScheduleInfo remove(String script){
        this.cronScheduleSet.remove(script);
        return this.scheduleInfoMap.remove(script);
    }
}
