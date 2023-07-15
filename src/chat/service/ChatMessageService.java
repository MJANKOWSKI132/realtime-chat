package chat.service;

import chat.dto.request.ChatMessageRequestDto;
import chat.dto.response.ChatMessageResponseDto;
import chat.dto.response.ErrorResponseDto;
import chat.entity.ChatMessage;
import chat.entity.ChatUser;
import chat.repository.ChatMessageRepository;
import chat.repository.ChatUserRepository;
import chat.security.jwt.JwtUtils;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final ChatUserRepository chatUserRepository;

    public ChatMessageService(final ChatMessageRepository chatMessageRepository,
                              final ChatUserRepository chatUserRepository,
                              final JwtUtils jwtUtils) {
        this.chatMessageRepository = chatMessageRepository;
        this.chatUserRepository = chatUserRepository;
    }

    public ResponseEntity<?> sendMessage(ChatMessageRequestDto messageRequest, String username) {
        var matchingSender = chatUserRepository.findByUsername(username);
        if (matchingSender.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponseDto.fromMessage(
                            String.format("Error, no such user with username: %s exists", username)
                    ));
        }
        Optional<ChatUser> matchingReceiver = Objects.nonNull(messageRequest.getReceiverId())
                ? chatUserRepository.findById(messageRequest.getReceiverId())
                : Optional.empty();
        if (Objects.nonNull(messageRequest.getReceiverId()) && matchingReceiver.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponseDto.fromMessage(
                            String.format("Error, no such user with ID: %s exists", messageRequest.getReceiverId())
                    ));
        }
        var sender = matchingSender.get();
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(messageRequest.getMessage());
        chatMessage.setSender(sender);
        matchingReceiver.ifPresent(chatMessage::setReceiver);
        chatMessageRepository.save(chatMessage);
        return ResponseEntity
                .ok(ChatMessageResponseDto.fromEntity(chatMessage));
    }

    public List<ChatMessageResponseDto> retrievePreviousMessages(Long senderId, Optional<Long> optionalReceiverId) {
        if (optionalReceiverId.isEmpty()) {
            return chatMessageRepository
                    .findAllByReceiverIdNullOrderByTimeSentAsc()
                    .stream()
                    .map(ChatMessageResponseDto::fromEntity)
                    .collect(Collectors.toList());
        }
        Long receiverId = optionalReceiverId.get();
        return chatMessageRepository
                .findAllBySenderIdAndReceiverIdOrderByTimeSentAsc(senderId, receiverId)
                .stream()
                .map(ChatMessageResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
}
