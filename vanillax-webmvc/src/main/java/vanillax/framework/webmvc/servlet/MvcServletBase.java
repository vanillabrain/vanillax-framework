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
import vanillax.framework.core.object.ObjectLoader;
import vanillax.framework.core.util.CollectionUtil;
import vanillax.framework.webmvc.config.ConfigHelper;
import vanillax.framework.webmvc.service.IFilter;
import vanillax.framework.webmvc.service.IService;
import vanillax.framework.webmvc.service.ServiceLoader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Restful Request처리 servlet
 */
public class MvcServletBase extends HttpServlet {
    private static final Logger log = Logger.getLogger(MvcServletBase.class.getName());
    protected static ResourceBundle localStrings = ResourceBundle.getBundle("javax.servlet.http.LocalStrings");
    private List<IFilter> filters = null;
    private long filterInitializedTime = 0;

    public void init() throws ServletException {
        // Do required initialization
        filterInitializedTime = 0;
        if(filters == null){
            filters = new ArrayList<IFilter>(8);
        }
        log.info("RestServlet initialized.....");
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        throw new ServletException("Not implemented Error");
    }

    @Override
    public void destroy() {
        //do nothing
        if(filters != null){
            filters.clear();
            filters = null;
        }
    }

    /**
     * JSON POST 데이터를 읽어서 List, Map 객체로 변환하여 반환한다.
     * @param request
     * @return 데이터가 존재하지 않을경우 빈 Map객체가 반환된다.
     * @throws Exception
     */
    protected Object readContent(HttpServletRequest request)throws Exception{
        JsonSlurper json = new JsonSlurper().setType(JsonParserType.LAX);
        if(request.getContentLength()<=0){
            return json.parseText("{}");
        }
        Object object = json.parse(request.getInputStream(),"UTF-8");
        if(object instanceof Map){
            object = CollectionUtil.map2map((Map)object);
        }else if(object instanceof List){
            object = CollectionUtil.list2list((List)object);
        }
        return object;
    }

    /**
     * 경로에 해당하는 서비스를 검색한다.
     * eg) /serveltContextName/my/company/employee/1234 --) my.company.employee
     * @param request
     * @return
     * @throws Exception
     */
    protected IService searchService(HttpServletRequest request)throws Exception{
        String path = request.getPathInfo();//"/servlet/toplist/213" --> "/toplist/213"
        IService serviceTmp = ServiceLoader.load(path);
        if(serviceTmp == null){
            String msg = "경로에 해당하는 서비스를 찾을 수 없습니다 : "+path;
            log.warning(msg);
            throw new Exception(msg);
        }
        return serviceTmp;
    }

    /**
     * Request의 메타 정보를 찾아서 생성해준다. <br>
     * _request : HttpServletRequest <br>
     * _response : HttpServletResponse <br>
     * _service : IService.id 값. eg) my.company.employee <br>
     * _path : request.getPathInfo()에서 서비스 id를 제외한 뒷부분. 통상 ID값으로 간주하는 숫자가 온다. <br>
     *   eg) /serveltContextName/my/company/employee/1234 --) 1234 <br>
     *       /serveltContextName/my/company/employee --) "" <br>
     * @param data 서비스를 처리하는데 사용하는 입력 데이터. 메타 데이터를이 입력된다.
     * @param request
     * @param response
     * @param id IService를 찾는데 사용했던 경로. eg) my/company/employee
     * @throws Exception
     */
    protected void setBaseData(Map<String,Object> data, HttpServletRequest request, HttpServletResponse response, String id)throws Exception{
        data.put("_request",request);
        data.put("_response",response);
        data.put("_service",id.replaceAll("/","."));
        String path = request.getPathInfo();//e.g) "/serveltContextName/my/company/employee/1234" --> "/my/company/employee/1234"
        String tmpPath = path.substring(path.indexOf(id) + id.length(), path.length());// eg) "/my/company/employee/1234" --> "/1234"
        if(tmpPath.startsWith("/")){
            tmpPath = tmpPath.substring(1,tmpPath.length());//e.g) "/1234" --> "1234"
        }
        data.put("_path",tmpPath);
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> parameterMapTmp = new HashMap<String,Object>();
        Iterator<String> it = parameterMap.keySet().iterator();
        while(it.hasNext()){
            String k = it.next();
            String[] arr = parameterMap.get(k);
            parameterMapTmp.put(k,parameterMap.get(k)[0]);
            if(arr.length > 1)
                parameterMapTmp.put(k+"_arr",parameterMap.get(k));
        }
        data.put("_param",parameterMapTmp);
    }

    /**
     * Filter를 초기화한다. vanilla.properties내에 script.filters 값으로 정의되어진 클래스를 로딩한다.
     * @throws Exception Filter로 정의된 클래스들이 IFilter객체가 아닐 경우.
     */
    protected void initFilters() throws Exception{
        String s = ConfigHelper.get("script.filters");
        if(s == null)
            return;
        //filter class를 로딩한지 30초가 지나지 않은경우는 그냥 패스.
        if(System.currentTimeMillis() - filterInitializedTime < 30*1000)
            return;
        s.replaceAll(";",",");
        s.replaceAll(" ","");
        String[] arr = s.split(",");
        filters.clear();
        for(String x:arr){
            if("".equals(x.trim()))
                continue;
            Object obj = ObjectLoader.load(x.trim());
            if(obj == null)
                continue;
            if(obj instanceof IFilter){
                filters.add((IFilter)obj);
            }else{
                throw new Exception("Filter는 "+IFilter.class.getCanonicalName()+" 객체만 허용됩니다.");
            }
        }
        filterInitializedTime = System.currentTimeMillis();
    }

    /**
     * 필터 전처리
     * @param data 입력 데이터
     * @return 입력데이터에 전처리후 결과 추가하여 반환한다.
     * @throws Exception
     */
    protected Map<String,Object> filterPreprocess(Map<String,Object> data)throws Exception{
        //Filter 전처리
        for(IFilter filter:filters){
            data = filter.preprocess(data);
        }
        return data;
    }

    /**
     * 필터 후처리
     * @param data 본처리의 결과 데이터
     * @return
     * @throws Exception
     */
    protected Object filterPostprocess(Object data)throws Exception{
        for(int i= 0; i < filters.size(); i++){
            IFilter filter= filters.get(filters.size() - 1 - i);
            data = filter.postprocess(data);
        }
        return data;
    }
}

