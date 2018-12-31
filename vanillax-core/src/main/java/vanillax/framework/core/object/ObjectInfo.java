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

import groovy.lang.GroovyObject;
import vanillax.framework.core.Constants;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 로딩한 Groovy Object의 정보
 */
public class ObjectInfo {
    /** GroovyObject script파일 */
    private File file = null;
    /** script가 변경된 시간 */
    private long lastModifed = 0L;
    /** 생성된 인스턴스. Proxy 클래스의 인스턴스일 수 있다 */
    private GroovyObject object = null;
    /** 원본 스크립트의 클래스. Proxy클래스가 아닌 원본이다. */
    private Class<GroovyObject> originClass = null;
    /** 변경유무를 마지막으로 확인한 시간 */
    private long lastCheckTime = 0L;

    /**
     * @Autowired를 이용해 참조하고있는 클래스명. script만 포함한다
     * key : 필드명
     * value : 클래스명
     */
    private Map<String, String> referencedClassMap = null;

    /** GroovyObject가 로딩된 시간 */
    private long loadedTime = 0L;

    /** 참조관계의 클래스들이 변경되었는지 여부 */
    private boolean referencedClassModified = false;
    /** JavaClass형태로 로딩되는지 여부. true이면 modified는 언제나 false이다 */
    private boolean readByClassLoader = false;
    /** GroovyObject 인스턴스 유형. NEW_INSTANCE인 경우 매번 인스턴스를 생성한다. */
    private int instanceType = Constants.INSTANCE_TYPE_SINGLETON;

    public ObjectInfo(){
        this.referencedClassMap = new LinkedHashMap<>();
    }

    public File getFile() {
        return file;
    }

    public long getLastModifed() {
        return lastModifed;
    }

    public boolean hasObject(){
        return this.object != null;
    }

    public GroovyObject getObject() {
        if(this.object == null)
            return  null;
        //instanceType 확인해서 newInstance인경우 새 인스턴스 생성해서 반환한다.
        if(this.instanceType == Constants.INSTANCE_TYPE_NEW_INSTANCE){
            GroovyObject groovyObject = null;
            synchronized (ObjectInfo.class){
                try{
                    groovyObject = this.object.getClass().newInstance();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            return groovyObject;
        }
        return object;
    }

    public void setFile(File file) {
        if(file != null && file.exists() && !file.isDirectory()){
            this.lastModifed = file.lastModified();
        }
        this.file = file;
    }
    public void resetModifiedTime() {
        if(file!=null && file.exists() && !file.isDirectory()){
            this.lastModifed = file.lastModified();
        }
    }
    public void setObject(GroovyObject object) {
        this.object = object;
        this.loadedTime = System.currentTimeMillis();
    }

    public Class<GroovyObject> getOriginClass() {
        return originClass;
    }

    public void setOriginClass(Class<GroovyObject> originClass) {
        this.originClass = originClass;
    }

    public boolean isModified(){
        return isModified(false);
    }

    /**
     * Class및 하위 Class가 변경되었는지 여부 판단.
     * @param delayAvailable 판단을 유예할지 여부. true이면 최근 확인시간으부터 2초내에는 변경되지 않은 것으로 처리.
     * @return
     */
    public boolean isModified(boolean delayAvailable){
        //Autowired로 참조관계에 있는 하위의 클래스들이 변경되었을 경우 이 클래스도 변경되었다고 판단한다.
        if(this.referencedClassModified)
            return true;
        //JavaClass로 로딩된 경우 변경되지 않은 것으로 처리된다.
        if(this.readByClassLoader){
            return false;
        }
        long curr = System.currentTimeMillis();
        if(delayAvailable && curr - this.lastCheckTime < 2000){
            //변경유무를 확인한지 2초가 지나지 않았으면 그냥 변경되지 않은 것으로 간주한다.
            //매번 파일 IO하는 것을 방지하기 위함이다.
            return false;
        }
        lastCheckTime = curr;
        if(!file.exists()){//파일이 삭제되었을 경우 변경된것으로 간주한다.
            return true;
        }
        return file.lastModified() > this.lastModifed;
    }

    public void putReferencedClass(String fieldName, String className){
        if(fieldName == null || className == null)
            return;
        this.referencedClassMap.put(fieldName, className);
    }

    public Map<String, String> getReferencedClassMap() {
        return referencedClassMap;
    }

    public boolean isReferencedClassModified() {
        return referencedClassModified;
    }

    public void setReferencedClassModified(boolean referencedClassModified) {
        this.referencedClassModified = referencedClassModified;
    }

    public int getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(int instanceType) {
        this.instanceType = instanceType;
    }

    public long getLoadedTime() {
        return loadedTime;
    }

    public boolean isReadByClassLoader() {
        return readByClassLoader;
    }

    public void setReadByClassLoader(boolean readByClassLoader) {
        this.readByClassLoader = readByClassLoader;
    }
}
