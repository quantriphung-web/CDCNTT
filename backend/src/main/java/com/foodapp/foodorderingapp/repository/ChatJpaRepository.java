package com.foodapp.foodorderingapp.repository;

import com.foodapp.foodorderingapp.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatJpaRepository extends JpaRepository<Chat, Long> {
    List<Chat> findByUsersId(Long userId);
}
