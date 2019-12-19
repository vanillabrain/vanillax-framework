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

import groovy.json.JsonParserType;
import groovy.json.JsonSlurper;
import vanillax.framework.core.db.TransactionManager;
import vanillax.framework.core.db.monitor.ConnectionMonitor;
import vanillax.framework.core.util.ReflectionUtil;
import vanillax.framework.core.util.json.JsonOutput;
import vanillax.framework.core.util.StringUtil;
import vanillax.framework.core.util.DateUtil;
import vanillax.framework.webmvc.config.ConfigHelper;
import vanillax.framework.webmvc.exception.BaseException;
import vanillax.framework.webmvc.service.IFilter;
import vanillax.framework.webmvc.service.IService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.logging.Logger;

/**
 * 순수 JSon형태의 Request처리 servlet. jsonp를 주로 처리하기 위한 servlet이다.
 * GET, POST방식만 허용하며 GET 방식의 경우 __input 파라미터의 value값으로 json문자열이 입려되어야한다.
 * 입력 데이터 예시)
 * {
 *     _method:"find",
 *     param:{}
 * }
 */

@WebServlet(name="vanillaWebJson", urlPatterns={"/json/*"})
public class JsonServlet extends MvcServletBase {
    private static final Logger log = Logger.getLogger(JsonServlet.class.getName());
    private static final String INPUT_PARAM_KEY = "__input";

    public void init() throws ServletException {
        // Do required initialization
        super.init();
        log.info("JsonServlet initialized.....");
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        IService service = null;
        Map<String, Object> data = new HashMap<String,Object>();

        Object result = null;
        boolean onError = false;
        String callback = request.getParameter("callback");
        List<IFilter> filterList = null;
        try{
            String method = request.getMethod();
            service = this.searchService(request);
            this.setBaseData(data, request, response, service.getId());
            Map<String,Object> inputParam = null;

            if(method.equals("GET")) {
                inputParam = readContentParam(request);
            } else if(method.equals("POST")) {
                inputParam = (Map<String,Object>)readContent(request);//String을 GroovyJson Object로 변환한다.
            } else {
                String errMsg1 = localStrings.getString("http.method_not_implemented");
                Map<String, String> errorMap = new LinkedHashMap<String, String>();
                errorMap.put("_result", "ERROR");
                errorMap.put("message", errMsg1);
                result = errorMap;
                response.setStatus(500);
                onError = true;
            }
            data.put("_input", inputParam);

            //Filter초기화
            filterList = makeFilterList();
            //Filter 전처리
            data = filterPreprocess(filterList, data);

            String method1 = (String)inputParam.remove("_method");
            if(method1 ==null || "find".equals(method1)){
                // URI path에 마지막이 ID로 입력되는 경우 기본으로는 findOne을 호출하고 ID값이 입력되지 않은 경우 기본적으로 findMany를 호출한다.
                // 단, findOne, findMany 함수가 정의되어있을 경우에만 호출한다.
                // 기본호출은 find()함수이다.
                int findType = 0;//default
                if(StringUtil.isEmpty((String)data.get("_path"))){
                    if(ReflectionUtil.findMethod(service.getClass(),"findMany") != null){
                        findType = 1;//findMany
                    }
                }else{
                    if(ReflectionUtil.findMethod(service.getClass(),"findOne") != null){
                        findType = 2;//findOne
                    }
                }
                switch (findType){
                    case 1:
                        result = service.findMany(data);
                        break;
                    case 2:
                        result = service.findOne(data);
                        break;
                    default:
                        result = service.find(data);
                        break;
                }
            }else if("insert".equals(method1)){
                result = service.insert(data);
            }else if("update".equals(method1)){
                result = service.update(data);
            }else if("delete".equals(method1)){
                result = service.delete(data);
            }else{
                Map<String, String> errorMap = new LinkedHashMap<String, String>();
                errorMap.put("_result", "ERROR");
                errorMap.put("message", "wrong parameter");
                result = errorMap;
                response.setStatus(500);
                onError = true;
            }
            //Filter 후처리. 전처리 역순. 오류가 발생하지 않을경우만 처리.
            if(!onError){
                result = filterPostprocess(filterList, result);
            }
        }catch(Throwable e){
            String stackTrace = StringUtil.errorStackTraceToString(e);
            log.warning("Error occurred during process the service : " + stackTrace);//서비스 실행중 오류가 발생했습니다
            Map<String, String> errorMap = new LinkedHashMap<String, String>();
            errorMap.put("_result", "ERROR");
            errorMap.put("message", e.getMessage());
            if(e instanceof BaseException){
                BaseException be = (BaseException)e;
                errorMap.put("detail", be.getDetail());
                errorMap.put("code", be.getCode());
            }
            if(ConfigHelper.getBoolean("response.stackTrace", false)){
                errorMap.put("stackTrace", stackTrace);
            }
            result = errorMap;
            //responseData = StringUtil.unescapeJava(JsonOutput.toJson(errorMap),true);
            response.setStatus(424);//메소드 실패
            onError = true;
        }finally {
            TransactionManager.getInstance().clearTxSession();//Transaction 관련 데이터를 초기화한다.
            ConnectionMonitor.getInstance().onThreadFinished();//Thread종료시 close되지 않은 Connection이 있는지 검사한다.
            if(filterList != null)
                filterList.clear(); //필터 정리
        }
        if(callback != null) {//jsonp처리일 경우
            response.setContentType("text/plain; charset=UTF-8");
        }else{
            response.setContentType("application/json; charset=UTF-8");
        }

        if(result == null){
            result = new LinkedHashMap<String, String>();
        }
        //입력 관리 데이터 삭제 및 처리 결과 관리 데이터 입력
        if(result instanceof Map){
            Map<String,Object> map = ((Map<String,Object>) result);
            for(Object obj:map.keySet().toArray()){
                String key = obj.toString();
                if(!"_result".equals(key) &&  key.startsWith("_")){
                    map.remove(key);
                }
            }
            if(!onError)
                map.put("_result","OK");
            map.put("_curr", DateUtil.getCurrentTimeString());
        }

        if(callback != null) {//jsonp처리일 경우
            response.setStatus(200);//jsonp는 ajax에서 오류처리를 하지 않아 정상처리로 메시지를 전달해야한다.
        }
        Writer writer = response.getWriter();
        if(callback != null){//jsonp처리일 경우
            writer.write( callback + "(");
        }
        JsonOutput.toJson(writer, result);
        if(callback != null){//jsonp처리일 경우
            writer.write(");");
        }
        response.flushBuffer();
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    private Map<String,Object> readContentParam(HttpServletRequest request)throws Exception{
        String jsonStr = request.getParameter(INPUT_PARAM_KEY);
        JsonSlurper json = new JsonSlurper().setType(JsonParserType.LAX);
        if(jsonStr == null){
            return (Map<String,Object>)json.parseText("{}");
        }
        Map<String,Object> object = (Map<String,Object>)json.parse(jsonStr.toCharArray());
        return object;
    }

}

