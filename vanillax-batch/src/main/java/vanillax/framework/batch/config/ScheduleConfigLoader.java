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
import vanillax.framework.batch.scheduler.ScheduleInfo;
import vanillax.framework.batch.scheduler.ScheduleInfoRepository;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Schedule config파일을 로딩하는 클래스.
 */
public class ScheduleConfigLoader {
    private static final Logger log = Logger.getLogger(ScheduleConfigLoader.class.getName());
    private File scheduleConfigFile = null;
    private long lastModified = 0L;

    public ScheduleConfigLoader(File jsonFile) {
        this.scheduleConfigFile = jsonFile;
    }

    public void load()throws Exception {
        load(this.scheduleConfigFile);
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
            Map json = (Map)jsonSlurper.parse(fin, "UTF-8");
            Map global = (Map)json.get("global");
            List<Map> scheduleList = (List<Map>)json.get("schedule") ;
            for(Map m:scheduleList){
                String script = (String)m.get("script");
                if(script == null)
                    continue;
                script  = makePackageName(script, global);

                if(ScheduleInfoRepository.getInstance().contains(script)){
                    throw new Exception("동일한 스케줄이 이미 정의되어있습니다 : "+script);
                }
                ScheduleInfo info = new ScheduleInfo();
                info.setScript(script);
                info.setInterval((Integer) m.get("interval"));
                if(m.containsKey("idleTime")) {
                    info.setIdleTime((String) m.get("idleTime"));
                }
                if(m.containsKey("postScript")) {
                    info.setPostScript((String) m.get("postScript"));
                }else if(global.containsKey("postScript")){
                    String postScript = makePackageName((String) global.get("postScript"),global);
                    info.setPostScript( postScript);
                }
                if(m.containsKey("preScript")) {
                    info.setPreScript((String) m.get("preScript"));
                }else if(global.containsKey("preScript")){
                    String preScript = makePackageName((String)global.get("preScript"),global);
                    info.setPreScript(preScript);
                }
                if(m.containsKey("active")) {
                    info.setActive((Boolean) m.get("active"));
                }
                ScheduleInfoRepository.getInstance().addSchedule(info);
            }//..for()
        }catch(Exception e){
            log.warning("스케줄 로딩중 오류가 발생했습니다 : " + e.getMessage());
            throw e;
        }finally {
            try{fin.close();}catch(Exception e){}
        }
    }

    private String makePackageName(String script, Map global){
        if(script == null)
            return null;
        String renamed = null;
        if(script.startsWith("/")){// '/'로 시작하면 절대경로로 인식한다.
            renamed = script.substring(1,script.length());
        }else{
            String prefix = "";
            if(global.containsKey("packagePrefix")){//글로벌 변수의 접두사가 있으면 패키지명에 붙여준다.
                prefix = ""+global.get("packagePrefix");
            }
            renamed = prefix+script;
        }
        return renamed;
    }

}
