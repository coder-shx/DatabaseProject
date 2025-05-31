package org.com.repair.service;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.com.repair.DTO.NewRepairOrderRequest;
import org.com.repair.DTO.RepairOrderResponse;
import org.com.repair.entity.RepairOrder;
import org.com.repair.entity.RepairOrder.RepairStatus;
import org.com.repair.entity.Technician;
import org.com.repair.entity.User;
import org.com.repair.entity.Vehicle;
import org.com.repair.repository.RepairOrderRepository;
import org.com.repair.repository.TechnicianRepository;
import org.com.repair.repository.UserRepository;
import org.com.repair.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RepairOrderService {
    
    private final RepairOrderRepository repairOrderRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final TechnicianRepository technicianRepository;
    
    public RepairOrderService(
            RepairOrderRepository repairOrderRepository,
            UserRepository userRepository,
            VehicleRepository vehicleRepository,
            TechnicianRepository technicianRepository) {
        this.repairOrderRepository = repairOrderRepository;
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
        this.technicianRepository = technicianRepository;
    }
    
    @Transactional
    public RepairOrderResponse createRepairOrder(NewRepairOrderRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
                .orElseThrow(() -> new RuntimeException("车辆不存在"));
        
        // 验证车辆是否属于该用户
        if (!vehicle.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("车辆不属于该用户");
        }
        
        // 分配维修技师
        Set<Technician> technicians = new HashSet<>();
        if (request.technicianIds() != null && !request.technicianIds().isEmpty()) {
            for (Long technicianId : request.technicianIds()) {
                Technician technician = technicianRepository.findById(technicianId)
                        .orElseThrow(() -> new RuntimeException("技师不存在: " + technicianId));
                technicians.add(technician);
            }
        }
        
        // 创建工单
        RepairOrder order = new RepairOrder();
        order.setOrderNumber(generateOrderNumber());
        order.setStatus(request.status() != null ? request.status() : RepairStatus.PENDING);
        order.setDescription(request.description());
        
        Date now = new Date();
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        
        order.setUser(user);
        order.setVehicle(vehicle);
        order.setTechnicians(technicians);
        
        // 如果有提供费用信息，则设置
        if (request.laborCost() != null) {
            order.setLaborCost(request.laborCost());
        }
        if (request.materialCost() != null) {
            order.setMaterialCost(request.materialCost());
        }
        if (request.totalCost() != null) {
            order.setTotalCost(request.totalCost());
        } else if (request.laborCost() != null && request.materialCost() != null) {
            order.setTotalCost(request.laborCost() + request.materialCost());
        }
        
        RepairOrder savedOrder = repairOrderRepository.save(order);
        return new RepairOrderResponse(savedOrder);
    }
    
    public Optional<RepairOrderResponse> getRepairOrderById(Long id) {
        return repairOrderRepository.findById(id)
                .map(RepairOrderResponse::new);
    }
    
    public List<RepairOrderResponse> getRepairOrdersByUserId(Long userId) {
        return repairOrderRepository.findByUserIdWithDetails(userId).stream()
                .map(RepairOrderResponse::new)
                .collect(Collectors.toList());
    }
    
    public List<RepairOrderResponse> getRepairOrdersByVehicleId(Long vehicleId) {
        return repairOrderRepository.findByVehicleId(vehicleId).stream()
                .map(RepairOrderResponse::new)
                .collect(Collectors.toList());
    }
    
    public List<RepairOrderResponse> getRepairOrdersByTechnician(Long technicianId) {
        return repairOrderRepository.findByTechnicianId(technicianId).stream()
                .map(RepairOrderResponse::new)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public RepairOrderResponse updateRepairOrderStatus(Long id, RepairStatus status) {
        RepairOrder order = repairOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("维修工单不存在"));
        
        order.setStatus(status);
        order.setUpdatedAt(new Date());
        
        if (status == RepairStatus.COMPLETED) {
            order.setCompletedAt(new Date());
        }
        
        RepairOrder updatedOrder = repairOrderRepository.save(order);
        return new RepairOrderResponse(updatedOrder);
    }
    
    @Transactional
    public RepairOrderResponse updateRepairOrder(Long id, NewRepairOrderRequest request) {
        RepairOrder order = repairOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("维修工单不存在"));
        
        if (request.status() != null) {
            order.setStatus(request.status());
        }
        
        if (request.description() != null) {
            order.setDescription(request.description());
        }
        
        if (request.vehicleId() != null && !order.getVehicle().getId().equals(request.vehicleId())) {
            Vehicle vehicle = vehicleRepository.findById(request.vehicleId())
                    .orElseThrow(() -> new RuntimeException("车辆不存在"));
            order.setVehicle(vehicle);
        }
        
        if (request.technicianIds() != null) {
            Set<Technician> technicians = new HashSet<>();
            for (Long technicianId : request.technicianIds()) {
                Technician technician = technicianRepository.findById(technicianId)
                        .orElseThrow(() -> new RuntimeException("技师不存在: " + technicianId));
                technicians.add(technician);
            }
            order.setTechnicians(technicians);
        }
        
        if (request.laborCost() != null) {
            order.setLaborCost(request.laborCost());
        }
        
        if (request.materialCost() != null) {
            order.setMaterialCost(request.materialCost());
        }
        
        if (request.totalCost() != null) {
            order.setTotalCost(request.totalCost());
        } else if (request.laborCost() != null || request.materialCost() != null) {
            double laborCost = request.laborCost() != null ? request.laborCost() : (order.getLaborCost() != null ? order.getLaborCost() : 0);
            double materialCost = request.materialCost() != null ? request.materialCost() : (order.getMaterialCost() != null ? order.getMaterialCost() : 0);
            order.setTotalCost(laborCost + materialCost);
        }
        
        order.setUpdatedAt(new Date());
        
        RepairOrder updatedOrder = repairOrderRepository.save(order);
        return new RepairOrderResponse(updatedOrder);
    }
    
    @Transactional
    public boolean deleteRepairOrder(Long id) {
        if (repairOrderRepository.existsById(id)) {
            repairOrderRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    public List<RepairOrderResponse> getAllRepairOrders() {
        return repairOrderRepository.findAll().stream()
                .map(RepairOrderResponse::new)
                .collect(Collectors.toList());
    }
    
    public List<RepairOrderResponse> getRepairOrdersByStatus(RepairStatus status) {
        return repairOrderRepository.findByStatus(status).stream()
                .map(RepairOrderResponse::new)
                .collect(Collectors.toList());
    }
    
    public List<RepairOrderResponse> getUncompletedRepairOrders() {
        return repairOrderRepository.findUncompletedOrders().stream()
                .map(RepairOrderResponse::new)
                .collect(Collectors.toList());
    }
    
    public Object[] getQuarterlyCostAnalysis(int year, int quarter) {
        return repairOrderRepository.getQuarterlyCostAnalysis(year, quarter);
    }
    
    public Object[] getMonthlyCostAnalysis(int year, int month) {
        return repairOrderRepository.getMonthlyCostAnalysis(year, month);
    }
    
    public List<Object[]> getOrdersWithNegativeFeedback(int maxRating) {
        return repairOrderRepository.findOrdersWithNegativeFeedback(maxRating);
    }
    
    public List<Object[]> getTaskStatisticsBySkillType(Date startDate, Date endDate) {
        return repairOrderRepository.getTaskStatisticsBySkillType(startDate, endDate);
    }
    
    private String generateOrderNumber() {
        return "RO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    // 新增的统计方法
    public long getTotalOrdersCount() {
        return repairOrderRepository.count();
    }
    
    public long getPendingOrdersCount() {
        return repairOrderRepository.countByStatus(RepairStatus.PENDING);
    }
    
    public long getCompletedOrdersCount() {
        return repairOrderRepository.countByStatus(RepairStatus.COMPLETED);
    }
} 