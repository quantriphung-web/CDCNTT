package com.foodapp.foodorderingapp.controller;

import com.foodapp.foodorderingapp.dto.message.CreateMessageRequest;
import com.foodapp.foodorderingapp.entity.Message;
import com.foodapp.foodorderingapp.repository.MessageJpaRepository;
import com.foodapp.foodorderingapp.repository.UserJpaRepository;
import com.foodapp.foodorderingapp.service.message.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    private final MessageService messageService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public MessageController(MessageJpaRepository messageJpaRepository, UserJpaRepository userJpaRepository, MessageService messageService) {
        this.messageService = messageService;
    }
    @PostMapping()
    @MessageMapping("/newMessage")
    public ResponseEntity<Message> getContent(@RequestBody CreateMessageRequest createMessageRequest){
         System.out.println("Go to message mapping");
        Message message = messageService.save(createMessageRequest);
        messageService.sendToUser(message);
        return ResponseEntity.ok(message);
    }
    @GetMapping()
    public ResponseEntity<List<Message>> getMessages(@RequestParam int offset, @RequestParam int limit, @RequestParam long chatId) {
        System.out.println("Get messages");
        return ResponseEntity.ok( messageService.getMessages(offset, limit, chatId).getContent().stream().toList());
    }
    @GetMapping("/{id}")
    public ResponseEntity<Message> getMessages(@PathVariable long id) {
        System.out.println("Get messages");
        return ResponseEntity.ok( messageService.getMessageById(id).get());
    }
}
