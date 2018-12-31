package vanillax.framework.batch.db;

import vanillax.framework.webmvc.db.ConnectionManagerBase;

/**
 * DB Connection을 가져오는 Singleton객체
 */
public class DbConnectionManager extends ConnectionManagerBase {

    @Override
    protected void makeDatasource() throws Exception{
        super.makeDatasource();
    }

}
