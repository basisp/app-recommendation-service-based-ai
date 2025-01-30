package com.example.springjwt.repository;

import com.example.springjwt.entity.UserDataEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface UserDataRepository extends JpaRepository<UserDataEntity, Long> {

    Optional<UserDataEntity> findByUsername(String username);
}
