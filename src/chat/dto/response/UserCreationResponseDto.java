package chat.dto.response;

import chat.entity.ChatUser;
import chat.entity.ChatUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.time.ZonedDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserCreationResponseDto {
    private Long id;
    private String username;
    private ZonedDateTime created;

    public static UserCreationResponseDto fromEntity(ChatUser chatUser) {
        var response = new UserCreationResponseDto();
        BeanUtils.copyProperties(chatUser, response);
        return response;
    }
}
