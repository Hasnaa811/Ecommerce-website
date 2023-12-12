package com.example.UsersService.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name="roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    //TODO : change type of role to long
    // TODO : add roles (Admin,Manager,Customer)
    // TODO : add user with post method with entity
    // TODO : CV
    private String id;
    private String name;

    public Role(String roleName) {
        this.name = roleName;
    }
}
