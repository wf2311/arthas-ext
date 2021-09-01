package com.wf2311.springboot.arthas;

import com.alibaba.arthas.spring.ArthasProperties;
import com.alibaba.arthas.spring.StringUtils;
import com.taobao.arthas.agent.attach.ArthasAgent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class ArthasEnvironmentChangeListener implements ApplicationListener<EnvironmentChangeEvent> {

    public static final AtomicBoolean REGISTER_STATE = new AtomicBoolean(false);

    @Autowired
    private Environment env;

    @Autowired(required = false)
    private Map<String, String> arthasConfigMap;

    @Autowired
    private ArthasProperties arthasProperties;

    @Autowired
    private ApplicationContext applicationContext;

    @Value("${spring.arthas.enabled:false}")
    private boolean enabled;

    @PostConstruct
    public void init() {
        if (enabled) {
            registerArthas();
        }
    }

    @Override
    public void onApplicationEvent(EnvironmentChangeEvent event) {
        Set<String> keys = event.getKeys();
        for (String key : keys) {
            if ("spring.arthas.enabled".equals(key)) {
                if ("true".equals(env.getProperty(key))) {
                    registerArthas();
                } else {
                    destoryArthas();
                }
            }
        }
    }

    private void registerArthas() {
        if (!REGISTER_STATE.compareAndSet(false, true)) {
            log.info("arthas already registered");
            return;
        }
        log.info("arthas register ...");
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        String bean = "arthasAgent";

        if (defaultListableBeanFactory.containsBean(bean)) {
            ((ArthasAgent) defaultListableBeanFactory.getBean(bean)).init();
            return;
        }
        defaultListableBeanFactory.registerSingleton(bean, arthasAgentInit());
    }

    private void destoryArthas() {
        if (!REGISTER_STATE.compareAndSet(true, false)) {
            log.info("arthas already destory");
            return;
        }
        log.info("arthas destory ...");
        DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        String bean = "arthasAgent";
        if (defaultListableBeanFactory.containsBean(bean)) {
            defaultListableBeanFactory.destroySingleton(bean);
        }
    }

    private ArthasAgent arthasAgentInit() {
        Map<String, String> mapWithPrefix = null;
        if (arthasConfigMap != null) {
            arthasConfigMap = StringUtils.removeDashKey(arthasConfigMap);
            // 给配置全加上前缀
            mapWithPrefix = new HashMap<>(arthasConfigMap.size());
            for (Map.Entry<String, String> entry : arthasConfigMap.entrySet()) {
                mapWithPrefix.put("arthas." + entry.getKey(), entry.getValue());
            }
        }
        final ArthasAgent arthasAgent = new ArthasAgent(mapWithPrefix, arthasProperties.getHome(),
                arthasProperties.isSlientInit(), null);
        arthasAgent.init();
        return arthasAgent;

    }

}

