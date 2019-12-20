package vanillax.framework.core.db;

import vanillax.framework.core.util.StringUtil;

import java.sql.Connection;
import java.util.*;

/**
 * TransactionManager에서 사용하는 스택의 그 레벨에서 관리중인 Connection 객체를 정보를 담고있는 클래스이다.
 */
public class TransactionSession {
    private String id = null;
    /** 동일 계층의 트랜잭션 세션에서는 DataSource 이름별로 Connection을 하나만 소유한다. */
    private Map<String, Connection> connectionMap = null;
    private boolean autoCommit = true;


    public TransactionSession(boolean autoCommit){
        this.id = StringUtil.makeUid();
        this.autoCommit = autoCommit;
        this.connectionMap = new HashMap<>(4);
    }

    public void add(String dataSourceName, Connection c){
        connectionMap.put(dataSourceName,c);
    }

    @Override
    public boolean equals(Object o){
        if(o == null){
            return false;
        }
        if(o instanceof TransactionSession){
            TransactionSession t = (TransactionSession)o;
            return this.id.equals(t.getId());
        }
        return false;
    }

    public String getId(){
        return this.id;
    }

    public boolean isAutoCommit() {
        return autoCommit;
    }

    public Map<String, Connection> getConnectionMap(){
        return this.connectionMap;
    }

    public void clear(){
        if(connectionMap == null || connectionMap.size() == 0)
            return;
        for(String key: this.connectionMap.keySet()){
            Connection c = this.connectionMap.get(key);
            try{ c.close(); }catch (Exception ignore){}
        }
        connectionMap.clear();
    }

}
