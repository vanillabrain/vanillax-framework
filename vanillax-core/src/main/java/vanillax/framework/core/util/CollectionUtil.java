package vanillax.framework.core.util;

import java.util.*;

public class CollectionUtil {
    public static Map map2map(Map m){
        if(m==null)
            return null;
        Map newMap = new LinkedHashMap(m.size());
        for(Object key:m.keySet()){
            Object value = m.get(key);
            if(value instanceof Map && !(value instanceof HashMap)){
                value = map2map((Map)value);
            }else if(value instanceof List){
                value = list2list((List)value);
            }
            newMap.put(key, value);
        }
        m.clear();
        return newMap;
    }

    public static List list2list(List list){
        if(list == null)
            return null;
        List newList = new ArrayList(list.size());
        for(Object value:list){
            if(value instanceof Map && !(value instanceof HashMap)){
                value = map2map((Map)value);
            }else if(value instanceof List){
                value = list2list((List)value);
            }
            newList.add(value);
        }
        list.clear();
        return newList;
    }
}
