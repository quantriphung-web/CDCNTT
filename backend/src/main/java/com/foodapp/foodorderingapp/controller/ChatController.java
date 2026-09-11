package com.foodapp.foodorderingapp.controller;

import com.foodapp.foodorderingapp.dto.chat.ChatRequest;
import com.foodapp.foodorderingapp.service.chat.ChatService;
import com.foodapp.foodorderingapp.service.chat.ChatServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.ACCEPTED;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/chats")
public class ChatController {
    private final ChatService chatService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getChat(
            @PathVariable Long id
    ) {
        return ResponseEntity
                .ok(chatService.getById(id));
    }
    @GetMapping("/user/{id}")
    public ResponseEntity<?> getChatsByUserId(
            @PathVariable Long id
    ) {
        return ResponseEntity
                .ok(chatService.getUserChat(id));
    }
    @PostMapping()
    public ResponseEntity<?> createChat(
            @RequestBody @Valid ChatRequest chatRequest
    ) {
        return ResponseEntity.status(201)
                .body(chatService.createChat(chatRequest));
    }

    @DeleteMapping("/{id}")
    public void deleteChat(
            @PathVariable Long id
    ) {
        chatService.deleteById(id);
    }

    @PutMapping("/leave")
    @ResponseStatus(ACCEPTED)
    public void leaveChat(
            @RequestParam(value = "chatId") Long chatId,
            @RequestParam(value = "userId") Long userId
    ) {
        chatService.leaveChat(userId, chatId);
    }

    @PutMapping("/join")
    @ResponseStatus(ACCEPTED)
    public void joinChat(
            @RequestParam(value = "chatId") Long chatId,
            @RequestParam(value = "userId") Long userId
    ) {
        chatService.joinChat(userId, chatId);
    }
}
