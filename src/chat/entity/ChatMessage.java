package chat.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Entity
@NoArgsConstructor
@Data
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String content;
    private ZonedDateTime timeSent;
    @ManyToOne
    @JoinColumn(name = "sender_id")
    private ChatUser sender;

    @PrePersist
    public void beforeSave() {
        timeSent = ZonedDateTime.now(ZoneOffset.UTC);
    }
}
