package com.wf2311.arthas.tunnel.controller;

import com.alibaba.arthas.tunnel.server.AgentInfo;
import com.alibaba.arthas.tunnel.server.TunnelServer;
import com.alibaba.arthas.tunnel.server.app.configuration.ArthasProperties;
import com.google.common.base.Strings;
import com.wf2311.arthas.tunnel.config.AuthExtProperties;
import com.wf2311.arthas.tunnel.model.ArthasAgent;
import com.wf2311.arthas.tunnel.model.ArthasAgentGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:wf2311@163.com">wf2311</a>
 * @since 2021/4/15 14:21.
 */
@RequestMapping("/api")
@RestController
public class ApiController {
    @Value("${arthas.agent.split:@}")
    private String arthasAgentSplit;
    @Autowired
    private TunnelServer tunnelServer;
    @Autowired
    private ArthasProperties arthasProperties;
    @Autowired
    private AuthExtProperties authExtProperties;
    @Resource
    private ReactiveUserDetailsService userDetailsService;

    private Set<String> getCurrentUserRole(String userName) {
        if (Strings.isNullOrEmpty(userName)) {
            return Collections.emptySet();
        }

        UserDetails u = userDetailsService.findByUsername(userName).block();
        if (u == null) {
            return Collections.emptySet();
        }
        return u.getAuthorities().stream().filter(g -> g.getAuthority() != null)
                .map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
    }

    @GetMapping("/arthas/html/title")
    public String getHtmlTitle() {
        return authExtProperties.getHtmlTitle();
    }

    /**
     * 获取tunnel server自身的相关信息(ip、port)
     *
     * @return
     */
    @GetMapping("/arthas/server")
    public ArthasProperties.Server getTunnelServerInfo() {
        return arthasProperties.getServer();
    }

    /**
     * 获取arthas agentId中应用名和随机值的分隔符
     *
     * @return
     */
    @GetMapping("/arthas/agent/split")
    public String getArthasAgentSplit() {
        return arthasAgentSplit;
    }

    /**
     * 获取当前用户可以访问的 arthas agents列表
     *
     * @return
     */
    @GetMapping(value = "/arthas/access/agents")
    public List<ArthasAgentGroup> getAgents(Principal principal) {
        Set<String> roles = getCurrentUserRole(principal.getName());
        if (roles.isEmpty()) {
            return Collections.emptyList();
        }
        boolean isSuperUser = isSuperAdmin(roles);

        Map<String, AgentInfo> agentInfoMap = tunnelServer.getAgentInfoMap();
        Map<String, List<ArthasAgent>> map = new HashMap<>(16);
        agentInfoMap.forEach((k, v) -> {
            String[] split = k.split(arthasAgentSplit, 2);
            String appName = split[0];
            if (isSuperUser || accessApp(roles, appName)) {
                List<ArthasAgent> agents = map.computeIfAbsent(appName, k1 -> new ArrayList<>());
                ArthasAgent agent = new ArthasAgent();
                agent.setId(split[1]);
                agent.setInfo(v);
                agents.add(agent);
            }
        });
        List<ArthasAgentGroup> groups = new ArrayList<>();
        map.forEach((k, v) -> {
            ArthasAgentGroup group = new ArthasAgentGroup();
            group.setAgents(v);
            group.setService(k);
            groups.add(group);
        });
        return groups;
    }

    private boolean isSuperAdmin(Set<String> roles) {
        return accessApp(roles, authExtProperties.getSuperAdminRoleSign());
    }

    private boolean accessApp(Set<String> roles, String appName) {
        for (String role : roles) {
            if (role.endsWith(appName)) {
                return true;
            }
        }
        return false;
    }
}
