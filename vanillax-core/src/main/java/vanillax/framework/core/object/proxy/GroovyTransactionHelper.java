package vanillax.framework.core.object.proxy;

import groovy.lang.Closure;
import vanillax.framework.core.db.TransactionManager;

/**
 * Created by gaedong on 2016. 8. 17..
 */
public class GroovyTransactionHelper {

    private boolean txAutoCommit = true;

    public GroovyTransactionHelper(boolean txAutoCommit){
        this.txAutoCommit = txAutoCommit;
    }

    public void tx(Closure closure) throws Exception {
        try {
            TransactionManager.getInstance().startTxSession(txAutoCommit);
            closure.call();
            TransactionManager.getInstance().finishTxSession();
        } catch (Throwable e) {
            TransactionManager.getInstance().finishTxSessionOnError();
            if(e instanceof Exception)
                throw (Exception)e;
            else{
                throw new Exception(e);
            }
        } finally {

        }

    }

}
