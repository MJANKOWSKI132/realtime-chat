package chat.controller;

import chat.dto.response.ChatMessageResponseDto;
import chat.service.ChatMessageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ChatMessageRestController {
    private final ChatMessageService chatMessageService;

    public ChatMessageRestController(final ChatMessageService chatMessageService) {
        this.chatMessageService = chatMessageService;
    }

    @GetMapping("/previous/messages")
    public List<ChatMessageResponseDto> retrievePreviousMessages() {
        return chatMessageService.retrievePreviousMessages();
    }
}
