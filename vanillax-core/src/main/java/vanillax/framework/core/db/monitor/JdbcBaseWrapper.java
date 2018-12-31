package vanillax.framework.core.db.monitor;

import vanillax.framework.core.util.StringUtil;

public class JdbcBaseWrapper implements IJdbcWrapper{
    protected String id;

    public JdbcBaseWrapper(){
        id = StringUtil.makeUid();
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }
}
