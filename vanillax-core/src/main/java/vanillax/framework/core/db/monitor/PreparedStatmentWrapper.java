package vanillax.framework.core.db.monitor;

import vanillax.framework.core.util.StringUtil;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class PreparedStatmentWrapper extends StatementWrapper implements PreparedStatement {
    private static Logger log = Logger.getLogger(PreparedStatmentWrapper.class.getName());
    private PreparedStatement preparedStatement;
    private String sql;
    private Map<Integer,String> paramMap;
    private DecimalFormat formatter = null;

    public PreparedStatmentWrapper(String sql, PreparedStatement preparedStatement) {
        super(preparedStatement);
        this.sql = sql;
        this.preparedStatement = preparedStatement;
        this.paramMap = new HashMap<>();
        formatter = new DecimalFormat("#,###");
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        long t1 = System.currentTimeMillis();
        log.info("Execution Query ["+this.id+"] : " + makeSql());
        ResultSet result = new ResultSetWrapper(preparedStatement.executeQuery(), this.id);
        long duration = System.currentTimeMillis() - t1;
        log.info("Query Execution Time ["+this.id+"] : " + formatter.format(duration) + " ms");
        return result;
    }

    @Override
    public int executeUpdate() throws SQLException {
        long t1 = System.currentTimeMillis();
        log.info("Execution Query ["+this.id+"] : " + makeSql());
        int result = preparedStatement.executeUpdate();
        long duration = System.currentTimeMillis() - t1;
        log.info("Query Execution Time ["+this.id+"] : " + formatter.format(duration) + " ms");
        return result;
    }

    @Override
    public boolean execute() throws SQLException {
        long t1 = System.currentTimeMillis();
        log.info("Execution Query ["+this.id+"] : " + makeSql());
        boolean result = preparedStatement.execute();
        long duration = System.currentTimeMillis() - t1;
        log.info("Query Execution Time ["+this.id+"] : " + formatter.format(duration) + " ms");
        return result;
    }

    @Override
    public long executeLargeUpdate() throws SQLException {
        long t1 = System.currentTimeMillis();
        log.info("Execution Query ["+this.id+"] : " + makeSql());
        long result = preparedStatement.executeLargeUpdate();
        long duration = System.currentTimeMillis() - t1;
        log.info("Query Execution Time ["+this.id+"] : " + formatter.format(duration) + " ms");
        return result;
    }

    @Override
    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        this.paramMap.put(parameterIndex, "null");
        preparedStatement.setNull(parameterIndex, sqlType);
    }

    @Override
    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        this.paramMap.put(parameterIndex, ""+x);
        preparedStatement.setBoolean(parameterIndex, x);
    }

    @Override
    public void setByte(int parameterIndex, byte x) throws SQLException {
        this.paramMap.put(parameterIndex, String.format("0x%02X",x));
        preparedStatement.setByte(parameterIndex, x);
    }

    @Override
    public void setShort(int parameterIndex, short x) throws SQLException {
        this.paramMap.put(parameterIndex, ""+x);
        preparedStatement.setShort(parameterIndex, x);
    }

    @Override
    public void setInt(int parameterIndex, int x) throws SQLException {
        this.paramMap.put(parameterIndex, ""+x);
        preparedStatement.setInt(parameterIndex, x);
    }

    @Override
    public void setLong(int parameterIndex, long x) throws SQLException {
        this.paramMap.put(parameterIndex, ""+x);
        preparedStatement.setLong(parameterIndex, x);
    }

    @Override
    public void setFloat(int parameterIndex, float x) throws SQLException {
        this.paramMap.put(parameterIndex, ""+x);
        preparedStatement.setFloat(parameterIndex, x);
    }

    @Override
    public void setDouble(int parameterIndex, double x) throws SQLException {
        this.paramMap.put(parameterIndex, ""+x);
        preparedStatement.setDouble(parameterIndex, x);
    }

    @Override
    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        this.paramMap.put(parameterIndex, String.valueOf(x.doubleValue()));
        preparedStatement.setBigDecimal(parameterIndex, x);
    }

    @Override
    public void setString(int parameterIndex, String x) throws SQLException {
        this.paramMap.put(parameterIndex, "'"+x+"'");
        preparedStatement.setString(parameterIndex, x);
    }

    @Override
    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        String s = StringUtil.bytesToHex(x);
        this.paramMap.put(parameterIndex, "0x"+s);
        preparedStatement.setBytes(parameterIndex, x);
    }

    @Override
    public void setDate(int parameterIndex, Date x) throws SQLException {
        this.paramMap.put(parameterIndex, "'"+x+"'");
        preparedStatement.setDate(parameterIndex, x);
    }

    @Override
    public void setTime(int parameterIndex, Time x) throws SQLException {
        String s = null;
        if(x != null){
            s = "'"+x.toString()+"'";
        }
        this.paramMap.put(parameterIndex, s);
        preparedStatement.setTime(parameterIndex, x);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        String s = null;
        if(x != null){
            s = "'"+x.toString()+"'";
        }
        this.paramMap.put(parameterIndex, s);
        preparedStatement.setTimestamp(parameterIndex, x);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        this.paramMap.put(parameterIndex, "ASCII_STREAM");
        preparedStatement.setAsciiStream(parameterIndex, x, length);
    }

    @Override
    @Deprecated
    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        this.paramMap.put(parameterIndex, "UNICODE_STREAM");
        preparedStatement.setUnicodeStream(parameterIndex, x, length);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        this.paramMap.put(parameterIndex, "BINARY_STREAM");
        preparedStatement.setBinaryStream(parameterIndex, x, length);
    }

    @Override
    public void clearParameters() throws SQLException {
        this.paramMap.clear();
        preparedStatement.clearParameters();
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        String s = null;
        if(x != null){
            switch (targetSqlType){
                case Types.VARCHAR:
                case Types.NCHAR:
                case Types.CHAR:
                case Types.DATE:
                case Types.TIME:
                case Types.TIMESTAMP:
                    s = "'"+x.toString()+"'";
                    break;
                default:
                    s = x.toString();
                    break;
            }
        }
        this.paramMap.put(parameterIndex, s);
        preparedStatement.setObject(parameterIndex, x, targetSqlType);
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        String s = null;
        if(x != null){
            if(x instanceof String || x instanceof Date || x instanceof java.util.Date || x instanceof Timestamp){
                s = "'"+x.toString()+"'";
            }else{
                s = x.toString();
            }
        }
        this.paramMap.put(parameterIndex, s);
        preparedStatement.setObject(parameterIndex, x);
    }


    @Override
    public void addBatch() throws SQLException {
        preparedStatement.addBatch();
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        this.paramMap.put(parameterIndex, "CHARACTER_STREAM");
        preparedStatement.setCharacterStream(parameterIndex, reader, length);
    }

    @Override
    public void setRef(int parameterIndex, Ref x) throws SQLException {
        this.paramMap.put(parameterIndex, "REF");
        preparedStatement.setRef(parameterIndex, x);
    }

    @Override
    public void setBlob(int parameterIndex, Blob x) throws SQLException {
        this.paramMap.put(parameterIndex, "BLOB");
        preparedStatement.setBlob(parameterIndex, x);
    }

    @Override
    public void setClob(int parameterIndex, Clob x) throws SQLException {
        this.paramMap.put(parameterIndex, "CLOB");
        preparedStatement.setClob(parameterIndex, x);
    }

    @Override
    public void setArray(int parameterIndex, Array x) throws SQLException {
        this.paramMap.put(parameterIndex, "ARRAY");
        preparedStatement.setArray(parameterIndex, x);
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return preparedStatement.getMetaData();
    }

    @Override
    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        String s = null;
        if(x != null){
            s = "'"+x.toString()+"'";
        }
        this.paramMap.put(parameterIndex, s);
        preparedStatement.setDate(parameterIndex, x, cal);
    }

    @Override
    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        String s = null;
        if(x != null){
            s = "'"+x.toString()+"'";
        }
        this.paramMap.put(parameterIndex, s);
        preparedStatement.setTime(parameterIndex, x, cal);
    }

    @Override
    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        String s = null;
        if(x != null){
            s = "'"+x.toString()+"'";
        }
        this.paramMap.put(parameterIndex, s);
        preparedStatement.setTimestamp(parameterIndex, x, cal);
    }

    @Override
    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
        this.paramMap.put(parameterIndex, "null");
        preparedStatement.setNull(parameterIndex, sqlType, typeName);
    }

    @Override
    public void setURL(int parameterIndex, URL x) throws SQLException {
        String s = null;
        if(x != null){
            s = "'"+x.toString()+"'";
        }
        this.paramMap.put(parameterIndex, s);
        preparedStatement.setURL(parameterIndex, x);
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        return preparedStatement.getParameterMetaData();
    }

    @Override
    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        this.paramMap.put(parameterIndex, x.toString());
        preparedStatement.setRowId(parameterIndex, x);
    }

    @Override
    public void setNString(int parameterIndex, String value) throws SQLException {
        this.paramMap.put(parameterIndex, "'"+value.toString()+"'");
        preparedStatement.setNString(parameterIndex, value);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        this.paramMap.put(parameterIndex, "NCHARACTER_STREAM");
        preparedStatement.setNCharacterStream(parameterIndex, value, length);
    }

    @Override
    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        this.paramMap.put(parameterIndex, "NCLOB");
        preparedStatement.setNClob(parameterIndex, value);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        this.paramMap.put(parameterIndex, "CLOB");
        preparedStatement.setClob(parameterIndex, reader, length);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        this.paramMap.put(parameterIndex, "BLOB");
        preparedStatement.setBlob(parameterIndex, inputStream, length);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        this.paramMap.put(parameterIndex, "NCLOB");
        preparedStatement.setNClob(parameterIndex, reader, length);
    }

    @Override
    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        this.paramMap.put(parameterIndex, xmlObject.toString());
        preparedStatement.setSQLXML(parameterIndex, xmlObject);
    }

    @Override
    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        String s = null;
        if(x != null){
            switch (targetSqlType){
                case Types.VARCHAR:
                case Types.NCHAR:
                case Types.CHAR:
                case Types.DATE:
                case Types.TIME:
                case Types.TIMESTAMP:
                    s = "'"+x.toString()+"'";
                    break;
                default:
                    s = x.toString();
                    break;
            }
        }
        this.paramMap.put(parameterIndex, s);
        preparedStatement.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        this.paramMap.put(parameterIndex, "ASCII_STREAM");
        preparedStatement.setAsciiStream(parameterIndex, x, length);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        this.paramMap.put(parameterIndex, "BINARY_STREAM");
        preparedStatement.setBinaryStream(parameterIndex, x, length);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        this.paramMap.put(parameterIndex, "CHARACTER_STREAM");
        preparedStatement.setCharacterStream(parameterIndex, reader, length);
    }

    @Override
    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        this.paramMap.put(parameterIndex, "ASCII_STREAM");
        preparedStatement.setAsciiStream(parameterIndex, x);
    }

    @Override
    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        this.paramMap.put(parameterIndex, "BINARY_STREAM");
        preparedStatement.setBinaryStream(parameterIndex, x);
    }

    @Override
    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        this.paramMap.put(parameterIndex, "CHARACTER_STREAM");
        preparedStatement.setCharacterStream(parameterIndex, reader);
    }

    @Override
    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        this.paramMap.put(parameterIndex, "NCHARACTER_STREAM");
        preparedStatement.setNCharacterStream(parameterIndex, value);
    }

    @Override
    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        this.paramMap.put(parameterIndex, "CLOB");
        preparedStatement.setClob(parameterIndex, reader);
    }

    @Override
    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        this.paramMap.put(parameterIndex, "BLOB");
        preparedStatement.setBlob(parameterIndex, inputStream);
    }

    @Override
    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        this.paramMap.put(parameterIndex, "NCLOB");
        preparedStatement.setNClob(parameterIndex, reader);
    }

    @Override
    public void setObject(int parameterIndex, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        this.paramMap.put(parameterIndex, x.toString());
        preparedStatement.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
    }

    @Override
    public void setObject(int parameterIndex, Object x, SQLType targetSqlType) throws SQLException {
        this.paramMap.put(parameterIndex, x.toString());
        preparedStatement.setObject(parameterIndex, x, targetSqlType);
    }

    private String makeSql(){
        StringBuffer sb = new StringBuffer();
        boolean commentFlag = false;
        boolean doubleSlash = false;
        if(this.sql == null)
            return null;
        char prev = 0;
        int paramIndex = 0;
        for(char c : sql.toCharArray()){
            if(!commentFlag && prev == '/' && c == '*'){
                commentFlag = true;
            }else if(!commentFlag && prev == '/' && c == '/'){
                commentFlag = true;
                doubleSlash = true;
            }else if(!commentFlag && prev == '-' && c == '-'){
                commentFlag = true;
                doubleSlash = true;
            }else if(commentFlag && prev == '*' && c == '/'){
                commentFlag = false;
            }else if(commentFlag && doubleSlash && c == '\n'){
                commentFlag = false;
                doubleSlash = false;
            }
            if(!commentFlag && c == '?'){
                paramIndex++;
                String s = "?";//세팅된 값이 없으면 ?표시 그대로 둔다.
                if(this.paramMap.containsKey(paramIndex)){
                    if(s == null)
                        s = "null";
                    else
                        s = this.paramMap.get(paramIndex);
                }
                sb.append(s);
            }else{
                sb.append(c);
            }
            prev = c;
        }
        return sb.toString();
    }


}
