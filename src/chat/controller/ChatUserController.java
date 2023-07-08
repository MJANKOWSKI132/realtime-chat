package chat.controller;

import chat.dto.request.UserCreationRequestDto;
import chat.dto.response.UserInfoResponseDto;
import chat.service.ChatUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ChatUserController {
    private final ChatUserService chatUserService;

    public ChatUserController(final ChatUserService chatUserService) {
        this.chatUserService = chatUserService;
    }

    @PostMapping("/user/register")
    public ResponseEntity<?> registerUser(@RequestBody UserCreationRequestDto userCreationRequest) {
        return chatUserService.registerUser(userCreationRequest);
    }

    @GetMapping("/connected/users")
    public List<UserInfoResponseDto> retrieveConnectedUsers() {
        return chatUserService.retrieveConnectedUsers();
    }
}
