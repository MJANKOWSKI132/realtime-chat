package chat.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

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

    public ChatUser(final String username) {
        this.username = username;
    }

    @PrePersist
    public void prePersist() {
        this.created = ZonedDateTime.now(ZoneOffset.UTC);
        this.connected = true;
    }
}
