package org.com.car_repair.repository;

import java.util.List;
import java.util.Optional;

import org.com.car_repair.entity.User;
import org.com.car_repair.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    
    List<User> findByRole(UserRole role);
    
    boolean existsByUsername(String username);
    
    Optional<User> findByUsernameAndPassword(String username, String password);
} 