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

package vanillax.framework.core.config;

import java.util.Properties;

/**
 * ObjectLoader의 설정값 관리 클래스.
 */
public class FrameworkConfig {
    private static String basePath = null;
    private static Boolean reload = null;
    private static Properties prop = null;

    public static String getBasePath(){
        if(basePath != null)
            return basePath;
        if(prop != null && prop.containsKey("script.base.path"))
            basePath = prop.getProperty("script.base.path");
        return basePath;
    }

    public static boolean isReload(){
        if(reload != null)
            return reload;
        if(prop != null && prop.containsKey("script.reload"))
            reload = "true".equals(prop.getProperty("script.reload"));
        return reload;
    }

    public static String get(String key){
        if(prop == null)
            return null;
        return prop.getProperty(key);
    }

    public static int getInt(String key){
        if(prop == null || !prop.containsKey(key))
            throw new RuntimeException("No contains key : "+key);
        String str = prop.getProperty(key);
        return Integer.parseInt(str);
    }

    public static int getInt(String key, int defaultValue){
        if(prop == null || !prop.containsKey(key))
            return defaultValue;
        String str = prop.getProperty(key);
        int result = defaultValue;
        try{
            result = Integer.parseInt(str);
        }catch(Exception ignore){}
        return result;
    }

    public static double getDouble(String key){
        if(prop == null || !prop.containsKey(key))
            throw new RuntimeException("No contains key : "+key);
        String str = prop.getProperty(key);
        return Double.parseDouble(str);
    }

    public static double getDouble(String key, double defaultValue){
        if(prop == null || !prop.containsKey(key))
            return defaultValue;
        String str = prop.getProperty(key);
        double result = defaultValue;
        try{
            result = Double.parseDouble(str);
        }catch(Exception ignore){}
        return result;
    }

    public static boolean getBoolean(String key){
        if(prop == null || !prop.containsKey(key))
            throw new RuntimeException("No contains key : "+key);
        String str = prop.getProperty(key);
        return Boolean.valueOf(str);
    }

    public static boolean getBoolean(String key, boolean defaultValue){
        if(prop == null || !prop.containsKey(key))
            return defaultValue;
        String str = prop.getProperty(key);
        boolean result = defaultValue;
        try{
            result = Boolean.valueOf(str);
        }catch(Exception ignore){}
        return result;
    }

    public static void setProp(Properties prop){
        FrameworkConfig.prop = prop;
    }

}
