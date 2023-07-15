package chat.dto.response;

import chat.utils.Constants;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class JwtResponseDto {
    private String token;
    private final String type = Constants.BEARER;
    private Long id;
    private String username;
    private List<String> roles;

    public JwtResponseDto(String accessToken, Long id, String username, List<String> roles) {
        this.token = accessToken;
        this.id = id;
        this.username = username;
        this.roles = roles;
    }
}
