package vanillax.framework.core.db;

import vanillax.framework.core.Constants;
import vanillax.framework.core.db.monitor.DataSourceWrapper;
import vanillax.framework.core.db.monitor.IJdbcWrapper;
import vanillax.framework.core.config.FrameworkConfig;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Connection을 pool에서 가져올 때 Transaction설정을 주입시키는 클래스.
 */
abstract public class AbstractConnectionManager implements IConnectionManager{
    protected Map<String, DataSource> dataSourceMap = null;

    protected AbstractConnectionManager(){
        try {
            init();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void init() throws Exception{
        makeDatasource();
        //DB 모니터링 설정값이 true 인경우 모니터링 클래스로 래핑한다.
        if(FrameworkConfig.getBoolean("monitor.db", false)){
            List<String> keyList = new ArrayList<>(5);
            for(String key:this.dataSourceMap.keySet()){
                keyList.add(key);
            }
            for(String key:keyList){
                DataSource dataSource1 = new DataSourceWrapper( this.dataSourceMap.remove(key) );
                if(dataSource1 != null)
                    this.dataSourceMap.put(key, dataSource1);
            }
        }
    }

    abstract protected void makeDatasource() throws Exception;

    @Override
    public DataSource getDataSource(String dataSourceName)throws Exception{
        if(dataSourceMap == null){
            init();
        }
        return dataSourceMap.get(dataSourceName);
    }

    @Override
    public DataSource getDataSource()throws Exception{
        return getDataSource(Constants.DEFAULT_DATA_SOURCE);
    }

    /**
     * Tx매니저의 관리를 받는 Connection을 가져온다.
     * @param dataSourceName dataSource 명
     * @return dataSource 내의 connection 반환
     * @throws Exception Connection이 close되었을 경우 오류생성
     */
    @Override
    public Connection getConnection(String dataSourceName)throws Exception{
        //Tx매니저를 통해서 현재 Thread에서 사용중인 Connection을 가져온다.
        Connection connection  = null;
        connection = TransactionManager.getInstance().getCurrentConnection(dataSourceName);
        if (connection != null) {
            if (connection.isClosed()) {
                throw new Exception("Connection closed : " + connection); //이 경우가 발생하면 매우 심각하다!
            }
            return connection;
        }
        //Tx매니저를 통해 제어되는 현재 계층상의 Connection이 없을 경우 DataSource에서 새로 얻어서 Tx매니저에게 추가하고 반환한다.
        if (!this.dataSourceMap.containsKey(dataSourceName)) {
            throw new Exception("Not found DataSource : " + dataSourceName);
        }
        try {
            connection = this.dataSourceMap.get(dataSourceName).getConnection();//이 지점에서 Connection 모니터에 모니터링 정보를 추가하고 있다.
            if (connection.isClosed()) {
                throw new Exception("Connection closed : " + connection);
            }
            TransactionManager.getInstance().addConnection(dataSourceName, connection);//Transaction을 제어하기 위해 Connection객체를 TransactionManager에게 전달한다.
        }catch (Exception e){
            if(connection != null && connection instanceof IJdbcWrapper){
                //ConnectionWrapper인 경우에 close()를 호출해야만 모니터링 정보를 마감시켜준다.
                // 그렇지 않으면 계속 모니터링상에서 Connection이 열려있는 채로 표시가 된다.
                try{connection.close();}catch (Exception ignore){}
            }
            throw e;
        }
        return connection;
    }

    @Override
    public Connection getConnection()throws Exception{
        return getConnection(Constants.DEFAULT_DATA_SOURCE);
    }

    /**
     * Tx매니저의 관리를 받지 않는 Connection을 가져온다.
     * 사용자가 직접 Conntion.close()를 호출해야한다.
     * @param dataSourceName dataSource 명
     * @param autoCommit autoCommit 여부
     * @return dataSource내의 Connection 객체
     * @throws Exception DataSource를 찾을 수 없나가 Connection이 close되었을 경우
     */
    public Connection getConnectionRaw(String dataSourceName, boolean autoCommit)throws Exception{
        if(!this.dataSourceMap.containsKey(dataSourceName)){
            throw new Exception("Not found DataSource : "+dataSourceName);
        }
        Connection connection = this.dataSourceMap.get(dataSourceName).getConnection();
        if(!connection.isClosed()){
            //do nothing..
        }else{
            throw new Exception("Connection closed : "+connection);
        }
        connection.setAutoCommit(autoCommit);
        return connection;
    }

    public Connection getConnectionRaw(String dataSourceName)throws Exception{
        return getConnectionRaw(dataSourceName, true);
    }

    public Connection getConnectionRaw()throws Exception{
        return getConnectionRaw(Constants.DEFAULT_DATA_SOURCE);
    }

    @Override
    public void setDataSource(String dataSourceName, DataSource dataSource) {
        this.dataSourceMap.put(dataSourceName,dataSource);
    }

    @Override
    public void setDataSource(DataSource dataSource) {
        this.dataSourceMap.put(Constants.DEFAULT_DATA_SOURCE,dataSource);
    }
}
