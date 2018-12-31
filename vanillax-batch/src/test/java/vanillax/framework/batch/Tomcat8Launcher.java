package vanillax.framework.batch;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import vanillax.framework.webmvc.servlet.ConfigInitBaseServlet;
import vanillax.framework.webmvc.servlet.JsonServlet;
import vanillax.framework.webmvc.servlet.RestServlet;

import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServlet;
import java.io.File;

/**
 * Created by gaedong on 1/31/16.
 * VM옵션에 추가할 내용
 * -Djava.util.logging.SimpleFormatter.format="%1$tF %1$tT %4$s %2$s %5$s%6$s%n"
 */
public class Tomcat8Launcher {
    private Tomcat tomcat = null;
    private int port = 8080;
    private String contextPath = null;
    private String docBase = null;
    private Context rootContext = null;

    public Tomcat8Launcher(){
        init();
    }

    public Tomcat8Launcher(int port, String contextPath, String docBase){
        this.port = port;
        this.contextPath = contextPath;
        this.docBase = docBase;
        init();
    }

    private void init(){
        tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.enableNaming();
        if(contextPath == null){
            contextPath = "";
        }
        if(docBase == null){
            File base = new File(System.getProperty("java.io.tmpdir"));
            docBase = base.getAbsolutePath();
        }
        rootContext = tomcat.addContext(contextPath, docBase);
    }

    public void addServlet(String servletName, String uri, HttpServlet servlet){
        Tomcat.addServlet(this.rootContext, servletName, servlet);
        rootContext.addServletMapping(uri, servletName);
    }

    public void addListenerServlet(ServletContextListener listener){
        rootContext.addApplicationListener(listener.getClass().getName());
    }

    public void startServer() throws LifecycleException {
        tomcat.start();
        tomcat.getServer().await();
    }

    public void stopServer() throws LifecycleException {
        tomcat.stop();
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
        System.setProperty(javax.naming.Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
        System.setProperty(javax.naming.Context.URL_PKG_PREFIXES, "org.apache.naming");

        Tomcat8Launcher tomcatServer = new Tomcat8Launcher();
        tomcatServer.addListenerServlet(new ConfigInitBaseServlet());
        tomcatServer.addServlet("restServlet", "/rest/*", new RestServlet());
        tomcatServer.addServlet("jsonServlet", "/json/*", new JsonServlet());
        tomcatServer.startServer();
    }
}