package chat.repository;

import chat.entity.ChatUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatUserRepository extends CrudRepository<ChatUser, Long> {
    boolean existsByUsername(String username);
}
