package com.foodapp.foodorderingapp.mapper;

import com.foodapp.foodorderingapp.dto.message.CreateMessageRequest;
import com.foodapp.foodorderingapp.dto.message.UpdateMessageRequest;
import com.foodapp.foodorderingapp.entity.Chat;
import com.foodapp.foodorderingapp.entity.Message;
import com.foodapp.foodorderingapp.entity.User;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

@Component
public class MessageMapper {
    private final ModelMapper modelMapper;

    public MessageMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public Message toMessage(CreateMessageRequest messageRequest, User sender, Chat chat){
        Message message = Message.builder()
                .sendAt(ZonedDateTime.now())
                .message(messageRequest.getMessage())
                .sender(sender)
                .chat(chat)
                .build();
        return message;
    }
    public Message updateMessage(UpdateMessageRequest updateMessageRequest, Message message){
        message.setMessage(updateMessageRequest.getUpdatedMessage());
        return message;
    }
}
