package genai.bookmeet.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String role;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    private Chat chat;

    @Builder
    private Message(String role, String content, Chat chat) {
        this.role = role;
        this.content = content;
        this.chat = chat;
    }
}
