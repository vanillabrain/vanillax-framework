package vanillax.framework.core.db.monitor;

import vanillax.framework.core.util.StringUtil;

import java.text.DecimalFormat;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * DB Connection자원을 모니터하는 클래스이다.
 * DataSourceWrapper를 통해 획득되는 Connection객체는 이 클래스에 정보가 저장된다.
 * 10초마다 Active Connection의 갯수를 확인하여 1이상이면 로그를 남긴다.
 */
public class ConnectionMonitor {
    private static Logger log = Logger.getLogger(ConnectionMonitor.class.getName());
    private static ConnectionMonitor instance;
    private final int ACTIVE_DURATION_FOR_LOG = 30;//로그를 기록할 Connection이 Active상태 시간. 30초이상 Active이면 로그기록
    /** Connection갯수를 확인하는 Thread가 작동할 주기 (초) */
    private final int LOG_INTERVAL = 10;

    /** 사용중인 Connection정보를 저장한다. close()되면 이테이블에서 사라진다. */
    private Map<String, ConnectionMonitorInfo> activeConnectionTable = null;
    private ScheduledFuture<?> taskHandle = null;
    private DecimalFormat formatter = null;

    private ConnectionMonitor(){
        formatter = new DecimalFormat("#,###");
        activeConnectionTable = new Hashtable<String, ConnectionMonitorInfo>();
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        //10초마다 Active Connection갯수 로그를 작성한다.
        Runnable task = () -> {
            if(this.activeConnectionTable.size() > 0)
                log.info("active connection count : "+this.activeConnectionTable.size());
            for(String k: activeConnectionTable.keySet()){
                ConnectionMonitorInfo info = this.activeConnectionTable.get(k);
                if(System.currentTimeMillis() - info.getActiveStartTime() > ACTIVE_DURATION_FOR_LOG*1000){
                    log.info("Active connection : "+info.toString());
                }
            }
        };
        taskHandle = executor.scheduleAtFixedRate(task, 0, LOG_INTERVAL, TimeUnit.SECONDS);
    }

    public static ConnectionMonitor getInstance(){
        if(instance == null){
            synchronized (ConnectionMonitor.class){
                if(instance == null){
                    instance = new ConnectionMonitor();
                }
            }
        }
        return instance;
    }

    /**
     * DataSource.getConnection()호출시 사용하는 메소드이다.
     * Connection정보를 Hashtable에 저장한다.
     * @param connectionId 처리할 대상의 Connection ID
     * @param traces .getConnection()이 호출된  method stack
     */
    public void onGetConnection(String connectionId, StackTraceElement[] traces){
        long threadId = Thread.currentThread().getId();
        ConnectionMonitorInfo info = new ConnectionMonitorInfo(connectionId, threadId, System.currentTimeMillis(),traces);
        activeConnectionTable.put(connectionId, info);
    }

    /**
     * Connection.close() 호출시 사용되는 메소드이다.
     * Connection정보를 Hashtable에서 제거한다.
     * @param connectionId colose할 대상의 Connection ID
     */
    public void onClose(String connectionId){
        ConnectionMonitorInfo info = activeConnectionTable.remove(connectionId);
        if(info == null)
            return;
        if(System.currentTimeMillis() - info.getActiveStartTime() > ACTIVE_DURATION_FOR_LOG*1000){
            String msg = "Connection ["+info.connectionId+"] on Thread-"+info.threadId+" just closed. Active Time : "
                    + formatter.format(System.currentTimeMillis() - info.activeStartTime) + "ms. Actived "
                    + StringUtil.stackTraceToString(info.traces);
            log.info(msg);
        }
    }

    /**
     * Thread가 종료되는 시점에서 호출해준다.
     * 해당 Thread에서 열었던 Connection이 닫히지 않은 상태로 있는지 확인한다.
     */
    public void onThreadFinished(){
        long threadId = Thread.currentThread().getId();
        for(String k:this.activeConnectionTable.keySet()){
            ConnectionMonitorInfo info = this.activeConnectionTable.get(k);
            if(info.getThreadId() == threadId){
                log.warning("NOT CLOSED CONNECTION!!!! "+info.toString());//이 경우가 매우 심각한 거다.
            }
        }
    }

    public void destroy(){
        taskHandle.cancel(true);
    }

    class ConnectionMonitorInfo {
        private String connectionId;
        private long threadId;
        private long activeStartTime;
        private StackTraceElement[] traces;

        public ConnectionMonitorInfo(String connectionId, long threadId, long activeStartTime, StackTraceElement[] traces) {
            this.connectionId = connectionId;
            this.threadId = threadId;
            this.activeStartTime = activeStartTime;
            this.traces = traces;
        }

        public String getConnectionId() {
            return connectionId;
        }

        public void setConnectionId(String connectionId) {
            this.connectionId = connectionId;
        }

        public long getThreadId() {
            return threadId;
        }

        public void setThreadId(long threadId) {
            this.threadId = threadId;
        }

        public long getActiveStartTime() {
            return activeStartTime;
        }

        public void setActiveStartTime(long activeStartTime) {
            this.activeStartTime = activeStartTime;
        }

        @Override
        public String toString(){
            return "Connection ["+this.connectionId+"] on Thread-"+this.threadId+" still active. Time : "+ formatter.format(System.currentTimeMillis() - this.activeStartTime) + "ms. Actived " + StringUtil.stackTraceToString(this.traces);
        }

    }

}
