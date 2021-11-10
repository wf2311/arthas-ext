package com.wf2311.arthas.tunnel.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;

import java.net.URI;


/**
 * @author <a href="mailto:wf2311@163.com">wf2311</a>
 * @since 2021/10/12 10:51.
 */
@EnableWebFluxSecurity
@Slf4j
@Configuration
public class SecurityConfiguration {

//    @Bean
//    public CorsWebFilter corsWebFilter() {
//        var corsConfiguration = new CorsConfiguration();
//        corsConfiguration.setAllowedOrigins(List.of("*"));
//        var source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", corsConfiguration);
//        return new CorsWebFilter(source);
//    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .authorizeExchange()
                .pathMatchers("/login*",
                        "/logout**",
                        "/favicon.ico*",
                        "/actuator/**",
                        "/actuator",
                        "/api/test",
                        "/*.css","/*.png","/*.js","/*.jpg","/*.ico",
                        "/static/**")
                    .permitAll()
                .pathMatchers("/**")
                    .authenticated()
                .and()
                .httpBasic().disable()
                .formLogin()
//                    .loginPage("/login")
                    .authenticationSuccessHandler(new RedirectServerAuthenticationSuccessHandler("/"))
                .and()
                .logout()
                    .logoutUrl("/logout")
                    .logoutSuccessHandler(logoutSuccessHandler("/"))
                .and()
                .build();

    }
    public ServerLogoutSuccessHandler logoutSuccessHandler(String uri) {
        RedirectServerLogoutSuccessHandler successHandler = new RedirectServerLogoutSuccessHandler();
        successHandler.setLogoutSuccessUrl(URI.create(uri));
        return successHandler;
    }
}
