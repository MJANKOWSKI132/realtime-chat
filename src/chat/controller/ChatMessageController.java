package chat.controller;

import chat.dto.request.ChatMessageRequestDto;
import chat.dto.request.UserJoinRequestDto;
import chat.dto.response.UserJoinResponseDto;
import chat.service.ChatMessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import java.util.Objects;

@Controller
public class ChatMessageController {
    private final ChatMessageService chatMessageService;

    public ChatMessageController(final ChatMessageService chatMessageService) {
        this.chatMessageService = chatMessageService;
    }

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> sendMessage(@Payload ChatMessageRequestDto messageRequest) {
        return chatMessageService.sendMessage(messageRequest);
    }

    @MessageMapping("/chat.userJoin")
    @SendTo("/topic/public")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<UserJoinResponseDto> newUserJoin(@Payload UserJoinRequestDto joinRequest,
                                                           SimpMessageHeaderAccessor headerAccessor) {
        if (Objects.isNull(headerAccessor.getSessionAttributes())) {
            return ResponseEntity.internalServerError()
                    .body(new UserJoinResponseDto(null, null));
        }
        headerAccessor.getSessionAttributes().put("userId", joinRequest.getUserId());
        return ResponseEntity.ok(new UserJoinResponseDto(joinRequest.getUserId(), joinRequest.getUsername()));
    }
}
