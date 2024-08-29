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
        String bootType = "";
        String character = "";
        int code = -500;

        String systemText = "입력된 책 제목을 확인하고, 책의 장르를 판별합니다. 장르가 문학이면 '대화할 등장인물의 이름이 무엇인가요?'라고 질문하고, 장르가 비문학이면 '대화할 저자의 이름이 무엇인가요?'라고 질문합니다. 책 제목이 이해되지 않으면 '정확히 이해하지 못했어요. 다시 말씀해 주실 수 있나요?'라고 답합니다. 이 외에 다른 정보는 출력하지 않습니다.";
        String userText = text;
        String gptRespond = RequestChatGPT(userId, systemText, userText);

        // TODO 프롬프트 // 대화할 등장인물의 이름이 무엇인가요? 부분을 //문학 으로 변경하면 좀 더 clear 할 것 같음
        if (gptRespond.compareToIgnoreCase("대화할 등장인물의 이름이 무엇인가요?") == 0) {
            bootType = "문학";
            code = 100;
        } else if (gptRespond.compareToIgnoreCase("대화할 등장인물의 이름이 무엇인가요?") == 0) {
            bootType = "비문학";
            code = 100;
        } else {
            bootType = "";
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
        System.out.println("bootType:" + bootType);
        System.out.println("character:" + character);
        System.out.println("text:" + gptRespond);
        System.out.println("code:" + code);

        Map<String, Object> map = new HashMap<String, Object>();
        map.put("book", text);
        map.put("bootType", bootType);
        map.put("character", character);
        map.put("text", gptRespond);
        map.put("code", code);

        JSONObject j = new JSONObject(map);
        return j.toString();

    }

    private String RequestChatGPT(String userId, String systemPrompt, String userPrompt) {

        // Request API
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(API_KEY);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-3.5-turbo");

        List<Map<String, String>> messages = Arrays.asList(
                new HashMap<String, String>() {
                    {
                        put("role", "system");
                        put("content",
                                systemPrompt);
                    }
                },
                new HashMap<String, String>() {
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
