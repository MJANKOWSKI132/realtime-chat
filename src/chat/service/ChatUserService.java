package chat.service;

import chat.dto.request.LoginRequestDto;
import chat.dto.request.UserCreationRequestDto;
import chat.dto.response.ErrorResponseDto;
import chat.dto.response.JwtResponseDto;
import chat.dto.response.UserCreationResponseDto;
import chat.dto.response.UserInfoResponseDto;
import chat.entity.ChatUser;
import chat.entity.Role;
import chat.enums.ERole;
import chat.model.UserDetailsImpl;
import chat.repository.ChatUserRepository;
import chat.repository.RoleRepository;
import chat.security.jwt.JwtUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ChatUserService {
    private final ChatUserRepository chatUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public ChatUserService(final ChatUserRepository chatUserRepository,
                           final PasswordEncoder passwordEncoder,
                           final RoleRepository roleRepository,
                           final AuthenticationManager authenticationManager,
                           final JwtUtils jwtUtils) {
        this.chatUserRepository = chatUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    public ResponseEntity<?> registerUser(UserCreationRequestDto userCreationRequest) {
        // TODO: add validation around username and password
        final String username = userCreationRequest.getUsername();
        final boolean userAlreadyExists = chatUserRepository.existsByUsername(username);
        if (userAlreadyExists) {
            return ResponseEntity
                    .badRequest()
                    .body(ErrorResponseDto.fromMessage(
                            String.format("User with username: %s already exists", username)
                    ));
        }
        final String encodedPassword = passwordEncoder.encode(userCreationRequest.getPassword());
        ChatUser chatUser = new ChatUser(username, encodedPassword);
        Set<String> strRoles = userCreationRequest.getRoles();
        Set<Role> roles = new HashSet<>();
        if (Objects.isNull(strRoles)) {
            Optional<Role> optionalUserRole = roleRepository.findByName(ERole.ROLE_USER);
            if (optionalUserRole.isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(ErrorResponseDto.fromMessage("The user role was not found"));
            }
            roles.add(optionalUserRole.get());
        } else {
            for (final var strRole : strRoles) {
                switch (strRole) {
                    case "admin": {
                        Optional<Role> optionalAdminRole = roleRepository.findByName(ERole.ROLE_ADMIN);
                        if (optionalAdminRole.isEmpty()) {
                            return ResponseEntity
                                    .status(HttpStatus.NOT_FOUND)
                                    .body(ErrorResponseDto.fromMessage("The admin role was not found"));
                        }
                        roles.add(optionalAdminRole.get());
                    }
                    case "moderator": {
                        Optional<Role> optionalModeratorRole = roleRepository.findByName(ERole.ROLE_MODERATOR);
                        if (optionalModeratorRole.isEmpty()) {
                            return ResponseEntity
                                    .status(HttpStatus.NOT_FOUND)
                                    .body(ErrorResponseDto.fromMessage("The moderator role was not found"));
                        }
                        roles.add(optionalModeratorRole.get());
                    }
                    case "user": {
                        Optional<Role> optionalUserRole = roleRepository.findByName(ERole.ROLE_USER);
                        if (optionalUserRole.isEmpty()) {
                            return ResponseEntity
                                    .status(HttpStatus.NOT_FOUND)
                                    .body(ErrorResponseDto.fromMessage("The user role was not found"));
                        }
                        roles.add(optionalUserRole.get());
                    }
                }
            }
        }
        chatUser.setRoles(roles);
        chatUserRepository.save(chatUser);
        return ResponseEntity.ok(UserCreationResponseDto.fromEntity(chatUser));
    }

    public List<UserInfoResponseDto> retrieveConnectedUsers() {
        return chatUserRepository
                .findAllByConnectedTrueOrderByCreatedAsc()
                .stream()
                .map(UserInfoResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    public ResponseEntity<?> signin(LoginRequestDto loginRequest) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        return ResponseEntity.ok(
                new JwtResponseDto(
                        jwt,
                        userDetails.getId(),
                        userDetails.getUsername(),
                        roles
                )
        );
    }
}
