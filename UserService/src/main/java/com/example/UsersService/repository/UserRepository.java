package com.example.UsersService.repository;

import com.example.UsersService.model.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {
    public UserEntity findByUsernameAndPassword(String username, String password);
    Optional<UserEntity> findByUsername(String username);
    Boolean existsByUsername(String username);

    ////////////////////********** Customer
    public Optional<UserEntity> findByEmail(String email);
    public Optional<UserEntity> getCustomerByEmailAndPassword(String email, String password);

    @Query("SELECT c FROM UserEntity c WHERE LOWER(c.firstName) LIKE LOWER(CONCAT('%', :query, '%')) ")
    public Page<UserEntity> searchCustomers(@Param("query") String query, Pageable pageable);

}
