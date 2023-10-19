package com.devhc.jobdeploy.extensions;

import com.devhc.jobdeploy.DeployContext;
import com.devhc.jobdeploy.config.structs.DeployExtension;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.Resource;
import java.lang.reflect.*;
import java.util.Map;

public class ExtensionInvocationHandler implements InvocationHandler {
    private final DeployExtension ext;
    private final ConfigurableApplicationContext context;
    private final DeployContext dCtx;
    private Object instance;
    private Map<String, Method> methods = Maps.newHashMap();

    public ExtensionInvocationHandler(DeployExtension ext, ConfigurableApplicationContext context, DeployContext dCtx) throws Throwable {
        this.dCtx = dCtx;
        this.ext = ext;
        this.context = context;
        Class<?> clsExt;
        if(ext.getLoader() != null){
            clsExt = ext.getLoader().loadClass(ext.getClassName());
        }else {
            clsExt = Class.forName(ext.getClassName());
        }
        for (Method m : clsExt.getMethods()) {
            methods.put(m.getName(), m);
        }

        Constructor<?>[] cons = clsExt.getConstructors();
        if (cons.length != 1) {
            throw new RuntimeException(ext.getName() + " has two constructor");
        }
        Constructor<?> con = cons[0];
        int argCnt = con.getParameterCount();
        if (argCnt == 0) {
            System.out.println("create " + argCnt + " args");
            this.instance = con.newInstance();
        } else {
            throw new RuntimeException(argCnt + " args construct not support");
        }
        for (Field f : FieldUtils.getAllFields(clsExt)) {
            Resource r = f.getAnnotation(Resource.class);
            if (r != null) {
                String name = StringUtils.isNotEmpty(r.name()) ? r.name() : f.getName();
                Object obj = context.getBean(name);
                f.setAccessible(true);
                f.set(instance, obj);
            }else{
                if(f.getName().equals("buildDir")){
                    f.setAccessible(true);
                    f.set(instance, dCtx.getBuildDir());
                }else if(f.getName().equals("srcDir")){
                    f.setAccessible(true);
                    f.set(instance, dCtx.getSrcDir());
                }else if(f.getName().equals("remoteTmp")){
                    f.setAccessible(true);
                    f.set(instance, dCtx.getRemoteTmp());
                }
            }
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("hasMethod")) {
            String methodName = args[0].toString();
            return methods.containsKey(methodName);
        }
        Method m = methods.get(method.getName());
        if (m == null) {
            throw new RuntimeException(method.getName() + "not support");
        }
        return m.invoke(this.instance, args);
    }
}