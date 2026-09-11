package com.foodapp.foodorderingapp.service.chat;

import com.foodapp.foodorderingapp.dto.chat.ChatRequest;
import com.foodapp.foodorderingapp.entity.Chat;
import com.foodapp.foodorderingapp.entity.User;
import com.foodapp.foodorderingapp.repository.ChatJpaRepository;
import com.foodapp.foodorderingapp.repository.UserJpaRepository;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ChatServiceImpl implements ChatService {
    private final ChatJpaRepository chatJpaRepository;
    private final UserJpaRepository userJpaRepository;

    public ChatServiceImpl(ChatJpaRepository chatJpaRepository, UserJpaRepository userJpaRepository) {
        this.chatJpaRepository = chatJpaRepository;
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    public void leaveChat(Long userId, Long chatId) {
        Optional<Chat> chatResponse = chatJpaRepository.findById(chatId);
        Optional<User> userResponse = userJpaRepository.findById(userId);
        Chat chat;
        User user;
        if(chatResponse.isPresent() && userResponse.isPresent()){
            chat = chatResponse.get();
            user = userResponse.get();
            List<User> newUsers = chat.getUsers().stream().filter(x -> !Objects.equals(x.getId(), user.getId())).toList();
            chat.setUsers(newUsers);
            chatJpaRepository.save(chat);
        }
        else throw new IllegalArgumentException("Error when user join chat");

    }

    @Override
    public void joinChat(Long userId, Long chatId) {
        Optional<Chat> chatResponse = chatJpaRepository.findById(chatId);
        Optional<User> userResponse = userJpaRepository.findById(userId);
        Chat chat;
        User user;
        if(chatResponse.isPresent() && userResponse.isPresent()){
            chat = chatResponse.get();
            user = userResponse.get();
            chat.getUsers().add(user);
            chatJpaRepository.save(chat);
        }
        else throw new IllegalArgumentException("Error when user join chat");
    }

    @Override
    public Chat createChat(ChatRequest chatRequest) {
        List<User> users = new ArrayList<>();
        User user1 = userJpaRepository.findById(chatRequest.getFirstUserId())
                .orElseThrow(()-> new IllegalArgumentException("Not found user"));
        users.add(user1);
        User user2 = userJpaRepository.findById(chatRequest.getSecondUserId())
                .orElseThrow(()-> new IllegalArgumentException("Not found user"));
        users.add(user2);
        return chatJpaRepository.save(Chat.builder()
                .createdTime(ZonedDateTime.now())
                .users(users)
                .build());
    }

    @Override
    public Chat getById(Long id) {
       return chatJpaRepository.findById(id).orElseThrow(()-> new IllegalArgumentException("Not found chat "));
    }

    @Override
    public List<Chat> getUserChat(Long userId) {
        return chatJpaRepository.findByUsersId(userId);
    }


    @Override
    public void deleteById(Long id) {
        chatJpaRepository.deleteById(id);
    }
}
