package cn.gransier.controller;

import cn.gransier.common.DefaultDifyStreamListener;
import cn.gransier.domain.query.AgentQuery;
import cn.gransier.domain.query.ChatCompleteQuery;
import cn.gransier.domain.query.ChatQuery;
import cn.gransier.util.DifyClient;
//import com.cx.aiot.common.core.web.domain.ApiResult;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import javax.annotation.Resource;

@RestController
@RequestMapping("/ai-cloud/chat")
public class ChatController {

    @Resource
    private DifyClient difyClient;

//        /**
//     * 获取聊天列表
//     */
//    @ApiOperation(value = "获取聊天列表")
//    @GetMapping("/list")
//    public ApiResult<?> list(@RequestBody ChatQuery chatQuery) {
//
//        return ApiResult.success();
//    }

    /**
     * 聊天接口
     */
    @ApiOperation(value = "聊天接口")
    @GetMapping(value = "/completions", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    @SneakyThrows
    public Flux<String> completions(ChatCompleteQuery query) {
        AgentQuery agentQuery = new AgentQuery(query.getContent(), "user-123", query.getChatId(), query.isStream());

        return Flux.create(sink -> difyClient.stream(
                "app-gItXxmtOzXp7S6TdJb7TNcdF",
                "/v1/chat-messages",
                agentQuery,
                DefaultDifyStreamListener.newInstance(sink)
        ));
    }
}
