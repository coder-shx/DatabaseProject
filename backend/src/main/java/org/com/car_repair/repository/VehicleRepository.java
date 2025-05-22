package org.com.car_repair.repository;

import java.util.List;
import java.util.Optional;

import org.com.car_repair.entity.User;
import org.com.car_repair.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findByUser(User user);
    
    Optional<Vehicle> findByLicensePlate(String licensePlate);
    
    List<Vehicle> findByBrandAndModel(String brand, String model);
} 