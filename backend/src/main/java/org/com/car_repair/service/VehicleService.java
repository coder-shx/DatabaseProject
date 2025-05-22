package org.com.car_repair.service;

import org.com.car_repair.entity.Vehicle;

public interface VehicleService {
    Vehicle getVehicleById(Long id);
    
    Vehicle updateVehicle(Vehicle vehicle);
    
    void deleteVehicle(Long id);
} 