package com.wf2311.arthas.tunnel.model;

import com.alibaba.arthas.tunnel.server.AgentInfo;
import lombok.Data;

/**
 * @author <a href="mailto:wf2311@163.com">wf2311</a>
 * @since 2021/7/20 20:07.
 */
@Data
public class ArthasAgent {
    private String id;
    private AgentInfo info;
}
