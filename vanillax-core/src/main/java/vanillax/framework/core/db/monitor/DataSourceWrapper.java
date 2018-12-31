package vanillax.framework.core.db.monitor;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * DataSource모니터링을 위한 랩퍼클래스
 */
public class DataSourceWrapper extends JdbcBaseWrapper implements DataSource{
    private DataSource dataSource = null;

    public DataSourceWrapper(DataSource dataSource){
        super();
        this.dataSource = dataSource;
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection c = this.dataSource.getConnection();
        ConnectionWrapper wrapper = new ConnectionWrapper(c);
        wrapper.resetActiveTime();
        StackTraceElement[] traces = Thread.currentThread().getStackTrace();//이게 비용이 상당히 비싸다
        ConnectionMonitor.getInstance().onGetConnection(wrapper.getId(), traces);
        return wrapper;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection c = this.dataSource.getConnection(username,password);
        ConnectionWrapper wrapper = new ConnectionWrapper(c);
        wrapper.resetActiveTime();
        //이미 closed된 Connection일 경우 모니터링 대상에 넣지 않는다.
        if(!c.isClosed()){
            StackTraceElement[] traces = Thread.currentThread().getStackTrace();//이게 비용이 상당히 비싸다
            ConnectionMonitor.getInstance().onGetConnection(wrapper.getId(), traces);
        }
        return wrapper;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return this.dataSource.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        this.dataSource.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        this.dataSource.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return this.dataSource.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return this.dataSource.getParentLogger();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return this.dataSource.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return this.dataSource.isWrapperFor(iface);
    }
}
