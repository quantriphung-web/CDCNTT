package com.foodapp.foodorderingapp.service.chat;

import com.foodapp.foodorderingapp.dto.chat.ChatRequest;
import com.foodapp.foodorderingapp.entity.Chat;

import java.util.List;

public interface ChatService {
    void leaveChat(Long userId , Long chatId);

    void joinChat(Long userId, Long chatId);

    Chat createChat(ChatRequest chatRequest);

    Chat getById(Long id);

    List<Chat> getUserChat(Long userId);

    void deleteById(Long id);

}
