package vanillax.framework.core.db.orm;

import groovy.sql.Sql;
import vanillax.framework.core.Constants;
import vanillax.framework.core.util.DateUtil;
import vanillax.framework.core.util.StringUtil;
import vanillax.framework.core.db.IConnectionManager;
import vanillax.framework.core.db.script.VelocityFacade;
import vanillax.framework.core.config.FrameworkConfig;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RepositoryBase {

    protected String dataSourceName = null;
    protected IConnectionManager connectionManager = null;

    /**
     * SQL parsing이 된 Template객체
     * key : 클래스명 + 메소드명 + 파라미터명[]
     */
    protected Map<String, SqlInfo> sqlInfoMap = null;

    protected RepositoryBase(){
        //이 경우가 발생하면 안된다. Groovy객체 compile오류 방지용으로 만들어놓은 것이다.
    }

    protected RepositoryBase(String dataSourceName)throws Exception{
        if(dataSourceName == null)
            this.dataSourceName = Constants.DEFAULT_DATA_SOURCE;
        else
            this.dataSourceName = dataSourceName;
        sqlInfoMap = new HashMap<>(32);
        //ConnectinManager를 획득한다.
        String connectionManagerClass = FrameworkConfig.get("connection.manager.class");
        if(connectionManagerClass == null){
            //do nothing. getConnection()메소드 호출시에 초기화 확인. SQL Object 로딩시에 작업을 수행하여 단위테스트가 안되는 문제가 발생한다.
        }else {
            Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(connectionManagerClass);
            Method m = clazz.getMethod("getInstance", new Class[0]);
            connectionManager = (IConnectionManager) m.invoke(null, new Object[0]);
        }
    }

    protected Connection getConnection()throws Exception{
        if(this.connectionManager == null){
            throw new Exception("ConnectionManager가 설정되지 않았습니다 : connection.manager.class 설정을 확인하세요");
        }
        return connectionManager.getConnection(this.dataSourceName);
    }

    protected List selectList(String methodPath, Map param)throws Exception{
        SqlInfo sqlInfo = this.sqlInfoMap.get(methodPath);
        String sqlString = sqlInfo.sqlString;
        if(sqlInfo.scriptType == ScriptType.VELOCITY){
            sqlString = VelocityFacade.apply(sqlInfo.template,param);
        }
        Sql sql = new Sql(this.getConnection());
        List resultList = null;
        if(StringUtil.hasSqlParameter(sqlString)){
            resultList = sql.rows(param, sqlString);
        }else{
            resultList = sql.rows(sqlString);
        }
        if(sqlInfo.timestampFiels != null && sqlInfo.timestampFiels.size() > 0){
            DateUtil.long2Date(resultList, sqlInfo.timestampFiels);
        }
        return resultList;
    }

    protected Map selectOne(String methodPath, Map param)throws Exception{
        SqlInfo sqlInfo = this.sqlInfoMap.get(methodPath);
        String sqlString = sqlInfo.sqlString;
        if(sqlInfo.scriptType == ScriptType.VELOCITY){
            sqlString = VelocityFacade.apply(sqlInfo.template,param);
        }
        Sql sql = new Sql(this.getConnection());
        Map resultMap = null;
        if(StringUtil.hasSqlParameter(sqlString)){
            resultMap = sql.firstRow(param, sqlString);
        }else{
            resultMap = sql.firstRow(sqlString);
        }
        if(sqlInfo.timestampFiels != null && sqlInfo.timestampFiels.size() > 0){
            DateUtil.long2Date(resultMap, sqlInfo.timestampFiels);
        }
        return resultMap;
    }

    protected List insertList(String methodPath, List<Map> paramList)throws Exception{
        SqlInfo sqlInfo = this.sqlInfoMap.get(methodPath);
        String sqlString = sqlInfo.sqlString;
        Sql sql = new Sql(this.getConnection());
        List resultList = new ArrayList();
        for(Map m:paramList) {
            if (sqlInfo.scriptType == ScriptType.VELOCITY) {
                sqlString = VelocityFacade.apply(sqlInfo.template, m);
            }
            List<List<Object>> l = null;
            if(StringUtil.hasSqlParameter(sqlString)){
                l = sql.executeInsert(m, sqlString);
            }else{
                l = sql.executeInsert(sqlString);
            }
            if (l != null && l.size() > 0 && l.get(0) != null && l.get(0).size() > 0) {
                resultList.add(l.get(0).get(0));
            } else {
                resultList.add(null);
            }
        }
        return resultList;
    }

    protected Object insertOne(String methodPath, Map param)throws Exception{
        SqlInfo sqlInfo = this.sqlInfoMap.get(methodPath);
        String sqlString = sqlInfo.sqlString;
        Sql sql = new Sql(this.getConnection());
        if (sqlInfo.scriptType == ScriptType.VELOCITY) {
            sqlString = VelocityFacade.apply(sqlInfo.template, param);
        }
        List<List<Object>> l = null;
        if(StringUtil.hasSqlParameter(sqlString)){
            l = sql.executeInsert(param, sqlString);
        }else{
            l = sql.executeInsert(sqlString);
        }
        if (l != null && l.size() > 0 && l.get(0) != null && l.get(0).size() > 0) {
            return l.get(0).get(0);
        }
        return null;
    }

    protected List<Integer> updateList(String methodPath, List<Map> paramList)throws Exception{
        SqlInfo sqlInfo = this.sqlInfoMap.get(methodPath);
        String sqlString = sqlInfo.sqlString;
        Sql sql = new Sql(this.getConnection());
        List<Integer> resultList = new ArrayList<>();
        for(Map m:paramList) {
            if (sqlInfo.scriptType == ScriptType.VELOCITY) {
                sqlString = VelocityFacade.apply(sqlInfo.template, m);
            }
            int i = 0;
            if(StringUtil.hasSqlParameter(sqlString)){
                i = sql.executeUpdate(m, sqlString);
            }else{
                i = sql.executeUpdate(sqlString);
            }
            resultList.add(i);
        }
        return resultList;
    }

    protected int updateOne(String methodPath, Map param)throws Exception{
        SqlInfo sqlInfo = this.sqlInfoMap.get(methodPath);
        String sqlString = sqlInfo.sqlString;
        Sql sql = new Sql(this.getConnection());
        if (sqlInfo.scriptType == ScriptType.VELOCITY) {
            sqlString = VelocityFacade.apply(sqlInfo.template, param);
        }
        int i = 0;
        if(StringUtil.hasSqlParameter(sqlString)){
            i = sql.executeUpdate(param, sqlString);
        }else{
            i = sql.executeUpdate(sqlString);
        }
        return i;
    }

    protected List<Boolean> deleteList(String methodPath, List<Map> paramList)throws Exception{
        SqlInfo sqlInfo = this.sqlInfoMap.get(methodPath);
        String sqlString = sqlInfo.sqlString;
        Sql sql = new Sql(this.getConnection());
        List<Boolean> resultList = new ArrayList<>();
        for(Map m:paramList) {
            if (sqlInfo.scriptType == ScriptType.VELOCITY) {
                sqlString = VelocityFacade.apply(sqlInfo.template, m);
            }
            Object[] arr = {m};
            boolean b = false;
            if(StringUtil.hasSqlParameter(sqlString)){
                b = sql.execute(m, sqlString);
            }else{
                b = sql.execute(sqlString);
            }
            resultList.add(b);
        }
        return resultList;
    }

    protected boolean deleteOne(String methodPath, Map param)throws Exception{
        SqlInfo sqlInfo = this.sqlInfoMap.get(methodPath);
        String sqlString = sqlInfo.sqlString;
        Sql sql = new Sql(this.getConnection());
        if (sqlInfo.scriptType == ScriptType.VELOCITY) {
            sqlString = VelocityFacade.apply(sqlInfo.template, param);
        }
        boolean b = false;
        if(StringUtil.hasSqlParameter(sqlString)){
            b = sql.execute(param, sqlString);
        }else{
            b = sql.execute(sqlString);
        }
        return b;
    }

    public void putSqlInfo(String k, SqlInfo sqlInfo){
        this.sqlInfoMap.put(k, sqlInfo);
    }



}
