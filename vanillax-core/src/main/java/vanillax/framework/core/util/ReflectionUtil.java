package vanillax.framework.core.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionUtil {
    public static Field findField(Class clazz, String fieldName){
        if(clazz == null)
            return null;
        Field field = null;
        try {
            field = clazz.getDeclaredField(fieldName);
        }catch (NoSuchFieldException e){
            field = findField(clazz.getSuperclass(), fieldName);
        }
        return field;
    }

    public static void setField(Object targetObject, String fieldName, Object fieldValue)throws IllegalAccessException{
        Field field = findField(targetObject.getClass(), fieldName);
        if(field != null){
            field.setAccessible(true);
            field.set(targetObject, fieldValue);
        }
    }

    public static void invokeSetter(Object targetObject, String fieldName, Object fieldValue)throws Exception{
        invokeSetter(targetObject, fieldName, fieldValue, false);
    }

    public static void invokeSetter(Object targetObject, String fieldName, Object fieldValue, boolean lookupSuperClass)throws Exception{
        String setter = "set"+ fieldName.substring(0,1).toUpperCase() + fieldName.substring(1, fieldName.length());
        String setter1 = "set"+fieldName;
        Method method = null;
        method = findMethod(targetObject.getClass(), setter, lookupSuperClass);
        if(method == null){
            method = findMethod(targetObject.getClass(), setter1, lookupSuperClass);
        }
        method.invoke(targetObject, fieldValue);
    }

    public static Method findMethod(Class clazz, String methodName){
        return findMethod(clazz, methodName, false);
    }

    public static Method findMethod(Class clazz, String methodName, boolean lookupSuperClass){
        if(clazz == null)
            return null;
        Method method = null;
        for(Method m:clazz.getDeclaredMethods()){
            if(m.getName().equals(methodName)){
                method = m;
            }
        }
        if(method == null && lookupSuperClass)
            method = findMethod(clazz.getSuperclass(), methodName);
        return method;
    }

}
