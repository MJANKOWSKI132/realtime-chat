package chat.controller;

import chat.dto.request.UserCreationRequestDto;
import chat.dto.response.UserCreationResponseDto;
import chat.service.ChatUserService;
import chat.service.ChatUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    private final ChatUserService chatUserService;

    public UserController(final ChatUserService chatUserService) {
        this.chatUserService = chatUserService;
    }

    @PostMapping("/user/register")
    public ResponseEntity<?> registerUser(@RequestBody UserCreationRequestDto userCreationRequest) {
        return chatUserService.registerUser(userCreationRequest);
    }
}
