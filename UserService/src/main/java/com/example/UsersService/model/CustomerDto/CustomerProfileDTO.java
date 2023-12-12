package com.example.UsersService.model.CustomerDto;

import lombok.Data;

@Data
public class CustomerProfileDTO {
    private String id;
    private String first_name;
    private String last_name;
    private String username;
    private String email;
    private Integer creation_date;
    private Integer last_login;
    private Boolean valid_account;
    private Boolean active;

}
