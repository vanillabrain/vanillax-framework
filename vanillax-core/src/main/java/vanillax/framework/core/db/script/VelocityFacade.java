package vanillax.framework.core.db.script;


import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.parser.node.SimpleNode;
import org.apache.velocity.util.StringUtils;

public class VelocityFacade {

    private static final String ADDITIONAL_CTX_ATTRIBUTES_KEY = "additional.context.attributes";
    private static final String EXTERNAL_PROPERTIES = "vanilla-velocity.properties";
    private static final String DIRECTIVES = InDirective.class.getName() + ","
            + RepeatDirective.class.getName() + ","
            + NotInDirective.class.getName() + ","
            + OrInDirective.class.getName() + ","
            + OrNotInDirective.class.getName() + ","
            + OldNotInDirective.class.getName();

    private static final RuntimeInstance engine;

    /** Contains thread safe objects to be set in the velocity context.*/
    private static final Map<String, Object> additionalCtxAttributes;
    private static final Properties settings;

    static {
        settings = loadProperties();
        additionalCtxAttributes = Collections.unmodifiableMap(loadAdditionalCtxAttributes());
        engine = new RuntimeInstance();
        engine.init(settings);
    }

    public static Template compile(String script, String name) {
        try {
            if(script != null){
                script = script.replaceAll("#end\n","#end\n\n"); //Velocity가 #end직후의 개행문자를 먹어버려서 개행문자를 의도적으로 하나 더 추가한다.
            }
            StringReader reader = new StringReader(script);
            SimpleNode node = engine.parse(reader, name);
            Template template = new Template();
            template.setRuntimeServices(engine);
            template.setData(node);
            template.initDocument();
            return template;
        } catch (Exception ex) {
            ex.printStackTrace();
            String errorLocation = "";
            if(ex instanceof TemplateInitException){
                TemplateInitException t = (TemplateInitException)ex;
                int colNum = t.getColumnNumber();
                int lineNum = t.getLineNumber();
                errorLocation = " at line : "+lineNum+", column : "+colNum;
            }
            throw new RuntimeException("Error parsing velocity script '" + name + "'"+errorLocation, ex);
        }
    }

    public static String apply(Template template, Map<String, Object> context) {
        final StringWriter out = new StringWriter();
        context.putAll(additionalCtxAttributes);
        template.merge(new VelocityContext(context), out);
        return out.toString();
    }

    private static Properties loadProperties() {
        final Properties props = new Properties();
        // Defaults
        props.setProperty("resource.loader", "class");
        props.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

        try {
            // External properties
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            props.load(cl.getResourceAsStream(EXTERNAL_PROPERTIES));
        } catch (Exception ex) {
            // No custom properties
        }

        //로그 관련 설정이 없으면 로그를 기록하지 않는다.
        if(!props.containsKey("runtime.log") && !props.containsKey("runtime.log.logsystem.class")){
            props.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogChute");
        }

        // Append the user defined directives if provided
        String userDirective = StringUtils.nullTrim(props.getProperty("userdirective"));
        if(userDirective == null) {
            userDirective = DIRECTIVES;
        } else {
            userDirective += "," + DIRECTIVES;
        }
        props.setProperty("userdirective", userDirective);
        return props;
    }

    private static Map<String, Object> loadAdditionalCtxAttributes() {
        Map<String, Object> attributes = new HashMap<String, Object>();
        String additionalContextAttributes = settings.getProperty(ADDITIONAL_CTX_ATTRIBUTES_KEY);
        if (additionalContextAttributes == null) {
            return attributes;
        }

        try {
            String[] entries = additionalContextAttributes.split(",");
            for (String str : entries) {
                String[] entry = str.trim().split(":");
                attributes.put(entry[0].trim(), Class.forName(entry[1].trim()).newInstance());
            }
        } catch (Exception ex) {
            throw new RuntimeException("Error parsing velocity property '" + ADDITIONAL_CTX_ATTRIBUTES_KEY + "'", ex);
        }
        return attributes;
    }
}