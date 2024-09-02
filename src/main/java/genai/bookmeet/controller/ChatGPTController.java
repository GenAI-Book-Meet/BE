package genai.bookmeet.controller;

import genai.bookmeet.dto.ChatRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import genai.bookmeet.service.ChatGPTService;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

@RestController
@RequestMapping("api/chat")
public class ChatGPTController {

    ChatGPTService chatgptService;

    public ChatGPTController(ChatGPTService chatgptService) {
        this.chatgptService = chatgptService;
    }

    @PostMapping(value = "/step1")
    public ResponseEntity<String> ChatStep1(@RequestBody ChatRequest chatRequest) {

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String strCurtime = d.format(timestamp).toString();
        System.out.println("[step1]" + strCurtime);

        return ResponseEntity.ok(chatgptService.getStep1(chatRequest));
    }

    @PostMapping(value = "/step2")
    public ResponseEntity<String> ChatStep2(@RequestBody ChatRequest chatRequest) {

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String strCurtime = d.format(timestamp).toString();
        System.out.println("[step2]" + strCurtime);

        return ResponseEntity.ok(chatgptService.getStep2(chatRequest));
    }

    @PostMapping(value = "/step3")
    public ResponseEntity<String> ChatStep3(@RequestBody ChatRequest chatRequest) {

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String strCurtime = d.format(timestamp).toString();
        System.out.println("[step3]" + strCurtime);

        return ResponseEntity.ok(chatgptService.getStep3(chatRequest));
    }

    @PostMapping(value = "/step4")
    public ResponseEntity<String> ChatStep4(@RequestBody ChatRequest chatRequest) {

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String strCurtime = d.format(timestamp).toString();
        System.out.println("[step4]" + strCurtime);

        return ResponseEntity.ok(chatgptService.getStep4(chatRequest));
    }
}