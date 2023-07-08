package chat.listener;

import chat.dto.request.UserDisconnectRequestDto;
import chat.dto.response.UserDisconnectResponseDto;
import chat.repository.ChatUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Objects;

@Component
@Slf4j
public class WebSocketEventListener {
    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatUserRepository chatUserRepository;
    public WebSocketEventListener(final SimpMessageSendingOperations messagingTemplate,
                                  final ChatUserRepository chatUserRepository) {
        this.messagingTemplate = messagingTemplate;
        this.chatUserRepository = chatUserRepository;
    }
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {

    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        if (Objects.isNull(headerAccessor.getSessionAttributes())) {
            log.error("Session attributes on Header Accessor is null");
            return;
        }
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        if (Objects.nonNull(userId)) {
            var disconnectMessage = ResponseEntity.ok(new UserDisconnectResponseDto(userId));
            messagingTemplate.convertAndSend("/topic/public", disconnectMessage);

            var matchingUser = chatUserRepository.findById(userId);
            matchingUser.ifPresentOrElse(user -> {
                user.setConnected(false);
                chatUserRepository.save(user);
            }, () -> log.error("No such user exists with ID: {}", userId));
        }
    }
}
