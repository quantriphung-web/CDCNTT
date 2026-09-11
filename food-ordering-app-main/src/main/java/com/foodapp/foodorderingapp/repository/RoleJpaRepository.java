package com.foodapp.foodorderingapp.repository;

import com.foodapp.foodorderingapp.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleJpaRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);
}
