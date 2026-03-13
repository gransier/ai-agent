package cn.gransier.controller;

import cn.gransier.domain.query.ChatCompleteQuery;
import cn.gransier.domain.query.DifyChatQuery;
import cn.gransier.domain.query.DifyConversationsQuery;
import cn.gransier.domain.query.DifyMessagesQuery;
import cn.gransier.domain.response.DifyChatResponse;
import cn.gransier.domain.response.GlmChatCompletionResponse;
import cn.gransier.service.DifyAgentService;
import cn.gransier.service.GlmService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import javax.annotation.Resource;

@Api(tags = "Glm对话接口")
@Slf4j
@RestController
@RequestMapping("/ai-cloud/chat/glm")
public class GlmController {

    @Resource
    private GlmService glmService;

    @ApiOperation("聊天接口")
    @PostMapping(value = "/completions", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    @SneakyThrows
    public Flux<GlmChatCompletionResponse> completions(@RequestBody Object query) {

        return glmService.completions(query);
    }
}
