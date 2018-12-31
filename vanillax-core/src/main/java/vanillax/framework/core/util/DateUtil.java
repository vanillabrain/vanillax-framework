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

package vanillax.framework.core.util;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by gaedong on 9/3/15.
 */
public class DateUtil {
    private static SimpleDateFormat normalDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static SimpleDateFormat fullDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    public static String getCurrentTimeString(){
        return normalDateFormat.format(new Date());
    }

    public static void long2Date(Map<String,Object> map, List<String> targetKey){
        if(map == null || targetKey == null || map.size() ==0 || targetKey.size() == 0)
            return;
        Set<String> set = new HashSet<>(targetKey.size());
        for(String k:targetKey){
            set.add(k);
        }
        long2Date(map, set);
    }

    public static void long2Date(Map<String,Object> map, Set<String> targetKeySet){
        if(map == null || targetKeySet == null || map.size() ==0 || targetKeySet.size() == 0)
            return;
        Set<String> keys = new HashSet<>();
        keys.addAll(map.keySet());
        for(String key:keys){
            if(targetKeySet.contains(key)){
                Object val = map.get(key);
                if(val != null && val instanceof Long){
                    //map.put(key, normalDateFormat.format(new Date((Long)val)));
                    map.put(key, new Date((Long)val));
                }
            }
        }
    }

    public static void long2Date(List<Map<String,Object>> list, Set<String> targetKeySet){
        if(list == null || targetKeySet == null || list.size() ==0 || targetKeySet.size() == 0)
            return;
        for(Map<String,Object> m:list){
            long2Date(m, targetKeySet);
        }
    }

    public static void long2Date(List<Map<String,Object>> list, List<String> targetKey){
        if(list == null || targetKey == null || list.size() ==0 || targetKey.size() == 0)
            return;
        Set<String> set = new HashSet<>(targetKey.size());
        for(String k:targetKey){
            set.add(k);
        }
        long2Date(list, set);
    }

    /**
     * for groovy
     * @param map
     * @param targetKey
     */
    public static void long2DateMap(Map map, List targetKey){
        if(map == null || targetKey == null || map.size() == 0 || targetKey.size() == 0)
            return;
        long2Date((Map<String,Object>) map, (List<String>) targetKey);
    }

    /**
     * for groovy
     * @param list
     * @param targetKey
     */
    public static void long2DateList(List list, List targetKey){
        if(list == null || targetKey == null || list.size() == 0 || targetKey.size() == 0)
            return;
        long2Date((List<Map<String,Object>>)list, (List<String>)targetKey);
    }
}
