package vanillax.framework.core.db.monitor;

import vanillax.framework.core.db.monitor.print.ResultSetCollector;
import vanillax.framework.core.db.monitor.print.ResultSetPrinter;
import vanillax.framework.core.config.FrameworkConfig;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.logging.Logger;

public class ResultSetWrapper extends JdbcBaseWrapper implements ResultSet{
    private static Logger log = Logger.getLogger(ResultSetWrapper.class.getName());

    private ResultSet resultSet;
    private boolean resultSetPrintable = false;
    private ResultSetCollector resultSetCollector = null;
    private long startTime = 0;
    private String traceId = null;
    private DecimalFormat formatter = null;

    public ResultSetWrapper(ResultSet resultSet) {
        this(resultSet, null);
    }

    public ResultSetWrapper(ResultSet resultSet, String traceId) {
        super();
        this.resultSet = resultSet;
        this.traceId = traceId;
        this.resultSetPrintable = FrameworkConfig.getBoolean("monitor.db",false)
                && FrameworkConfig.getBoolean("monitor.db.result_set_printable",false);
        if(this.resultSetPrintable){
            this.resultSetCollector = new ResultSetCollector(resultSet);
        }
        startTime = System.currentTimeMillis();
        formatter = new DecimalFormat("#,###");
    }

    @Override
    public boolean next() throws SQLException {
        boolean result = resultSet.next();
        if(!result){
            //nothing.
        }else{
            if(this.resultSetPrintable){
                this.resultSetCollector.onNext();
            }
        }
        return result;
    }

    @Override
    public void close() throws SQLException {
        resultSet.close();
        if(this.resultSetPrintable){
            ResultSetPrinter resultSetPrinter = new ResultSetPrinter();
            String s = resultSetPrinter.getResultSetToPrint(this.resultSetCollector);
            log.info("Fetched Data ["+traceId+"] : "+s);
            log.info("Fetch Time ["+traceId+"]: " + formatter.format(System.currentTimeMillis() - startTime) + " ms");
        }
    }

    @Override
    public boolean wasNull() throws SQLException {
        return resultSet.wasNull();
    }

    @Override
    public String getString(int columnIndex) throws SQLException {
        String result = resultSet.getString(columnIndex);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnIndex, result);
        }
        return result;
    }

    @Override
    public boolean getBoolean(int columnIndex) throws SQLException {
        boolean result = resultSet.getBoolean(columnIndex);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnIndex, result);
        }
        return result;
    }

    @Override
    public byte getByte(int columnIndex) throws SQLException {
        byte result = resultSet.getByte(columnIndex);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnIndex, result);
        }
        return result;
    }

    @Override
    public short getShort(int columnIndex) throws SQLException {
        short result = resultSet.getShort(columnIndex);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnIndex, result);
        }
        return result;
    }

    @Override
    public int getInt(int columnIndex) throws SQLException {
        int result = resultSet.getInt(columnIndex);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnIndex, result);
        }
        return result;
    }

    @Override
    public long getLong(int columnIndex) throws SQLException {
        long result = resultSet.getLong(columnIndex);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnIndex, result);
        }
        return result;
    }

    @Override
    public float getFloat(int columnIndex) throws SQLException {
        float result = resultSet.getFloat(columnIndex);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnIndex, result);
        }
        return result;
    }

    @Override
    public double getDouble(int columnIndex) throws SQLException {
        double result = resultSet.getDouble(columnIndex);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnIndex, result);
        }
        return result;
    }

    @Override
    @Deprecated
    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        BigDecimal result = resultSet.getBigDecimal(columnIndex, scale);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnIndex, result);
        }
        return result;
    }

    @Override
    public byte[] getBytes(int columnIndex) throws SQLException {
        byte[] result = resultSet.getBytes(columnIndex);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnIndex, result);
        }
        return result;
    }

    @Override
    public Date getDate(int columnIndex) throws SQLException {
        Date result = resultSet.getDate(columnIndex);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnIndex, result);
        }
        return result;
    }

    @Override
    public Time getTime(int columnIndex) throws SQLException {
        Time result = resultSet.getTime(columnIndex);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnIndex, result);
        }
        return result;
    }

    @Override
    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        Timestamp result = resultSet.getTimestamp(columnIndex);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnIndex, result);
        }
        return result;
    }

    @Override
    public InputStream getAsciiStream(int columnIndex) throws SQLException {
        InputStream result = resultSet.getAsciiStream(columnIndex);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnIndex, "[ASCII_STREAM]");
        }
        return result;
    }

    @Override
    @Deprecated
    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        InputStream result = resultSet.getUnicodeStream(columnIndex);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnIndex, "[UNICODE_STREAM]");
        }
        return result;
    }

    @Override
    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        InputStream result = resultSet.getBinaryStream(columnIndex);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnIndex, "[BINARY_STREAM]");
        }
        return result;
    }

    @Override
    public String getString(String columnLabel) throws SQLException {
        String result = resultSet.getString(columnLabel);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnLabel, result);
        }
        return result;
    }

    @Override
    public boolean getBoolean(String columnLabel) throws SQLException {
        boolean result = resultSet.getBoolean(columnLabel);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnLabel, result);
        }
        return result;
    }

    @Override
    public byte getByte(String columnLabel) throws SQLException {
        byte result = resultSet.getByte(columnLabel);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnLabel, result);
        }
        return result;
    }

    @Override
    public short getShort(String columnLabel) throws SQLException {
        short result = resultSet.getShort(columnLabel);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnLabel, result);
        }
        return result;
    }

    @Override
    public int getInt(String columnLabel) throws SQLException {
        int result = resultSet.getInt(columnLabel);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnLabel, result);
        }
        return result;
    }

    @Override
    public long getLong(String columnLabel) throws SQLException {
        long result = resultSet.getLong(columnLabel);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnLabel, result);
        }
        return result;
    }

    @Override
    public float getFloat(String columnLabel) throws SQLException {
        float result = resultSet.getFloat(columnLabel);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnLabel, result);
        }
        return result;
    }

    @Override
    public double getDouble(String columnLabel) throws SQLException {
        double result = resultSet.getDouble(columnLabel);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnLabel, result);
        }
        return result;
    }

    @Override
    @Deprecated
    public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
        BigDecimal result = resultSet.getBigDecimal(columnLabel, scale);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnLabel, result);
        }
        return result;
    }

    @Override
    public byte[] getBytes(String columnLabel) throws SQLException {
        byte[] result = resultSet.getBytes(columnLabel);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnLabel, result);
        }
        return result;
    }

    @Override
    public Date getDate(String columnLabel) throws SQLException {
        Date result = resultSet.getDate(columnLabel);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnLabel, result);
        }
        return result;
    }

    @Override
    public Time getTime(String columnLabel) throws SQLException {
        Time result = resultSet.getTime(columnLabel);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnLabel, result);
        }
        return result;
    }

    @Override
    public Timestamp getTimestamp(String columnLabel) throws SQLException {
        Timestamp result = resultSet.getTimestamp(columnLabel);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnLabel, result);
        }
        return result;
    }

    @Override
    public InputStream getAsciiStream(String columnLabel) throws SQLException {
        InputStream result = resultSet.getAsciiStream(columnLabel);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnLabel, "[ASCII_STREAM]");
        }
        return result;
    }

    @Override
    @Deprecated
    public InputStream getUnicodeStream(String columnLabel) throws SQLException {
        InputStream result = resultSet.getUnicodeStream(columnLabel);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnLabel, "[UNICODE_STREAM]");
        }
        return result;
    }

    @Override
    public InputStream getBinaryStream(String columnLabel) throws SQLException {
        InputStream result = resultSet.getBinaryStream(columnLabel);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnLabel, "[BINARY_STREAM]");
        }
        return result;
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return resultSet.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        resultSet.clearWarnings();
    }

    @Override
    public String getCursorName() throws SQLException {
        return resultSet.getCursorName();
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return resultSet.getMetaData();
    }

    @Override
    public Object getObject(int columnIndex) throws SQLException {
        Object result = resultSet.getObject(columnIndex);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnIndex, result);
        }
        return result;
    }

    @Override
    public Object getObject(String columnLabel) throws SQLException {
        Object result = resultSet.getObject(columnLabel);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnLabel, result);
        }
        return result;
    }

    @Override
    public int findColumn(String columnLabel) throws SQLException {
        return resultSet.findColumn(columnLabel);
    }

    @Override
    public Reader getCharacterStream(int columnIndex) throws SQLException {
        Reader result = resultSet.getCharacterStream(columnIndex);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnIndex, "[CHARACTER_STREAM]");
        }
        return result;
    }

    @Override
    public Reader getCharacterStream(String columnLabel) throws SQLException {
        Reader result = resultSet.getCharacterStream(columnLabel);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnLabel, "[CHARACTER_STREAM]");
        }
        return result;
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        BigDecimal result = resultSet.getBigDecimal(columnIndex);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnIndex, result);
        }
        return result;
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
        BigDecimal result = resultSet.getBigDecimal(columnLabel);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnLabel, result);
        }
        return result;
    }

    @Override
    public boolean isBeforeFirst() throws SQLException {
        return resultSet.isBeforeFirst();
    }

    @Override
    public boolean isAfterLast() throws SQLException {
        return resultSet.isAfterLast();
    }

    @Override
    public boolean isFirst() throws SQLException {
        return resultSet.isFirst();
    }

    @Override
    public boolean isLast() throws SQLException {
        return resultSet.isLast();
    }

    @Override
    public void beforeFirst() throws SQLException {
        resultSet.beforeFirst();
    }

    @Override
    public void afterLast() throws SQLException {
        resultSet.afterLast();
    }

    @Override
    public boolean first() throws SQLException {
        return resultSet.first();
    }

    @Override
    public boolean last() throws SQLException {
        return resultSet.last();
    }

    @Override
    public int getRow() throws SQLException {
        return resultSet.getRow();
    }

    @Override
    public boolean absolute(int row) throws SQLException {
        return resultSet.absolute(row);
    }

    @Override
    public boolean relative(int rows) throws SQLException {
        return resultSet.relative(rows);
    }

    @Override
    public boolean previous() throws SQLException {
        return resultSet.previous();
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        resultSet.setFetchDirection(direction);
    }

    @Override
    public int getFetchDirection() throws SQLException {
        return resultSet.getFetchDirection();
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        resultSet.setFetchSize(rows);
    }

    @Override
    public int getFetchSize() throws SQLException {
        return resultSet.getFetchSize();
    }

    @Override
    public int getType() throws SQLException {
        return resultSet.getType();
    }

    @Override
    public int getConcurrency() throws SQLException {
        return resultSet.getConcurrency();
    }

    @Override
    public boolean rowUpdated() throws SQLException {
        return resultSet.rowUpdated();
    }

    @Override
    public boolean rowInserted() throws SQLException {
        return resultSet.rowInserted();
    }

    @Override
    public boolean rowDeleted() throws SQLException {
        return resultSet.rowDeleted();
    }

    @Override
    public void updateNull(int columnIndex) throws SQLException {
        resultSet.updateNull(columnIndex);
    }

    @Override
    public void updateBoolean(int columnIndex, boolean x) throws SQLException {
        resultSet.updateBoolean(columnIndex, x);
    }

    @Override
    public void updateByte(int columnIndex, byte x) throws SQLException {
        resultSet.updateByte(columnIndex, x);
    }

    @Override
    public void updateShort(int columnIndex, short x) throws SQLException {
        resultSet.updateShort(columnIndex, x);
    }

    @Override
    public void updateInt(int columnIndex, int x) throws SQLException {
        resultSet.updateInt(columnIndex, x);
    }

    @Override
    public void updateLong(int columnIndex, long x) throws SQLException {
        resultSet.updateLong(columnIndex, x);
    }

    @Override
    public void updateFloat(int columnIndex, float x) throws SQLException {
        resultSet.updateFloat(columnIndex, x);
    }

    @Override
    public void updateDouble(int columnIndex, double x) throws SQLException {
        resultSet.updateDouble(columnIndex, x);
    }

    @Override
    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
        resultSet.updateBigDecimal(columnIndex, x);
    }

    @Override
    public void updateString(int columnIndex, String x) throws SQLException {
        resultSet.updateString(columnIndex, x);
    }

    @Override
    public void updateBytes(int columnIndex, byte[] x) throws SQLException {
        resultSet.updateBytes(columnIndex, x);
    }

    @Override
    public void updateDate(int columnIndex, Date x) throws SQLException {
        resultSet.updateDate(columnIndex, x);
    }

    @Override
    public void updateTime(int columnIndex, Time x) throws SQLException {
        resultSet.updateTime(columnIndex, x);
    }

    @Override
    public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
        resultSet.updateTimestamp(columnIndex, x);
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
        resultSet.updateAsciiStream(columnIndex, x, length);
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
        resultSet.updateBinaryStream(columnIndex, x, length);
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
        resultSet.updateCharacterStream(columnIndex, x, length);
    }

    @Override
    public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {
        resultSet.updateObject(columnIndex, x, scaleOrLength);
    }

    @Override
    public void updateObject(int columnIndex, Object x) throws SQLException {
        resultSet.updateObject(columnIndex, x);
    }

    @Override
    public void updateNull(String columnLabel) throws SQLException {
        resultSet.updateNull(columnLabel);
    }

    @Override
    public void updateBoolean(String columnLabel, boolean x) throws SQLException {
        resultSet.updateBoolean(columnLabel, x);
    }

    @Override
    public void updateByte(String columnLabel, byte x) throws SQLException {
        resultSet.updateByte(columnLabel, x);
    }

    @Override
    public void updateShort(String columnLabel, short x) throws SQLException {
        resultSet.updateShort(columnLabel, x);
    }

    @Override
    public void updateInt(String columnLabel, int x) throws SQLException {
        resultSet.updateInt(columnLabel, x);
    }

    @Override
    public void updateLong(String columnLabel, long x) throws SQLException {
        resultSet.updateLong(columnLabel, x);
    }

    @Override
    public void updateFloat(String columnLabel, float x) throws SQLException {
        resultSet.updateFloat(columnLabel, x);
    }

    @Override
    public void updateDouble(String columnLabel, double x) throws SQLException {
        resultSet.updateDouble(columnLabel, x);
    }

    @Override
    public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {
        resultSet.updateBigDecimal(columnLabel, x);
    }

    @Override
    public void updateString(String columnLabel, String x) throws SQLException {
        resultSet.updateString(columnLabel, x);
    }

    @Override
    public void updateBytes(String columnLabel, byte[] x) throws SQLException {
        resultSet.updateBytes(columnLabel, x);
    }

    @Override
    public void updateDate(String columnLabel, Date x) throws SQLException {
        resultSet.updateDate(columnLabel, x);
    }

    @Override
    public void updateTime(String columnLabel, Time x) throws SQLException {
        resultSet.updateTime(columnLabel, x);
    }

    @Override
    public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {
        resultSet.updateTimestamp(columnLabel, x);
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {
        resultSet.updateAsciiStream(columnLabel, x, length);
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {
        resultSet.updateBinaryStream(columnLabel, x, length);
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {
        resultSet.updateCharacterStream(columnLabel, reader, length);
    }

    @Override
    public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {
        resultSet.updateObject(columnLabel, x, scaleOrLength);
    }

    @Override
    public void updateObject(String columnLabel, Object x) throws SQLException {
        resultSet.updateObject(columnLabel, x);
    }

    @Override
    public void insertRow() throws SQLException {
        resultSet.insertRow();
    }

    @Override
    public void updateRow() throws SQLException {
        resultSet.updateRow();
    }

    @Override
    public void deleteRow() throws SQLException {
        resultSet.deleteRow();
    }

    @Override
    public void refreshRow() throws SQLException {
        resultSet.refreshRow();
    }

    @Override
    public void cancelRowUpdates() throws SQLException {
        resultSet.cancelRowUpdates();
    }

    @Override
    public void moveToInsertRow() throws SQLException {
        resultSet.moveToInsertRow();
    }

    @Override
    public void moveToCurrentRow() throws SQLException {
        resultSet.moveToCurrentRow();
    }

    @Override
    public Statement getStatement() throws SQLException {
        return resultSet.getStatement();
    }

    @Override
    public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
        return resultSet.getObject(columnIndex, map);
    }

    @Override
    public Ref getRef(int columnIndex) throws SQLException {
        Ref result = resultSet.getRef(columnIndex);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnIndex, "[REF]");
        }
        return result;
    }

    @Override
    public Blob getBlob(int columnIndex) throws SQLException {
        Blob result = resultSet.getBlob(columnIndex);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnIndex, "[BLOB]");
        }
        return result;
    }

    @Override
    public Clob getClob(int columnIndex) throws SQLException {
        Clob result = resultSet.getClob(columnIndex);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnIndex, "[CLOB]");
        }
        return result;
    }

    @Override
    public Array getArray(int columnIndex) throws SQLException {
        Array result = resultSet.getArray(columnIndex);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnIndex, "[ARRAY]");
        }
        return result;
    }

    @Override
    public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
        Object result = resultSet.getObject(columnLabel, map);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnLabel, result);
        }
        return result;
    }

    @Override
    public Ref getRef(String columnLabel) throws SQLException {
        Ref result = resultSet.getRef(columnLabel);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnLabel, "[REF]");
        }
        return result;
    }

    @Override
    public Blob getBlob(String columnLabel) throws SQLException {
        Blob result = resultSet.getBlob(columnLabel);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnLabel, "[BLOB]");
        }
        return result;
    }

    @Override
    public Clob getClob(String columnLabel) throws SQLException {
        Clob result = resultSet.getClob(columnLabel);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnLabel, "[CLOB]");
        }
        return result;
    }

    @Override
    public Array getArray(String columnLabel) throws SQLException {
        Array result = resultSet.getArray(columnLabel);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnLabel, "[ARRAY]");
        }
        return result;
    }

    @Override
    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        Date result = resultSet.getDate(columnIndex, cal);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnIndex, result);
        }
        return result;
    }

    @Override
    public Date getDate(String columnLabel, Calendar cal) throws SQLException {
        Date result = resultSet.getDate(columnLabel, cal);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnLabel, result);
        }
        return result;
    }

    @Override
    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        Time result = resultSet.getTime(columnIndex, cal);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnIndex, result);
        }
        return result;
    }

    @Override
    public Time getTime(String columnLabel, Calendar cal) throws SQLException {
        Time result = resultSet.getTime(columnLabel, cal);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnLabel, result);
        }
        return result;
    }

    @Override
    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        Timestamp result = resultSet.getTimestamp(columnIndex, cal);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnIndex, result);
        }
        return result;
    }

    @Override
    public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
        Timestamp result = resultSet.getTimestamp(columnLabel, cal);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnLabel, result);
        }
        return result;
    }

    @Override
    public URL getURL(int columnIndex) throws SQLException {
        URL result = resultSet.getURL(columnIndex);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnIndex, result);
        }
        return result;
    }

    @Override
    public URL getURL(String columnLabel) throws SQLException {
        URL result = resultSet.getURL(columnLabel);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnLabel, result);
        }
        return result;
    }

    @Override
    public void updateRef(int columnIndex, Ref x) throws SQLException {
        resultSet.updateRef(columnIndex, x);
    }

    @Override
    public void updateRef(String columnLabel, Ref x) throws SQLException {
        resultSet.updateRef(columnLabel, x);
    }

    @Override
    public void updateBlob(int columnIndex, Blob x) throws SQLException {
        resultSet.updateBlob(columnIndex, x);
    }

    @Override
    public void updateBlob(String columnLabel, Blob x) throws SQLException {
        resultSet.updateBlob(columnLabel, x);
    }

    @Override
    public void updateClob(int columnIndex, Clob x) throws SQLException {
        resultSet.updateClob(columnIndex, x);
    }

    @Override
    public void updateClob(String columnLabel, Clob x) throws SQLException {
        resultSet.updateClob(columnLabel, x);
    }

    @Override
    public void updateArray(int columnIndex, Array x) throws SQLException {
        resultSet.updateArray(columnIndex, x);
    }

    @Override
    public void updateArray(String columnLabel, Array x) throws SQLException {
        resultSet.updateArray(columnLabel, x);
    }

    @Override
    public RowId getRowId(int columnIndex) throws SQLException {
        return resultSet.getRowId(columnIndex);
    }

    @Override
    public RowId getRowId(String columnLabel) throws SQLException {
        return resultSet.getRowId(columnLabel);
    }

    @Override
    public void updateRowId(int columnIndex, RowId x) throws SQLException {
        resultSet.updateRowId(columnIndex, x);
    }

    @Override
    public void updateRowId(String columnLabel, RowId x) throws SQLException {
        resultSet.updateRowId(columnLabel, x);
    }

    @Override
    public int getHoldability() throws SQLException {
        return resultSet.getHoldability();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return resultSet.isClosed();
    }

    @Override
    public void updateNString(int columnIndex, String nString) throws SQLException {
        resultSet.updateNString(columnIndex, nString);
    }

    @Override
    public void updateNString(String columnLabel, String nString) throws SQLException {
        resultSet.updateNString(columnLabel, nString);
    }

    @Override
    public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
        resultSet.updateNClob(columnIndex, nClob);
    }

    @Override
    public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
        resultSet.updateNClob(columnLabel, nClob);
    }

    @Override
    public NClob getNClob(int columnIndex) throws SQLException {
        NClob result = resultSet.getNClob(columnIndex);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnIndex, "[NCLOB]");
        }
        return result;
    }

    @Override
    public NClob getNClob(String columnLabel) throws SQLException {
        NClob result = resultSet.getNClob(columnLabel);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnLabel, "[NCLOB]");
        }
        return result;
    }

    @Override
    public SQLXML getSQLXML(int columnIndex) throws SQLException {
        SQLXML result = resultSet.getSQLXML(columnIndex);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnIndex, result);
        }
        return result;
    }

    @Override
    public SQLXML getSQLXML(String columnLabel) throws SQLException {
        SQLXML result = resultSet.getSQLXML(columnLabel);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnLabel, result);
        }
        return result;
    }

    @Override
    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
        resultSet.updateSQLXML(columnIndex, xmlObject);
    }

    @Override
    public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
        resultSet.updateSQLXML(columnLabel, xmlObject);
    }

    @Override
    public String getNString(int columnIndex) throws SQLException {
        String result = resultSet.getNString(columnIndex);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnIndex, result);
        }
        return result;
    }

    @Override
    public String getNString(String columnLabel) throws SQLException {
        String result = resultSet.getNString(columnLabel);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnLabel, result);
        }
        return result;
    }

    @Override
    public Reader getNCharacterStream(int columnIndex) throws SQLException {
        Reader result = resultSet.getNCharacterStream(columnIndex);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnIndex, "[NCHARACTER_STREAM]");
        }
        return result;
    }

    @Override
    public Reader getNCharacterStream(String columnLabel) throws SQLException {
        Reader result = resultSet.getNCharacterStream(columnLabel);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnLabel, "[NCHARACTER_STREAM]");
        }
        return result;
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        resultSet.updateNCharacterStream(columnIndex, x, length);
    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        resultSet.updateNCharacterStream(columnLabel, reader, length);
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
        resultSet.updateAsciiStream(columnIndex, x, length);
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
        resultSet.updateBinaryStream(columnIndex, x, length);
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        resultSet.updateCharacterStream(columnIndex, x, length);
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
        resultSet.updateAsciiStream(columnLabel, x, length);
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
        resultSet.updateBinaryStream(columnLabel, x, length);
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        resultSet.updateCharacterStream(columnLabel, reader, length);
    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
        resultSet.updateBlob(columnIndex, inputStream, length);
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
        resultSet.updateBlob(columnLabel, inputStream, length);
    }

    @Override
    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
        resultSet.updateClob(columnIndex, reader, length);
    }

    @Override
    public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
        resultSet.updateClob(columnLabel, reader, length);
    }

    @Override
    public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
        resultSet.updateNClob(columnIndex, reader, length);
    }

    @Override
    public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
        resultSet.updateNClob(columnLabel, reader, length);
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
        resultSet.updateNCharacterStream(columnIndex, x);
    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
        resultSet.updateNCharacterStream(columnLabel, reader);
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
        resultSet.updateAsciiStream(columnIndex, x);
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
        resultSet.updateBinaryStream(columnIndex, x);
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
        resultSet.updateCharacterStream(columnIndex, x);
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
        resultSet.updateAsciiStream(columnLabel, x);
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
        resultSet.updateBinaryStream(columnLabel, x);
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
        resultSet.updateCharacterStream(columnLabel, reader);
    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
        resultSet.updateBlob(columnIndex, inputStream);
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
        resultSet.updateBlob(columnLabel, inputStream);
    }

    @Override
    public void updateClob(int columnIndex, Reader reader) throws SQLException {
        resultSet.updateClob(columnIndex, reader);
    }

    @Override
    public void updateClob(String columnLabel, Reader reader) throws SQLException {
        resultSet.updateClob(columnLabel, reader);
    }

    @Override
    public void updateNClob(int columnIndex, Reader reader) throws SQLException {
        resultSet.updateNClob(columnIndex, reader);
    }

    @Override
    public void updateNClob(String columnLabel, Reader reader) throws SQLException {
        resultSet.updateNClob(columnLabel, reader);
    }

    @Override
    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        T result = resultSet.getObject(columnIndex, type);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnIndex, result);
        }
        return result;
    }

    @Override
    public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
        T result = resultSet.getObject(columnLabel, type);
        if(this.resultSetPrintable){
            this.resultSetCollector.setColumnValue(columnLabel, result);
        }
        return result;
    }

    @Override
    public void updateObject(int columnIndex, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        resultSet.updateObject(columnIndex, x, targetSqlType, scaleOrLength);
    }

    @Override
    public void updateObject(String columnLabel, Object x, SQLType targetSqlType, int scaleOrLength) throws SQLException {
        resultSet.updateObject(columnLabel, x, targetSqlType, scaleOrLength);
    }

    @Override
    public void updateObject(int columnIndex, Object x, SQLType targetSqlType) throws SQLException {
        resultSet.updateObject(columnIndex, x, targetSqlType);
    }

    @Override
    public void updateObject(String columnLabel, Object x, SQLType targetSqlType) throws SQLException {
        resultSet.updateObject(columnLabel, x, targetSqlType);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return resultSet.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return resultSet.isWrapperFor(iface);
    }
}
