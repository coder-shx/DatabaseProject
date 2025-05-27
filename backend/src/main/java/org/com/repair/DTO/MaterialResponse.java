package org.com.repair.DTO;

import org.com.repair.entity.Material;
import org.com.repair.entity.RepairOrder;

public record MaterialResponse(
    Long id,
    String name,
    Double quantity,
    String unit,
    Double unitPrice,
    Double totalPrice,
    RepairOrder repairOrder
) {
    public MaterialResponse(Material material) {
        this(material.getId(), material.getName(), material.getQuantity(), 
             material.getUnit(), material.getUnitPrice(), material.getTotalPrice(), 
             material.getRepairOrder());
    }
} 