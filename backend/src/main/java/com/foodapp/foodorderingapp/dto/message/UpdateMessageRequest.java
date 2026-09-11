package com.foodapp.foodorderingapp.dto.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class UpdateMessageRequest {
    private Long id;
    public String updatedMessage;

}
