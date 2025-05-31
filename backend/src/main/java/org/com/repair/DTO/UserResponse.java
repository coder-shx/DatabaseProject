package org.com.repair.DTO;

import java.util.List;
import java.util.stream.Collectors;

import org.com.repair.entity.User;

public record UserResponse(
    Long id,
    String username,
    String name,
    String phone,
    String email,
    String address,
    List<Long> vehicleIds,
    List<Long> repairOrderIds
) {
    public UserResponse(User user) {
        this(
            user.getId(), 
            user.getUsername(), 
            user.getName(), 
            user.getPhone(), 
            user.getEmail(), 
            user.getAddress(),
            user.getVehicles() != null ? 
                user.getVehicles().stream().map(vehicle -> vehicle.getId()).collect(Collectors.toList()) : 
                List.of(),
            user.getRepairOrders() != null ? 
                user.getRepairOrders().stream().map(order -> order.getId()).collect(Collectors.toList()) : 
                List.of()
        );
    }
}
