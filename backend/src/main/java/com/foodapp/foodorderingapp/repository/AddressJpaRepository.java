package com.foodapp.foodorderingapp.repository;

import com.foodapp.foodorderingapp.entity.Address;
import com.foodapp.foodorderingapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressJpaRepository extends JpaRepository<Address, Long> {
    List<Address> findByUsers(User user);
    Address findById(long id);

}
