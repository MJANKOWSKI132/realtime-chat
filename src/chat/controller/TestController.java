package chat.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/test/public")
    public String testPublic() {
        return "Public content";
    }

    @GetMapping("/test/authorized")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public String testAuthorized() {
        return "Authorized content";
    }
}
