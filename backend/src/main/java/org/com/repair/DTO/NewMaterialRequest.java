package org.com.repair.DTO;

public record NewMaterialRequest(
    String name,
    Double quantity,
    String unit,
    Double unitPrice,
    Double totalPrice,
    Long repairOrderId
) {
} 