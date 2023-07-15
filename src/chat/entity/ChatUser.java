package chat.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@NoArgsConstructor
@Data
public class ChatUser {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private ZonedDateTime created;
    private String username;
    @OneToMany(mappedBy = "sender")
    private List<ChatMessage> messagesSent = new ArrayList<>();
    @OneToMany(mappedBy = "receiver")
    private List<ChatMessage> messagesReceived = new ArrayList<>();
    private boolean connected;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();
    private String password;

    public ChatUser(final String username,
                    final String encodedPassword) {
        this.username = username;
        this.password = encodedPassword;
    }

    @PrePersist
    public void prePersist() {
        this.created = ZonedDateTime.now(ZoneOffset.UTC);
        this.connected = true;
    }
}
