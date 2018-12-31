package vanillax.framework.core.db.monitor.print;

import vanillax.framework.core.config.FrameworkConfig;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

public class ResultSetCollector {
    private ResultSet resultSet;

    private static final String NULL_STRING = "(null)";
    private static final String UNREAD = "(unread)";
    private static final int DEFAULT_MAX_ROWS_PRINTABLE = 100;
    private int maxRowsPrintable = 0;
    private int totalRows = 0;

    /**
     * ResultSetMetaData를 읽었는지 확인한다.
     */
    private boolean metaDataLoaded;
    private int columnCount;
    private Map<Integer, String> columnLabels;
    private Map<Integer, String> columnNames;
    private List<Object> row;
    private List<List<Object>> rows;
    private Map<String, Integer> colNameToColIndex;
    private int colIndex;

    public ResultSetCollector(ResultSet resultSet) {
        this.resultSet = resultSet;
        this.reset();
        this.loadMetaData(this.resultSet);
    }

    public List<List<Object>> getRows() {
        return rows;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public void reset() {
        metaDataLoaded = false;
        rows = new ArrayList<>();
        row = null;
        colNameToColIndex = null;
        colIndex = -1;// Useful for wasNull calls
        columnCount = 0;
        columnLabels = new HashMap<Integer, String>();
        columnNames = new HashMap<Integer, String>();
        maxRowsPrintable = FrameworkConfig.getInt("monitor.db.result_set_max_rows_printable", DEFAULT_MAX_ROWS_PRINTABLE);
    }

    public void loadMetaData(ResultSet rs) {
        if (this.metaDataLoaded) {
            return;
        }
        try {
            if (!rs.isClosed()) {
                this.loadMetaData(rs.getMetaData());
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        this.metaDataLoaded = true;
    }

    private void loadMetaData(ResultSetMetaData metaData)
    {
        if (this.metaDataLoaded) {
            return;
        }
        try {
            if (metaData == null) {
                this.columnCount = 0;
            } else {
                this.columnCount = metaData.getColumnCount();
            }
            this.colNameToColIndex = new HashMap<String, Integer>(this.columnCount);
            for (int column = 1; column <= this.columnCount; column++) {
                String label = metaData.getColumnLabel(column);
                String name  = metaData.getColumnName(column);
                this.columnLabels.put(column, label);
                this.columnNames.put(column, name);
                colNameToColIndex.put(label, column);
                colNameToColIndex.put(name, column);
            }
            this.metaDataLoaded = true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getColumnName(int column) {
        return this.columnNames.get(column);
    }

    public String getColumnLabel(int column) {
        return this.columnLabels.get(column);
    }


    private void makeRow() {
        if (row == null) {
            row = new ArrayList<Object>(getColumnCount());
            for (int i = 0; i < getColumnCount(); ++i) {
                row.add(UNREAD);
            }
        }
    }

    public void onFirst(){
        if(this.rows != null){
            this.rows.clear();
        }
        this.totalRows = 0;
    }

    public void onNext(){
        this.totalRows++;
        if(this.rows.size() >= this.maxRowsPrintable){//최대 출력줄을 넘어서면 담지 않는다.
            return;
        }
        row = null;
        makeRow();
        this.rows.add(row);
    }

    public void setColumnValue(int index, Object value){
        if(totalRows > this.maxRowsPrintable){//최대 출력줄을 넘어서면 담지 않는다.
            return;
        }
        if(value == null){
            value = NULL_STRING;
        }
        this.row.set(index-1, value);
    }

    public void setColumnValue(String columnLabel, Object value){
        if(totalRows > this.maxRowsPrintable){//최대 출력줄을 넘어서면 담지 않는다.
            return;
        }
        if(value == null){
            value = NULL_STRING;
        }
        this.row.set(this.colNameToColIndex.get(columnLabel)-1, value);
    }

    public int getTotalRows() {
        return totalRows;
    }
}
