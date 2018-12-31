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

package vanillax.framework.webmvc.service;

import groovy.lang.GroovyObject;
import vanillax.framework.core.object.ObjectCache;
import vanillax.framework.webmvc.config.ConfigHelper;
import vanillax.framework.core.object.ObjectLoader;
import vanillax.framework.core.util.StringUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * service groovy 파일을 읽어서 Java Instance로 만들어 Cache에 저장한다.
 * 변경여부를 확인하여 변경되었을 경우 다시로딩한다.
 */
public class ServiceLoader {
    private static List<String> basePackageList = null;
    private static Set<String> readByClassLoaderSet = null;

    /**
     * Service 객체를 로딩한다.
     * @param path 예) "/top/news"
     * @return
     * @throws Exception
     */
    public static IService load(String path)throws Exception{
        initBasePackageList();
        List<String> list = StringUtil.extractPathArr(path);
        // 서비스 객체가 cache에 저장되어있는지 먼저 확인한다.
        IService service = loadServiceFromCache(list);
        if(service != null)
            return service;

        int endIndex = list.size()-1;
        boolean hasLastAsNumber = isLastPathNumber(list.get(endIndex));
        //만일 path의 마지막이 숫자로 구성됐을 경우. 로딩순서를 마지막으로 변경해본다.
        //path 마자막 값은 통상 ID 값을 의미하기 때문에 실제 서비스 파일이 없을 경우가 많기 때문이다.
        // eg) /my/path/1234
        if(hasLastAsNumber){
            endIndex = list.size()-2;
        }

        for(int i=endIndex; i >= 0 ;i--){
            String x = list.get(i);
            GroovyObject object = loadService(x);
            if(object == null || !(object instanceof IService))
                continue;
            service = (IService)object;
            service.setId(x);
            return service;
        }

        //앞서 검사한 path들에 해당하는 서비스가 모두 존재하지 않을 경우 숫자라고 하더라도 첫 path를 로딩시도해본다.
        if(hasLastAsNumber){
            String x = list.get(list.size()-1);
            GroovyObject object = loadService(x);
            if(object != null && object instanceof IService){
                service = (IService)object;
                service.setId(x);
                return service;
            }
        }

        return null;
    }

    /**
     * ObjectCache에 저장되어있는지 확인하고 저장되어있으면 서비스 객체를 로딩한다.
     * @param pathList URI path를 리스트로 바꾼 정보
     * @return
     * @throws Exception
     */
    private static IService loadServiceFromCache(List<String> pathList) throws Exception{
        GroovyObject object = null;
        String basePackage = null;
        ObjectCache cache = ObjectCache.getInstance();
        for(int i = pathList.size() -1 ; i >=0 ; i--){
            String path = pathList.get(i);
            for(String s:basePackageList){
                if(s.length() > 0 && !s.endsWith(".")){
                    basePackage = s + ".";
                }else{
                    basePackage = "";
                }
                String className = basePackage + path.replaceAll("/", ".");
                if(cache.contains(className)){
                    object = ObjectLoader.load(className);
                }
                if(object != null){
                    IService service = (IService)object;
                    service.setId(path);
                    return service;
                }
            }
        }
        return null;
    }

    private static GroovyObject loadService(String path)throws Exception{
        GroovyObject object = null;
        String basePackage = null;
        for(String s:basePackageList){
            if(s.length() > 0 && !s.endsWith(".")){
                basePackage = s + ".";
            }else{
                basePackage = "";
            }
            String className = basePackage + path.replaceAll("/", ".");
            try {
                object = ObjectLoader.load(className);
            }catch (Exception e){
                if(e instanceof ClassNotFoundException && readByClassLoaderMatch(className)){
                    return null;
                }else {
                    throw e;
                }
            }
            if(object != null)
                return object;
        }
        return null;
    }

    /**
     * service base package 초기화.
     * ClassLoader에 의해 로딩되는 인스턴스는 나중에 로딩할 수 있게 페키지 확인 순위로 뒤로 둔다.
     */
    private static void initBasePackageList(){
        if(basePackageList == null){
            basePackageList = new ArrayList<>(4);
            List<String> readByClassLoaderPackage = new ArrayList<>(4);
            Set<String> set = new HashSet<>(4);
            String serviceBasePackageInit = ConfigHelper.get("service.base.package.init");
            if(serviceBasePackageInit != null && !"".equals(serviceBasePackageInit)){
                String[] arr = serviceBasePackageInit.split(",");
                for(String s:arr){
                    String s1 = s.trim();
                    if(readByClassLoaderMatch(s1)){
                        readByClassLoaderPackage.add(s1);
                    }else if(!set.contains(s1)){
                        basePackageList.add(s1);
                        set.add(s1);
                    }
                }
            }
            String serviceBasePackage = ConfigHelper.get("service.base.package");
            if(serviceBasePackage != null && !"".equals(serviceBasePackage)){
                String[] arr = serviceBasePackage.split(",");
                for(String s:arr){
                    String s1 = s.trim();
                    if(readByClassLoaderMatch(s1)){
                        readByClassLoaderPackage.add(s1);
                    }else if(!set.contains(s1)){
                        basePackageList.add(s1);
                        set.add(s1);
                    }
                }
            }
            //ClassLoader에 의해 로딩되는 인스턴스는 후순위로 미뤄둔다.
            for(String s:readByClassLoaderPackage){
                if(!set.contains(s)){
                    basePackageList.add(s);
                    set.add(s);
                }
            }
            if(basePackageList.size() == 0 && !set.contains("")){
                basePackageList.add("");
            }
            set.clear();
        }
    }

    /**
     * 클래스 로더에 의해 로딩되는 클래스 패키지인지 확인한다
     * @param packageName 검사할 스크립트의 패키지명 혹은 클래스명
     * @return
     */
    private static boolean readByClassLoaderMatch(String packageName){

        if(readByClassLoaderSet == null){
            readByClassLoaderSet = new HashSet<>();
            String str = ConfigHelper.get("read.by.classloader");
            if(str != null){
                String[] arr = str.split(",");
                for(String s:arr){
                    String s1 = s.trim();
                    if("".equals(s1))
                        continue;
                    readByClassLoaderSet.add(s1);
                }
            }
        }
        if(readByClassLoaderSet.size() > 0){
            for(String pattern: readByClassLoaderSet){
                if(StringUtil.match(packageName, pattern)){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 문자열이 숫자인지 확인한다. 16진수일 경우라도 숫자로 간주한다.
     * @param path
     * @return 10진수, 16진수 숫자일 경우 true 반환
     */
    private static boolean isLastPathNumber(String path){
        if(path == null || path.length() == 0)
            return false;
        boolean hex = false;
        for(int i=path.length()-1; i >=0; i-- ){
            char c = path.charAt(i);
            if(c == '/')
                return true;
            if(c >= '0' && c <= '9'){
                //OK
            }else if(c >= 'A' && c <= 'F'){
                hex = true;
            }else if(c >= 'a' && c <= 'f'){
                hex = true;
            }else{
                return false;
            }
        }
        if(hex && path.length() % 2 != 0){
            //16진수값인데 전체길이 짝수가 아니면 숫자가 아니라고 판단한다.
            return false;
        }
        return true;
    }
}
