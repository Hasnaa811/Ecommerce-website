package com.example.UsersService.service;

import com.example.UsersService.model.UserEntity;
import com.example.UsersService.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {


    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserEntity createUser(UserEntity user) {
        return userRepository.save(user);
    }

    public UserEntity getUserByUsername(String username){
        return userRepository.findByUsername(username).orElse(null);
    }
    public UserEntity getUserById(String id) {
        return userRepository.findById(id).orElse(null);
    }

    public UserEntity updateUser(UserEntity user1) {
        return userRepository.save(user1);
    }

    public List<UserEntity> getAllUser() {
        return userRepository.findAll();
    }


    public Page<UserEntity> findUsersWithPaginationAndSorting(int offset, int pageSize, String field){
        return userRepository.findAll(PageRequest.of(offset, pageSize).withSort(Sort.by(Sort.Direction.DESC,field)));
    }


    public void deleteUser(UserEntity user) {
        userRepository.delete(user);
    }


    public UserEntity applyPatchToUser(JsonPatch patch, UserEntity targetCustomer) throws JsonPatchException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode patched = patch.apply(objectMapper.convertValue(targetCustomer, JsonNode.class));
        return objectMapper.treeToValue(patched, UserEntity.class);
    }


}
