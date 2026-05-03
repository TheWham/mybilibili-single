package com.easylive.web.controller;

import com.easylive.entity.dto.AiChatRequestDTO;
import com.easylive.entity.vo.ResponseVO;
import com.easylive.web.service.AiChatService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/ai")
public class AiChatController extends ABaseController {

    @Resource
    private AiChatService aiChatService;

    @PostMapping("/chat")
    public ResponseVO chat(@Valid @RequestBody AiChatRequestDTO request) {
        return getSuccessResponseVO(aiChatService.chat(request));
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestBody AiChatRequestDTO request) {
        return aiChatService.streamChat(request);
    }
}
