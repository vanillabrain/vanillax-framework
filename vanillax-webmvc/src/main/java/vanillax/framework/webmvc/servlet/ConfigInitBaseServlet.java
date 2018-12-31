/*
 * Copyright (C) 2016 Vanilla Brain, Team - All Rights Reserved
 *
 * This file is part of 'VanillaTopic'
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Vanilla Brain Team and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Vanilla Brain Incorporated
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Vanilla Brain Team.
 */

package vanillax.framework.webmvc.servlet;

import vanillax.framework.core.util.StringUtil;
import vanillax.framework.webmvc.config.ConfigHelper;
import vanillax.framework.core.config.FrameworkConfig;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Config 정보 로딩하는 ListenerServlet클래스
 */
@WebListener
public class ConfigInitBaseServlet implements ServletContextListener {
//    private static final Logger log = LogManager.getLogger(ConfigInitBaseServlet.class.getName());
    private static final Logger log = Logger.getLogger(ConfigInitBaseServlet.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        Properties prop = new Properties();
        InputStream input = null;

        try {
            //초기 설정파일 vanilla-init.properties 로딩
            String initFileName = "vanilla-init.properties";
            input = Thread.currentThread().getContextClassLoader().getResourceAsStream(initFileName);
            if(input==null){
                log.info("설정파일 'vanilla-init.properties'를 찾을 수 없습니다");
            }else{
                prop.load(input);
            }

            //기본 사용자 설정 파일 로딩
            String filename = "vanilla.properties";
            String tmp = servletContextEvent.getServletContext().getInitParameter("vanilla.properties");
            if(tmp != null)
                filename = tmp.trim();

            log.info("vanilla properties file name : "+filename);

            input = ConfigInitBaseServlet.class.getClassLoader().getResourceAsStream(filename);
            if(input==null){
                log.warning("설정파일 'vanilla.properties'를 찾을 수 없습니다");
                return;
            }

            //load a properties file from class path, inside static method
            prop.load(input);

            //WEB-INF경로 입력
            String webInfPath = null;
            try {
                webInfPath = servletContextEvent.getServletContext().getResource("WEB-INF").getFile();
            }catch(Exception e){
                //e.printStackTrace();
            }
            if(webInfPath != null){
                if(webInfPath.endsWith("/")){
                    //nothing
                } else {
                    webInfPath = webInfPath + "/";
                }
                prop.setProperty("web.inf.path", webInfPath);
            }

            ConfigHelper.setProp(prop);
            prop.setProperty("script.base.path", ConfigHelper.getBasePath());
            FrameworkConfig.setProp(prop);//GroovyObjectLoader에서 사용하는 설정파일 초기화.

            //Startup 클래스 실행
            String startupClassName = ConfigHelper.get("startup.class");
            if(startupClassName != null && !startupClassName.trim().equals("")){
                try{
                    Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(startupClassName.trim());
                    IStartup startup = (IStartup)clazz.newInstance();
                    startup.start();
                }catch (Exception e){
//                    e.printStackTrace();
                    log.warning( StringUtil.errorStackTraceToString(e));
                }
            }

            //get the property value and print it out
            log.info("script.base.path : "+prop.getProperty("script.base.path"));
            log.info("script.reload : " + prop.getProperty("script.reload"));
        } catch (IOException ex) {
            log.warning("서비스 초기화중 오류가 발생했습니다 : " + StringUtil.errorStackTraceToString(ex));
        } finally{
            if(input!=null){
                try {input.close();} catch (IOException e) {}
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        //do nothing.
    }
}
