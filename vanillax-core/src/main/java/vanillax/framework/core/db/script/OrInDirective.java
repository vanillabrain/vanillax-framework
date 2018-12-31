package vanillax.framework.core.db.script;


/**
 * #or_in($collection $item COLUMN) $item.field #end
 *
 */
public class OrInDirective extends InDirective {
    private String clauseName = "or_in";
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