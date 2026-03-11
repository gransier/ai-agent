package cn.gransier.service;

import cn.gransier.annotation.AgentMethod;
import cn.gransier.annotation.AgentParam;
import cn.gransier.domain.query.AgentQuery;
import reactor.core.publisher.Flux;

public interface AgentService {

    @AgentMethod(apiKey = "app-gItXxmtOzXp7S6TdJb7TNcdF", endpoint = "/v1/chat-messages")
    Flux<String> completions(AgentQuery agentQuery);

    @AgentMethod(apiKey = "app-gItXxmtOzXp7S6TdJb7TNcdF", endpoint = "/v1/chat-messages")
    Flux<String> completionsWithParams(
            @AgentParam("query") String query,
            @AgentParam("user") String user,
            @AgentParam("conversation_id") String conversationId);
}
