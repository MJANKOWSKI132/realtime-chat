package chat.controller;

import chat.dto.request.LoginRequestDto;
import chat.dto.request.UserCreationRequestDto;
import chat.dto.response.UserInfoResponseDto;
import chat.service.ChatUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

    @PostMapping("/user/signin")
    public ResponseEntity<?> signin(@RequestBody LoginRequestDto loginRequest) {
        return chatUserService.signin(loginRequest);
    }

    @GetMapping("/connected/users")
    @PreAuthorize("isAuthenticated()")
    public List<UserInfoResponseDto> retrieveConnectedUsers() {
        return chatUserService.retrieveConnectedUsers();
    }

    @GetMapping("/ping")
    @PreAuthorize("isAuthenticated()")
    public void ping() {

    }

    @PutMapping("/user/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> logout(@AuthenticationPrincipal UserDetails userDetails) {
        return chatUserService.logout(userDetails);
    }
}
