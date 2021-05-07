package com.wmz.hbase.plugin.spring.boot.autoconfigure;

import com.wmz.hbase.plugin.accessor.HbaseAccessor;
import com.wmz.hbase.plugin.annotation.HbaseTable;
import com.wmz.hbase.plugin.spring.scan.HbaseTableScannerConfigurer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@EnableConfigurationProperties({HbasePluginProperties.class})
@Configuration
public class HbasePluginAutoConfigure {

    @ConditionalOnMissingBean
    @Bean
    public org.apache.hadoop.conf.Configuration configuration(HbasePluginProperties properties){
        String zookeeperQuorum = properties.getZookeeperQuorum();
        Assert.hasLength(zookeeperQuorum,"config hbase.zookeeper.quorum should  is not blank");
        org.apache.hadoop.conf.Configuration configuration = HBaseConfiguration.create();
        String quorum = configuration.get("hbase.zookeeper.quorum");;
        if(StringUtils.isBlank(quorum) || quorum.equals("localhost")){
            configuration.set("hbase.zookeeper.quorum",zookeeperQuorum);
        }
        return configuration;
    }


    @Bean
    public HbaseAccessor hbaseAccessor(org.apache.hadoop.conf.Configuration configuration) throws IOException {
        HbaseAccessor hbaseAccessor = new HbaseAccessor(configuration);
        return hbaseAccessor;
    }

    @Configuration
    @Import({AutoConfiguredHbaseTableScannerRegistrar.class})
    @ConditionalOnMissingBean({HbaseTableScannerConfigurer.class})
    public static class HbaseTableScannerRegistrarNotFoundConfiguration implements InitializingBean {

        public void afterPropertiesSet() {
            log.debug("Not found configuration for scan HbaseTable Entity bean using @HTableScan.");
        }
    }

    public static class AutoConfiguredHbaseTableScannerRegistrar implements BeanFactoryAware, ImportBeanDefinitionRegistrar {
        private BeanFactory beanFactory;

        public AutoConfiguredHbaseTableScannerRegistrar() {
        }

        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
            if (!AutoConfigurationPackages.has(this.beanFactory)) {
                log.debug("Could not determine auto-configuration package, automatic HbaseTable scanning disabled.");
            } else {
                log.debug("Searching for mappers annotated with @HbaseTable");
                List<String> packages = AutoConfigurationPackages.get(this.beanFactory);
                if (log.isDebugEnabled()) {
                    packages.forEach((pkg) -> {
                        log.debug("Using auto-configuration base package '{ " + pkg + " }'");
                    });
                }
                BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(HbaseTableScannerConfigurer.class);
                builder.addPropertyValue("processPropertyPlaceHolders", true);
                builder.addPropertyValue("annotationClass", HbaseTable.class);
                builder.addPropertyValue("basePackage", org.springframework.util.StringUtils.collectionToCommaDelimitedString(packages));
                registry.registerBeanDefinition(HbaseTableScannerConfigurer.class.getName(), builder.getBeanDefinition());
            }
        }

        public void setBeanFactory(BeanFactory beanFactory) {
            this.beanFactory = beanFactory;
        }

    }

}
