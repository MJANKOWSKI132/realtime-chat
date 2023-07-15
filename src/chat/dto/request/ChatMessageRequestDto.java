package chat.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageRequestDto {
    private String message;
    // TODO: use senderId extracted from JWT token
    private Long senderId;
    private Long receiverId;
}
