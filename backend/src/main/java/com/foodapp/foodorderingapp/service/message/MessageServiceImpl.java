package com.foodapp.foodorderingapp.service.message;

import com.foodapp.foodorderingapp.dto.message.CreateMessageRequest;
import com.foodapp.foodorderingapp.dto.message.UpdateMessageRequest;
import com.foodapp.foodorderingapp.entity.Chat;
import com.foodapp.foodorderingapp.entity.Message;
import com.foodapp.foodorderingapp.entity.User;
import com.foodapp.foodorderingapp.mapper.MessageMapper;
import com.foodapp.foodorderingapp.repository.ChatJpaRepository;
import com.foodapp.foodorderingapp.repository.MessageJpaRepository;
import com.foodapp.foodorderingapp.repository.UserJpaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MessageServiceImpl implements MessageService {
    private final MessageJpaRepository messageJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final ChatJpaRepository chatJpaRepository;
    private final MessageMapper messageMapper;
    private final SimpMessagingTemplate messagingTemplate;

    public MessageServiceImpl(MessageJpaRepository messageJpaRepository, UserJpaRepository userJpaRepository, ChatJpaRepository chatJpaRepository, MessageMapper messageMapper, SimpMessagingTemplate messagingTemplate) {
        this.messageJpaRepository = messageJpaRepository;
        this.userJpaRepository = userJpaRepository;
        this.chatJpaRepository = chatJpaRepository;
        this.messageMapper = messageMapper;
        this.messagingTemplate = messagingTemplate;
    }


    @Override
    public void sendToUser(Message message) {
        List<User> users = message.getChat().getUsers();
        for (User user : users) {
            messagingTemplate.convertAndSendToUser(user.getUsername(), "/queue/messages", message);
        }
    }

    @Override
    public Message save(CreateMessageRequest request) {
        User user = userJpaRepository.findById(request.getSenderId()).orElseThrow(()-> new IllegalArgumentException("Not found user"));
        Chat chat = chatJpaRepository.findById(request.getChatId()).orElseThrow(()-> new IllegalArgumentException("Not found chat"));
        return messageJpaRepository.save(
                messageMapper.toMessage(
                        request,
                        user,
                        chat
                )
        );
    }

    @Override
    public void update(UpdateMessageRequest request, Long id) {
        var msg = messageJpaRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(" Not found message"));
        messageMapper.updateMessage(request, msg);
        messageJpaRepository.save(msg);
    }
    @Override
    public void deleteById(Long id) {
        messageJpaRepository.deleteById(id);
    }
    @Override
    public Page<Message> getMessages(int offset, int limit, long chatId){
        return messageJpaRepository.findMessageByChatIdOrderBySendAtDesc(PageRequest.of(offset/limit, limit), chatId);
    }

    @Override
    public Optional<Message> getMessageById(long messageId) {
        return messageJpaRepository.findById( messageId);
    }
}
