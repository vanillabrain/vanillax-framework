package vanillax.framework.core.db.script;

/**
 * #not_in($collection $item COLUMN) $item.field #end
 *
 */
@Deprecated
public class OldNotInDirective extends InDirective{
    private String clauseName = "notin";
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

