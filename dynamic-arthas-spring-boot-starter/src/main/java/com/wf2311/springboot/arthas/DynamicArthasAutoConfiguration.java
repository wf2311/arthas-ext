package com.wf2311.springboot.arthas;

import com.alibaba.arthas.spring.ArthasProperties;
import com.taobao.arthas.agent.attach.ArthasAgent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * @author <a href="mailto:wf2311@163.com">wf2311</a>
 * @since 2021/7/21 09:17.
 */
@Configuration
@ComponentScan
public class DynamicArthasAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties(prefix = "arthas")
    public ArthasProperties arthasProperties() {
        return new ArthasProperties();
    }

    /**
     * 覆盖掉ArthasConfiguration中的
     * @return
     */
    @Bean
    @Primary
    public ArthasAgent unuseArthasAgent() {
        return new ArthasAgent();
    }

    @Bean
    public ArthasEnvironmentChangeListener arthasEnvironmentChangeListener() {
        return new ArthasEnvironmentChangeListener();
    }
}
