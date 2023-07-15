package chat.dto.response;

import chat.entity.ChatMessage;
import chat.enums.ChatMessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.Objects;

@NoArgsConstructor
@Data
@AllArgsConstructor
public class ChatMessageResponseDto {
    private Long id;
    private String message;
    private ZonedDateTime timeSent;
    private Long senderUserId;
    private String senderUsername;
    private Long receiverUserId;
    private String receiverUsername;
    private ChatMessageType type = ChatMessageType.NEW_MESSAGE;

    public static ChatMessageResponseDto fromEntity(ChatMessage chatMessage) {
        var response = new ChatMessageResponseDto();
        response.setId(chatMessage.getId());
        response.setTimeSent(chatMessage.getTimeSent());
        response.setMessage(chatMessage.getContent());
        response.setSenderUsername(chatMessage.getSender().getUsername());
        response.setSenderUserId(chatMessage.getSender().getId());
        if (Objects.nonNull(chatMessage.getReceiver())) {
            response.setReceiverUserId(chatMessage.getReceiver().getId());
            response.setReceiverUsername(chatMessage.getReceiver().getUsername());
        }
        return response;
    }
}
