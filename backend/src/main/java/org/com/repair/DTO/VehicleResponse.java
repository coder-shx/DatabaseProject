package org.com.repair.DTO;

import java.util.List;

import org.com.repair.entity.RepairOrder;
import org.com.repair.entity.User;
import org.com.repair.entity.Vehicle;

public record VehicleResponse(
    Long id,
    String licensePlate,
    String brand,
    String model,
    Integer year,
    String color,
    String vin,
    User user,
    List<RepairOrder> repairOrders
) {
    public VehicleResponse(Vehicle vehicle) {
        this(vehicle.getId(), vehicle.getLicensePlate(), vehicle.getBrand(), vehicle.getModel(), 
             vehicle.getYear(), vehicle.getColor(), vehicle.getVin(), vehicle.getUser(), 
             vehicle.getRepairOrders());
    }
} 