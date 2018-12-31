package vanillax.framework.core.db.script;


import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.directive.StopCommand;
import org.apache.velocity.runtime.parser.ParserTreeConstants;
import org.apache.velocity.runtime.parser.node.ASTReference;
import org.apache.velocity.runtime.parser.node.ASTStringLiteral;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.util.introspection.Info;

/**
 * #in($collection $item COLUMN) $item.field #end
 *
 */
public class InDirective extends RepeatDirective {

    /**
     * Immutable fields
     */
    private String var;

    private String open = "(";

    private String close = ")";

    private String separator = ", ";

    private String column = "";

    private String clauseName = "in";
    private String clauseSqlName = "IN";
    private String conjunction = "AND";

    @Override
    public String getName() {
        return clauseName;
    }

    protected String getClauseSqlName(){
        return clauseSqlName;
    }

    protected String getConjunction(){
        return conjunction;
    }

    @Override
    public void init(RuntimeServices rs, InternalContextAdapter context, Node node) throws TemplateInitException {
        super.init(rs, context, node);
        final int n = node.jjtGetNumChildren() - 1;
        for (int i = 1; i < n; i++) {
            Node child = node.jjtGetChild(i);
            if (i == 1) {
                if (child.getType() == ParserTreeConstants.JJTREFERENCE) {
                    this.var = ((ASTReference) child).getRootString();
                }
                else {
                    throw new TemplateInitException("Syntax error", getTemplateName(), getLine(), getColumn());
                }
            }
            else if (child.getType() == ParserTreeConstants.JJTSTRINGLITERAL) {
                String value = (String) ((ASTStringLiteral) child).value(context);
                switch (i) {
                    case 2:
                        this.column = value;
                        break;
                    default:
                        break;
                }
            }
            else {
                throw new TemplateInitException("Syntax error", getTemplateName(), getLine(), getColumn());
            }
        }
        this.uberInfo = new Info(this.getTemplateName(), getLine(), getColumn());
    }

    @Override
    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException,
            ParseErrorException, MethodInvocationException {
        Object listObject = node.jjtGetChild(0).value(context);
        if (listObject == null) {
            return false;
        }

        Iterator<?> iterator = null;

        try {
            iterator = this.rsvc.getUberspect().getIterator(listObject, this.uberInfo);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception ee) {
            String msg = "Error getting iterator for #in at " + this.uberInfo;
            this.rsvc.getLog().error(msg, ee);
            throw new VelocityException(msg, ee);
        }

        if (iterator == null) {
            throw new VelocityException("Invalid collection");
        }

        int counter = 0;
        Object o = context.get(this.var);

        RepeatScope foreach = new RepeatScope(this, context.get(getName()), this.var);
        context.put(getName(), foreach);

        NullHolderContext nullHolderContext = null;
        Object value = null;
        StringWriter buffer = new StringWriter();

        while (iterator.hasNext()) {

            if (counter == 0) {
                buffer.append(this.column);
                buffer.append(" ").append(getClauseSqlName()).append(" ");
                buffer.append(this.open); // In starts
            }

            value = iterator.next();
            put(context, this.var, value);
            foreach.index++;
            foreach.hasNext = iterator.hasNext();

            try {
                if (value == null) {
                    if (nullHolderContext == null) {
                        nullHolderContext = new NullHolderContext(this.var, context);
                    }
                    node.jjtGetChild(node.jjtGetNumChildren() - 1).render(nullHolderContext, buffer);
                } else {
                    node.jjtGetChild(node.jjtGetNumChildren() - 1).render(context, buffer);
                }
            } catch (StopCommand stop) {
                if (stop.isFor(this)) {
                    break;
                }
                clean(context, o);
                // close does not perform any action and this is here
                // to avoid eclipse reporting possible leak.
                buffer.close();
                throw stop;
            }
            counter++;

            if (iterator.hasNext()) {
                buffer.append(this.separator);
            }else{
                buffer.append(this.close);
            }

        }
        String content = buffer.toString().trim();
        if (!"".equals(content)) {
            writer.append(getConjunction()).append(" ");
            writer.append(content);
            writer.append("");
        }
        clean(context, o);
        return true;
    }

    @Override
    public int getType() {
        return BLOCK;
    }

}