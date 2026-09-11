package com.foodapp.foodorderingapp.entity;

import com.foodapp.foodorderingapp.utils.JsonListConverter;
import jakarta.persistence.*;
import lombok.*;
import java.time.ZonedDateTime;
import java.util.List;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "messages")
@Entity
@Getter
@Setter
@Builder
public class Message {
    @Id
    @GeneratedValue
    private Long id;
    private String message;
    private ZonedDateTime sendAt;
    @ElementCollection
    @CollectionTable(name = "media", joinColumns = @JoinColumn(name = "message_id"))
    @Column(name = "url")
    private List<String> media;
    @ManyToOne
    @JoinColumn(name = "sender_id")
    private User sender;
    @ManyToOne
    @JoinColumn(name = "chat_id")
    private Chat chat;
    @ManyToMany
    @JoinTable(
            name = "message_read_by",
            joinColumns = @JoinColumn(name = "message_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> readBy;

}
