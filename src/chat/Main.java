package chat;

import chat.entity.Role;
import chat.enums.ERole;
import chat.repository.RoleRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class Main {
    private final RoleRepository roleRepository;

    public Main(final RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @EventListener
    public void applicationReady(ApplicationReadyEvent event) {
        Role userRole = new Role(ERole.ROLE_USER);
        roleRepository.save(userRole);
        Role adminRole = new Role(ERole.ROLE_ADMIN);
        roleRepository.save(adminRole);
        Role moderatorRole = new Role(ERole.ROLE_MODERATOR);
        roleRepository.save(moderatorRole);
    }
}
