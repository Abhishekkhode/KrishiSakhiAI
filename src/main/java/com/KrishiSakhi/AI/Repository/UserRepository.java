package com.KrishiSakhi.AI.Repository;

//package com.krishisakhi.app.repository;

import com.KrishiSakhi.AI.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Custom Query Method
    Optional<User> findByPhoneNumber(String phoneNumber);
    Optional<User> findByUsername(String username);
}