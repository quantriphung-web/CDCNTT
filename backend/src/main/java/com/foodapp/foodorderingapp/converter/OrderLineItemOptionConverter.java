package com.foodapp.foodorderingapp.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodapp.foodorderingapp.entity.OrderLineItem_GroupOptionList;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.stereotype.Component;


@Converter
@Component
public class OrderLineItemOptionConverter implements AttributeConverter<OrderLineItem_GroupOptionList, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(OrderLineItem_GroupOptionList options) {
        try {
            return objectMapper.writeValueAsString(options);
        } catch (JsonProcessingException jpe) {

            return null;
        }
    }

    @Override
    public OrderLineItem_GroupOptionList convertToEntityAttribute(String value) {
        try {
            return objectMapper.readValue(value, OrderLineItem_GroupOptionList.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}