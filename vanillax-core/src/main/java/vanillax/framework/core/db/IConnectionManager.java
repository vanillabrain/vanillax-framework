package vanillax.framework.core.db;

import javax.sql.DataSource;
import java.sql.Connection;

public interface IConnectionManager {


    public DataSource getDataSource(String dataSourceName)throws Exception;

    public DataSource getDataSource()throws Exception;

    public Connection getConnection(String dataSourceName)throws Exception;

    public Connection getConnection()throws Exception;

    public void setDataSource(String dataSourceName, DataSource dataSource) ;

    public void setDataSource(DataSource dataSource) ;
}
