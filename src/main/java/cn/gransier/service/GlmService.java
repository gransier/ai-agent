package cn.gransier.service;

import cn.gransier.annotation.AgentMethod;
import cn.gransier.annotation.AgentService;
import cn.gransier.domain.response.GlmChatCompletionResponse;
import cn.gransier.enums.AgentMethods;
import reactor.core.publisher.Flux;

@AgentService("https://open.bigmodel.cn/api/paas/v4/")
public interface GlmService {

    @AgentMethod(endpoint = "/chat/completions", method = AgentMethods.POST)
    Flux<GlmChatCompletionResponse> completions(Object query);
}