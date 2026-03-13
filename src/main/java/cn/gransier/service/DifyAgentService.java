package cn.gransier.service;

import cn.gransier.annotation.AgentMethod;
import cn.gransier.annotation.AgentParam;
import cn.gransier.annotation.AgentService;
import cn.gransier.domain.query.DifyChatQuery;
import cn.gransier.domain.query.DifyConversationsQuery;
import cn.gransier.domain.query.DifyMessagesQuery;
import cn.gransier.enums.AgentMethods;
import reactor.core.publisher.Flux;

@AgentService
public interface DifyAgentService {

    @AgentMethod(endpoint = "/chat-messages", method = AgentMethods.POST)
    Flux<String> completions(DifyChatQuery difyQuery);

    @AgentMethod(endpoint = "/conversations", method = AgentMethods.GET)
    Object conversations(DifyConversationsQuery difyQuery);

    @AgentMethod(endpoint = "/messages", method = AgentMethods.GET)
    Object messages(DifyMessagesQuery difyQuery);

    @AgentMethod(endpoint = "/conversations/{conversation_id}", method = AgentMethods.DELETE)
    void deleteConversations(@AgentParam("conversation_id") String conversationId);
}