package chat.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class UserDisconnectResponseDto {
    private Long userId;
    private ChatMessageType type = ChatMessageType.DISCONNECT;

    public UserDisconnectResponseDto(final Long userId) {
        this.userId = userId;
    }
}
