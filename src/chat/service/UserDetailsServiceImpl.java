package chat.service;

import chat.model.UserDetailsImpl;
import chat.repository.ChatUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final ChatUserRepository chatUserRepository;

    public UserDetailsServiceImpl(final ChatUserRepository chatUserRepository) {
        this.chatUserRepository = chatUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var chatUser = chatUserRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format("No such user exists with username: %s", username)
                ));
        return UserDetailsImpl.build(chatUser);
    }
}
