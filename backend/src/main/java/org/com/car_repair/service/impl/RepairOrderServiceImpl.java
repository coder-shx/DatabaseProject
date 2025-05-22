package org.com.car_repair.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.com.car_repair.entity.Material;
import org.com.car_repair.entity.Mechanic;
import org.com.car_repair.entity.RepairOrder;
import org.com.car_repair.entity.RepairStatus;
import org.com.car_repair.entity.User;
import org.com.car_repair.entity.Vehicle;
import org.com.car_repair.repository.MechanicRepository;
import org.com.car_repair.repository.RepairOrderRepository;
import org.com.car_repair.repository.UserRepository;
import org.com.car_repair.repository.VehicleRepository;
import org.com.car_repair.service.RepairOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RepairOrderServiceImpl implements RepairOrderService {

    private final RepairOrderRepository repairOrderRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final MechanicRepository mechanicRepository;

    @Autowired
    public RepairOrderServiceImpl(
            RepairOrderRepository repairOrderRepository,
            UserRepository userRepository,
            VehicleRepository vehicleRepository,
            MechanicRepository mechanicRepository) {
        this.repairOrderRepository = repairOrderRepository;
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
        this.mechanicRepository = mechanicRepository;
    }

    @Override
    @Transactional
    public RepairOrder createRepairOrder(RepairOrder repairOrder) {
        // 默认构造函数已经设置了创建时间和初始状态
        return repairOrderRepository.save(repairOrder);
    }

    @Override
    public RepairOrder getRepairOrderById(Long id) {
        return repairOrderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("工单不存在"));
    }

    @Override
    public List<RepairOrder> getAllRepairOrders() {
        return repairOrderRepository.findAll();
    }

    @Override
    public List<RepairOrder> getRepairOrdersByCustomerId(Long customerId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new NoSuchElementException("用户不存在"));
        return repairOrderRepository.findByCustomer(customer);
    }

    @Override
    public List<RepairOrder> getRepairOrdersByVehicleId(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new NoSuchElementException("车辆不存在"));
        return repairOrderRepository.findByVehicle(vehicle);
    }

    @Override
    public List<RepairOrder> getRepairOrdersByStatus(String status) {
        try {
            RepairStatus repairStatus = RepairStatus.valueOf(status.toUpperCase());
            return repairOrderRepository.findByStatus(repairStatus);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("无效的维修状态");
        }
    }

    @Override
    public List<RepairOrder> getRepairOrdersByMechanicId(Long mechanicId) {
        Mechanic mechanic = mechanicRepository.findById(mechanicId)
                .orElseThrow(() -> new NoSuchElementException("技工不存在"));
        return repairOrderRepository.findByMechanicsContains(mechanic);
    }

    @Override
    @Transactional
    public RepairOrder updateRepairOrderStatus(Long orderId, String status) {
        RepairOrder order = getRepairOrderById(orderId);
        try {
            RepairStatus repairStatus = RepairStatus.valueOf(status.toUpperCase());
            order.setStatus(repairStatus);
            
            // 如果状态是"已完成"，设置完成时间
            if (RepairStatus.COMPLETED.equals(repairStatus)) {
                order.setCompletedAt(LocalDateTime.now());
            }
            
            return repairOrderRepository.save(order);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("无效的维修状态");
        }
    }

    @Override
    @Transactional
    public RepairOrder assignMechanicToOrder(Long orderId, Long mechanicId) {
        RepairOrder order = getRepairOrderById(orderId);
        Mechanic mechanic = mechanicRepository.findById(mechanicId)
                .orElseThrow(() -> new NoSuchElementException("技工不存在"));
        
        order.addMechanic(mechanic);
        order.setStatus(RepairStatus.ASSIGNED);
        
        return repairOrderRepository.save(order);
    }

    @Override
    @Transactional
    public RepairOrder removeMechanicFromOrder(Long orderId, Long mechanicId) {
        RepairOrder order = getRepairOrderById(orderId);
        Mechanic mechanic = mechanicRepository.findById(mechanicId)
                .orElseThrow(() -> new NoSuchElementException("技工不存在"));
        
        order.removeMechanic(mechanic);
        
        // 如果没有技工了，更新订单状态为PENDING
        if (order.getMechanics().isEmpty()) {
            order.setStatus(RepairStatus.PENDING);
        }
        
        return repairOrderRepository.save(order);
    }

    @Override
    @Transactional
    public RepairOrder addMaterialToOrder(Long orderId, Material material) {
        RepairOrder order = getRepairOrderById(orderId);
        material.setRepairOrder(order);
        order.getMaterials().add(material);
        
        return repairOrderRepository.save(order);
    }

    @Override
    public List<Material> getOrderMaterials(Long orderId) {
        RepairOrder order = getRepairOrderById(orderId);
        return List.copyOf(order.getMaterials());
    }

    @Override
    @Transactional
    public RepairOrder completeRepairOrder(Long orderId) {
        return updateRepairOrderStatus(orderId, RepairStatus.COMPLETED.name());
    }

    @Override
    @Transactional
    public RepairOrder addCustomerFeedback(Long orderId, Integer rating, String feedback) {
        RepairOrder order = getRepairOrderById(orderId);
        order.setCustomerRating(rating);
        order.setCustomerFeedback(feedback);
        return repairOrderRepository.save(order);
    }

    @Override
    @Transactional
    public void deleteRepairOrder(Long id) {
        if (!repairOrderRepository.existsById(id)) {
            throw new NoSuchElementException("工单不存在");
        }
        repairOrderRepository.deleteById(id);
    }

    @Override
    public List<RepairOrder> getRepairOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return repairOrderRepository.findByCreatedAtBetween(startDate, endDate);
    }

    @Override
    public Map<String, Object> getVehicleRepairStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("vehicleRepairs", repairOrderRepository.findVehicleRepairStatistics());
        stats.put("totalRepairs", repairOrderRepository.count());
        return stats;
    }

    @Override
    public Map<String, Object> getMonthlyCostBreakdown() {
        Map<String, Object> breakdown = new HashMap<>();
        breakdown.put("monthlyCosts", repairOrderRepository.findMonthlyCostBreakdown());
        return breakdown;
    }

    @Override
    public Long countUncompletedOrders() {
        return repairOrderRepository.countUncompletedOrders();
    }

    @Override
    public List<RepairOrder> getNegativeFeedbackOrders() {
        // 负面评价定义为评分低于或等于2分
        return repairOrderRepository.findByCustomerRatingLessThanEqual(2);
    }

    @Override
    public Map<String, Object> getRepairsByMechanicType() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("mechanicTypeRepairs", repairOrderRepository.countCompletedRepairsByMechanicType());
        return stats;
    }
} 