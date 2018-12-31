package vanillax.framework.batch.scheduler;

import java.util.concurrent.ScheduledFuture;

/**
 * Created by gaedong on 2016. 10. 7..
 */
public class WorkerInfo {
    private int id;
    /** 실행하는 script명 */
    private String script;
    private ScheduledFuture<IWorker> workerHandle;
    private IWorker worker;
    /** script가 실행중에 있는 여부. Thread 구동과 별개로 Script가 실재 구동되고 있는지 여부이다. 중복으로 script를 실행하지 않기위한 장치이다.*/
    private boolean scriptOnProcess = false;
    private ScheduleType scheduleType = null;

    public WorkerInfo(int id, String script, ScheduledFuture<IWorker> workerHandle, IWorker worker) {
        this.id = id;
        this.script = script;
        this.workerHandle = workerHandle;
        this.worker = worker;
        this.scheduleType = ScheduleType.FIXED_DELAY;
    }
    public WorkerInfo(int id, String script, ScheduledFuture<IWorker> workerHandle, IWorker worker, ScheduleType scheduleType) {
        this.id = id;
        this.script = script;
        this.workerHandle = workerHandle;
        this.worker = worker;
        this.scheduleType = scheduleType;
    }

    public String getScript(){
        return script;
    }

    public ScheduledFuture<IWorker> getWorkerHandle() {
        return workerHandle;
    }

    public IWorker getWorker() {
        return worker;
    }

    public boolean isScriptOnProcess() {
        return scriptOnProcess;
    }

    public void setScriptOnProcess(boolean scriptOnProcess) {
        this.scriptOnProcess = scriptOnProcess;
    }

    public ScheduleType getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(ScheduleType scheduleType) {
        this.scheduleType = scheduleType;
    }
}
