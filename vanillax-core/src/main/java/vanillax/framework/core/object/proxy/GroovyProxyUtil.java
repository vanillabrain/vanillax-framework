package vanillax.framework.core.object.proxy;

import vanillax.framework.core.db.Transactional;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * GroovyObject의 Proxy소스를 생성한다.
 * 아이디어가 환상이다.
 */
public class GroovyProxyUtil {
    private static final String[] IGNORE_METHODS = {"getProperty","setProperty","getMetaClass","setMetaClass","invokeMethod"};

    public static String makeProxySrc(Class clazz)throws Exception{
        String className = clazz.getSimpleName()+"_proxy"+System.currentTimeMillis();
        return makeProxySrc(clazz, className);
    }

    public static String makeProxySrc(Class clazz, String className)throws Exception{
        StringBuffer sb = new StringBuffer();
        Set<String> dupCheckSet = new HashSet<String>();

        sb.append("/* This source generated by SYSTEM. */\n");
        String name = clazz.getName();
        if(clazz.getPackage() != null){
            String packageName = clazz.getPackage().getName();
            if(!"".equals(packageName)){
                sb.append("package "+packageName+"\n");
            }
        }

        if(className == null)
            className = clazz.getSimpleName()+"_proxy"+System.currentTimeMillis();

        sb.append("public class "+className+" extends "+name+" {\n");

        Constructor[] constructors = clazz.getConstructors();

        for(Constructor c:constructors){
            if(Modifier.isPublic(c.getModifiers())){
                String checkString = className + "/" + c.getParameterCount();
                if(dupCheckSet.contains(checkString)){
                    continue;
                }
                dupCheckSet.add(checkString);
                sb.append("public "+className+" ");
                Class[] exceptions = c.getExceptionTypes();
                if(exceptions != null && exceptions.length > 0){
                    sb.append("throws ");
                }
                for(int i = 0; i < exceptions.length; i++){
                    sb.append(exceptions[i].getName());
                    if(i < exceptions.length -1){
                        sb.append(", ");
                    }
                }
                Class[] paramClasses = c.getParameterTypes();
                sb.append("(");
                int paramCnt = c.getParameterCount();
                for(int i=0; i < paramCnt;i++){
                    Class paramClass = paramClasses[i];
                    String paramClassName = paramClass.getCanonicalName();
                    //java.lang.Object가 아닌 경우 파라미터 클래스명을 명시해준다. 그렇지 않으면 모두 java.lang.Object로 인식한다.
                    //스크립트상의 Overroading오류를 피하기 위함이다.
                    if(!"java.lang.Object".equals(paramClassName)){
                        sb.append(paramClassName).append(" ");
                    }
                    sb.append("param"+i);
                    if(i < paramCnt-1)
                        sb.append(", ");
                }
                sb.append(") ");//parameter

                sb.append("{\n");
                sb.append("\tsuper(");
                for(int i=0; i < paramCnt;i++){
                    sb.append("param"+i);
                    if(i < paramCnt-1)
                        sb.append(", ");
                }
                sb.append(")\n");

                sb.append("}\n");//constructor
            }
        }

        Method[] methods = clazz.getDeclaredMethods();
        dupCheckSet.clear();
        for(Method m:methods){
            if(isIgnoreMethod(m))
                continue;
            if(Modifier.isPublic(m.getModifiers())){
                String checkString = m.getName() + "/" + m.getParameterCount();
                if(dupCheckSet.contains(checkString)){
                    continue;
                }
                dupCheckSet.add(checkString);
                String returnType = m.getReturnType().getSimpleName().toLowerCase();
                String returnString = "";
                if(returnType.equals("boolean") || returnType.equals("int") || returnType.equals("integer") || returnType.equals("double")
                        || returnType.equals("short") || returnType.equals("long") || returnType.equals("float") || returnType.equals("void")){
                    if(returnType.equals("integer")){
                        returnString = "int";
                    }else{
                        returnString = returnType;
                    }
                }else if(returnType.equals("object[]")){
                    returnString = "Object[]";
                }else if(!returnType.equals("object")){
                    returnString = m.getReturnType().getName();
                }

                sb.append("def "+returnString+" "+m.getName() +" (");

                Class[] paramClasses = m.getParameterTypes();
                int paramCnt = m.getParameterCount();
                for(int i=0; i < paramCnt;i++){
                    Class paramClass = paramClasses[i];
                    String paramClassName = paramClass.getCanonicalName();
                    //java.lang.Object가 아닌 경우 파라미터 클래스명을 명시해준다. 그렇지 않으면 모두 java.lang.Object로 인식한다.
                    //스크립트상의 Overroading오류를 피하기 위함이다.
                    if(!"java.lang.Object".equals(paramClassName)){
                        sb.append(paramClassName).append(" ");
                    }
                    sb.append("param"+i);
                    if(i < paramCnt-1)
                        sb.append(", ");
                }
                sb.append(") ");

                Class[] exceptions = m.getExceptionTypes();
                if(exceptions != null && exceptions.length > 0){
                    sb.append("throws ");
                }
                for(int i = 0; i < exceptions.length; i++){
                    sb.append(exceptions[i].getName());
                    if(i < exceptions.length -1){
                        sb.append(", ");
                    }
                }

                sb.append("{\n");
                if(!"void".equals(returnType)){
                    if("".equals(returnString)){
                        sb.append("def resultValue = null\n");
                    }else{
                        sb.append(returnString + " resultValue = null\n");
                    }
                }

                //transaction 처리 closure추가
                String txString = "";
                int txType = getTxType(m);
                if(txType > -1){
                    String param = ""+isAutoCommit(m);
                    txString = GroovyTransactionHelper.class.getName() +" txHelper = new "+GroovyTransactionHelper.class.getName()+"("+param+")";
                    sb.append(txString).append("\n");
                    sb.append("txHelper.tx{\n");
//                    sb.append("println 'Yea! it is transactional!!!!'\n");//..debug line
                }
                sb.append("\t");
                if(!"void".equals(returnType)){
                    sb.append("resultValue = ");
                }

                sb.append("super."+m.getName()+"(");
                for(int i=0; i < paramCnt;i++) {
                    sb.append("param" + i);
                    if (i < paramCnt - 1)
                        sb.append(", ");
                }
                sb.append(")\n");
                if(txType > -1){
                    sb.append("}//..transaction closure\n");
                }
                if(!"void".equals(returnType)){
                    sb.append("return resultValue\n");
                }
                sb.append("}//..method\n");//method
            }
        }

        sb.append("}//..class\n");//class

        return sb.toString();
    }


    /**
     * Transaction유형확인
     * @param m
     * @return 0 : transactional, -1 : non transactional
     */
    private static int getTxType(Method m){
        if(m.isAnnotationPresent(Transactional.class)){
                return 0;
        }
        return -1;
    }

    private static boolean isAutoCommit(Method m){
        if(m.isAnnotationPresent(Transactional.class)){
            Annotation annotation = m.getAnnotation(Transactional.class);
            Transactional tx = (Transactional) annotation;
            return tx.autoCommit();
        }
        return true;//명시되어있지 않으면 auto-commit true로 간주한다.
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

    public static void main(String[] args) throws Exception{
        String s = makeProxySrc(ArrayList.class);
        System.out.println(s);
    }
}
