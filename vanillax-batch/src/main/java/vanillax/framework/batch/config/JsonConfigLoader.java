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

package vanillax.framework.batch.config;

import groovy.json.JsonParserType;
import groovy.json.JsonSlurper;

import java.io.File;
import java.io.FileInputStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * JSon config파일을 로딩하여 configMap입력한다.
 */
public class JsonConfigLoader {
    private static final Logger log = Logger.getLogger(JsonConfigLoader.class.getName());
    private static JsonConfigLoader ourInstance = new JsonConfigLoader();
    private Map<String, Object> configMap = null;
    private File configFile = null;
    private long lastModified = 0L;

    public static JsonConfigLoader getInstance() {
        return ourInstance;
    }

    private JsonConfigLoader() {
        configMap = new Hashtable<String, Object>();
    }

    public void load()throws Exception {
        load(this.configFile);
    }

    public void load(File jsonFile)throws Exception {
        if(jsonFile == null)
            return;
        if(jsonFile.exists() && jsonFile.lastModified() <= this.lastModified){
            return;
        }
        this.lastModified = jsonFile.lastModified();

        FileInputStream fin = null;
        try {
            fin = new FileInputStream(jsonFile);
            JsonSlurper jsonSlurper = new JsonSlurper().setType(JsonParserType.LAX);
            Map map = (Map)jsonSlurper.parse(fin, "UTF-8");
            Set s = map.keySet();
            Iterator it = s.iterator();
            this.configMap.clear();
            while(it.hasNext()){
                String key = (String)it.next();
                Object value = map.get(key);
                this.configMap.put(key,value);
            }
        }catch(Exception e){
            log.warning("Config JSon 파일 로딩중 오류가 발생했습니다 : " + e.getMessage());
            throw e;
        }finally {
            try{fin.close();}catch(Exception e){}
        }
    }

    public Object get(String path){// system.reload
        if(path == null)
            return null;
        String[] arr = path.split("\\.");
        Map<String,Object> tmpMap = this.configMap;
        for(int i=0;i<arr.length;i++){
            Object obj = tmpMap.get(arr[i]);
            if(obj == null)
                return null;
            if(i == arr.length-1){//마지막 문자이면 반환
                return obj;
            }
            if(obj instanceof Map == false){
                return null;
            }
            tmpMap = (Map)obj;
        }
        return null;
    }

    public int getInt(String path){
        Object obj = get(path);
        if(obj == null)
            return 0;
        if(obj instanceof Integer){
            return (Integer)obj;
        }
        return Integer.parseInt(obj.toString());
    }

    public String getString(String path){
        Object obj = get(path);
        if(obj == null )
            return null;
        return obj.toString();
    }

    public boolean getBoolean(String path){
        Object obj = get(path);
        if(obj == null)
            return false;
        if(obj instanceof Boolean){
            return (Boolean)obj;
        }
        return "true".equals(obj.toString().toLowerCase());
    }

}
