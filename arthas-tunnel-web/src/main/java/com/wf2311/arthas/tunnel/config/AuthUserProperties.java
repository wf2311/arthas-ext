package com.wf2311.arthas.tunnel.config;

import lombok.Data;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.Serializable;
import java.util.Set;

/**
 * @author <a href="mailto:wf2311@163.com">wf2311</a>
 * @since 2021/7/28 09:16.
 */
@ConfigurationProperties(prefix = AuthUserProperties.PREFIX)
@Configuration
@Data
public class AuthUserProperties implements Serializable {
    public static final String PREFIX = "arthas.tunnel";

    private String superAdminRoleSign;

    private Set<SecurityProperties.User> users;
}
