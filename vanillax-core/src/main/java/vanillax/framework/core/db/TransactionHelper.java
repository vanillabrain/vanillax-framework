package vanillax.framework.core.db;

import java.lang.reflect.Method;

/**
 * Created by gaedong on 2016. 7. 20..
 */
public class TransactionHelper {

    /**
     * obj의 클래스 단위로 Transactional annotation이 정의되어있거나 methodName의 메소드에 Transactional annotatioin이 정의되어있는지 확인한다.
     * @param obj 판별 대상 객체
     * @param methodName 판별할 메소드 명
     * @return -1 : transactional이 아니다. 0:Transaction이다.
     */
    public static int isTransactional(Object obj, String methodName){
        if(obj == null || methodName == null)
            return -1;

        Class clazz = obj.getClass();
        if(clazz.isAnnotationPresent(Transactional.class)){
            return 0;
        }

        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName) && method.isAnnotationPresent(Transactional.class)) {
                return 0;
            }
        }
        return -1;
    }


    public static boolean isTransactional(Class clazz){
        if(clazz == null)
            return false;
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Transactional.class)) {
                return true;
            }
        }
        return false;
    }
}
