package cn.gransier.controller;

import cn.gransier.common.conversation.domain.ChatRequest;
import cn.gransier.common.conversation.domain.Conversation;
import cn.gransier.common.conversation.domain.Message;
import cn.gransier.common.domain.ApiResult;
import cn.gransier.service.ConversationalGlmService;
import cn.gransier.domain.response.GlmChatResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Api(tags = "GLM多轮对话接口")
@Slf4j
@RestController
@RequestMapping("/ai-cloud/chat/glm")
public class GlmConversationController {

    private final ConversationalGlmService conversationalGlmService;

    public GlmConversationController(ConversationalGlmService conversationalGlmService) {
        this.conversationalGlmService = conversationalGlmService;
    }

    @ApiOperation("多轮对话聊天")
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<GlmChatResponse> chat(@RequestBody ChatRequest request) {
        return conversationalGlmService.chat(request);
    }

    @ApiOperation("带工具调用的多轮对话")
    @PostMapping(value = "/chat/tools", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<GlmChatResponse> chatWithTools(@RequestBody ChatRequest request, 
                                               @RequestBody Map<String, Object> tools) {
        return conversationalGlmService.chatWithTools(request, tools);
    }

    @ApiOperation("获取用户的会话列表")
    @GetMapping("/conversations")
    public ApiResult<List<Conversation>> getConversations(@RequestParam String userId) {
        return conversationalGlmService.getConversations(userId);
    }

    @ApiOperation("获取会话详情")
    @GetMapping("/conversations/{conversationId}")
    public ApiResult<Conversation> getConversation(@PathVariable String conversationId) {
        return conversationalGlmService.getConversation(conversationId);
    }

    @ApiOperation("删除会话")
    @DeleteMapping("/conversations/{conversationId}")
    public ApiResult<String> deleteConversation(@PathVariable String conversationId) {
        return conversationalGlmService.deleteConversation(conversationId);
    }

    @ApiOperation("获取会话消息历史")
    @GetMapping("/conversations/{conversationId}/messages")
    public ApiResult<List<Message>> getMessages(@PathVariable String conversationId) {
        return conversationalGlmService.getMessages(conversationId);
    }

    @ApiOperation("清空会话历史")
    @DeleteMapping("/conversations/{conversationId}/messages")
    public ApiResult<String> clearHistory(@PathVariable String conversationId) {
        return conversationalGlmService.clearHistory(conversationId);
    }

    @ApiOperation("压缩上下文")
    @PostMapping("/conversations/{conversationId}/compress")
    public ApiResult<String> compressContext(@PathVariable String conversationId) {
        return conversationalGlmService.compressContext(conversationId);
    }
}
