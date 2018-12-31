package vanillax.framework.core.db.script;


/**
 * #or_not_in($collection $item COLUMN) $item.field #end
 *
 */
public class OrNotInDirective extends NotInDirective {
    private String clauseName = "or_not_in";
    private String conjunction = "OR";

    @Override
    public String getName() {
        return clauseName;
    }
    @Override
    protected String getConjunction(){
        return conjunction;
    }

}