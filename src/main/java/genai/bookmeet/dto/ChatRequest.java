package genai.bookmeet.dto;

import lombok.*;

import java.text.StringCharacterIterator;
import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRequest {

    private String userId;
    private String book;
    private String bookType;
    private String character;
    private String text;

}
