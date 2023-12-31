package chat.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
public class UserCreationRequestDto {
    private String username;
    private String password;
    private Set<String> roles;
}
