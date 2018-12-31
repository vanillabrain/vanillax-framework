package vanillax.framework.core.db.orm;

import org.apache.velocity.Template;

import java.util.HashSet;
import java.util.Set;

public class SqlInfo{
    Template template = null;
    String sqlString = null;
    ScriptType scriptType = ScriptType.NORMAL;
    Set<String> timestampFiels = null;

    public SqlInfo(Template template, String sqlString, ScriptType scriptType) {
        this.template = template;
        this.sqlString = sqlString;
        this.scriptType = scriptType;
    }
    public SqlInfo(Template template, String sqlString, ScriptType scriptType, String timestampFieldStr) {
        this.template = template;
        this.sqlString = sqlString;
        this.scriptType = scriptType;
        if(timestampFieldStr != null){
            this.timestampFiels = new HashSet<>();
            String[] arr = timestampFieldStr.split(",");
            for(String s:arr){
                String s1 = s.trim();
                if("".equals(s1)){
                    continue;
                }
                this.timestampFiels.add(s1);
            }
        }
    }

    public Template getTemplate() {
        return template;
    }

    public String getSqlString() {
        return sqlString;
    }

    public ScriptType getScriptType() {
        return scriptType;
    }

    public Set<String> getTimestampFiels() {
        return timestampFiels;
    }
}