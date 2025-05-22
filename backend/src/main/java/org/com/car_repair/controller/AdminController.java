package org.com.car_repair.controller;

import java.util.List;
import java.util.Map;

import org.com.car_repair.entity.RepairOrder;
import org.com.car_repair.entity.User;
import org.com.car_repair.entity.Vehicle;
import org.com.car_repair.service.RepairOrderService;
import org.com.car_repair.service.UserService;
import org.com.car_repair.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class AdminController {
    
    private final UserService userService;
    private final VehicleService vehicleService;
    private final RepairOrderService repairOrderService;
    
    @Autowired
    public AdminController(UserService userService, VehicleService vehicleService, RepairOrderService repairOrderService) {
        this.userService = userService;
        this.vehicleService = vehicleService;
        this.repairOrderService = repairOrderService;
    }
    
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
    
    @GetMapping("/mechanics")
    public ResponseEntity<List<User>> getAllMechanics() {
        return ResponseEntity.ok(userService.getUsersByRole("MECHANIC"));
    }
    
    @GetMapping("/vehicles")
    public ResponseEntity<List<Vehicle>> getAllVehicles() {
        return ResponseEntity.ok(List.of());
    }
    
    @GetMapping("/repair-orders")
    public ResponseEntity<List<RepairOrder>> getAllOrders() {
        return ResponseEntity.ok(repairOrderService.getAllRepairOrders());
    }
    
    @GetMapping("/stats/vehicle-repairs")
    public ResponseEntity<Map<String, Object>> getVehicleRepairStats() {
        return ResponseEntity.ok(repairOrderService.getVehicleRepairStatistics());
    }
    
    @GetMapping("/stats/monthly-costs")
    public ResponseEntity<Map<String, Object>> getMonthlyCostBreakdown() {
        return ResponseEntity.ok(repairOrderService.getMonthlyCostBreakdown());
    }
    
    @GetMapping("/stats/uncompleted-orders")
    public ResponseEntity<Long> getUncompletedOrders() {
        return ResponseEntity.ok(repairOrderService.countUncompletedOrders());
    }
    
    @GetMapping("/stats/negative-feedback")
    public ResponseEntity<List<RepairOrder>> getNegativeFeedbackOrders() {
        return ResponseEntity.ok(repairOrderService.getNegativeFeedbackOrders());
    }
    
    @GetMapping("/stats/repairs-by-mechanic-type")
    public ResponseEntity<Map<String, Object>> getRepairsByMechanicType() {
        return ResponseEntity.ok(repairOrderService.getRepairsByMechanicType());
    }
} 