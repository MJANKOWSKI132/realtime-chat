package chat.dto.response;

import chat.enums.ChatMessageType;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class UserJoinResponseDto {
    private Long userId;
    private String username;
    private ChatMessageType type = ChatMessageType.JOIN;

    public UserJoinResponseDto(final Long userId, final String username) {
        this.userId = userId;
        this.username = username;
    }
}
