package com.wf2311.arthas.tunnel.config;

import com.wf2311.arthas.tunnel.service.DelegateReactiveUserDetailsService;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;

import java.util.List;

/**
 * @author <a href="mailto:wf2311@163.com">wf2311</a>
 * @since 2021/7/28 11:58.
 */
@Configuration
@AutoConfigureAfter(ReactiveUserDetailsServiceAutoConfiguration.class)
public class AuthUserConfiguration {

    @Primary
    @Bean
    public ReactiveUserDetailsService delegateReactiveUserDetailsService(List<ReactiveUserDetailsService> services) {
        return new DelegateReactiveUserDetailsService(services);
    }
}
