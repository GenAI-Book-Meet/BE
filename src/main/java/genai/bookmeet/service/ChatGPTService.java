package genai.bookmeet.service;

import org.springframework.stereotype.Service;
import org.json.JSONObject;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

@Service
public class ChatGPTService {

    private final String API_URL = "https://api.openai.com/v1/chat/completions";
    private final String API_KEY = "";

    private final RestTemplate restTemplate;

    public ChatGPTService() {
        this.restTemplate = new RestTemplate();
    }

    public String getStep1(String json) {

        JSONObject jsonObject = new JSONObject(json);
        String userId = jsonObject.getString("userId");
        String text = jsonObject.getString("text");
        String bookType = "";
        String character = "";
        int code = -500;

        String systemText = "입력된 책 제목을 확인하고, 책의 장르를 판별합니다. 장르가 문학이면 '대화할 등장인물의 이름이 무엇인가요?'라고 질문하고, 장르가 비문학이면 '대화할 저자의 이름이 무엇인가요?'라고 질문합니다. 책 제목이 이해되지 않으면 '정확히 이해하지 못했어요. 다시 말씀해 주실 수 있나요?'라고 답합니다. 이 외에 다른 정보는 출력하지 않습니다.";
        String userText = text;
        String gptRespond = RequestChatGPT(userId, systemText, userText, new ArrayList<>());

        // TODO 프롬프트 // 대화할 등장인물의 이름이 무엇인가요? 부분을 //문학 으로 변경하면 좀 더 clear 할 것 같음
        if (gptRespond.compareToIgnoreCase("대화할 등장인물의 이름이 무엇인가요?") == 0) {
            bookType = "문학";
            code = 100;
        } else if (gptRespond.compareToIgnoreCase("대화할 등장인물의 이름이 무엇인가요?") == 0) {
            bookType = "비문학";
            code = 100;
        } else {
            bookType = "";
            code = -100;
        }

        // TODO userId 이름으로 로컬에 파일 저장 step 1 에서 필요한가?
        String filePath = userId + ".txt";
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {

            writer.printf("{user:%s}", text);

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("book:" + text);
        System.out.println("bookType:" + bookType);
        System.out.println("character:" + character);
        System.out.println("text:" + gptRespond);
        System.out.println("code:" + code);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("book", text);
        map.put("bookType", bookType);
        map.put("character", character);
        map.put("text", gptRespond);
        map.put("code", code);

        JSONObject j = new JSONObject(map);
        return j.toString();

    }

    public String getStep2(String json) {

        JSONObject jsonObject = new JSONObject(json);
        String userId = jsonObject.getString("userId");
        String text = jsonObject.getString("text");
        String book = jsonObject.getString("book");
        String bookType = jsonObject.getString("bookType");
        String character = "";
        int code = -500;

        String systemText = "";
        if (bookType.compareTo("문학") == 0) {
            systemText = String.format(
                    "입력된 %s 이름을 확인하고, 그 %s이 %s에 등장하는 인물과 일치하는지 판별합니다. 맞다면 '%s과의 대화를 시작할게요' 라고 답하고, 맞지 않다면 '정확하게 이해하지 못했어요. 다시 말씀해 주실 수 있나요?' 라고 대답합니다. 이 외에 다른 정보는 출력하지 않습니다.",
                    text, text, book, text);
        } else { // if (bookType.compareTo("비문학") == 0) {
            systemText = String.format(
                    "입력된 %s 이름을 확인하고, 그 %s가 %s의 저자와 일치하는지 판별합니다. 맞다면 '%s와의 대화를 시작할게요' 라고 답하고, 맞지 않다면 '정확하게 이해하지 못했어요. 다시 말씀해 주실 수 있나요?' 라고 대답합니다. 이 외에 다른 정보는 출력하지 않습니다.",
                    text, text, book, text);
        }

        String userText = text;
        String gptRespond = RequestChatGPT(userId, systemText, userText, new ArrayList<>());

        if (gptRespond.contains("대화를 시작할게요") == true) {
            code = 100;
            character = text;
        } else {

            System.out.println("[Error] chat gpt response: " + gptRespond);

            code = -100;
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("book", book);
            map.put("bookType", bookType);
            map.put("character", character);
            map.put("text", gptRespond);
            map.put("code", code);

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

        userText = "안녕하세요. 자기소개를 해주세요.";
        gptRespond = RequestChatGPT(userId, systemText, userText, new ArrayList<>());

        // TODO 대화내용 DB 저장

        System.out.println("book:" + book);
        System.out.println("bookType:" + bookType);
        System.out.println("character:" + character);
        System.out.println("text:" + gptRespond);
        System.out.println("code:" + code);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("book", book);
        map.put("bookType", bookType);
        map.put("character", character);
        map.put("text", gptRespond);
        map.put("code", code);

        JSONObject j = new JSONObject(map);
        return j.toString();

    }

    public String getStep3(String json) {

        JSONObject jsonObject = new JSONObject(json);
        String userId = jsonObject.getString("userId");
        String text = jsonObject.getString("text");
        String book = jsonObject.getString("book");
        String bookType = jsonObject.getString("bookType");
        String character = jsonObject.getString("character");
        ;
        int code = -500;

        String systemText = "";
        systemText = String.format(
                "타인이 당신과 대화를 시작할만한 질문 3가지를 다음 형식으로 출력하세요: \r\n" + //
                        "1) 질문\r\n" + //
                        "2) 질문\r\n" + //
                        "3) 질문\r\n" + //
                        "이 외에 다른 어떤 정보도 출력하지 마세요.");

        String userText = text;

        // 이전 대화 step2 HISTORY 무조건 넣어야함
        List<Map<String, String>> previousMessages = new ArrayList<>();
        previousMessages.add(new HashMap<String, String>() {
            {
                put("role", "user");
                put("content", "DB에서 가져와야함");
            }
        });
        previousMessages.add(new HashMap<String, String>() {
            {
                put("role", "assistant");
                put("content", "DB에서 가져와야함");
            }
        });

        String gptRespond = RequestChatGPT(userId, systemText, userText, previousMessages);

        code = 100;

        // TODO 대화내용 DB 저장

        System.out.println("book:" + book);
        System.out.println("bookType:" + bookType);
        System.out.println("character:" + character);
        System.out.println("text:" + gptRespond);
        System.out.println("code:" + code);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("book", text);
        map.put("bookType", bookType);
        map.put("character", character);
        map.put("text", gptRespond);
        map.put("code", code);

        JSONObject j = new JSONObject(map);
        return j.toString();

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
