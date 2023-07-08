package chat.service;

import chat.dto.request.UserCreationRequestDto;
import chat.dto.response.ErrorResponseDto;
import chat.dto.response.UserCreationResponseDto;
import chat.entity.ChatUser;
import chat.repository.ChatUserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ChatUserService {
    private final ChatUserRepository chatUserRepository;

    public ChatUserService(final ChatUserRepository chatUserRepository) {
        this.chatUserRepository = chatUserRepository;
    }

    public ResponseEntity<?> registerUser(UserCreationRequestDto userCreationRequest) {
        final String username = userCreationRequest.getUsername();
        final boolean userAlreadyExists = chatUserRepository.existsByUsername(username);
        if (userAlreadyExists) {
            return ResponseEntity
                    .badRequest()
                    .body(ErrorResponseDto.fromMessage(
                            String.format("User with username: %s already exists", username)
                    ));
        }
        ChatUser chatUser = new ChatUser(username);
        chatUserRepository.save(chatUser);
        return ResponseEntity.ok(UserCreationResponseDto.fromEntity(chatUser));
    }
}
