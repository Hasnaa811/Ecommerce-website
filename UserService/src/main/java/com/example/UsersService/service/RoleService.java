package com.example.UsersService.service;

import com.example.UsersService.model.Role;
import com.example.UsersService.repository.RoleRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class RoleService {
    @Autowired
    private RoleRepository roleRepository;
    public boolean roleExist(String roleName){
        Optional<Role> role = roleRepository.findByName(roleName);
        if(role.isEmpty()){
            return false;
        }
        return true;
    }

    public void addRole(String roleName){
        roleRepository.save(new Role(roleName));
    }

    public void deleteRole(String roleName) {
        Optional<Role> role = roleRepository.findByName(roleName);
        roleRepository.delete(role.get());
    }
}
