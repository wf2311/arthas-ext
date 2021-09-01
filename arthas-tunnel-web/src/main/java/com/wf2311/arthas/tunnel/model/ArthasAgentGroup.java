package com.wf2311.arthas.tunnel.model;

import lombok.Data;

import java.util.List;

/**
 * @author <a href="mailto:wf2311@163.com">wf2311</a>
 * @since 2021/7/20 20:08.
 */
@Data
public class ArthasAgentGroup {
    private String service;
    private List<ArthasAgent> agents;
}
