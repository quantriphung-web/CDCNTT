package com.foodapp.foodorderingapp.repository;

import com.foodapp.foodorderingapp.entity.OptionItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OptionItemJpaRepository extends JpaRepository<OptionItem, Long> {
}
