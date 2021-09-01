package com.wf2311.arthas.tunnel.service;

import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author <a href="mailto:wf2311@163.com">wf2311</a>
 * @since 2021/7/28 11:41.
 */
public class DelegateReactiveUserDetailsService implements ReactiveUserDetailsService {
    private final List<ReactiveUserDetailsService> delegates;

    public DelegateReactiveUserDetailsService(List<ReactiveUserDetailsService> delegates) {
        this.delegates = delegates;
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        for (ReactiveUserDetailsService delegate : delegates) {
            UserDetails exists = delegate.findByUsername(username).block();
            if (exists != null) {
                return Mono.just(exists);
            }
        }
        return Mono.empty();
    }
}
