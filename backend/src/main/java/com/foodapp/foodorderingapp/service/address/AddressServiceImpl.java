package com.foodapp.foodorderingapp.service.address;

import com.foodapp.foodorderingapp.dto.address.CreateAddress;
import com.foodapp.foodorderingapp.entity.Address;
import com.foodapp.foodorderingapp.entity.User;
import com.foodapp.foodorderingapp.exception.DataNotFoundException;
import com.foodapp.foodorderingapp.repository.AddressJpaRepository;
import com.foodapp.foodorderingapp.repository.UserJpaRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {
    private final AddressJpaRepository addressJpaRepository;
    private final UserJpaRepository userJpaRepository;

    @Override
    @Transactional
    public Address addAddress(CreateAddress createAddress) {

        User user = userJpaRepository.findById(createAddress.getUserId()).orElseThrow(() ->
                new EntityNotFoundException(
                        "Cannot find user with id: "+ createAddress.getUserId()));

        Address address = Address.builder()
                .address(createAddress.getAddress())
                .provinceCode(createAddress.getProvinceCode())
                .districtCode(createAddress.getDistrictCode())
                .wardCode(createAddress.getWardCode())
                .build();
        user.getAddresses().add(address);
        userJpaRepository.save(user);
        return address;
    }
@Transactional
    @Override
    public List<Address> getAddresses(Long userId) {
        User user = userJpaRepository.findById(userId).orElseThrow(()-> new EntityNotFoundException("not found user"));
        Hibernate.initialize(user.getAddresses());
        return user.getAddresses().stream().toList();
    }
@Override
public Address findById(Long id) {
   return addressJpaRepository.findById(id).orElseThrow(()-> new DataNotFoundException("not found address"));
}


}
