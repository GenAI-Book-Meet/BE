package genai.bookmeet.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import genai.bookmeet.service.ChatGPTService;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

@RestController
@RequestMapping("chat")
public class ChatGPTController {

    private final RestTemplate restTemplate;
    ChatGPTService chatgptService;

    public ChatGPTController(RestTemplate restTemplate, ChatGPTService chatgptService) {
        this.restTemplate = restTemplate;
        this.chatgptService = chatgptService;
    }

    @PostMapping(value = "/step1")
    public ResponseEntity<String> ChatStep1(@RequestParam("json") String json) {

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String strCurtime = d.format(timestamp).toString();
        System.out.println("[step1]" + strCurtime);

        return ResponseEntity.ok(chatgptService.getStep1(json));
    }

    @PostMapping(value = "/step2")
    public ResponseEntity<String> ChatStep2(@RequestParam("json") String json) {

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String strCurtime = d.format(timestamp).toString();
        System.out.println("[step2]" + strCurtime);

        return ResponseEntity.ok(chatgptService.getStep2(json));
    }

    @PostMapping(value = "/step3")
    public ResponseEntity<String> ChatStep3(@RequestParam("json") String json) {

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String strCurtime = d.format(timestamp).toString();
        System.out.println("[step3]" + strCurtime);

        return ResponseEntity.ok(chatgptService.getStep3(json));
    }
}