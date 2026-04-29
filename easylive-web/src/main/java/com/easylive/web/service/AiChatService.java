package com.easylive.web.service;

import com.easylive.entity.dto.AiChatRequestDTO;
import com.easylive.entity.vo.AiChatResultVO;

public interface AiChatService {

    AiChatResultVO chat(AiChatRequestDTO request);
}
