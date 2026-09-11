package com.foodapp.foodorderingapp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.TimeZone;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "chats")
@Entity
@Getter
@Builder
public class Chat {
    @Id
    @GeneratedValue
    private Long id;
    @OneToMany(fetch = FetchType.EAGER)
    private List<User> users;
    private ZonedDateTime createdTime;
}
