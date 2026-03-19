package cn.gransier.service;

import cn.gransier.common.annotation.AgentMethod;
import cn.gransier.common.annotation.AgentParam;
import cn.gransier.common.annotation.AgentService;
import cn.gransier.common.consts.AgentConst;
import cn.gransier.domain.query.DifyChatQuery;
import cn.gransier.domain.query.DifyConversationsQuery;
import cn.gransier.domain.query.DifyMessagesQuery;
import cn.gransier.domain.response.DifyChatResponse;
import cn.gransier.common.enums.AgentMethods;
import cn.gransier.domain.response.DifyUploadFileResponse;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

@AgentService
public interface DifyAgentService {

    @AgentMethod(endpoint = "/chat-messages", method = AgentMethods.POST)
    Flux<DifyChatResponse> completions(DifyChatQuery difyQuery);

    @AgentMethod(endpoint = "/conversations", method = AgentMethods.GET)
    Object conversations(DifyConversationsQuery difyQuery);

    @AgentMethod(endpoint = "/messages", method = AgentMethods.GET)
    Object messages(DifyMessagesQuery difyQuery);

    @AgentMethod(endpoint = "/conversations/{conversation_id}", method = AgentMethods.DELETE)
    void deleteConversations(@AgentParam("conversation_id") String conversationId);

    @AgentMethod(endpoint = "/files/upload", method = AgentMethods.POST, contentType = AgentConst.MULTIPART_FORM_DATA)
    DifyUploadFileResponse uploadFiles(@AgentParam("file") MultipartFile file, @AgentParam("user") String user);
}