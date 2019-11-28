package vanillax.framework.core.db;

import java.sql.Connection;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Logger;

/**
 * Transaction관리자.
 * TrheadLocal을 이용하여 동일한 Thread내에서의 Transaction을 관리한다.
 */
public class TransactionManager {
    private static final Logger log = Logger.getLogger(TransactionManager.class.getName());

    private static TransactionManager instance = null;
    private static ThreadLocal<Stack<TransactionSession>> stackThreadLocal = new ThreadLocal<Stack<TransactionSession>>();

    DecimalFormat formatter = null;

    public static TransactionManager getInstance(){
        if(instance == null){
            synchronized (TransactionManager.class){
                if(instance == null){
                    instance = new TransactionManager();
                }
            }
        }
        return instance;
    }

    private TransactionManager(){
        try {
            formatter = new DecimalFormat("#,###");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Transaction을 시작한다.
     * Transaction Session을 ThreadLocal에 추가한다
     * @return 만들어진 세션ID를 반환한다
     */
    public String startTxSession(){
        return startTxSession(true);
    }

    public String startTxSession(boolean autoCommit){
        log.info("transaction started.");
        Stack<TransactionSession> stack = stackThreadLocal.get();
        if(stack == null){
            stack = new Stack<TransactionSession>();
            stackThreadLocal.set(stack);
        }
        TransactionSession txSession = new TransactionSession(autoCommit);
        stack.add(txSession);
        return txSession.getId();
    }

    /**
     * transaction 시작이 선언되고 중간 단계에서 ConnectionManager에 의해 입력된다.
     * startTxSession()이 실행되지 않았다면 강제로 startTxSession()을 호출한다.
     * @param connection IConnectionManager에 의해 생성된 Connection객체
     */
    public void addConnection(String dataSourceName, Connection connection)throws Exception{
        Stack<TransactionSession> stack = stackThreadLocal.get();
        if(stack == null){//Transaction이 정의되지 않았을 경우이다.
            startTxSession(true);//Transaction을 기본값으로 시작한다. 이 경우 로직이 최종 마무리되었을 때 반드시 clearTxSession()메소드를 호출해야한다.
            stack = stackThreadLocal.get();
        }
        TransactionSession session = stack.peek();
        if(!session.isAutoCommit() && connection.getAutoCommit())
            connection.setAutoCommit(false);
        if(session.isAutoCommit() && !connection.getAutoCommit())
            connection.setAutoCommit(true);
        stack.peek().add(dataSourceName, connection);
        log.info("stack : "+stack.size()+"\t current session connections : "+stack.peek().getConnectionMap().size());
    }

    /**
     * 현재 레벨에서 사용중인 dataSourceName의 Connection을 반환한다.
     * @param dataSourceName
     * @return
     * @throws Exception
     */
    public Connection getCurrentConnection(String dataSourceName) throws  Exception{
        Stack<TransactionSession> stack = stackThreadLocal.get();
        if(stack == null || stack.size() == 0){//Transaction이 정의되지 않았을 경우이다. 매우 위험한데 사용자가 직접 Connection.close()를 수행코드를 작성해야한다.
            return null;
        }
        TransactionSession session = stack.peek();
        return session.getConnectionMap().get(dataSourceName);
    }

    /**
     * Transaction을 종료한다.
     * 정상적인 종료를 처리한다. commit을 수행한다.
     * @throws Exception
     */
    public void finishTxSession()throws Exception{
        Stack<TransactionSession> stack = stackThreadLocal.get();
        if(stack == null || stack.size() == 0){
            return;
        }
        //최근 TxSession을 가져온다
        TransactionSession currSession = stack.pop();
        //모두 가져와 commit해야.
        Map<String,Connection> connectionMap = currSession.getConnectionMap();
        Set<String> set = connectionMap.keySet();
        try{
            for(String dataSourceName:set){
                Connection c = connectionMap.get(dataSourceName);
                if(!c.getAutoCommit()){
                    long start = System.currentTimeMillis();
                    c.commit();
                    long duration = System.currentTimeMillis() - start;
                    if(duration > 5L * 1000L){//5초이상의 시간이 소요되면 로그를 찍어준다
                        log.info("Connection["+c+"] committing took : "+ formatter.format(duration) +" ms");
                    }
                }
                long start = System.currentTimeMillis();
                c.close();
                long duration = System.currentTimeMillis() - start;
                if(duration > 5L * 1000L){//5초이상의 시간이 소요되면 로그를 찍어준다
                    log.info("Connection["+c+"] closing took : "+ formatter.format(duration) +" ms");
                }
            }
        }catch(Exception e){
            //commit중에 오류가 발생할 경우 나머지 rollback하고 모두 close한다.
            for(String dataSourceName:set){
                Connection c = connectionMap.get(dataSourceName);
                if(!c.isClosed())
                    try{c.rollback();}catch(Exception ignore){}
            }
            currSession.clear();
            //오류처리를 위해 다시 스택에 넣는다.
            //ActionTransactionProxy에서 Exception을 다시 잡기 때문에 스택에 빈 객체를 넣어두는 것이다.
            stack.add(currSession);
            throw e;
        }
        log.info("transaction finished");
    }

    /**
     * Exception이 발생했을 때 수행한다.
     * 현재 Transaction의 rollback를 수행한다.
     */
    public void finishTxSessionOnError(){
        Stack<TransactionSession> stack = stackThreadLocal.get();
        if(stack == null || stack.size() == 0){
            return;
        }

        //최근 TxSession을 가져온다
        TransactionSession currSession = stack.pop();

        //모두가져와 rollback한다.
        Map<String,Connection> connectionMap = currSession.getConnectionMap();
        Set<String> set = connectionMap.keySet();
        for(String dataSourceName:set){
            Connection c = connectionMap.get(dataSourceName);
            try{if(!c.isClosed()) c.rollback();}catch(Exception ignore){}
        }
        currSession.clear();
    }

    /**
     * 이 메소드는 주의해서 써야한다. TX 스택에서 마지막 단계일 때만 사용해야한다.
     * 서비스의 마지막 단계에서 Connection을 초기화할 목적으로 사용해야한다.
     */
    public void clearTxSession() {
        Stack<TransactionSession> stack = stackThreadLocal.get();
        if(stack == null){
            stackThreadLocal.remove();
            return;
        }
        log.info("clearing transaction stack : "+stack.size());
        for(TransactionSession s:stack){
            s.clear();
        }
        stack.clear();
        stackThreadLocal.remove();
    }

    public int getTxStackSize(){
        return stackThreadLocal.get().size();
    }

}
