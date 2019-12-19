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

package vanillax.framework.core.object;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import org.apache.velocity.Template;
import org.codehaus.groovy.control.CompilerConfiguration;

import vanillax.framework.core.Constants;
import vanillax.framework.core.config.FrameworkConfig;
import vanillax.framework.core.db.orm.*;
import vanillax.framework.core.db.script.TimestampFields;
import vanillax.framework.core.util.ReflectionUtil;
import vanillax.framework.core.util.StringUtil;
import vanillax.framework.core.db.TransactionHelper;
import vanillax.framework.core.db.script.Velocity;
import vanillax.framework.core.db.script.VelocityFacade;
import vanillax.framework.core.object.proxy.GroovyProxyUtil;
import vanillax.framework.core.util.StreamUtil;
import vanillax.framework.core.object.proxy.GroovySqlProxyUtil;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.logging.Logger;

/**
 * service groovy 파일을 읽어서 Java Instance로 만들어 Cache에 저장한다.
 * 변경여부를 확인하여 변경되었을 경우 다시로딩한다.
 */
public class ObjectLoader {
    private static final Logger log = Logger.getLogger(ObjectLoader.class.getName());

    /**
     * 존재하지 않는 파일을 계속 로딩하려할 때 5초간 파일 존재유무를 유예하게 처리하기위한 상수.
     */
    private static final long DUP_CHECK_TIME = 5000;
    /** path에 해당하는 파일유무 확인 map. 해당 path로 지속적인 Request가 발생하더라도 File IO를 최소화하기 위해 사용. */
    private static Map<String,Long> checkedFileMap = null;
    /** 로딩중인 클래스. 재귀적인 클래스로딩을 방지하기 위함이다. */
    private static Map<String,Long> onLoadingClasses = null;
    /** Groovy Script를 파싱하여 클래스를 생성한다. proxy source 파싱할 때만 사용한다. */
    private static GroovyClassLoader groovyClassLoader = null;
    /**
     * script내의 참조하는 클래스를 로딩할 때 사용한다. java 클래스의 경우 부모 ClassLoader에게 위임하고
     *  script클래스인 경우 다시 파싱하여 클래스를 생성한다.
     */
    private static ScriptClassLoader scriptClassLoader = null;
    private static CompilerConfiguration config = null;
    /** JavaClass 형태로 로딩되는 클래스 인지 판단할 수 있는 패턴 */
    private static List<String> readByClassLoaderPatterns = null;
    private static Set<String> readByClassLoaderSet = null;

    static{
        checkedFileMap = new Hashtable<String,Long>();
        onLoadingClasses = new Hashtable<String,Long>();
        config = new CompilerConfiguration();
        config.setSourceEncoding("UTF-8");
        scriptClassLoader = new ScriptClassLoader();
        groovyClassLoader = new GroovyClassLoader(scriptClassLoader, config);
        readByClassLoaderPatterns = new ArrayList<>(8);
        readByClassLoaderSet = new HashSet<>();
    }

    /**
     * GroovyObject 객체를 로딩한다.
     * 변경유무를 확인하여 변경되었을 경우 다시 로딩한다.
     * @param className 예) "com.hello.GagaObject"
     * @return Autowired 필드까지 모두 적용된 Groovy 객체를 반환한다.
     * @throws Exception 클래스를 찾을 수 없거나, 상호참조할 경우 오류를 생성
     */
    public static GroovyObject load(String className)throws Exception{
        if(className == null)
            return null;

        GroovyObject groovyObject = null;
        ObjectCache cache = ObjectCache.getInstance();
        if(cache.contains(className)){
            ObjectInfo objectInfo = cache.get(className);
            if(!FrameworkConfig.isReload()){//재로딩 아니라면 Cache에 존재하는 인스턴스를 넘겨준다.
                return getGroovyObjectWithAutowired(objectInfo);
            }
            // 참조관계상의 모든 클래스들(script)의 변경유무를 확인한다.
            // 변경된 모든 script의 ObjectInfo에 변경여부 값을 변경으로 수정해준다.
            isReferencedClassModified(objectInfo);

            //재로딩 세팅이 되어있고 실제 파일이 변경되었거나 클래스만 로딩된 상태이면 다시 로딩한다.
            if(FrameworkConfig.isReload() && (!objectInfo.hasObject() || objectInfo.isModified(true))){
                if(objectInfo.getFile() != null && !objectInfo.getFile().exists()){
                    //해당파일이 삭제되었을 경우 넘겨줄 클래스가 없다.
                    return null;
                }
                objectInfo = loadGroovyObject(className, objectInfo.getFile());
            }
            return getGroovyObjectWithAutowired(objectInfo);//objectInfo.getObject();
        }

        //실제경로에 해당 파일이 있는지 확인한다.
        long curr = System.currentTimeMillis();
        if(checkedFileMap.containsKey(className)){
            //존재하지 않는 파일을 5초내에 또 조회하려할 경우 null을 반환한다.
            if(curr - checkedFileMap.get(className) < DUP_CHECK_TIME){
                checkedFileMap.put(className, curr);
                return null;
            }
        }

        File file = null;
        // 클래스 로더 대상인 경우 스크립트 파일이 존재하지않는다. 그외 경우만 파일을 확인한다.
        if(!readByClassLoaderMatch(className)){
            file = new File(FrameworkConfig.getBasePath() + "/" + className.replaceAll("\\.","/") + ".groovy");
        }

        if(readByClassLoaderMatch(className) || file.exists()){
            ObjectInfo objectInfo = loadGroovyObject(className, file);
            if(objectInfo == null){
                return null;
            }
            groovyObject = getGroovyObjectWithAutowired(objectInfo);//objectInfo.getObject();
        }else{
            checkedFileMap.put(className, curr);
        }

        //존재하지않는 파일 체크하지않게 확인하는 시간 테이블을 정리하는 로직
        //5만개가 넘어가면 정해진 시간값이 지난 데이터는 모두 삭제한다.
        if(checkedFileMap.size() > 50000){
            for(String key:checkedFileMap.keySet()){
                if(curr - checkedFileMap.get(key) > DUP_CHECK_TIME + 1000){
                    checkedFileMap.remove(key);
                }
            }
        }
        return groovyObject;
    }

    /**
     * Groovy 클래스를 로딩한다
     * @param className 클래스 이름
     * @return 로딩된 Groovy 클래스를반환
     * @throws Exception 클래스를 찾을 수 없을 경우 오류 생성
     */
    synchronized public static Class<GroovyObject> loadGroovyClass(String className)throws Exception{

        ObjectCache cache = ObjectCache.getInstance();
        if(cache.contains(className) && !cache.get(className).isModified()){//Lock 결려있는 동안 이미 로딩되었을 수 있으므로 캐시된 객체를 확인한다.
            return cache.get(className).getOriginClass();
        }
        Class<GroovyObject> clazz = null;
        //클래스 로더에 의해 로딩해야할 대상이라면 현재의 클래스 로더를 통해 JavaClass 형태로 로딩한다.
        if(readByClassLoaderMatch(className)){
            clazz = (Class<GroovyObject>)Thread.currentThread().getContextClassLoader().loadClass(className);
            createObjectInfo(clazz, null, true);
            return clazz;
        }

        String groovyFileName = "/" + className.replaceAll("\\.","/") + ".groovy";
        File file = new File(FrameworkConfig.getBasePath() + groovyFileName);

        //파일이 존재하지 않으면 서비스가 없다고 간주한다.
        if(!file.exists() || file.isDirectory())
            return null;

        /*
         * 중요 : Vanilla Framework에서 GroovyObject간 상호 참조가 허용되지 않는다. 상호 참조를 확인하기 위한 로직이다.
         * service -> common groovy object -> DAO 와 같이 일방향 참조만 가능하다.
         */
        if(onLoadingClasses.containsKey(className)){
            onLoadingClasses.remove(className);
            log.severe("The Class on loading couldn't be load again : "+className);// 로딩중인 클래스는 다시 로딩을 시도할 수 없습니다
            throw new Exception("The Class on loading couldn't be load again : "+className);
        }

        onLoadingClasses.put(className, System.currentTimeMillis());

        try {
            // 새로은 GroovyClassLoader 인스턴스를 만들어서 파싱하는 이유는 내부적인 cache 알고리즘 때문에
            // 과거 class가 잔존하여 새로운 클래스로 대입이 안되는 오류가 있다
            // 그래서 매번 새로운 GroovyClassLoader를 생성하여 script를 파싱한다.
            GroovyClassLoader groovyClassLoader1 = new GroovyClassLoader(scriptClassLoader, config);
            String groovyCode = StreamUtil.file2str(file);
//        log.fine("groovyCode : "+groovyCode);
            clazz = groovyClassLoader1.parseClass(groovyCode, groovyFileName);
            // 클래스명과 ID가 일치하지 않을 경우 오류발생.
            if (!className.equals(clazz.getName())) {
                log.severe("Class or package name doesn't match to : " + className +" / " + clazz.getName());//코드내의 클래스명 혹은 패키지명이 제시된 이름과 일치하지 않습니다
                throw new Exception("Class or package name doesn't match to : " + className +" / " + clazz.getName());
            }

            createObjectInfo(clazz, file);
        }catch (Exception e){
            throw e;
        }finally {
            onLoadingClasses.remove(className);
        }

        return clazz;
    }

    /**
     * file을 읽어서 GroovyObject로 로딩한다. Transactional일경우 proxy코드를 생성하여 wrapping한다.
     * @param id 클래스 ID. eg) my.pack.MyClass
     * @param file id클래스에 해당하는 파일
     * @return
     * @throws Exception
     */
    synchronized private static ObjectInfo loadGroovyObject(String id, File file)throws Exception{
        ObjectCache cache = ObjectCache.getInstance();
        if(cache.contains(id) && cache.get(id).hasObject() && !cache.get(id).isModified()){//Lock 결려있는 동안 이미 로딩되었을 수 있으므로 캐시된 객체를 확인한다.
            return cache.get(id);
        }
        //클래스 로더 대상이 아닌데도 파일이 존재하지 않으면 서비스가 없다고 간주한다.
        if(!readByClassLoaderMatch(id)){
            if(!file.exists() || file.isDirectory())
                return null;
        }

        Class<GroovyObject> clazz = loadGroovyClass(id);

        String groovyFileName = "/" + id.replaceAll("\\.","/") + ".groovy";
        GroovyObject newGroovyObject = null;
        //SQL 정의 interface를 읽어서 RepositoryBase클래스로 생성한다.
        if(clazz.isAnnotationPresent(Repository.class)){
            if(!clazz.isInterface()){
                throw new Exception("@Repository definition can be used on Interface: "+clazz.getName());
            }
            long currentTime = System.currentTimeMillis();
            String proxySrc = GroovySqlProxyUtil.makeProxySrc(clazz, clazz.getSimpleName()+"_sqlProxy"+currentTime);
            String proxyClassName = groovyFileName.substring(0, groovyFileName.length() - ".groovy".length()) + "_proxy" + currentTime + ".groovy";
            log.fine("vanillax.framework.object.proxy class source : \n" + proxySrc);//..debug line
            String dataSourceName = null;
            Annotation a1 = clazz.getAnnotation(Repository.class);
            dataSourceName = ((Repository)a1).value();
            Class<RepositoryBase> clazz1 = groovyClassLoader.parseClass(proxySrc, proxyClassName);
            RepositoryBase repository = clazz1.getDeclaredConstructor(String.class).newInstance(dataSourceName);

            //@Select, @Insert 등의 Annotation의 value를 읽어서 velocity 컴파일을 수행하고 RepositoryBase 인스턴스내의 Map에 저장해둔다.
            Method[] methods = clazz.getDeclaredMethods();
            for(Method m:methods){
                if(Modifier.isPublic(m.getModifiers())){
                    if(m.isAnnotationPresent(Select.class) || m.isAnnotationPresent(Insert.class)
                            || m.isAnnotationPresent(Update.class)||m.isAnnotationPresent(Delete.class)){
                        ScriptType scriptType = ScriptType.NORMAL;
                        if(m.isAnnotationPresent(Velocity.class)){
                            scriptType = ScriptType.VELOCITY;
                        }
                        String sql = null;
                        Template template = null;
                        String timestampFieldStr = null;
                        if(m.isAnnotationPresent(Select.class)){
                            Annotation annotation = m.getAnnotation(Select.class);
                            sql = ((Select)annotation).value();
                            if(m.isAnnotationPresent(TimestampFields.class)){
                                annotation = m.getAnnotation(TimestampFields.class);
                                timestampFieldStr = ((TimestampFields)annotation).value();
                            }
                        }else if(m.isAnnotationPresent(Insert.class)){
                            Annotation annotation = m.getAnnotation(Insert.class);
                            sql = ((Insert)annotation).value();
                        }else if(m.isAnnotationPresent(Update.class)){
                            Annotation annotation = m.getAnnotation(Update.class);
                            sql = ((Update)annotation).value();
                        }else if(m.isAnnotationPresent(Delete.class)){
                            Annotation annotation = m.getAnnotation(Delete.class);
                            sql = ((Delete)annotation).value();
                        }
                        //SQL 추적을 위해 클래스, 메소드 명을 SQL에 추가
                        sql = sql +"\n/* Repository Method : " + id +"."+m.getName()+"() */\n";
                        if(scriptType == ScriptType.VELOCITY){
                            template = VelocityFacade.compile(sql, clazz.getName()+"."+m.getName()+"()");
                        }
                        String paramClassName = "";
                        if(m.getParameterCount()>=1){
                            paramClassName = m.getParameterTypes()[0].getName();
                        }
                        String methodPath = m.getName()+"("+paramClassName+")";//"selectList(java.util.Map)"
                        SqlInfo sqlInfo = new SqlInfo(template, sql, scriptType, timestampFieldStr);
                        repository.putSqlInfo(methodPath, sqlInfo);
                    }
                }
            }
            newGroovyObject = (GroovyObject) repository;
        }else {
            //여기에 Transaction Proxy클래스를 만들고 인스턴스를 생성해서 기존 GroovyObject를 대체해야한다
            boolean isTx = TransactionHelper.isTransactional(clazz);
            if (isTx) {
                //Proxy생성해서 다시 컴파일하고 serviceObject를 대체한다.
                long currentTime = System.currentTimeMillis();
                String proxySrc = GroovyProxyUtil.makeProxySrc(clazz, clazz.getSimpleName()+"_proxy"+currentTime);
                String proxyClassName = groovyFileName.substring(0, groovyFileName.length() - ".groovy".length()) + "_proxy" + currentTime + ".groovy";
                log.fine("vanillax.framework.object.proxy class source : \n" + proxySrc);//..debug line
                Class<GroovyObject> clazz1 = groovyClassLoader.parseClass(proxySrc, proxyClassName);
                newGroovyObject = clazz1.newInstance();
            } else {
                newGroovyObject = clazz.newInstance();
            }
        }

        ObjectInfo objectInfo = cache.get(id);
        //새로생성된 Object에 Autowired 객체를 생성해서 추가해준다.
        Map<String,String> referencedClassMap = objectInfo.getReferencedClassMap();
        for(String fieldName : referencedClassMap.keySet()){
            String className = referencedClassMap.get(fieldName);
            ObjectInfo infoTemp = cache.get(className);
            if(infoTemp == null){
                //class가 아니고 script인데 이런 경우는 절대 발생하면 안된다. loadGroovyClass() 메소드에서 이미 모두 해결됐어야 한다.
            }
            if(readByClassLoaderMatch(className) && infoTemp == null){
                //class로 로딩된 객체는 참조 클래스까지 모두 로딩했기때문에 cache에 저장되지 않을 수 있다.
                infoTemp = loadGroovyObject(className, null);
            }else{
                infoTemp = loadGroovyObject(className, infoTemp.getFile());
            }

            //setter를 이용해서 추가한다.
            GroovyObject autowiredObject = getGroovyObjectWithAutowired(infoTemp);
            ReflectionUtil.invokeSetter(newGroovyObject, fieldName, autowiredObject);
            log.fine("Autowired '"+fieldName+"' as "+autowiredObject+" on the class "+id);
//            ReflectionUtil.invokeSetter(newGroovyObject, fieldName, infoTemp.getObject());
        }
        objectInfo.setObject(newGroovyObject);
        objectInfo.resetModifiedTime();//변경시간을 갱신한다.
        return objectInfo;
    }

    /**
     * JavaClass 형태로 바로 로딩할 대상인지 확인한다.
     * @param className
     * @return vanilla.properties에 read.by.classloader값으로 정의 되어있는 패턴과 일치할 경우 true 반환
     */
    private static boolean readByClassLoaderMatch(String className){
        if(readByClassLoaderPatterns.size() ==0){
            String str = FrameworkConfig.get("read.by.classloader");
            if(str != null){
                String[] arr = str.split(",");
                for(String s:arr){
                    String s1 = s.trim();
                    if("".equals(s1))
                        continue;
                    readByClassLoaderPatterns.add(s1);
                }
            }
        }
        if(readByClassLoaderSet.contains(className))
            return true;
        if(readByClassLoaderPatterns.size() > 0){
            for(String pattern: readByClassLoaderPatterns){
                if(StringUtil.match(className, pattern)){
                    readByClassLoaderSet.add(className);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * ObjectInfo 객체를 생성하고 필요한 정보를 입력한다.
     * @Autowired 되어있는 필드를 뽑아서 목록을 작성한다.
     * InstanceType singleton여부 확인하여 objectInfo.setInstacneType()을 호출한다.
     * @param clazz 생성할 GroovyObject의 클래스
     * @param file 대상 groovy파일. null허용.
     * @return
     * @throws Exception 스크립트 컴파일 오류 발생시
     */
    private static ObjectInfo createObjectInfo(Class<GroovyObject> clazz, File file, boolean readByClassLoader)throws Exception{
        ObjectInfo objectInfo = new ObjectInfo();
        objectInfo.setFile(file);
        objectInfo.setOriginClass(clazz);
        objectInfo.setReadByClassLoader(readByClassLoader);
        ObjectCache cache = ObjectCache.getInstance();

        // @Autowired 되어있는 필드를 뽑아서 목록을 작성한다.
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowired.class)) {
                Class class1 = field.getType();
                // Autowired대상의 클래스인지 확인한다.
                // ScriptClassLoader에서 이미 로딩한 클래스라면 ObjectCache에 존재하여 확인할 필요가 없다.
                // GroovyObject도 아니고 @Repository 인터페이스가 아니라면 오류를 뱉어낸다.
                if (!cache.contains(class1.getName())
                        && !class1.isAssignableFrom(GroovyObject.class)
                        && !class1.isAnnotationPresent(Repository.class)
                        && !class1.getName().startsWith("vanillax.")
                ) {
                    throw new Exception("None GroovyObject can't be declare as @Autowired");
                }
                //Field의 클래스를 참조 클래스로 간주한다.
                objectInfo.putReferencedClass(field.getName(), class1.getName());
            }
        }

        // InstanceType singleton여부 확인하여 objectInfo.setInstanceType()을 호출해야 한다.
        if (clazz.isAnnotationPresent(InstanceType.class)) {
            Annotation a1 = clazz.getAnnotation(InstanceType.class);
            String type = ((InstanceType) a1).value();
            if ("newInstance".equals(type)) {
                objectInfo.setInstanceType(Constants.INSTANCE_TYPE_NEW_INSTANCE);
            } else if (type == null || "".equals(type) || "singleton".equals(type)) {
                objectInfo.setInstanceType(Constants.INSTANCE_TYPE_SINGLETON);
            } else {
                throw new Exception("@InstanceType doesn't allow following value : " + type);
            }
        }
        cache.put(clazz.getName(), objectInfo);//방금 로딩된 클래스를 캐시에 넣는다.
        return objectInfo;
    }

    private static ObjectInfo createObjectInfo(Class<GroovyObject> clazz, File file)throws Exception{
        return createObjectInfo(clazz, file, false);
    }

    /**
     * @Autowired로 참조하고있는 하위 모든 클래스의 변경 유무를 확인한다.
     * @param objectInfo
     */
    private static boolean isReferencedClassModified(ObjectInfo objectInfo){
        if(objectInfo == null || objectInfo.getReferencedClassMap().size() == 0)
            return false;
        boolean isModified = false;
        ObjectCache cache = ObjectCache.getInstance();
        Map<String, String> referencedClassMap = objectInfo.getReferencedClassMap();
        for(String fieldName : referencedClassMap.keySet()){
            String referencedClassName = referencedClassMap.get(fieldName);
            if(cache.contains(referencedClassName)){
                ObjectInfo referencedObjectInfo = cache.get(referencedClassName);
                if(objectInfo.getLoadedTime() < referencedObjectInfo.getLoadedTime()){
                    log.fine("Referenced Class Loaded later than referencing class : "+referencedObjectInfo.getOriginClass().getName() + " at " + objectInfo.getOriginClass().getName());
                    isModified = true;
                }else if(referencedObjectInfo.isModified(true)){
                    log.fine("Referenced Class Modified : "+referencedObjectInfo.getOriginClass().getName() + " at " + objectInfo.getOriginClass().getName());
                    isModified = true;
                }
                boolean b1 = isReferencedClassModified(referencedObjectInfo);
                if(b1){
                    isModified = true;
                    log.fine("Referenced Class Modified : some class at " + objectInfo.getOriginClass().getName());
                }
            }
        }
        if(isModified){
            objectInfo.setReferencedClassModified(true);
        }
        return isModified;
    }

    /**
     * @Autowired로 명시된 클래스의 인스턴스를 모두 생성하여 필드명에 연결해준 GroovyObject를 반환한다.
     * @param objectInfo
     * @return
     * @throws Exception
     */
    synchronized private static GroovyObject getGroovyObjectWithAutowired(ObjectInfo objectInfo)throws Exception{
        GroovyObject groovyObject = objectInfo.getObject();
        if(objectInfo.getInstanceType() == Constants.INSTANCE_TYPE_NEW_INSTANCE){
            ObjectCache cache = ObjectCache.getInstance();
            //새로생성된 Object에 Autowired 객체를 생성해서 추가해준다.
            Map<String,String> referencedClassMap = objectInfo.getReferencedClassMap();
            for(String fieldName : referencedClassMap.keySet()){
                String className = referencedClassMap.get(fieldName);
                ObjectInfo infoTemp = cache.get(className);
                if(infoTemp == null){
                    //이런 경우는 절대 발생하면 안된다. loadGroovyClass() 메소드에서 이미 모두 해결됐어야 한다.
                }
                infoTemp = loadGroovyObject(className, infoTemp.getFile());
                //setter를 이용해서 추가한다.
                ReflectionUtil.invokeSetter(groovyObject, fieldName, getGroovyObjectWithAutowired(infoTemp));
            }
        }
        return groovyObject;
    }

}
