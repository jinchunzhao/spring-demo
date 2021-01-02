package com.jy.ioc.utils;


import org.apache.commons.collections.ArrayStack;
import org.apache.commons.collections4.CollectionUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 获取包内所有带自定义注解的类
 *
 * @author jinchunzhao
 * @version 1.0
 * @date 2021-01-02 21:43
 */
public class ClassUtil {

    /**
     * 通过包名获取包内所有类
     *
     * @param pkg
     * @return
     * @throws Exception
     */
    public static List<Class<?>> getAllClassByPackageName(Package pkg) throws Exception{
        String packageName = pkg.getName();
        //获取当前包下以及子包下所有的类
        List<Class<?>> reClassList = getClasses(packageName);
        return reClassList;
    }

    /**
     * 通过接口名获取某个接口下所有的实现类
     *
     * @param c
     * @return
     */
    public static List<Class<?>> getAllClassByInterface(Class<?> c){
        List<Class<?>> returnClassList = null;

        if (c.isInterface()){
            //获取当前的包名
            String packageName = c.getPackage().getName();
            //获取当前包下以及子包下所有的类
            List<Class<?>> allClassList = getClasses(packageName);
            if (CollectionUtils.isNotEmpty(allClassList)){
                returnClassList = new ArrayList<>();
                for (Class< ? > allClass : allClassList) {
                    //判断是否是同一个接口
                    if (c.isAssignableFrom(allClass)) {
                        //本身不加入进去
                        if (Objects.equals(c,allClass)){
                            returnClassList.add(allClass);
                        }
                    }
                }
            }
        }
        return returnClassList;
    }

    /**
     * 获取某一个类所在包的所有类名，不含迭代
     *
     * @param classLocation
     * @param packageName
     * @return
     */
    public static String[] getPackageAllClassName(String classLocation,String packageName){
        //将packageName分解
        String[] packagePathSplit = packageName.split("[.]");
        String realClassLocation = classLocation;
        int packageLength = packagePathSplit.length;
        for (int i = 0; i < packageLength; i++) {
            realClassLocation = realClassLocation + File.separator + packagePathSplit[i];
        }
        File packageFile = new File(realClassLocation);
        if (packageFile.isDirectory()){
            String[] allClassName = packageFile.list();
            return allClassName;
        }
        return null;
    }

    /**
     * 从包package中获取所有的class
     *
     * @param packageName
     * @return
     */
    private static List<Class<?>> getClasses(String packageName) {
        //第一个class类的集合
        List<Class<?>> classes = new ArrayList<>();
        //是否循环迭代
        boolean recursive = true;

        //获取包的名字，并进行替换
        String packageFileName = packageName.replace('.', '/');
        //定义一个枚举的集合，并进行循环来处理这个目录
        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageFileName);
            //循环迭代下去
            while (dirs.hasMoreElements()) {
                //获取下一个元素
                URL url = dirs.nextElement();
                //获取协议的名字
                String protocol = url.getProtocol();
                //如果是以文本的形成保存在服务器上
                if (Objects.equals("file",protocol)){
                    //获取包的物理路径
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    //以文件的方式扫描整个包下的文件，并添加到集合中
                    getAndAddClassesInPackageByFile(packageName,filePath,recursive,classes);
                }else if(Objects.equals("jar",protocol)){
                    //如果是jar包文件，定义一个JarFile
                    JarFile jarFile;
                    //获取jar文件
                    jarFile = ((JarURLConnection)url.openConnection()).getJarFile();
                    //从当前jar包中得到一个枚举类
                    Enumeration<JarEntry> entries = jarFile.entries();
                    //循环迭代
                    while (entries.hasMoreElements()) {
                        //获取jar里的一个实体，可以是目录和一些jar包里的其他文件，例如：META-INF等文件
                        JarEntry jarEntry = entries.nextElement();
                        String jarEntryName = jarEntry.getName();
                        //如果是以/开头的
//                        if (jarEntryName.charAt(0) == '/'){}
                        if (jarEntryName.startsWith("/")){
                            //获取后面的字符串
                            jarEntryName = jarEntryName.substring(1);
                        }
                        //如果前半部分和定义的包名相同
                        if (jarEntryName.startsWith(packageFileName)){
                            int idx = jarEntryName.lastIndexOf('/');
                            //如果以“/”结尾是一个包
                            if (idx != -1){
                                //获取包名，并且把“/”替换成“.”
                                jarEntryName.substring(0,idx).replace('/','.');
                            }
                            //如果可以迭代下去，并且是一个包
                            if ((idx != -1) || recursive){
                                //如果是一个.class文件，而且不是目录
                                if (jarEntryName.endsWith(".class") && !jarEntry.isDirectory()){
                                    //去掉后面的“.class”,获取真正的类名
                                    String className = jarEntryName.substring(packageName.length() + 1, jarEntryName.length() - 6);
                                    try {
                                        //添加到classes
                                        classes.add(Class.forName(packageName + '.' +className));
                                    } catch (ClassNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return classes;
    }

    /**
     * 以文件的形式来获取包下的所有class
     *
     * @param packageName
     * @param packagePath
     * @param recursive
     * @param classes
     */
    private static void getAndAddClassesInPackageByFile(String packageName, String packagePath, boolean recursive, List<Class<?>> classes) {

        //获取当前包的目录，建立一个file
        File diFile = new File(packagePath);
        //如果不存在获取也不是目录则直接返回
        if (!diFile.exists() || !diFile.isDirectory()){
            return;
        }
        //如果存在就获取包下的所有文件包括目录
        File[] files = diFile.listFiles(new FileFilter() {
            //自定义过滤规则，如果可以循环（包含子目录）或则是以.class结果的文件（编译好的java类文件）
            @Override
            public boolean accept(File pathname) {
                return (recursive && diFile.isDirectory()) || (diFile.getName().endsWith(".class"));
            }
        });

        //循环所有文件
        for (File file : files) {
            //如果是目录则继续扫描
            if (file.isDirectory()) {
                getAndAddClassesInPackageByFile(packageName + "." + file.getName(),file.getAbsolutePath(),recursive,classes);
            }else{
                //如果是java类文件，去掉后面的.class只留下类名
                String className = file.getName().substring(0, file.getName().length() - 6);
                try {
                    //添加到集合中去
                    classes.add(Class.forName(packageName + '.' + className));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
