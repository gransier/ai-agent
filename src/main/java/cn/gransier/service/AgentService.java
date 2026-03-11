package cn.gransier.service;

import cn.gransier.domain.query.AgentQuery;
import reactor.core.publisher.Flux;

public interface AgentService {

    Flux<String> completions(AgentQuery agentQuery);
}
