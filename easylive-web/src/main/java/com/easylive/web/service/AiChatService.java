package com.easylive.web.service;

import com.easylive.entity.dto.AiChatRequestDTO;
import com.easylive.entity.vo.AiChatResultVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface AiChatService {

    AiChatResultVO chat(AiChatRequestDTO request);

    SseEmitter streamChat(AiChatRequestDTO request);
}
