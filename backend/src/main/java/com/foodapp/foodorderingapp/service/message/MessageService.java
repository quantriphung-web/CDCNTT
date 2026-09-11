package com.foodapp.foodorderingapp.service.message;

import com.foodapp.foodorderingapp.dto.message.CreateMessageRequest;
import com.foodapp.foodorderingapp.dto.message.UpdateMessageRequest;
import com.foodapp.foodorderingapp.entity.Message;
import com.foodapp.foodorderingapp.repository.MessageJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

public interface MessageService {
    void sendToUser(Message message);
    Message save(CreateMessageRequest createMessageRequest);

    void update(UpdateMessageRequest updateMessageRequest, Long id);

    void deleteById(Long id);

    public Page<Message> getMessages(int offset, int limit, long chatId);
    Optional<Message> getMessageById(long messageId);
}
