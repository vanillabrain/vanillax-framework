package vanillax.framework.webmvc.db;

import vanillax.framework.core.Constants;
import vanillax.framework.core.util.ReflectionUtil;
import vanillax.framework.webmvc.config.ConfigHelper;
import vanillax.framework.core.db.AbstractConnectionManager;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * DB Connection객체를 제어한다.
 */
public class ConnectionManagerBase extends AbstractConnectionManager {

    private static final Logger log = Logger.getLogger(ConnectionManagerBase.class.getName());
    private static ConnectionManagerBase instance = null;

    public static ConnectionManagerBase getInstance(){
        if(instance == null){
            synchronized (ConnectionManagerBase.class){
                if(instance == null){
                    instance = new ConnectionManagerBase();
                }
            }
        }
        return instance;
    }

    protected ConnectionManagerBase(){
        super();
    }

    protected void makeDatasource() throws Exception{
        if(this.dataSourceMap != null){
            return;
        }
        this.dataSourceMap = new HashMap<>(4);

        //기본 DataSource설정이 있을경우 기본 DataSource생성
        if(ConfigHelper.get("db.type") != null && !"".equals(ConfigHelper.get("db.type").trim())){
            this.makeDataSource(Constants.DEFAULT_DATA_SOURCE);
        }
        //data_source.list에 명시된 DataSource 생성
        if(ConfigHelper.get("data_source.list") != null){
            String dataSourceListString = ConfigHelper.get("data_source.list");
            String[] arr = dataSourceListString.split(",");
            for(String d:arr){
                String s1 = d.trim();
                if("".equals(s1)){
                    continue;
                }
                makeDataSource(s1);
            }
        }
    }

    private void makeDataSource(String dataSourceName)throws Exception{
        String dsPrefix = "";
        if(!Constants.DEFAULT_DATA_SOURCE.equals(dataSourceName)){
            dsPrefix = dataSourceName + ".";
        }
        if("JNDI".equals(ConfigHelper.get(dsPrefix+"db.type"))){
            InitialContext ic2 = new InitialContext();
            javax.naming.Context envContext = (javax.naming.Context) ic2.lookup("java:/comp/env");
            DataSource dataSource = (DataSource) envContext.lookup(ConfigHelper.get(dsPrefix+"db.jndi"));
            this.dataSourceMap.put(dataSourceName, dataSource);
        }else if("POOL".equals(ConfigHelper.get(dsPrefix+"db.type"))){
            String driverClassName = ConfigHelper.get(dsPrefix+"db.driverClassName");
            String url = ConfigHelper.get(dsPrefix+"db.url");
            String username = ConfigHelper.get(dsPrefix+"db.username");
            String password = ConfigHelper.get(dsPrefix+"db.password");
            int initialSize = ConfigHelper.getInt(dsPrefix+"db.initialSize");
            int maxActive = ConfigHelper.getInt(dsPrefix+"db.maxActive");
            int maxIdle = ConfigHelper.getInt(dsPrefix+"db.maxIdle");
            int minIdle = ConfigHelper.getInt(dsPrefix+"db.minIdle");
            int validationInterval = -1;
            try {
                validationInterval = ConfigHelper.getInt(dsPrefix + "db.validation.interval");
            }catch(Exception ignore){}

            Class clazz = Thread.currentThread().getContextClassLoader().loadClass("org.apache.tomcat.jdbc.pool.DataSource");
            Object object = clazz.newInstance();
            ReflectionUtil.invokeSetter(object,"driverClassName",driverClassName, true);
            ReflectionUtil.invokeSetter(object,"url",url, true);
            ReflectionUtil.invokeSetter(object,"username",username, true);
            ReflectionUtil.invokeSetter(object,"password",password, true);
            ReflectionUtil.invokeSetter(object,"initialSize",initialSize, true);
            ReflectionUtil.invokeSetter(object,"maxActive",maxActive, true);
            ReflectionUtil.invokeSetter(object,"maxIdle",maxIdle, true);
            ReflectionUtil.invokeSetter(object,"minIdle",minIdle, true);

            if(validationInterval > 0){
                ReflectionUtil.invokeSetter(object,"validationInterval",validationInterval, true);
                String validationQuery = ConfigHelper.get(dsPrefix+"db.validation.query");
                ReflectionUtil.invokeSetter(object,"validationQuery",validationQuery, true);
            }

            this.dataSourceMap.put(dataSourceName, (DataSource)object);
        }else{
            //error
        }
        log.fine("DataSource created : "+dataSourceName);
    }

}
