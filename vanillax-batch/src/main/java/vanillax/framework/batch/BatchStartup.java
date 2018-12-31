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

package vanillax.framework.batch;

import vanillax.framework.batch.action.IAction;
import vanillax.framework.batch.action.ActionLoader;
import vanillax.framework.batch.scheduler.ScheduleInfoRepository;
import vanillax.framework.batch.scheduler.WorkerScheduler;
import vanillax.framework.webmvc.config.ConfigHelper;
import vanillax.framework.webmvc.servlet.IStartup;

import java.util.List;
import java.util.logging.Logger;

/**
 * 배치 시스템이 시작될 때 작동되는 클래스
 */
public class BatchStartup implements IStartup{
    private static final Logger log = Logger.getLogger(BatchStartup.class.getName());

    public void start()throws Exception{
        // init action 실행정 system 초기화 Action을 실행한다.
        String initAction = ConfigHelper.get("schedule.initAction");
        if(initAction != null){
            IAction action = ActionLoader.load(initAction);
            action.process(null);
        }

        // startup action실행
        // 여기서 Schedule정보를 DB에서 읽어서 loading 한다.
        String startAction = ConfigHelper.get("schedule.startupAction");
        IAction action = ActionLoader.load(startAction);
        action.process(null);

        List<String> list = ScheduleInfoRepository.getInstance().getScheduleList();

        for(String k:list){
            log.info("loading : "+ ScheduleInfoRepository.getInstance().get(k));
        }

        //Scheduler 구동
        WorkerScheduler scheduler = WorkerScheduler.getInstance();
        scheduler.crawlerSchedulerStart();
    }
}
