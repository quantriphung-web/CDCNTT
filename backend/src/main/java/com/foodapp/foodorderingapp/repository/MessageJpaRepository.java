package com.foodapp.foodorderingapp.repository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import com.foodapp.foodorderingapp.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageJpaRepository extends JpaRepository<Message, Long> {
    Page<Message> findMessageByChatIdOrderBySendAtDesc(Pageable pageable, Long chatId);
}
