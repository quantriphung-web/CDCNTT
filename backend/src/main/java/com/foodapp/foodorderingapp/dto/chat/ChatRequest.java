package com.foodapp.foodorderingapp.dto.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.foodapp.foodorderingapp.enumeration.ChatType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ChatRequest {
    @NotNull
    ChatType type;
    Long firstUserId;
    Long secondUserId;
}
