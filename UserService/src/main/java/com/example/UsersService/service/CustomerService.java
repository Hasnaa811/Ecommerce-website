package com.example.UsersService.service;

import com.example.UsersService.model.CustomerDto.CustomerRequestDTO;
import com.example.UsersService.model.UserEntity;
import com.example.UsersService.repository.UserRepository;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class CustomerService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ModelMapper modelMapper;
    // Admin + Manager
    public Optional<UserEntity> getCostumerByID(String id){
        Optional<UserEntity> costumerToFind = userRepository.findById(id);
        if(costumerToFind.isPresent()){
            return costumerToFind;
        }
        return Optional.empty();
    }
    public Optional<UserEntity> getCostumerByEmail(String email){
        Optional<UserEntity> costumerToFind = userRepository.findByEmail(email);
        if(costumerToFind.isPresent()){
            return costumerToFind;
        }
        return Optional.empty();
    }

    public Page<UserEntity> searchCustomer(int page, String order, int size, String query, String field){
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.fromString(order), field));
        return userRepository.searchCustomers(query, pageable);
    }

    //
    public boolean customerDtoExist(CustomerRequestDTO costumerRequestDto){
        Optional<UserEntity> costumerToAdd = userRepository.findByEmail(costumerRequestDto.getEmail());
        if(!costumerToAdd.isPresent()){
            return false; // user doesn't exist
        }
        return true;

    }


    public Optional<UserEntity> getCustomerByLogin(String email, String password){
        return userRepository.getCustomerByEmailAndPassword(email,password);
    }

    public void saveCustomer (UserEntity customer){
        userRepository.save(customer);
    }
    public String encodePassBcrypt(String pass ){
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(16);
        return encoder.encode(pass);
    }

    public Optional<UserEntity> getCostumerByUsername(String userName) {
        return userRepository.findByUsername(userName);
    }

    public void updateProfile(String customerID, ObjectNode customerData) {
        UserEntity existingUser = userRepository.findById(customerID).get();
        modelMapper.map(customerData, existingUser);
        userRepository.save(existingUser);
    }

    public Boolean customerByIdExist(String customerID) {
        Optional<UserEntity> costumerToAdd = userRepository.findById(customerID);
        if(costumerToAdd.isEmpty()){
            return false; // user doesn't exist
        }
        return true;
    }

    public boolean isEmailExist(String customerEmail) {
        Optional<UserEntity> costumerToFind = userRepository.findByEmail(customerEmail);
        if(costumerToFind.isPresent()){
            return true;
        }
        return false;
    }

    public boolean isUsernameExist(String customerUsername) {
        Optional<UserEntity> costumerToFind = userRepository.findByUsername(customerUsername);
        if(costumerToFind.isPresent()){
            return true;
        }
        return false;
    }
}
