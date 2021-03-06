package vanillax.framework.core.object;

import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import vanillax.framework.core.config.FrameworkConfig;

import java.io.File;

public class ScriptClassLoader extends ClassLoader{

    private final String[] ignorePackages = {
            "java.","javax.","groovy.","org.apache.",
            "vanillax.framework.core","vanillax.framework.webmvc.",
            "org.codehaus."
    };

    public ScriptClassLoader(){
        super(Thread.currentThread().getContextClassLoader());
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class clazz = null;

        try {
            //0. java. 혹은 javax.와 같이 명확하게 스크립트가 아닌 클래스는 무조건 넘긴다. Inner클래스도 넘긴다
            if (!isIgnorePackage(name) && name.indexOf("$") < 0) {
//                System.out.println("loadClass() >> finding class : "+name);//..debug line
                File file = new File(FrameworkConfig.getBasePath() + "/" + name.replaceAll("\\.", "/") + ".groovy");
                // 파일이 있는지 확인하고 없으면 기냥 넘긴다.
                if (file.exists()) {
                    //1. ObjectCache에 클래스가 있는지 확인하여 있으면
                    if (ObjectCache.getInstance().contains(name)) {
                        ObjectInfo objectInfo = ObjectCache.getInstance().get(name);
                        // 1.1 변경유무를 확인한다.
                        if (!objectInfo.isModified()) {
                            // 1.1.1 변경이 발생하지 않은 경우 클래스를 그대로 사용한다.
                            return objectInfo.getOriginClass();
                        } else {
                            // 1.1.2 변경이 발생한 경우 새로 컴파일 한다.
                            clazz = ObjectLoader.loadGroovyClass(name);
                        }
                    }
                    //2. ObjectCache에 클래스가 없으면 GroovyClassLoader를 하나 생성하여 새로 컴파일 한다.
                    else {
                        clazz = ObjectLoader.loadGroovyClass(name);
                    }
                }
            }
        }catch (MultipleCompilationErrorsException e){
            //Script 컴파일시 대상 클래스가 존재하지 않아서 import 오류가 발생할 때 상위 script에서 ClassNotFoundException으로 잡히면
            //상위 script의 import 구문 오류만 표시되고 실제 문제가된 script의 오류를 표시하지않게된다
            //그래서 최초 script의 import 오류발생시 바로 종료하게하기 위해 RuntimeException을 던진다
            throw new RuntimeException(e);
        }catch (Exception e){
            throw new ClassNotFoundException("can not found class : "+name, e);
        }
        if(clazz != null)
            return clazz;

        return super.loadClass(name, resolve);
    }

    private boolean isIgnorePackage(String className){
        if(className == null)
            return false;
        for(String s:ignorePackages){
            if(className.startsWith(s))
                return true;
        }
        return false;
    }
}


