package com.wf2311.arthas.tunnel.service;

import com.google.common.base.Strings;
import com.wf2311.arthas.tunnel.config.AuthExtProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:wf2311@163.com">wf2311</a>
 * @since 2021/7/28 11:21.
 */
@Component
@Slf4j
public class PropertiesSourceMapReactiveUserDetailsService implements ReactiveUserDetailsService {

    private static final String NOOP_PASSWORD_PREFIX = "{noop}";

    private static final Pattern PASSWORD_ALGORITHM_PATTERN = Pattern.compile("^\\{.+}.*$");

    private final Map<String, UserDetails> users=new ConcurrentHashMap<>();


    @Resource
    private AuthExtProperties authExtProperties;

    @Resource
    private ObjectProvider<PasswordEncoder> passwordEncoder;

    private void reset(Collection<UserDetails> users) {
        Assert.notEmpty(users, "users cannot be null or empty");
        this.users.clear();
        log.info("users reset");
        for (UserDetails user : users) {
            String password = user.getPassword();
            if (Strings.isNullOrEmpty(password)) {
                log.warn("username= {} password is empty", user.getUsername());
                continue;
            }
            log.info("add user : name={} {}", user.getUsername(), password);
            this.users.put(getKey(user.getUsername()), user);
        }
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        String key = getKey(username);
        UserDetails result = this.users.get(key);
        return (result != null) ? Mono.just(User.withUserDetails(result).build()) : Mono.empty();
    }


    private String getKey(String username) {
        return username.toLowerCase();
    }

    @PostConstruct
    public void init() {
        updateUsers();
    }

    public void updateUsers() {
        Set<SecurityProperties.User> users = authExtProperties.getUsers();
        if (CollectionUtils.isEmpty(users)) {
            return;
        }
        Set<UserDetails> uds = users.stream().map(this::convert).collect(Collectors.toSet());
        reset(uds);
    }

    private UserDetails convert(SecurityProperties.User user) {
        List<String> roles = user.getRoles();
        return User.withUsername(user.getName()).password(getOrDeducePassword(user, passwordEncoder.getIfAvailable()))
                .roles(StringUtils.toStringArray(roles)).build();
    }


    private String getOrDeducePassword(SecurityProperties.User user, PasswordEncoder encoder) {
        String password = user.getPassword();
        if (user.isPasswordGenerated()) {
            log.info(String.format("%n%nUsing generated security password: %s%n", user.getPassword()));
        }
        if (encoder != null || PASSWORD_ALGORITHM_PATTERN.matcher(password).matches()) {
            return password;
        }
        return NOOP_PASSWORD_PREFIX + password;
    }
}
