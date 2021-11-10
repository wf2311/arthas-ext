package com.wf2311.arthas.tunnel.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

/**
 * @author <a href="mailto:wf2311@163.com">wf2311</a>
 * @since 2021/10/12 11:14.
 */
@Configuration
public class WebConfig implements WebFluxConfigurer {


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
    }

    @Bean
    public RouterFunction<ServerResponse> indexRouter(@Value("classpath:/static/arthas.html") final Resource indexHtml) {
        return route(GET("/"), request -> ok().contentType(MediaType.TEXT_HTML).bodyValue(indexHtml));
    }
}

