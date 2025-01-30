package com.example.springjwt.repository;

import com.example.springjwt.entity.UserEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    Boolean existsByUsername(String username);

    Optional<UserEntity> findByUsername(String username);
}
