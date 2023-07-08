package chat.dto.response;

import chat.entity.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.time.ZonedDateTime;

@NoArgsConstructor
@Data
@AllArgsConstructor
public class ChatMessageResponseDto {
    private Long id;
    private String message;
    private ZonedDateTime timeSent;
    private String senderUsername;

    public static ChatMessageResponseDto fromEntity(ChatMessage chatMessage) {
        var response = new ChatMessageResponseDto();
        response.setId(chatMessage.getId());
        response.setTimeSent(chatMessage.getTimeSent());
        response.setMessage(chatMessage.getContent());
        response.setSenderUsername(chatMessage.getSender().getUsername());
        return response;
    }
}
