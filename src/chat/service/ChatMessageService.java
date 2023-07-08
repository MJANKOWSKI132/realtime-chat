package chat.service;

import chat.dto.request.ChatMessageRequestDto;
import chat.dto.response.ChatMessageResponseDto;
import chat.dto.response.ErrorResponseDto;
import chat.entity.ChatMessage;
import chat.repository.ChatMessageRepository;
import chat.repository.ChatUserRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final ChatUserRepository chatUserRepository;

    public ChatMessageService(final ChatMessageRepository chatMessageRepository,
                              final ChatUserRepository chatUserRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.chatUserRepository = chatUserRepository;
    }

    public ResponseEntity<?> sendMessage(ChatMessageRequestDto messageRequest) {
        var matchingSender = chatUserRepository.findById(messageRequest.getSenderId());
        if (matchingSender.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponseDto.fromMessage(
                            String.format("Error, no such user with ID: %s exists", messageRequest.getSenderId())
                    ));
        }
        var sender = matchingSender.get();
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContent(messageRequest.getMessage());
        chatMessage.setSender(sender);
        chatMessageRepository.save(chatMessage);
        return ResponseEntity
                .ok(ChatMessageResponseDto.fromEntity(chatMessage));
    }

    public List<ChatMessageResponseDto> retrievePreviousMessages() {
        return chatMessageRepository
                .findAll()
                .stream()
                .map(ChatMessageResponseDto::fromEntity)
                .collect(Collectors.toList());
    }
}
