package org.com.car_repair.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.com.car_repair.entity.RepairOrder;
import org.com.car_repair.service.RepairOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/repair-orders")
@CrossOrigin
public class RepairOrderController {

    private final RepairOrderService repairOrderService;
    
    @Autowired
    public RepairOrderController(RepairOrderService repairOrderService) {
        this.repairOrderService = repairOrderService;
    }
    
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody RepairOrder order, @RequestHeader("Authorization") String token) {
        try {
            // 实际项目中应从令牌中提取用户ID
            RepairOrder createdOrder = repairOrderService.createRepairOrder(order);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrder(@PathVariable Long id) {
        try {
            RepairOrder order = repairOrderService.getRepairOrderById(id);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
    
    @GetMapping("/user")
    public ResponseEntity<List<RepairOrder>> getUserOrders(@RequestHeader("Authorization") String token) {
        // 实际项目中应从令牌中提取用户ID
        Long userId = 1L;
        List<RepairOrder> orders = repairOrderService.getRepairOrdersByCustomerId(userId);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/mechanic")
    public ResponseEntity<List<RepairOrder>> getMechanicOrders(@RequestHeader("Authorization") String token) {
        // 实际项目中应从令牌中提取技工ID
        Long mechanicId = 1L;
        List<RepairOrder> orders = repairOrderService.getRepairOrdersByMechanicId(mechanicId);
        return ResponseEntity.ok(orders);
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> statusData) {
        try {
            String status = statusData.get("status");
            RepairOrder updatedOrder = repairOrderService.updateRepairOrderStatus(id, status);
            return ResponseEntity.ok(updatedOrder);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    @PostMapping("/{orderId}/mechanics/{mechanicId}")
    public ResponseEntity<?> assignMechanic(@PathVariable Long orderId, @PathVariable Long mechanicId) {
        try {
            RepairOrder updatedOrder = repairOrderService.assignMechanicToOrder(orderId, mechanicId);
            return ResponseEntity.ok(updatedOrder);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    @DeleteMapping("/{orderId}/mechanics/{mechanicId}")
    public ResponseEntity<?> removeMechanic(@PathVariable Long orderId, @PathVariable Long mechanicId) {
        try {
            RepairOrder updatedOrder = repairOrderService.removeMechanicFromOrder(orderId, mechanicId);
            return ResponseEntity.ok(updatedOrder);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    @PutMapping("/{id}/complete")
    public ResponseEntity<?> completeOrder(@PathVariable Long id) {
        try {
            RepairOrder completedOrder = repairOrderService.completeRepairOrder(id);
            return ResponseEntity.ok(completedOrder);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    @PostMapping("/{id}/feedback")
    public ResponseEntity<?> addOrderFeedback(@PathVariable Long id, @RequestBody Map<String, Object> feedbackData) {
        try {
            Integer rating = (Integer) feedbackData.get("rating");
            String feedback = (String) feedbackData.get("feedback");
            RepairOrder updatedOrder = repairOrderService.addCustomerFeedback(id, rating, feedback);
            return ResponseEntity.ok(updatedOrder);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
} 