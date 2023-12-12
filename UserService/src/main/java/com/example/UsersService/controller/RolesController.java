package com.example.UsersService.controller;

import com.example.UsersService.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/users/roles")
public class RolesController {

    @Autowired
    private RoleService roleService;
    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/add/{roleName}")
    public ResponseEntity<String> addRole(@PathVariable("roleName") String roleName){
        if(roleService.roleExist(roleName)){
            return ResponseEntity.badRequest().body("Role Already Exist!");
        }
        roleService.addRole(roleName);
        return ResponseEntity.ok().body("Successfully Added Role : " + roleName);
    }
    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/delete/{roleName}")
    public ResponseEntity<String> deleteRole(@PathVariable("roleName") String roleName){
        if(!roleService.roleExist(roleName)){
            return ResponseEntity.badRequest().body("Role doesn't exist!");
        }
        roleService.deleteRole(roleName);
        return ResponseEntity.ok().body("Successfully Deleted Role : " + roleName);
    }
}
