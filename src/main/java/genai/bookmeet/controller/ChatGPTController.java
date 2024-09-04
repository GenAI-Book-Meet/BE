package genai.bookmeet.controller;

import genai.bookmeet.dto.ChatRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import genai.bookmeet.service.ChatGPTService;

@RestController
@RequestMapping("api/chat")
public class ChatGPTController {

    ChatGPTService chatgptService;

    public ChatGPTController(ChatGPTService chatgptService) {
        this.chatgptService = chatgptService;
    }

    @PostMapping(value = "/step1")
    public ResponseEntity<String> ChatStep1(@RequestBody ChatRequest chatRequest) {

        return ResponseEntity.ok(chatgptService.getStep1(chatRequest));
    }

    @PostMapping(value = "/step2")
    public ResponseEntity<String> ChatStep2(@RequestBody ChatRequest chatRequest) {

        return ResponseEntity.ok(chatgptService.getStep2(chatRequest));
    }

    @PostMapping(value = "/step3")
    public ResponseEntity<String> ChatStep3(@RequestBody ChatRequest chatRequest) {

        return ResponseEntity.ok(chatgptService.getStep3(chatRequest));
    }

    @PostMapping(value = "/step4")
    public ResponseEntity<String> ChatStep4(@RequestBody ChatRequest chatRequest) {

        return ResponseEntity.ok(chatgptService.getStep4(chatRequest));
    }
}