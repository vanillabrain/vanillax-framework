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
import vanillax.framework.batch.action.IErrorAction;
import vanillax.framework.core.db.TransactionManager;
import vanillax.framework.core.db.monitor.ConnectionMonitor;
import vanillax.framework.webmvc.config.ConfigHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Crawler 스크립트를 구동시키는 기본 Thread 클래스
 */
public class WorkerDefault extends WorkerBase {
//    private static final Logger log = LogManager.getLogger(WorkerDefault.class.getName());
    private static final Logger log = Logger.getLogger(WorkerDefault.class.getName());
    private Object result = null;


    public WorkerDefault(ScheduleInfo scheduleInfo){
        super(scheduleInfo);
    }

    @Override
    public void run() {
        Object scheduleLogDoc = null;
        try{
            this.lastStartedTime = System.currentTimeMillis();//시작시간 입력
            //active인지 확인한다.
            if(!this.scheduleInfo.isActive()){
                log.info("실행 상태가 아닙니다 : " + this.scheduleInfo.getScript());
                return;
            }
            //미작동 시간인지 확인하다.
            if(!this.scheduleInfo.canStartNow()){
                log.info("실행 시간이 아닙니다 : "+this.scheduleInfo.getScript()+", 미작동시간 : "+this.scheduleInfo.getIdleTime());
                return;
            }
            if(!alive) throw new Exception("종료처리되었습니다.");//종료 명령이 떨어지면 일을 진행하지 않는다.
            // script가 이미 실행중인지 확인한다.
            if(hasScriptOnProcessInOthers()){
                String msg = "다른 쓰레드에서 이 스크립트를 실행중에 있습니다 : "+this.scheduleInfo.getScript();
                log.warning(msg);
                throw new Exception(msg);
            }
            setScriptOnProcess(true);
            //schedule 로그입력
            IAction startLogger = ActionLoader.load(ConfigHelper.get("schedule.startLogger"));
            if(startLogger != null){
                scheduleLogDoc = startLogger.process(this.scheduleInfo);
            }
            //전처리
            Object result = null;
            IAction action = ActionLoader.load(this.scheduleInfo.getPreScript());
            if(action != null) {
                result = action.process(this.scheduleInfo);
            }
            //본처리
            action = ActionLoader.load(this.scheduleInfo.getMainScript());
            if(action == null){
                throw new Exception("Not found script file : "+this.scheduleInfo.getMainScript());
            }
            if(result == null){
                result = new HashMap<>();
            }
            if(result instanceof Map){
                ((Map)result).put("_param", this.scheduleInfo.getInputParam());
            }
            result = action.process(result);
            //후처리
            action = ActionLoader.load(this.scheduleInfo.getPostScript());
            if(action != null) {
                result = action.process(result);
            }
            //schedule 로그입력
            IAction endLogger = ActionLoader.load(ConfigHelper.get("schedule.endLogger"));
            if(startLogger != null){
                endLogger.process(scheduleLogDoc);
            }
        }catch(Exception e){
            if(e instanceof InterruptedException){
                log.info("Worker가 정지당했습니다 : "+this.scheduleInfo.getScript());
            }else{
                log.warning("스케줄 실행중 오류가 발생했습니다. "+this.scheduleInfo.getScript() + " : "+ e.getMessage());
            }

            try{
                //공통에러로그 처리
                IErrorAction scheduleErrorLogger = (IErrorAction)ActionLoader.load(ConfigHelper.get("schedule.errorLogger"));
                if(scheduleErrorLogger != null && scheduleLogDoc != null) {
                    result = scheduleErrorLogger.process(scheduleLogDoc, e);
                }
                //스케줄별 에러 처리
                IErrorAction action = (IErrorAction)ActionLoader.load(this.scheduleInfo.getErrorScript());
                if(action != null) {
                    result = action.process(this.scheduleInfo, e);
                }
            }catch(Exception errEx){
                log.warning("스케줄 오류처리중 오류가 발생했습니다 : "+this.scheduleInfo.getScript() + " : "+ errEx.getMessage());
            }
        }finally {
            setScriptOnProcess(false);
            this.lastWorkedTime = System.currentTimeMillis();//종료시간 입력
            TransactionManager.getInstance().clearTxSession();//Thread가 끝났으니 Transaction을 완전히 초기화한다.
            ConnectionMonitor.getInstance().onThreadFinished();//Thread끝날때 Connection확인
        }
    }
}
