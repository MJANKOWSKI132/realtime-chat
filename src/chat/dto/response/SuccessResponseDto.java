package chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor(staticName = "fromMessage")
@NoArgsConstructor
public class SuccessResponseDto {
    private String message;
}
