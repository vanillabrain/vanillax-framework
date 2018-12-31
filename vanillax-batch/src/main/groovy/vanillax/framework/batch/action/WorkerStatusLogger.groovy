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
import vanillax.framework.batch.sql.WorkerStatusDAO
import vanillax.framework.core.object.Autowired
import vanillax.framework.core.db.Transactional

import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * 주기적으로 Worker상태정보를 DB에 저장한다.
 */
@Log
class WorkerStatusLogger extends ActionBase{
    @Autowired
    WorkerStatusDAO workerStatusDAO

    def process(obj){
        if (1==1) {
            return null //우선은 로직을 수행하지 않게 처리한다.
        }
        process0(obj)
    }

    @Transactional(autoCommit = false)
    def process0(obj){
        log.info "I'm WorkerStatusLogger ....."

        def list = []
        def cnt = 0
        vanillax.framework.batch.scheduler.WorkerScheduler scheduler = vanillax.framework.batch.scheduler.WorkerScheduler.getInstance();
        scheduler.workerHandleMap.each{ id, workerInfo ->
            def status = null //D: Done
            ScheduledFuture<vanillax.framework.batch.scheduler.IWorker> workerHandle = workerInfo.workerHandle;
            if(workerHandle.cancelled){
                status = "C" // Cancelled
            }else if(workerHandle.done){
                status = "D" // Done
            }else if(workerHandle.getDelay(TimeUnit.SECONDS) <= 0){
                status = "A" // Active
            }else if(workerHandle.getDelay(TimeUnit.SECONDS) > 0){
                status = "S" // Sleeping
            }
            def item  = [script:id, statusCd:status]
            if(workerInfo.worker.lastStartedTime > 0){
                item.lastStartedTime = new java.sql.Timestamp(workerInfo.worker.lastStartedTime)
            }
            if(workerInfo.worker.lastWorkedTime > 0 && workerInfo.worker.lastWorkedTime > workerInfo.worker.lastStartedTime){
                item.lastWorkedTime = new java.sql.Timestamp(workerInfo.worker.lastWorkedTime)
            }
            if(workerInfo.worker.lastStartedTime > 0 && workerInfo.worker.lastWorkedTime > 0){
                item.executionTime = workerInfo.worker.lastWorkedTime - workerInfo.worker.lastStartedTime
            }else{
                item.executionTime = null
            }
            list << item
            cnt++
        }


        scheduler.canceledWorkerHandleMap.each{ id, workerInfo ->
            def item  = [script:id, statusCd:'C']
            if(workerInfo.worker.lastStartedTime > 0){
                item.lastStartedTime = new java.sql.Timestamp(workerInfo.worker.lastStartedTime)
            }
            if(workerInfo.worker.lastWorkedTime > 0 && workerInfo.worker.lastWorkedTime > workerInfo.worker.lastStartedTime){
                item.lastWorkedTime = new java.sql.Timestamp(workerInfo.worker.lastWorkedTime)
            }
            if(workerInfo.worker.lastStartedTime > 0 && workerInfo.worker.lastWorkedTime > 0){
                item.executionTime = workerInfo.worker.lastWorkedTime - workerInfo.worker.lastStartedTime
            }else{
                item.executionTime = null
            }

            list << item
            cnt++
        }

        if(cnt == 0){
            workerStatusDAO.deleteWorkerStatusAll([:])
            return obj
        }else{
//            def delete = """
//                delete from WorkerStatus where script not in ( $ids )
//            """
//            sql.execute(delete)
            workerStatusDAO.deleteWorkerStatusExclude([workers:list])
        }

        workerStatusDAO.insertWorkerStatusList(list)

        return obj

    }


}
