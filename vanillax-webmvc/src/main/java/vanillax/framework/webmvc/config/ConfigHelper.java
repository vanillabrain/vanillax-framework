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

package vanillax.framework.webmvc.config;

import java.util.Properties;

/**
 * Created by gaedong on 8/5/15.
 */
public class ConfigHelper {
    private static String basePath = null;
    private static Boolean reload = null;
    private static Properties prop = null;

    public static String getBasePath(){
        if(basePath != null)
            return basePath;
        if(prop != null && prop.containsKey("script.base.path"))
            basePath = prop.getProperty("script.base.path");
        else
            basePath = prop.getProperty("web.inf.path")+"scripts";
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
        return get(key, null);
    }

    public static String get(String key, String defaultValue){
        if(prop == null)
            return null;

        if(prop.containsKey(key)) {
            String s = prop.getProperty(key);
            if (s != null) {
                return s.trim();
            }
        }
        return defaultValue;
    }

    public static int getInt(String key){
        if(prop == null || !prop.containsKey(key))
            throw new RuntimeException("No contains key : "+key);
        String str = get(key);
        return Integer.parseInt(str);
    }

    public static int getInt(String key, int defaultValue){
        if(prop == null)
            throw new RuntimeException("No contains key : "+key);
        if(!prop.containsKey(key)){
            return defaultValue;
        }
        return Integer.parseInt(get(key));
    }

    public static float getFloat(String key){
        if(prop == null || !prop.containsKey(key))
            throw new RuntimeException("No contains key : "+key);
        return Float.parseFloat(get(key));
    }

    public static float getFloat(String key, float defaultValue){
        if(prop == null)
            throw new RuntimeException("No contains key : "+key);
        if(!prop.containsKey(key)){
            return defaultValue;
        }
        return Float.parseFloat(get(key));
    }

    public static double getDouble(String key){
        if(prop == null || !prop.containsKey(key))
            throw new RuntimeException("No contains key : "+key);
        return Double.parseDouble(get(key));
    }

    public static double getDouble(String key, double defaultValue){
        if(prop == null)
            throw new RuntimeException("No contains key : "+key);
        if(!prop.containsKey(key)){
            return defaultValue;
        }
        return Double.parseDouble(get(key));
    }

    public static boolean getBoolean(String key){
        if(prop == null || !prop.containsKey(key))
            throw new RuntimeException("No contains key : "+key);
        String str = get(key);
        if(str == null){
            throw new RuntimeException("No contains key : "+key);
        }
        if("true".equalsIgnoreCase(str)){
            return true;
        }
        if("false".equalsIgnoreCase(str)){
            return false;
        }
        throw new RuntimeException("wrong value : "+str);
    }

    public static boolean getBoolean(String key, boolean defaultValue){
        if(prop == null)
            throw new RuntimeException("No contains key : "+key);
        if(!prop.containsKey(key)){
            return defaultValue;
        }
        String str = get(key);
        if(str == null){
            return defaultValue;
        }
        if("true".equalsIgnoreCase(str)){
            return true;
        }
        if("false".equalsIgnoreCase(str)){
            return false;
        }
        throw new RuntimeException("wrong value : "+str);
    }

    public static void setProp(Properties prop){
        ConfigHelper.prop = prop;
    }


}
