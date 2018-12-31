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

import java.util.Hashtable;
import java.util.Map;

/**
 * Created by gaedong on 8/5/15.
 */
public class ObjectCache {
    private static ObjectCache instance = null;
    private Map<String, ObjectInfo> cacheMap = null;

    public static ObjectCache getInstance() {
        if (instance == null) {
            synchronized (ObjectCache.class) {
                if (instance == null) {
                    instance = new ObjectCache();
                }
            }
        }
        return instance;
    }

    private ObjectCache() {
        cacheMap = new Hashtable<String, ObjectInfo>();
    }

    public boolean contains(String key){
        return cacheMap.containsKey(key);
    }

    public ObjectInfo get(String key){
        return cacheMap.get(key);
    }

    public void put(String key, ObjectInfo objectInfo){
        cacheMap.put(key, objectInfo);
    }

    public void remove(String key){
        cacheMap.remove(key);
    }
}
