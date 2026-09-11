package com.foodapp.foodorderingapp.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
public class ErrorDTO {
    private final String code;
    private final String message;
}
