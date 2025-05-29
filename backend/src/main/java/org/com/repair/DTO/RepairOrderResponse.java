package org.com.repair.DTO;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.com.repair.entity.RepairOrder;
import org.com.repair.entity.RepairOrder.RepairStatus;

public record RepairOrderResponse(
    Long id,
    String orderNumber,
    RepairStatus status,
    String description,
    Date createdAt,
    Date updatedAt,
    Date completedAt,
    Double laborCost,
    Double materialCost,
    Double totalCost,
    Long userId,
    String userName,
    String userPhone,
    String userEmail,
    Long vehicleId,
    String vehicleLicensePlate,
    String vehicleBrand,
    String vehicleModel,
    List<Long> technicianIds,
    List<String> technicianNames
) {
    public RepairOrderResponse(RepairOrder order) {
        this(
            order.getId(),
            order.getOrderNumber(),
            order.getStatus(),
            order.getDescription(),
            order.getCreatedAt(),
            order.getUpdatedAt(),
            order.getCompletedAt(),
            order.getLaborCost(),
            order.getMaterialCost(),
            order.getTotalCost(),
            order.getUser() != null ? order.getUser().getId() : null,
            order.getUser() != null ? order.getUser().getName() : null,
            order.getUser() != null ? order.getUser().getPhone() : null,
            order.getUser() != null ? order.getUser().getEmail() : null,
            order.getVehicle() != null ? order.getVehicle().getId() : null,
            order.getVehicle() != null ? order.getVehicle().getLicensePlate() : null,
            order.getVehicle() != null ? order.getVehicle().getBrand() : null,
            order.getVehicle() != null ? order.getVehicle().getModel() : null,
            order.getTechnicians() != null ? 
                order.getTechnicians().stream().map(t -> t.getId()).collect(Collectors.toList()) : 
                List.of(),
            order.getTechnicians() != null ? 
                order.getTechnicians().stream().map(t -> t.getName()).collect(Collectors.toList()) : 
                List.of()
        );
    }
} 