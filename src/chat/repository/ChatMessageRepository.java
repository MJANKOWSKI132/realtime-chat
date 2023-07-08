package chat.repository;

import chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends CrudRepository<ChatMessage, Long> {

    @Query(value = """
        SELECT *
        FROM CHAT_MESSAGE
        WHERE (SENDER_ID = :senderId AND RECEIVER_ID = :receiverId)
        OR (SENDER_ID = :receiverId AND RECEIVER_ID = :senderId)
        ORDER BY TIME_SENT ASC
    """, nativeQuery = true)
    List<ChatMessage> findAllBySenderIdAndReceiverIdOrderByTimeSentAsc(@Param(value = "senderId") Long senderId,
                                                                       @Param(value = "receiverId") Long receiverId);

    List<ChatMessage> findAllByReceiverIdNullOrderByTimeSentAsc();
}
