package org.com.car_repair.repository;

import java.math.BigDecimal;
import java.util.List;

import org.com.car_repair.entity.Material;
import org.com.car_repair.entity.RepairOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {
    List<Material> findByRepairOrder(RepairOrder repairOrder);
    
    List<Material> findByNameContaining(String name);
    
    @Query("SELECT m.name, SUM(m.quantity) as totalUsed, SUM(m.totalPrice) as totalCost " +
           "FROM Material m GROUP BY m.name ORDER BY totalUsed DESC")
    List<Object[]> findMostUsedMaterials();
    
    List<Material> findByUnitPriceGreaterThan(BigDecimal price);
    
    @Query("SELECT m.name, AVG(m.unitPrice) as avgPrice " +
           "FROM Material m GROUP BY m.name ORDER BY avgPrice DESC")
    List<Object[]> findAverageMaterialPrices();
} 