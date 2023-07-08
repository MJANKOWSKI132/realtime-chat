package chat.dto.response;

import chat.entity.ChatUser;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
public class UserInfoResponseDto {
    private Long userId;
    private String username;
    private ZonedDateTime created;

    public static UserInfoResponseDto fromEntity(ChatUser chatUser) {
        var response = new UserInfoResponseDto();
        response.setUserId(chatUser.getId());
        response.setUsername(chatUser.getUsername());
        response.setCreated(chatUser.getCreated());
        return response;
    }
}
