package com.wf2311.arthas.tunnel.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.server.RequestUpgradeStrategy;
import org.springframework.web.reactive.socket.server.WebSocketService;
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService;
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.WebsocketClientSpec;
import reactor.netty.http.server.WebsocketServerSpec;

/**
 * @author <a href="mailto:wf2311@163.com">wf2311</a>
 * @since 2021/7/21 18:39.
 */
@Configuration
public class WebSocketConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ReactorNettyRequestUpgradeStrategy reactorNettyRequestUpgradeStrategy() {
        WebsocketServerSpec.Builder builder = WebsocketServerSpec.builder();
        return new ReactorNettyRequestUpgradeStrategy(builder);
    }

    @Bean
    @ConditionalOnMissingBean
    public HttpClient httpClient() {
        return HttpClient.create();
    }

    @Bean
    @ConditionalOnMissingBean
    public ReactorNettyWebSocketClient reactorNettyWebSocketClient(HttpClient httpClient) {
        WebsocketClientSpec.Builder builder = WebsocketClientSpec.builder();
        return new ReactorNettyWebSocketClient(httpClient, builder);
    }

    @Bean
    public WebSocketService webSocketService(RequestUpgradeStrategy requestUpgradeStrategy) {
        return new HandshakeWebSocketService(requestUpgradeStrategy);
    }
}
