package genai.bookmeet.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRequest {

    private String model;
    private List<MessageDto> messages;

}
