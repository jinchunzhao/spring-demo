package com.jy.mvc.servlet;

import com.jy.mvc.annotation.JyController;
import com.jy.mvc.annotation.JyRequestMapping;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * 自定义DispatcherServlet前置控制器
 * 
 * @author jinchunzhao
 * @version 1.0
 * @date 2021-01-09 13:44
 */
public class JyDispatcherServlet extends HttpServlet {

    private Properties properties = new Properties();

    private List<String> classNames = new ArrayList<>();

    private Map<String,Object> ioc = new HashMap<>();

    private Map<String, Method> handlerMap = new HashMap<>();

    private Map<String,Object> controllerMap = new HashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doDispatch(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init();
        //加载配置文件
        doLoadConfig(servletConfig.getInitParameter("contextConfigLocation"));
        //扫描类
        doScanner(properties.getProperty("scanPackage"));
        //拿到扫描的类后，通过反射机制实例化，并发放到ioc容器（key - > value） map
        doInstance();
        //初始化HandlerMapping(url[login] -> method(login()))
        initHandlerMapping();
    }

    /**
     * 获取servlet初始化时配置的配置文件
     *
     * @param location
     */
    private void doLoadConfig(String location){
        //把web.xml中的contextConfigLocation对应value值的文件加载到流中
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(location);

        try {
            //用Properties文件加载文件中的内容
            properties.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (resourceAsStream != null) {
                try {
                    resourceAsStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 递归扫描包
     *
     * @param packageName
     *        包名
     */
    private void doScanner(String packageName){
        URL url = this.getClass().getClassLoader().getResource("/"+packageName.replaceAll("\\.","/"));
        File dir = new File(url.getFile());
        for (File file : dir.listFiles()) {
            if (file.isDirectory()){
                doScanner(packageName+"."+file.getName());
            }else {
                String className = packageName+"."+file.getName().replaceAll(".class","");
                classNames.add(className);
            }
        }
    }

    /**
     * 实例化
     */
    private void doInstance(){
        for (String className : classNames){
            try {
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(JyController.class)){
                    ioc.put(toLowerFirstWord(clazz.getSimpleName()),clazz.newInstance());
                }else{
                    continue;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 把字符串的首字母转换为小写
     *
     * @param str
     *        字符串
     * @return
     *        转换后的字符串
     */
    private String toLowerFirstWord(String str){
        char[] chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    /**
     * 初始化
     */
    private void initHandlerMapping(){
        try {
            for (Map.Entry entry : ioc.entrySet()){
                Class<?> clazz = entry.getValue().getClass();
                if (!clazz.isAnnotationPresent(JyController.class)){
                    continue;
                }
                //拼接url时，是controller头上的url加上方法上的url
                String baseUrl = "";
                if (clazz.isAnnotationPresent(JyRequestMapping.class)){
                    JyRequestMapping annotation = clazz.getAnnotation(JyRequestMapping.class);
                    baseUrl = annotation.value();
                }
                Method[] methods = clazz.getMethods();
                for (Method method : methods) {
                    if (!method.isAnnotationPresent(JyRequestMapping.class)){
                        continue;
                    }
                    JyRequestMapping annotation = method.getAnnotation(JyRequestMapping.class);
                    String url = annotation.value();
                    url = (baseUrl+"/" +url).replaceAll("/+","/");
                    handlerMap.put(url,method);
                    controllerMap.put(url,clazz.newInstance());

                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void doDispatch(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (handlerMap.isEmpty()){
            return;
        }
        String url = request.getRequestURI();
        String contextPath = request.getContextPath();

        //拼接url，并把多个/替换成一个
        url = url.replace(contextPath, "").replaceAll("/+", "/");

        if (!this.handlerMap.containsKey(url)){
            response.getWriter().write("404 NOT FOUND!");
            return;
        }
        Method method = this.handlerMap.get(url);
        //获取方法中的参数列表
        Class<?>[] parameterTypes = method.getParameterTypes();
        //获取请求中的参数
        Map<String, String[]> parameterMap = request.getParameterMap();
        //保持参数值
        Object[] paramValues = new Object[parameterTypes.length];
        //方法的参数列表
        for (int i = 0; i < parameterTypes.length; i++) {
            //根据参数名称，做处理
            String requestParam = parameterTypes[i].getSimpleName();
            if (Objects.equals(requestParam,"HttpServletRequest")){
                //明确参数类型，进行强制转换
                paramValues[i] = request;
                continue;
            }
            if (Objects.equals(requestParam,"HttpServletResponse")){
                paramValues[i] = response;
                continue;
            }
            if (Objects.equals(requestParam,"String")){
                for (Map.Entry<String,String[]> param : parameterMap.entrySet()){
                    String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]","");
                    paramValues[i] = value;
                }
            }
        }
        //利用反射机制
        //第一个参数是在ioc容器中的method所对应的实例
        try {
            method.invoke(this.controllerMap.get(url),paramValues);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
