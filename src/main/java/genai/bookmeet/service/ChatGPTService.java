package genai.bookmeet.service;

import genai.bookmeet.dto.ChatRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.json.JSONObject;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import genai.bookmeet.dto.MessageDto;
import genai.bookmeet.entity.Chat;
import genai.bookmeet.entity.Message;
import genai.bookmeet.repository.ChatRepository;
import genai.bookmeet.repository.MessageRepository;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ChatGPTService {

    private final String API_URL = "https://api.openai.com/v1/chat/completions";
    @Value("${openai.api.key}")
    private String API_KEY;

    private final RestTemplate restTemplate;
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;

    public ChatGPTService(ChatRepository chatRepository, MessageRepository messageRepository) {
        this.restTemplate = new RestTemplate();
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
    }

    @Transactional
    public String getStep1(ChatRequest chatRequest) {

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String strCurtime = d.format(timestamp).toString();
        log.info("[step1]" + strCurtime);

        String userId = chatRequest.getUserId();
        String text = chatRequest.getText();
        String bookType = "";
        String character = "";
        int code = -500;
        String msg = "";

        String systemText = "입력된 책 제목을 확인하고, 책의 장르를 판별합니다. 장르가 문학이면 '대화할 등장인물의 이름이 무엇인가요?'라고 질문하고, 장르가 비문학이면 '대화할 저자의 이름이 무엇인가요?'라고 질문합니다. 책 제목이 이해되지 않으면 '정확히 이해하지 못했어요. 다시 말씀해 주실 수 있나요?'라고 답합니다. 이 외에 다른 정보는 출력하지 않습니다.";
        String userText = text;
        String gptRespond = RequestChatGPT(userId, systemText, userText, new ArrayList<>());

        if (gptRespond.contains("대화할 등장인물의 이름이 무엇인가요?") == true) {
            bookType = "문학";
            code = 100;
            msg = "성공";
        } else if (gptRespond.contains("대화할 저자의 이름이 무엇인가요?") == true) {
            bookType = "비문학";
            code = 100;
            msg = "성공";
        } else {
            bookType = "";
            code = -100;
            msg = "책 이름 알수없음";
        }

        log.info("book:" + text);
        log.info("bookType:" + bookType);
        log.info("character:" + character);
        log.info("text:" + gptRespond);
        log.info("code:" + code);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("book", text);
        map.put("bookType", bookType);
        map.put("character", character);
        map.put("text", gptRespond);
        map.put("code", code);
        map.put("msg", msg);

        JSONObject j = new JSONObject(map);
        return j.toString();

    }

    public String getStep2(ChatRequest chatRequest) {

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String strCurtime = d.format(timestamp).toString();
        log.info("[step2]" + strCurtime);

        String userId = chatRequest.getUserId();
        String text = chatRequest.getText();
        String book = chatRequest.getBook();
        String bookType = chatRequest.getBookType();
        String character = "";
        int code = -500;
        String msg = "";

        String systemText = "";
        if (bookType.compareTo("문학") == 0) {
            systemText = String.format(
                    "입력된 이름을 확인하고, '%s' 에 등장하는 인물인지 판별합니다. 등장하는 인물이라면 '%s 과의 대화를 시작할게요' 라고 답하고, 등장인물이 아니라면 '정확하게 이해하지 못했어요. 다시 말씀해 주실 수 있나요?' 라고 대답합니다. 이 외에 다른 정보는 출력하지 않습니다.",
                    book, text);
        } else { // if (bookType.compareTo("비문학") == 0) {
            systemText = String.format(
                    "입력된 이름을 확인하고, '%s' 의 저자와 일치하는지 판별합니다. 저자와 일치한다면 '%s 와의 대화를 시작할게요' 라고 답하고, 일치하지 않다면 '정확하게 이해하지 못했어요. 다시 말씀해 주실 수 있나요?' 라고 대답합니다. 이 외에 다른 정보는 출력하지 않습니다.",
                    book, text);
        }

        String userText = text;
        String gptRespond = RequestChatGPT(userId, systemText, userText, new ArrayList<>());

        if (gptRespond.contains("대화를 시작할게요") == true) {
            code = 100;
            character = text;
            msg = "성공";
        } else {

            log.info("[Error] chat gpt response: " + gptRespond);

            code = -100;

            if (bookType == "문학")
                msg = "등장인물 알수없음";
            else if (bookType == "비문학")
                msg = "저자 알수없음";

            Map<String, Object> map = new HashMap<String, Object>();
            map.put("book", book);
            map.put("bookType", bookType);
            map.put("character", character);
            map.put("text", gptRespond);
            map.put("code", code);
            map.put("msg", msg);

            JSONObject j = new JSONObject(map);
            return j.toString();
        }

        // 역할 부여
        systemText = "";
        if (bookType.compareTo("문학") == 0) {
            systemText = String.format(
                    "이제부터 당신은 %s의 등장인물 %s입니다. 이 역할을 부여받은 순간부터, %s의 관점에서 대답하세요. 또한 언제나 일관된 어투를 사용하세요. 만약 %s의 관점에서 대답하기 어려운 상황이라면, 이해하지 못했으니 다시 입력하라는 의미의 답변을 %s의 어투로 대답하세요. 또한 이 요청에 대한 답변 없이, %s에 맞는 자기소개를 포함해 말을 걸어주시고, 앞으로의 모든 답변은 4줄을 넘기지 마세요. 시작하세요.",
                    book, character, character, character, character, character);
        } else { // if (bookType.compareTo("비문학") == 0) {
            systemText = String.format(
                    "이제부터 당신은 %s의 저자 %s입니다. 이 역할을 부여받은 순간부터, %s의 관점에서 대답하세요. 또한 언제나 일관된 어투를 사용하세요. 만약 %s의 관점에서 대답하기 어려운 상황이라면, 이해하지 못했으니 다시 입력하라는 의미의 답변을 %s의 어투로 대답하세요. 또한 이 요청에 대한 답변 없이, %s에 맞는 자기소개를 포함해 말을 걸어주시고, 앞으로의 모든 답변은 4줄을 넘기지 마세요. 시작하세요.",
                    book, character, character, character, character, character);
        }

        // 대화 시작
        userText = "안녕하세요. 자기소개를 해주세요.";
        gptRespond = RequestChatGPT(userId, systemText, userText, new ArrayList<>());

        // 대화 내용 DB 저장
        SaveMessageToDB(userId, "user", userText);
        SaveMessageToDB(userId, "assistant", gptRespond);

        log.info("book:" + book);
        log.info("bookType:" + bookType);
        log.info("character:" + character);
        log.info("text:" + gptRespond);
        log.info("code:" + code);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("book", book);
        map.put("bookType", bookType);
        map.put("character", character);
        map.put("text", gptRespond);
        map.put("code", code);
        map.put("msg", msg);

        JSONObject j = new JSONObject(map);
        return j.toString();

    }

    public String getStep3(ChatRequest chatRequest) {

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String strCurtime = d.format(timestamp).toString();
        log.info("[step3]" + strCurtime);

        String userId = chatRequest.getUserId();
        String text = chatRequest.getText();
        String book = chatRequest.getBook();
        String bookType = chatRequest.getBookType();
        String character = chatRequest.getCharacter();
        int code = -500;
        String msg = "";

        String systemText = "";
        systemText = String.format(
                "입력된 이야기에 타인이 당신과 대화를 시작할만한 질문 3가지를 다음 형식으로 출력하세요: \r\n" + //
                        "1) 질문\r\n" + //
                        "2) 질문\r\n" + //
                        "3) 질문\r\n" + //
                        "이 외에 다른 어떤 정보도 출력하지 마세요.",
                book, character);

        // 이전 대화 HISTORY 가져오기
        List<Map<String, String>> previousMessages = MakePreviousMessages(userId);

        String assistantText = "";
        for (Map<String, String> map : previousMessages) {
            if ("assistant".equals(map.get("role"))) {

                assistantText = map.get("content");
                break;
            }
        }

        String userText = assistantText;
        String gptRespond = RequestChatGPT(userId, systemText, userText, previousMessages);

        code = 100;
        msg = "성공";

        String input = gptRespond.replace("\n", "").replace("\r", "");
        String[] parts = input.split("\\d\\) ");

        // parts 배열에는 첫 번째 빈 요소가 생기므로, 이를 제거하고 필요한 문자열 추출
        String res1 = parts.length > 1 ? parts[1].trim() : "";
        String res2 = parts.length > 2 ? parts[2].trim() : "";
        String res3 = parts.length > 3 ? parts[3].trim() : "";

        log.info("book:" + book);
        log.info("bookType:" + bookType);
        log.info("character:" + character);
        log.info("text:" + gptRespond);
        log.info("res1:" + res1);
        log.info("res2:" + res2);
        log.info("res3:" + res3);
        log.info("code:" + code);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("book", book);
        map.put("bookType", bookType);
        map.put("character", character);
        map.put("text", gptRespond);
        map.put("res1", res1);
        map.put("res2", res2);
        map.put("res3", res3);
        map.put("code", code);
        map.put("msg", msg);

        JSONObject j = new JSONObject(map);
        return j.toString();

    }

    public String getStep4(ChatRequest chatRequest) {

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String strCurtime = d.format(timestamp).toString();
        log.info("[step4]" + strCurtime);

        String userId = chatRequest.getUserId();
        String text = chatRequest.getText();
        String book = chatRequest.getBook();
        String bookType = chatRequest.getBookType();
        String character = chatRequest.getCharacter();
        int code = -500;
        String message = "";

        String systemText = "";
        if (bookType.compareTo("문학") == 0) {
            systemText = String.format(
                    "이제부터 당신은 %s의 등장인물 %s입니다. 이 역할을 부여받은 순간부터, %s의 관점에서 대답하세요. 또한 언제나 일관된 어투를 사용하세요. 만약 %s의 관점에서 대답하기 어려운 상황이라면, 이해하지 못했으니 다시 입력하라는 의미의 답변을 %s의 어투로 대답하세요.",
                    book, character, character, character, character);
        } else { // if (bookType.compareTo("비문학") == 0) {
            systemText = String.format(
                    "이제부터 당신은 %s의 저자 %s입니다. 이 역할을 부여받은 순간부터, %s의 관점에서 대답하세요. 또한 언제나 일관된 어투를 사용하세요. 만약 %s의 관점에서 대답하기 어려운 상황이라면, 이해하지 못했으니 다시 입력하라는 의미의 답변을 %s의 어투로 대답하세요.",
                    book, character, character, character, character);
        }

        String userText = text;

        // 이전 대화 HISTORY 가져오기
        List<Map<String, String>> previousMessages = MakePreviousMessages(userId);

        String gptRespond = RequestChatGPT(userId, systemText, userText, previousMessages);

        code = 100;
        message = "성공";

        // 대화 내용 DB 저장
        SaveMessageToDB(userId, "user", userText);
        SaveMessageToDB(userId, "assistant", gptRespond);

        log.info("book:" + book);
        log.info("bookType:" + bookType);
        log.info("character:" + character);
        log.info("text:" + gptRespond);
        log.info("code:" + code);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("book", book);
        map.put("bookType", bookType);
        map.put("character", character);
        map.put("text", gptRespond);
        map.put("code", code);
        map.put("msg", message);

        JSONObject j = new JSONObject(map);
        return j.toString();

    }

    private void SaveMessageToDB(String userId, String role, String content) {

        Chat chat = chatRepository.findByUserIdWithMessages(userId);
        if (chat == null) {
            chat = Chat.builder().userId(userId).build();
        }

        Message message = Message.builder()
                .role(role)
                .content(content)
                .chat(chat)
                .build();

        chat.getMessages().add(message);
        chatRepository.save(chat);
    }

    private List<Map<String, String>> MakePreviousMessages(String userId) {

        Chat chat = chatRepository.findByUserIdWithMessages(userId);
        List<MessageDto> messages = chat.getMessages().stream().map(MessageDto::from).toList();

        List<Map<String, String>> previousMessages = new ArrayList<>();
        for (int i = 0; i < messages.size(); i++) {
            String role = messages.get(i).getRole();
            String content = messages.get(i).getContent();

            previousMessages.add(new HashMap<String, String>() {
                {
                    put("role", role);
                    put("content", content);
                }
            });
        }

        return previousMessages;
    }

    private String RequestChatGPT(String userId, String systemPrompt, String userPrompt,
            List<Map<String, String>> previousMessages) {

        // Request API
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(API_KEY);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");

        // 이전 대화 메시지 추가
        List<Map<String, String>> messages = new ArrayList<>(previousMessages);

        // 현재 시스템 및 사용자 프롬프트 추가
        messages.add(new HashMap<String, String>() {
            {
                put("role", "system");
                put("content", systemPrompt);
            }
        });
        messages.add(new HashMap<String, String>() {
            {
                put("role", "user");
                put("content", userPrompt);
            }
        });

        requestBody.put("messages", messages);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(API_URL, HttpMethod.POST, entity, Map.class);

        // 응답 처리
        String gptRespond = "";
        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null) {
                Map<String, Object> choice = ((List<Map<String, Object>>) responseBody.get("choices")).get(0);
                Map<String, String> message = (Map<String, String>) choice.get("message");
                gptRespond = message.get("content");
            }
        }

        return gptRespond;

    }

}
