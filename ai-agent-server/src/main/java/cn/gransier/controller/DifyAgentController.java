package cn.gransier.controller;

import cn.gransier.common.domain.ApiResult;
import cn.gransier.domain.query.DifyChatQuery;
import cn.gransier.domain.query.DifyConversationsQuery;
import cn.gransier.domain.query.DifyMessagesQuery;
import cn.gransier.domain.response.DifyChatResponse;
import cn.gransier.domain.response.DifyUploadFileResponse;
import cn.gransier.service.DifyAgentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import javax.annotation.Resource;

@Api(tags = "DifyAgent对话接口")
@Slf4j
@RestController
@RequestMapping("/ai-cloud/chat")
public class DifyAgentController {

    @Resource
    private DifyAgentService difyAgentService;

    @ApiOperation("聊天接口")
    @PostMapping(value = "/completions", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    @SneakyThrows
    public Flux<DifyChatResponse> completions(@RequestBody DifyChatQuery query) {

        return difyAgentService.completions(query);
    }

    @ApiOperation("会话列表")
    @GetMapping("/conversations")
    @ResponseBody
    @SneakyThrows
    public ApiResult<?> conversations(DifyConversationsQuery difyQuery) {

        return ApiResult.success(difyAgentService.conversations(difyQuery));
    }

    @ApiOperation("会话历史消息")
    @GetMapping("/messages")
    @ResponseBody
    @SneakyThrows
    public ApiResult<?> messages(DifyMessagesQuery difyQuery) {

        return ApiResult.success(difyAgentService.messages(difyQuery));
    }

    @ApiOperation("删除会话")
    @DeleteMapping("/conversations/{conversation_id}")
    @ResponseBody
    @SneakyThrows
    public void deleteConversations(@PathVariable String conversation_id) {

        difyAgentService.deleteConversations(conversation_id);
    }

    @ApiOperation("文件上传接口")
    @PostMapping("/files/upload")
    public ApiResult<DifyUploadFileResponse> uploadFiles(@RequestParam("file") MultipartFile file, @RequestParam("user") String user) {

        return ApiResult.success(difyAgentService.uploadFiles(file, user));
    }
}
