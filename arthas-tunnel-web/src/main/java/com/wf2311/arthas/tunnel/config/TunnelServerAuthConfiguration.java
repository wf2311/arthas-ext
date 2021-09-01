package com.wf2311.arthas.tunnel.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author <a href="mailto:wf2311@163.com">wf2311</a>
 * @since 2021/7/22 17:27.
 */
@Configuration
@ComponentScan({"com.alibaba.arthas.tunnel.server.app.configuration"})
@EnableCaching
public class TunnelServerAuthConfiguration {

}
