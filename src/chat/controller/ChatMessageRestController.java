package chat.controller;

import chat.dto.response.ChatMessageResponseDto;
import chat.service.ChatMessageService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
public class ChatMessageRestController {
    private final ChatMessageService chatMessageService;

    public ChatMessageRestController(final ChatMessageService chatMessageService) {
        this.chatMessageService = chatMessageService;
    }

    @GetMapping("/previous/messages")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public List<ChatMessageResponseDto> retrievePreviousMessages(@RequestParam Long senderId,
                                                                 @RequestParam(value = "receiverId" ) Optional<Long> optionalReceiverId) {
        return chatMessageService.retrievePreviousMessages(senderId, optionalReceiverId);
    }
}
