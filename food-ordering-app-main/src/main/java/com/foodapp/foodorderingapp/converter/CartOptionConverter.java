package com.foodapp.foodorderingapp.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodapp.foodorderingapp.entity.CartItem_GroupOptionList;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;

@Converter
@Component
public class CartOptionConverter implements AttributeConverter<CartItem_GroupOptionList, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(CartItem_GroupOptionList options) {
        try {
            return objectMapper.writeValueAsString(options);
        } catch (JsonProcessingException jpe) {

            return null;
        }
    }

    @Override
    public CartItem_GroupOptionList convertToEntityAttribute(String value) {
        try {
            return objectMapper.readValue(value, CartItem_GroupOptionList.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}