package com.jy.ioc.context;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通过xml的方式创建容器
 *
 * @author jinchunzhao
 * @version 1.0
 * @date 2021-01-01 21:50
 */
public class JyClassPathXmlApplicationContext {
    /**
     * 用来存储bean
     */
    private static final Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256);

    /**
     * xml路径
     */
    private String xmlPath;

    public JyClassPathXmlApplicationContext(String xmlPath) {
        this.xmlPath = xmlPath;
    }

    /**
     * 加载bean
     *
     * @param beanId beanId
     * @return bean
     */
    public Object getBean(String beanId) throws Exception {
        if (Objects.isNull(beanId)) {
            throw new IllegalArgumentException("beanId不能为空！");
        }
        if (singletonObjects.containsKey(beanId)) {
            return singletonObjects.get(beanId);
        }
//        "classpath:applicationContext.xml"
        List<Element> elements = readXml(xmlPath);
        if (CollectionUtils.isEmpty(elements)) {
            throw new Exception("配置文件id为空");
        }
        String className = getElementClass(elements, beanId);
        if (StringUtils.isBlank(className)) {
            throw new Exception("配置文件class，无效");
        }
        return newInstance(className);
    }

    /**
     * 读取xml，或者xml中所有的节点信息
     *
     * @param xmlPath xml路径
     * @return 节点信息集合
     * @throws DocumentException
     */
    public List<Element> readXml(String xmlPath) throws DocumentException {
        SAXReader saxReader = new SAXReader();
        Document document = saxReader.read(getResourceAsStream(xmlPath));
        Element rootElement = document.getRootElement();
        return rootElement.elements();
    }

    /**
     * 根据路径或者当前流
     *
     * @param xmlPath xml路径
     * @return 流
     */
    private InputStream getResourceAsStream(String xmlPath) {
        return this.getClass().getClassLoader().getResourceAsStream(xmlPath);
    }

    /**
     * 获取节点上的class
     *
     * @param elements 节点信息集合
     * @param beanId   beanId
     * @return class
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws ClassNotFoundException
     */
    private String getElementClass(List<Element> elements, String beanId) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        for (Element element : elements) {
            String xmlBeanId = element.attributeValue("id");
            if (Objects.equals(xmlBeanId, beanId)) {
                continue;
            }
            String xmlClass = element.attributeValue("class");
            singletonObjects.put(beanId, newInstance(xmlClass));
            return xmlClass;
        }
        return null;
    }

    /**
     * 根据className 获取对象
     *
     * @param className className
     * @return 对象
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private Object newInstance(String className) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class<?> aClass = Class.forName(className);
        return aClass.newInstance();
    }

}
