package com.example.UsersService.model.CustomerDto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class CustomerRequestDTO {
     private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Integer creation_date;
    private String username;
   // private Integer last_login;
   // private Boolean valid_account;
   // private Boolean active;
}