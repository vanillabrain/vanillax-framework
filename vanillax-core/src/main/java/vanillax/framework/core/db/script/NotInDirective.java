package vanillax.framework.core.db.script;

/**
 * #not_in($collection $item COLUMN) $item.field #end
 *
 */
public class NotInDirective extends InDirective{
    private String clauseName = "not_in";
    private String clauseSqlName = "NOT IN";
    @Override
    public String getName() {
        return clauseName;
    }
    @Override
    protected String getClauseSqlName(){
        return clauseSqlName;
    }
}

