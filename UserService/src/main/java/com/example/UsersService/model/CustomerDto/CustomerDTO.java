package com.example.UsersService.model.CustomerDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
@Builder
public class CustomerDTO {

    //private String id;
    private String first_name;
    private String last_name;
    private String username;
    private String email;
//    private String password;
//    private Integer creation_date;
//    private Integer last_login;
//    private Boolean valid_account;
//    private String username;
//    private Date last_update;

    private Boolean active;


}
