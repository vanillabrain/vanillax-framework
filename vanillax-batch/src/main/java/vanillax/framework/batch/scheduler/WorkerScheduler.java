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

import vanillax.framework.core.util.StringUtil;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Worker 실행 스케줄을 관리한다.
 * 참조 : http://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ScheduledExecutorService.html#scheduleWithFixedDelay(java.lang.Runnable,%20long,%20long,%20java.util.concurrent.TimeUnit)
 */
public class WorkerScheduler {
    private static Logger log = Logger.getLogger(WorkerScheduler.class.getName());

    private static WorkerScheduler instance = null;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(300);
    /** 실행되는 모든 Worker 정보 */
    private Map<Integer, WorkerInfo> workerHandleMap = null;
    /** Script와 workerHandleMap을 매핑하기 위한 맵. 하나의 script에 여러개 Worker가 존재할 수 있다. */
    private Map<String, Set<Integer>> workerScriptIdMap = null;
    /** CronWatchDog에 대한 Handle 객체 */
    private ScheduledFuture<IWorker> cronWatchDogHandle = null;
    private int scriptSeq = 0;

    public static WorkerScheduler getInstance(){
        if(instance == null){
            synchronized (WorkerScheduler.class){
                if(instance == null){
                    instance = new WorkerScheduler();
                }
            }
        }
        return instance;
    }

    private WorkerScheduler() {
        workerHandleMap = new Hashtable<>();
        workerScriptIdMap = new Hashtable<>();
        try{
            //Cron trigging 시간을 화인하는 쓰레드를 구동시킨다.
            cronWatchDogHandle = (ScheduledFuture<IWorker>) scheduler.scheduleAtFixedRate(
                    new CronWatchDog(),
                    1000,
                    550,
                    MILLISECONDS);
        }catch(Exception e){
            log.severe(StringUtil.errorStackTraceToString(e));
        }
    }

    /**
     * Crawler 스크립트를 수행하는 Worker를 모두 구동시킨다. 1초의 간격을 두고 구동하기 때문에 시간차가 발생한다.
     * @throws Exception
     */
    public void crawlerSchedulerStart()throws Exception {
        List<String> list = ScheduleInfoRepository.getInstance().getScheduleList();
        int cnt = 0;
        for(String script:list){
            ScheduleInfo scheduleInfo = ScheduleInfoRepository.getInstance().get(script);
            if(scheduleInfo == null || scheduleInfo.getScheduleType() == ScheduleType.CRON) {
                continue;
            }
            arrangeWorkerOnSchedule(scheduleInfo, cnt);
            cnt++;
        }
    }

    /**
     * Worker를 스케줄에 등록한다.
     * @param scheduleInfo 실생 스크립트 정보
     */
    public int arrangeWorkerOnSchedule(ScheduleInfo scheduleInfo, Integer initialDelay)throws Exception{
        if(scheduleInfo == null) {
            throw new Exception("실행할 대상이 입력되지 않았습니다");
        }
        if(initialDelay == null)
            initialDelay = 0;
        String script = scheduleInfo.getScript();
        if(scheduleInfo.getScheduleType() == ScheduleType.CRON){
            throw new Exception("CRON 스케줄은 WorkerScheduler에 등록할 수 없습니다 : "+script);
        }
        if(hasScheduledWorker(script)){//이미 등록된 스케줄이라면 진행하지 않는다.
            throw new Exception("해당 Worker는 이미 등록되어 있습니다 : "+script);
        }
        IWorker worker = new WorkerDefault(scheduleInfo);
        ScheduledFuture<IWorker> workerHandle = (ScheduledFuture<IWorker>)scheduler.scheduleWithFixedDelay(
                                                worker,
                                                initialDelay,
                                                scheduleInfo.getInterval(),
                                                SECONDS);
        int id = getNextScriptSeq();
        addSchedule(id, script, workerHandle, worker, ScheduleType.FIXED_DELAY);
        return id;
    }

    public int arrangeWorkerOnSchedule(ScheduleInfo scheduleInfo)throws Exception{
        return arrangeWorkerOnSchedule(scheduleInfo, null);
    }

    /**
     * Worker를 실행시킨다. 일회만 실행한다. 반복하지 않는다.
     * @param scheduleInfo 실생 스크립트 정보
     */
    public int startWorkerOnce(ScheduleInfo scheduleInfo, Integer initialDelay) throws Exception{
        String script = scheduleInfo.getScript();

        if(initialDelay == null)
            initialDelay = 0;

        if(this.hasScriptOnProcess(script))
            throw new Exception("해당 스크립트는 현재 구동중에 있습니다. 구동중일 경우 재실행 할 수 없습니다 : "+script);

        scheduleInfo.setIdleTime(null);
        scheduleInfo.setActive(true);
        IWorker worker = new WorkerDefault(scheduleInfo);
        ScheduledFuture<IWorker> workerHandle = (ScheduledFuture<IWorker>)scheduler.schedule(
                worker,
                initialDelay,
                SECONDS);
        int id = getNextScriptSeq();
        ScheduleType scheduleType = ScheduleType.ONCE;
        if(scheduleInfo.getScheduleType() == ScheduleType.CRON){
            scheduleType = ScheduleType.CRON;
        }
        addSchedule(id, script, workerHandle, worker, scheduleType);
        return id;
    }

    public int startWorkerOnce(ScheduleInfo scheduleInfo) throws Exception{
        return startWorkerOnce(scheduleInfo, null);
    }

    /**
     * Worker를 정지시킨다. 구동중일경우 Interupt를 건다.
     * 스케줄도 취소 시킨다.
     * @param id 실생 스크립트의 ID.
     */
    public void shutdownWorker(int id){
        WorkerInfo x = workerHandleMap.get(id);
        if(x != null){
            ScheduledFuture<IWorker> f = x.getWorkerHandle();
            f.cancel(true);
            IWorker w = x.getWorker();
            w.destroy();
        }
    }

    /**
     * script에 해당하는 스케줄을 모두 취소시킨다. 작동중인 worker가 있으면 interrupt를 건다.
     * @param script
     */
    public void cancelWorker(String script){
        Set<Integer> set = this.workerScriptIdMap.get(script);
        if(set != null){
            for(Integer i:set){
                shutdownWorker(i);
            }
        }
    }

    /**
     * 모든 Worker를 종료시칸다.
     */
    public void shutdownAll(){
        for(Integer k: workerHandleMap.keySet()){
            WorkerInfo x = workerHandleMap.get(k);
            x.getWorkerHandle().cancel(true);
            x.getWorker().destroy();
        }
    }

    /**
     * 프로세스를 종료한다.
     */
    public void shutdownProcess(){
        shutdownAll();
        this.cronWatchDogHandle.cancel(true);
    }

    /**
     * script가 지금 작동중인 쓰레드가 있는지 확인한다. scheduler상에서는 cancel처리 되었더라도 아직 작동중인 경우가 있을 수 있다.
     * @param script
     * @return
     */
    public boolean hasScriptOnProcess(String script){
        if(!this.workerScriptIdMap.containsKey(script)){
            return false;
        }
        Set<Integer> set = this.workerScriptIdMap.get(script);
        for(int id:set){
            WorkerInfo workerInfo = this.workerHandleMap.get(id);
            if(workerInfo.getWorker().isScriptOnProcess()){
                return true;
            }
        }
        return false;
    }

    /**
     * 수명을 다한 WorkerHandle은 삭제한다.
     */
    public void removeUselessWorkerHandles(){
        List<Integer> list = this.workerHandleMap.keySet().stream().collect(Collectors.toList());
        for(Integer key:list){
            removeUselessWorkerHandle(key);
        }
        list.clear();
    }

    public void removeUselessWorkerHandle(Integer key){
        WorkerInfo workerInfo = this.workerHandleMap.get(key);
        if(workerInfo.getWorker().isScriptOnProcess()){
            return;
        }
        ScheduledFuture<IWorker> handler = workerInfo.getWorkerHandle();
        if(handler.isCancelled() || handler.isDone()){
            this.workerHandleMap.remove(key);
            Set<Integer> set = this.workerScriptIdMap.get(workerInfo.getScript());
            if(set != null){
                set.remove(key);
            }
        }
    }

    public List<Map<String,Object>> getWorkerInfoList(){
        List<Map<String,Object>> list = new ArrayList<>();
        for(Integer id:this.workerHandleMap.keySet()){
            WorkerInfo workerInfo = this.workerHandleMap.get(id);
            ScheduledFuture<IWorker> workerHandle = workerInfo.getWorkerHandle();
            String status = null;
            long delay = 0;
            if(workerHandle.isCancelled()){
                status = "C"; // Cancelled
            }else if(workerHandle.isDone()){
                status = "D"; // Done
            }else if(workerHandle.getDelay(TimeUnit.SECONDS) <= 0){
                status = "A"; // Active
                delay = workerHandle.getDelay(TimeUnit.SECONDS);
            }else if(workerHandle.getDelay(TimeUnit.SECONDS) > 0){
                status = "S"; // Sleeping
                delay = workerHandle.getDelay(TimeUnit.SECONDS);
            }
            Map<String,Object> item = new LinkedHashMap<>();
            item.put("id",id);
            item.put("script",workerInfo.getScript());
            item.put("statusCd",status);
            item.put("scriptOnProcess",workerInfo.getWorker().isScriptOnProcess());
            item.put("scheduleType",workerInfo.getScheduleType());
            item.put("scheduleTypeName",workerInfo.getScheduleType().toString());
            item.put("delay",delay);
            item.put("lastStartedTime",null);
            item.put("lastWorkedTime",null);
            item.put("executionTime",null);
            list.add(item);

            long lastStartedTime = workerInfo.getWorker().getLastStartedTime();
            long lastWorkedTime = workerInfo.getWorker().getLastWorkedTime();

            if(lastStartedTime > 0){
                item.put("lastStartedTime", new Date(lastStartedTime));
            }
            if(lastWorkedTime > 0 && lastWorkedTime > lastStartedTime){
                item.put("lastWorkedTime", new Date(lastWorkedTime));
            }
            if(lastStartedTime > 0 && lastWorkedTime > 0){
                item.put("executionTime",lastWorkedTime - lastStartedTime);
            }
        }
        return list;
    }

    public WorkerInfo getWorkerInfo(Integer id){
        return this.workerHandleMap.get(id);
    }

    private int getNextScriptSeq(){
        return ++scriptSeq;
    }

    /**
     * 주기적으로 작동되는 script가 있는지 확인한다.
     * @param script
     * @return scheduler에 등록되어있고 cancel 처리가 되어있지 않으면 true를 반환한다.
     */
    public boolean hasScheduledWorker(String script){
        if(!this.workerScriptIdMap.containsKey(script)){
            return false;
        }
        Set<Integer> set = this.workerScriptIdMap.get(script);
        for(int id:set){
            WorkerInfo workerInfo = this.workerHandleMap.get(id);
            if(workerInfo.getScheduleType() == ScheduleType.FIXED_DELAY ){
                if(!workerInfo.getWorkerHandle().isCancelled() && !workerInfo.getWorkerHandle().isDone())
                    return true;
            }
        }
        return false;
    }

    /**
     * scheduler에 등록된 worker정보를 map에 저장한다.
     * @param id 생성된 int ID
     */
    private void addSchedule(int id, String script, ScheduledFuture<IWorker> workerHandle, IWorker worker, ScheduleType scheduleType){
        WorkerInfo workerInfo = new WorkerInfo(id, script, workerHandle, worker, scheduleType);
        this.workerHandleMap.put(id, workerInfo);
        if(this.workerScriptIdMap.containsKey(script)){
            Set<Integer> set = this.workerScriptIdMap.get(script);
            set.add(id);
        }else{
            Set<Integer> set = new HashSet<>(5);
            set.add(id);
            this.workerScriptIdMap.put(script, set);
        }
    }
}
