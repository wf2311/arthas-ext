package com.wf2311.arthas.tunnel.filter;

import com.wf2311.arthas.tunnel.filter.headers.ForwardedHeadersFilter;
import com.wf2311.arthas.tunnel.filter.headers.HttpHeadersFilter;
import com.wf2311.arthas.tunnel.filter.headers.RemoveHopByHopHeadersFilter;
import com.wf2311.arthas.tunnel.filter.headers.XForwardedHeadersFilter;
import com.alibaba.arthas.tunnel.server.AgentInfo;
import com.alibaba.arthas.tunnel.server.TunnelServer;
import com.alibaba.arthas.tunnel.server.app.configuration.ArthasProperties;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import org.springframework.web.reactive.socket.server.WebSocketService;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.util.UriUtils;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.springframework.util.StringUtils.commaDelimitedListToStringArray;

/**
 * @author <a href="mailto:wf2311@163.com">wf2311</a>
 * @since 2021/7/21 17:58.
 */
@Slf4j
@Component
public class WebsocketWebFilter implements WebFilter {

    /**
     * Sec-Websocket protocol.
     */
    public static final String SEC_WEBSOCKET_PROTOCOL = "Sec-WebSocket-Protocol";

    private static final Pattern PATTERN = Pattern.compile("^/arthas/(.+)/ws$");

    @Resource
    private WebSocketClient webSocketClient;

    @Resource
    private WebSocketService webSocketService;
    @Resource
    private TunnelServer tunnelServer;
    @Resource
    private ArthasProperties arthasProperties;
    private volatile List<HttpHeadersFilter> headersFilters;


    private AgentInfo findArthasAgent(URI uri) {
        String path = uri.getPath();
        Matcher matcher = PATTERN.matcher(path);
        if (!matcher.find()) {
            return null;
        }
        String serviceId = matcher.group(1);
        return tunnelServer.getAgentInfoMap().get(serviceId);
    }

    private URI getForwardUrl(URI uri, ArthasProperties arthasProperties) {
        ArthasProperties.Server server = arthasProperties.getServer();
        String query = UriUtils.encodeQuery(uri.getQuery(), CharsetUtil.UTF_8.name());
        String url =   "ws://" + server.getClientConnectHost() + ":" + server.getPort() + "/ws";
        if (!Strings.isNullOrEmpty(url)) {
            url += "?" + query;
        }
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            return null;
        }
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain)  {

        URI uri = exchange.getRequest().getURI();
        AgentInfo agentInfo = findArthasAgent(uri);
        if (agentInfo==null) {
            return chain.filter(exchange);
        }

        HttpHeaders headers = exchange.getRequest().getHeaders();
        HttpHeaders filtered = HttpHeadersFilter.filterRequest(getHeadersFilters(), exchange);

        List<String> protocols = headers.get(SEC_WEBSOCKET_PROTOCOL);
        if (protocols != null) {
            protocols = headers.get(SEC_WEBSOCKET_PROTOCOL).stream()
                    .flatMap(header -> Arrays.stream(commaDelimitedListToStringArray(header))).map(String::trim)
                    .collect(Collectors.toList());
        }

        URI requestUrl = getForwardUrl(uri, arthasProperties);
        return this.webSocketService.handleRequest(exchange,
                new WebsocketWebFilter.ProxyWebSocketHandler(requestUrl, this.webSocketClient, filtered, protocols));
    }

    List<HttpHeadersFilter> getHeadersFilters() {
        if (this.headersFilters == null) {
            this.headersFilters = Lists.newArrayList(new ForwardedHeadersFilter(),new XForwardedHeadersFilter(),new RemoveHopByHopHeadersFilter());

            // remove host header unless specifically asked not to
            headersFilters.add((headers, exchange) -> {
                HttpHeaders filtered = new HttpHeaders();
                filtered.addAll(headers);
                filtered.remove(HttpHeaders.HOST);
                return filtered;
            });

            headersFilters.add((headers, exchange) -> {
                HttpHeaders filtered = new HttpHeaders();
                headers.entrySet().stream().filter(entry -> !entry.getKey().toLowerCase().startsWith("sec-websocket"))
                        .forEach(header -> filtered.addAll(header.getKey(), header.getValue()));
                return filtered;
            });
        }

        return this.headersFilters;
    }


    public static class ProxyWebSocketHandler implements WebSocketHandler {

        private final WebSocketClient client;

        private final URI url;

        private final HttpHeaders headers;

        private final List<String> subProtocols;

        ProxyWebSocketHandler(URI url, WebSocketClient client, HttpHeaders headers, List<String> protocols) {
            this.client = client;
            this.url = url;
            this.headers = headers;
            if (protocols != null) {
                this.subProtocols = protocols;
            } else {
                this.subProtocols = Collections.emptyList();
            }
        }

        @Override
        public List<String> getSubProtocols() {
            return this.subProtocols;
        }

        @Override
        public Mono<Void> handle(WebSocketSession session) {
            // pass headers along so custom headers can be sent through
            return client.execute(url, this.headers, new WebSocketHandler() {
                @Override
                public Mono<Void> handle(WebSocketSession proxySession) {
                    // Use retain() for Reactor Netty
                    Mono<Void> proxySessionSend = proxySession
                            .send(session.receive().doOnNext(WebSocketMessage::retain));
                    // .log("proxySessionSend", Level.FINE);
                    Mono<Void> serverSessionSend = session
                            .send(proxySession.receive().doOnNext(WebSocketMessage::retain));
                    // .log("sessionSend", Level.FINE);
                    return Mono.zip(proxySessionSend, serverSessionSend).then();
                }

                /**
                 * Copy subProtocols so they are available downstream.
                 * @return
                 */
                @Override
                public List<String> getSubProtocols() {
                    return WebsocketWebFilter.ProxyWebSocketHandler.this.subProtocols;
                }
            });
        }

    }

}
