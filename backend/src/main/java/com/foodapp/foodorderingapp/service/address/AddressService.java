package com.foodapp.foodorderingapp.service.address;

import com.foodapp.foodorderingapp.dto.address.CreateAddress;
import com.foodapp.foodorderingapp.entity.Address;
import com.foodapp.foodorderingapp.entity.User;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AddressService {
    Address addAddress(CreateAddress createAddress);

    List<Address> getAddresses(Long userId);

    Address findById(Long id);

}
