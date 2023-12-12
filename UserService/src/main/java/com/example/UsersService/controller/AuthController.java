package com.example.UsersService.controller;
import com.example.UsersService.model.*;
import com.example.UsersService.repository.RoleRepository;
import com.example.UsersService.repository.UserRepository;
import com.example.UsersService.security.JWTGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
    @RequestMapping("/v1/users")
public class AuthController {

    private AuthenticationManager authenticationManager;
    private UserRepository userRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;
    private JWTGenerator jwtGenerator;
    private static final Logger LOG = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository,
                          RoleRepository roleRepository, PasswordEncoder passwordEncoder, JWTGenerator jwtGenerator) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtGenerator= jwtGenerator;

    }


    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterDto registerDto) {

        if (userRepository.existsByUsername(registerDto.getUsername())) {
            return new ResponseEntity<>("Username is taken!", HttpStatus.BAD_REQUEST);
        }

        UserEntity user = new UserEntity();
        user.setUsername(registerDto.getUsername());
        user.setPassword(passwordEncoder.encode((registerDto.getPassword())));
        List<String> roles=registerDto.getRoles();
        Role role;
        ArrayList<Role> rolesToAdd = new ArrayList<>();
        for (String s : roles) {
            role = roleRepository.findByName(s).get();
            System.out.println(role);
            rolesToAdd.add(role);
            System.out.println(rolesToAdd);
        }
        user.setRoles(rolesToAdd);

        userRepository.save(user);

        return new ResponseEntity<>("User registered success!", HttpStatus.OK);
    }



    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginDto loginDto){
        UserEntity user = userRepository.findByUsername(loginDto.getUsername()).get();
        if (!user.getActive()) {
            return new ResponseEntity<>(new AuthResponseDTO("Inactive user"), HttpStatus.BAD_REQUEST);
        }
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDto.getUsername(),
                        loginDto.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Date instance = new Date();
        user.setLast_login(instance);
        userRepository.save(user);
        String token = jwtGenerator.generateToken(authentication);
        return new ResponseEntity<>(new AuthResponseDTO(token), HttpStatus.OK);
    }

    @GetMapping("/hello")
    public String home(Authentication authentication){
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return "Hello, "+ userDetails.getUsername()+" has roles of "+ authentication.getAuthorities()+ "and password: "+userDetails.getPassword();
    }

//    @PostMapping("/token")
//    public String token(Authentication authentication){
//        LOG.debug("Token requested for user '{}'", authentication.getName());
//        String token = jwtGenerator.generateToken(authentication);
//        LOG.debug("Token granted {}", token);
//        return token;
//    }
}