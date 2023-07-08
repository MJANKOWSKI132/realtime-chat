package chat.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class UserDisconnectRequestDto {
    private Long userId;

    public UserDisconnectRequestDto(final Long userId) {
        this.userId = userId;
    }
}
