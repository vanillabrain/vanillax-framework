package vanillax.framework.batch.scheduler;

import vanillax.framework.core.util.StringUtil;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * 주기적으로 작동되면서 Cron 표현식에 부합하는 ScheduleInfo를 찾아서 시간이 맞다면 작동시켜준다.
 */
public class CronWatchDog implements Runnable {
    private static Logger log = Logger.getLogger(CronWatchDog.class.getName());
    private ZonedDateTime lastCheckedTime = null;
    private ZoneId zoneId = null;

    public CronWatchDog(){
        zoneId = TimeZone.getDefault().toZoneId();
        init();
    }

    public CronWatchDog(String timeZone){
        zoneId = TimeZone.getTimeZone(timeZone).toZoneId();
        init();
    }

    private void init(){
        lastCheckedTime = currentTime().plusSeconds(-1).withNano(0);//1초전 시간을 초기값으로 한다.
    }

    @Override
    public void run() {
        if(lastCheckedTime.equals(currentTime())){
            return;
        }
        ScheduleInfoRepository repository = ScheduleInfoRepository.getInstance();
        for(String key:repository.getCronScheduleList()){
            ScheduleInfo scheduleInfo = repository.get(key);
            if(scheduleInfo.getCronExpression().isNowOnTime()){
                try {
                    WorkerScheduler.getInstance().startWorkerOnce(scheduleInfo);
                }catch (Exception e){
                    log.warning(StringUtil.errorStackTraceToString(e));
                }
            }
        }
        lastCheckedTime = currentTime();
    }

    private ZonedDateTime currentTime(){
        return ZonedDateTime.now(zoneId).withNano(0);
    }
}
