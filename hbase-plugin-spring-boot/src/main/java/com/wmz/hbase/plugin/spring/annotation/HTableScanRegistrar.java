package com.wmz.hbase.plugin.spring.annotation;

import com.wmz.hbase.plugin.spring.boot.autoconfigure.HbasePluginAutoConfigure;
import com.wmz.hbase.plugin.spring.scan.HbaseTableScannerConfigurer;
import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description:
 * @create: 2020-05-29
 **/
@AutoConfigureAfter(HbasePluginAutoConfigure.class)
@Slf4j
public class HTableScanRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {


    private ResourceLoader resourceLoader;
    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes hTableScanAttrs = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(HTableScan.class.getName()));
        this.registerBeanDefinitions(hTableScanAttrs, registry);
        /*if (hTableScanAttrs != null) {

        }else{
            //只扫描启动类包路径下的类
            String basePackage = getDefaultBootBasePackage();
            List<String> list = new ArrayList<>(1);
            if(!StringUtils.isEmpty(basePackage)){
                list.add(basePackage);
            }
            scan(list);
        }*/
    }



   /* private String getDefaultBootBasePackage(){
        Class mainApplicationClass = deduceMainApplicationClass();
        if(mainApplicationClass == null){
            log.error("deduceMainApplicationClass is null");
            return null;
        }
        return mainApplicationClass.getPackage().getName();
    }

    private Class<?> deduceMainApplicationClass() {
        try {
            StackTraceElement[] stackTrace = new RuntimeException().getStackTrace();
            for (StackTraceElement stackTraceElement : stackTrace) {
                if ("main".equals(stackTraceElement.getMethodName())) {
                    return Class.forName(stackTraceElement.getClassName());
                }
            }
        }
        catch (ClassNotFoundException ex) {
        }
        return null;
    }*/

    void registerBeanDefinitions(AnnotationAttributes annoAttrs, BeanDefinitionRegistry registry) {

        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(HbaseTableScannerConfigurer.class);
        builder.addPropertyValue("processPropertyPlaceHolders", true);

        Class<? extends Annotation> annotationClass = annoAttrs.getClass("annotationClass");
        if (!Annotation.class.equals(annotationClass)) {
            builder.addPropertyValue("annotationClass", annotationClass);
        }

        List<String> basePackages = new ArrayList<>();
        basePackages.addAll(Arrays.stream(annoAttrs.getStringArray("value")).filter(StringUtils::hasText).collect(Collectors.toList()));
        basePackages.addAll(Arrays.stream(annoAttrs.getStringArray("basePackages")).filter(StringUtils::hasText).collect(Collectors.toList()));
        basePackages.addAll(Arrays.stream(annoAttrs.getClassArray("basePackageClasses")).map(ClassUtils::getPackageName).collect(Collectors.toList()));

        builder.addPropertyValue("basePackage", StringUtils.collectionToCommaDelimitedString(basePackages));
        registry.registerBeanDefinition(HbaseTableScannerConfigurer.class.getName(), builder.getBeanDefinition());
    }





    static class RepeatingRegistrar extends HTableScanRegistrar {
        RepeatingRegistrar() {
        }

        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
            AnnotationAttributes hTableScansAttrs = AnnotationAttributes.fromMap(importingClassMetadata.getAnnotationAttributes(HTableScans.class.getName()));
            if (hTableScansAttrs != null) {
                Arrays.stream(hTableScansAttrs.getAnnotationArray("value")).forEach((hTableScansAttr) -> {
                    this.registerBeanDefinitions(hTableScansAttr, registry);
                });
            }

        }
    }
}
