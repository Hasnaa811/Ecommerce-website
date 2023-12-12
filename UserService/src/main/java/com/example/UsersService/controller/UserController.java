package com.example.UsersService.controller;

import com.example.UsersService.model.UserEntity;
import com.example.UsersService.model.UserDTO;
import com.example.UsersService.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/v1/users")
public class UserController {
    private UserService userService;
    private ModelMapper modelMapper;
    private PasswordEncoder passwordEncoder;

    public UserController(UserService userService, ModelMapper modelMapper, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
    }


    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome this endpoint is not secure";
    }

    //Add a new user
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping()
    public String createUser(@RequestBody UserEntity user) {
        System.out.println(user);
        userService.createUser(user);
        return "User created successfully!";
    }

    //Get all the users list
    @GetMapping("/All")
    @PreAuthorize("hasAuthority('MANAGER')")
    public List<UserEntity> getAllUsers() {
        return userService.getAllUser();
    }

    //Get all the users list with pagination and sorting
    @GetMapping()
    @PreAuthorize("hasAuthority('MANAGER')")
    public Page<UserEntity> getUsersWithPaginationAndSort(@RequestParam(defaultValue ="0") int offset, @RequestParam(defaultValue ="10") int pageSize, @RequestParam(defaultValue ="username") String field) {
        return userService.findUsersWithPaginationAndSorting(offset, pageSize, field);
    }

    //Get a user by ID
    @PreAuthorize("hasAuthority('MANAGER')")
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable String id) {
        UserEntity user = userService.getUserById(id);
        UserDTO userDTO = modelMapper.map(user, UserDTO.class);

        if (user == null ) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(userDTO);
    }

    //Get the user's data depending on the search query value

    //Update the user's data
    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<UserEntity> updateUser(@RequestBody UserEntity user, @PathVariable String id) {
        UserEntity user1 = userService.getUserById(id);
        if (user == null ) {
            return ResponseEntity.notFound().build();
        }
        user1.setFirstName(user.getFirstName());
        user1.setLastName(user.getLastName());
        user1.setEmail(user.getEmail());
        user1.setRole(user.getRole());
        user1.setUsername(user.getUsername());
        user1.setPassword(passwordEncoder.encode(user.getPassword()));

        UserEntity updatedUser = userService.updateUser(user1);
        return ResponseEntity.ok(updatedUser);
    }

    //Update only some fields in the user's data
    @PreAuthorize("hasAuthority('ADMIN')")

    @PatchMapping(path = "/{id}", consumes = "application/json-patch+json")
    public ResponseEntity<UserEntity> updateUser(
            @PathVariable String id,
            @RequestBody JsonPatch patch) throws JsonPatchException, JsonProcessingException {

        UserEntity user = userService.getUserById(id);
        UserEntity customerPatched = userService.applyPatchToUser(patch, user);
        return ResponseEntity.ok(customerPatched);
    }

    //Delete a user
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<UserEntity> deleteUser(@PathVariable String id) {
        UserEntity user = userService.getUserById(id);
        if (user == null || !user.getActive())
            return ResponseEntity.notFound().build();
        user.setActive(false);
        //userService.deleteUser(user);
        return ResponseEntity.ok().build();
    }

////////////////



}
