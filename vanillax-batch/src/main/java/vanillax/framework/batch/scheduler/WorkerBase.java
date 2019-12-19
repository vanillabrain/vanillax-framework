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

import vanillax.framework.batch.action.IAction;
import vanillax.framework.batch.action.ActionLoader;
import vanillax.framework.core.db.TransactionManager;
import vanillax.framework.core.db.monitor.ConnectionMonitor;
import vanillax.framework.core.util.StringUtil;

import java.util.logging.Logger;

/**
 * Action클래스를 기본으로 수행하는 Worker클래스이다.
 */
public class WorkerBase implements IWorker {
    private static final Logger log = Logger.getLogger(WorkerBase.class.getName());
    private Object result = null;

    protected ScheduleInfo scheduleInfo = null;
    protected boolean alive = true;
    protected long lastStartedTime = 0L;
    protected long lastWorkedTime = 0L;
    protected boolean scriptOnProcess = false;

    public WorkerBase() {
    }

    public WorkerBase(ScheduleInfo scheduleInfo) {
        this.scheduleInfo = scheduleInfo;
    }

    @Override
    public void destroy() {
        alive = false;
    }

    @Override
    public long getLastStartedTime() {
        return this.lastStartedTime;
    }

    @Override
    public long getLastWorkedTime() {
        return this.lastWorkedTime;
    }

    @Override
    public void run() {
        Object scheduleLogDoc = null;
        try{
            if(!alive) return;
            // script가 이미 실행중인지 확인한다.
            if(hasScriptOnProcessInOthers()){
                String msg = "다른 쓰레드에서 이 스크립트를 실행중에 있습니다 : "+this.scheduleInfo.getScript();
                log.warning(msg);
                throw new Exception(msg);
            }
            setScriptOnProcess(true);
            //본처리
            this.lastStartedTime = System.currentTimeMillis();
            IAction action = ActionLoader.load(this.scheduleInfo.getScript());
            if(action == null){
                throw new Exception("Not found script file : "+this.scheduleInfo.getMainScript());
            }
            result = action.process(result);
        }catch(Throwable e){
            log.warning("스케줄 실행중 오류가 발생했습니다. "+this.scheduleInfo.getScript() + " : "+ StringUtil.errorStackTraceToString(e));
        }finally {
            try {
                setScriptOnProcess(false);
                this.lastWorkedTime = System.currentTimeMillis();
                TransactionManager.getInstance().clearTxSession();//Thread가 끝났으니 Transaction을 완전히 초기화한다.
                ConnectionMonitor.getInstance().onThreadFinished();//Thread끝날때 Connection확인
            }catch (Throwable e){
                log.warning(StringUtil.errorStackTraceToString(e));
            }
        }
    }

    protected boolean hasScriptOnProcessInOthers(){
        return WorkerScheduler.getInstance().hasScriptOnProcess(this.scheduleInfo.getScript());
    }

    protected void setScriptOnProcess(boolean b){
        scriptOnProcess = b;
    }

    @Override
    public boolean isScriptOnProcess(){
        return scriptOnProcess;
    }

}
