package chat.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserJoinRequestDto {
    private Long userId;
    private String username;
}
