package org.com.car_repair.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.com.car_repair.entity.Material;
import org.com.car_repair.entity.RepairOrder;

public interface RepairOrderService {
    RepairOrder createRepairOrder(RepairOrder repairOrder);
    
    RepairOrder getRepairOrderById(Long id);
    
    List<RepairOrder> getAllRepairOrders();
    
    List<RepairOrder> getRepairOrdersByCustomerId(Long customerId);
    
    List<RepairOrder> getRepairOrdersByVehicleId(Long vehicleId);
    
    List<RepairOrder> getRepairOrdersByStatus(String status);
    
    List<RepairOrder> getRepairOrdersByMechanicId(Long mechanicId);
    
    RepairOrder updateRepairOrderStatus(Long orderId, String status);
    
    RepairOrder assignMechanicToOrder(Long orderId, Long mechanicId);
    
    RepairOrder removeMechanicFromOrder(Long orderId, Long mechanicId);
    
    RepairOrder addMaterialToOrder(Long orderId, Material material);
    
    List<Material> getOrderMaterials(Long orderId);
    
    RepairOrder completeRepairOrder(Long orderId);
    
    RepairOrder addCustomerFeedback(Long orderId, Integer rating, String feedback);
    
    void deleteRepairOrder(Long id);
    
    List<RepairOrder> getRepairOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    Map<String, Object> getVehicleRepairStatistics();
    
    Map<String, Object> getMonthlyCostBreakdown();
    
    Long countUncompletedOrders();
    
    List<RepairOrder> getNegativeFeedbackOrders();
    
    Map<String, Object> getRepairsByMechanicType();
} 