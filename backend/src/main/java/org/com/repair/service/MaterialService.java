package org.com.repair.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.com.repair.DTO.MaterialResponse;
import org.com.repair.DTO.NewMaterialRequest;
import org.com.repair.entity.Material;
import org.com.repair.entity.RepairOrder;
import org.com.repair.repository.MaterialRepository;
import org.com.repair.repository.RepairOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MaterialService {
    
    private final MaterialRepository materialRepository;
    private final RepairOrderRepository repairOrderRepository;
    
    public MaterialService(MaterialRepository materialRepository, RepairOrderRepository repairOrderRepository) {
        this.materialRepository = materialRepository;
        this.repairOrderRepository = repairOrderRepository;
    }
    
    @Transactional
    public MaterialResponse addMaterial(NewMaterialRequest request) {
        RepairOrder repairOrder = repairOrderRepository.findById(request.repairOrderId())
                .orElseThrow(() -> new RuntimeException("维修工单不存在"));
        
        Material material = new Material();
        material.setName(request.name());
        material.setQuantity(request.quantity());
        material.setUnit(request.unit());
        material.setUnitPrice(request.unitPrice());
        
        // 计算总价
        double totalPrice = request.quantity() * request.unitPrice();
        material.setTotalPrice(totalPrice);
        
        material.setRepairOrder(repairOrder);
        
        Material savedMaterial = materialRepository.save(material);
        
        // 更新维修工单的材料费用
        updateRepairOrderMaterialCost(repairOrder.getId());
        
        return new MaterialResponse(savedMaterial);
    }
    
    public Optional<MaterialResponse> getMaterialById(Long id) {
        return materialRepository.findById(id)
                .map(MaterialResponse::new);
    }
    
    public List<MaterialResponse> getMaterialsByRepairOrderId(Long repairOrderId) {
        return materialRepository.findByRepairOrderId(repairOrderId).stream()
                .map(MaterialResponse::new)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public MaterialResponse updateMaterial(Long id, NewMaterialRequest request) {
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("材料不存在"));
        
        // 如果维修工单发生变化
        if (!material.getRepairOrder().getId().equals(request.repairOrderId())) {
            RepairOrder newRepairOrder = repairOrderRepository.findById(request.repairOrderId())
                    .orElseThrow(() -> new RuntimeException("维修工单不存在"));
            
            Long oldRepairOrderId = material.getRepairOrder().getId();
            material.setRepairOrder(newRepairOrder);
            
            // 保存后需要更新两个维修工单的材料费用
            Material updatedMaterial = materialRepository.save(material);
            updateRepairOrderMaterialCost(oldRepairOrderId);
            updateRepairOrderMaterialCost(request.repairOrderId());
            
            return new MaterialResponse(updatedMaterial);
        }
        
        material.setName(request.name());
        material.setQuantity(request.quantity());
        material.setUnit(request.unit());
        material.setUnitPrice(request.unitPrice());
        
        // 计算总价
        double totalPrice = request.quantity() * request.unitPrice();
        material.setTotalPrice(totalPrice);
        
        Material updatedMaterial = materialRepository.save(material);
        
        // 更新维修工单的材料费用
        updateRepairOrderMaterialCost(request.repairOrderId());
        
        return new MaterialResponse(updatedMaterial);
    }
    
    @Transactional
    public boolean deleteMaterial(Long id) {
        Optional<Material> materialOpt = materialRepository.findById(id);
        if (materialOpt.isPresent()) {
            Long repairOrderId = materialOpt.get().getRepairOrder().getId();
            materialRepository.deleteById(id);
            
            // 更新维修工单的材料费用
            updateRepairOrderMaterialCost(repairOrderId);
            
            return true;
        }
        return false;
    }
    
    public List<MaterialResponse> getAllMaterials() {
        return materialRepository.findAll().stream()
                .map(MaterialResponse::new)
                .collect(Collectors.toList());
    }
    
    public List<MaterialResponse> getMaterialsByName(String name) {
        return materialRepository.findByNameContaining(name).stream()
                .map(MaterialResponse::new)
                .collect(Collectors.toList());
    }
    
    public List<MaterialResponse> getMaterialsByPriceRange(Double minPrice, Double maxPrice) {
        return materialRepository.findByUnitPriceBetween(minPrice, maxPrice).stream()
                .map(MaterialResponse::new)
                .collect(Collectors.toList());
    }
    
    public Double calculateTotalMaterialCost(Long repairOrderId) {
        return materialRepository.calculateTotalMaterialCost(repairOrderId);
    }
    
    public List<Object[]> getMostUsedMaterials() {
        return materialRepository.findMostUsedMaterials();
    }
    
    public List<Object[]> getMostUsedMaterialsByRepairType(String description) {
        return materialRepository.findMostUsedMaterialsByRepairType(description);
    }
    
    @Transactional
    private void updateRepairOrderMaterialCost(Long repairOrderId) {
        Double materialCost = materialRepository.calculateTotalMaterialCost(repairOrderId);
        if (materialCost == null) {
            materialCost = 0.0;
        }
        
        RepairOrder repairOrder = repairOrderRepository.findById(repairOrderId)
                .orElseThrow(() -> new RuntimeException("维修工单不存在"));
        
        repairOrder.setMaterialCost(materialCost);
        
        // 更新总费用
        Double laborCost = repairOrder.getLaborCost() != null ? repairOrder.getLaborCost() : 0.0;
        repairOrder.setTotalCost(laborCost + materialCost);
        
        repairOrderRepository.save(repairOrder);
    }
} 