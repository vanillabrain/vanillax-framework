package vanillax.framework.core.object.proxy;

import vanillax.framework.core.db.orm.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Sql Interface의 Implement Proxy소스를 생성한다.
 */
public class GroovySqlProxyUtil {
    private static final String[] IGNORE_METHODS = {"getProperty","setProperty","getMetaClass","setMetaClass","invokeMethod"};

    public static String makeProxySrc(Class clazz)throws Exception{
        String className = clazz.getSimpleName()+"_sqlProxy"+System.currentTimeMillis();
        return makeProxySrc(clazz, className);
    }

    public static String makeProxySrc(Class interfaceClass, String className)throws Exception{
        StringBuffer sb = new StringBuffer();
        Set<String> dupCheckSet = new HashSet<String>();

        sb.append("/* This source generated by SYSTEM. */\n");
        String name = interfaceClass.getName();
        if(interfaceClass.getPackage() != null){
            String packageName = interfaceClass.getPackage().getName();
            if(!"".equals(packageName)){
                sb.append("package "+packageName+"\n");
            }
        }

        if(className == null)
            className = interfaceClass.getSimpleName()+"_sqlProxy"+System.currentTimeMillis();

        sb.append("public class "+className+" extends "+ RepositoryBase.class.getCanonicalName() +" implements "+name+" {\n");

        //기본 생성자 구성.
        sb.append("public ").append(className).append("(String dataSourceName){\n");
        sb.append("\tsuper(dataSourceName)\n");
        sb.append("}//..constructor\n");

        Method[] methods = interfaceClass.getDeclaredMethods();
        dupCheckSet.clear();
        for(Method m:methods){
            if(isIgnoreMethod(m))
                continue;
            if(Modifier.isPublic(m.getModifiers())){
                Class paramClass = null;
                String paramClassName = "";
                if(m.getParameterCount() > 1){
                    throw new Exception("SQL 객체 메소드는 단일한 인자만 허용합니다 : "+m.getName());
                }
                if(m.getParameterCount() > 0){
                    paramClass = m.getParameterTypes()[0];
                    paramClassName = m.getParameterTypes()[0].getCanonicalName();
                }

                String checkString = m.getName() + "/" + m.getParameterCount();
                if(dupCheckSet.contains(checkString)){
                    throw new Exception("함수 이름이 중복되었습니다 : "+m.getName()+"("+paramClassName+")");
                }
                dupCheckSet.add(checkString);
                String returnType = m.getReturnType().getSimpleName().toLowerCase();
                String returnTypeString = "";
                if(returnType.equals("boolean") || returnType.equals("int") || returnType.equals("integer") || returnType.equals("double")
                        || returnType.equals("short") || returnType.equals("long") || returnType.equals("float") || returnType.equals("void")){
                    if(returnType.equals("integer")){
                        returnTypeString = "int";
                    }else{
                        returnTypeString = returnType;
                    }
                }else if(returnType.equals("object[]")){
                    returnTypeString = "Object[]";
                }else if(!returnType.equals("object")){
                    returnTypeString = m.getReturnType().getName();
                }

                sb.append("def "+returnTypeString+" "+m.getName() +" (");

                //parameter 처리 시작
                //java.lang.Object가 아닌 경우 파라미터 클래스명을 명시해준다. 그렇지 않으면 모두 java.lang.Object로 인식한다.
                //스크립트상의 Overroading오류를 피하기 위함이다.
                if(!"java.lang.Object".equals(paramClassName)){
                    sb.append(paramClassName).append(" ");
                }
                sb.append("param");
                sb.append(")");//parameter 처리 끝

                sb.append("{\n");
                sb.append("\treturn ");

                if(m.isAnnotationPresent(Select.class)){
                    sb.append("this.select");
                }else if(m.isAnnotationPresent(Insert.class)){
                    sb.append("this.insert");
                }else if(m.isAnnotationPresent(Update.class)){
                    sb.append("this.update");
                }else if(m.isAnnotationPresent(Delete.class)){
                    sb.append("this.delete");
                }else{
                    throw new Exception("@Repository Interface는 SQL 유형 Annotation을 반드시 명시해야합니다 : "+interfaceClass.getName()+"."+m.getName()+"()");
                }

                if(m.isAnnotationPresent(Select.class)){
                    if(paramClass.isAssignableFrom(List.class)){
                        throw new Exception("@Select는 List를 인자로 허용하지 않습니다 : "+interfaceClass.getName()+"."+m.getName()+"(List list)");
                    }
                    if(m.getReturnType().isAssignableFrom(List.class)){
                        sb.append("List");
                    }else{
                        sb.append("One");
                    }
                }else{
                    if(paramClass != null && paramClass.isAssignableFrom(List.class)){
                        sb.append("List");
                    }else {
                        sb.append("One");
                    }
                }

                sb.append("('").append(m.getName()).append("(").append(paramClassName).append(")'").append(", param)\n");
                sb.append("}//..method\n");//method
            }
        }

        sb.append("}//..class\n");//class

        return sb.toString();
    }


    private static boolean isIgnoreMethod(Method m){
        if(m.getName().indexOf("super$")>-1){
            return true;
        }
        for(String s:IGNORE_METHODS){
            if(s.equals(m.getName()))
                    return true;
        }
        return false;
    }

}
