package chat.controller;

import chat.dto.request.ChatMessageRequestDto;
import chat.dto.request.UserJoinRequestDto;
import chat.dto.response.ErrorResponseDto;
import chat.dto.response.UserJoinResponseDto;
import chat.security.jwt.JwtUtils;
import chat.service.ChatMessageService;
import chat.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Controller
@Slf4j
public class ChatMessageController {
    private final ChatMessageService chatMessageService;
    private final JwtUtils jwtUtils;

    public ChatMessageController(final ChatMessageService chatMessageService,
                                 final JwtUtils jwtUtils) {
        this.chatMessageService = chatMessageService;
        this.jwtUtils = jwtUtils;
    }

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ResponseEntity<?> sendMessage(@Payload ChatMessageRequestDto messageRequest,
                                         SimpMessageHeaderAccessor headerAccessor) {
        Optional<String> optionalExtractedUsername = jwtUtils.validateWebSocketHeaders(headerAccessor);
        if (optionalExtractedUsername.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponseDto.fromMessage(
                            "You are not permitted to access this resource"
                    ));
        }
        return chatMessageService.sendMessage(messageRequest, optionalExtractedUsername.get());
    }

    @MessageMapping("/chat.userJoin")
    @SendTo("/topic/public")
    public ResponseEntity<?> newUserJoin(@Payload UserJoinRequestDto joinRequest,
                                                           SimpMessageHeaderAccessor headerAccessor) {
        Optional<String> optionalExtractedUsername = jwtUtils.validateWebSocketHeaders(headerAccessor);
        if (optionalExtractedUsername.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(ErrorResponseDto.fromMessage(
                            "You are not permitted to access this resource"
                    ));
        }
        if (Objects.isNull(headerAccessor.getSessionAttributes())) {
            return ResponseEntity.internalServerError()
                    .body(new UserJoinResponseDto(null, null));
        }
        headerAccessor.getSessionAttributes().put("userId", joinRequest.getUserId());
        return ResponseEntity.ok(new UserJoinResponseDto(joinRequest.getUserId(), joinRequest.getUsername()));
    }
}
