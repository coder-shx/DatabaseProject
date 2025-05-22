package org.com.car_repair.service;

import java.util.List;

import org.com.car_repair.entity.User;
import org.com.car_repair.entity.Vehicle;

public interface UserService {
    User registerUser(User user);
    
    User updateUser(User user);
    
    User getUserById(Long id);
    
    User getUserByUsername(String username);
    
    List<User> getAllUsers();
    
    List<User> getUsersByRole(String role);
    
    boolean checkUsernameExists(String username);
    
    User authenticateUser(String username, String password);
    
    void deleteUser(Long id);
    
    Vehicle addVehicleToUser(Long userId, Vehicle vehicle);
    
    List<Vehicle> getUserVehicles(Long userId);
} 