package org.com.repair.repository;

import java.util.List;

import org.com.repair.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {
    
    /**
     * 根据维修工单ID查找材料列表
     * @param repairOrderId 维修工单ID
     * @return 材料列表
     */
    List<Material> findByRepairOrderId(Long repairOrderId);
    
    /**
     * 根据材料名称查找材料列表
     * @param name 材料名称
     * @return 材料列表
     */
    List<Material> findByNameContaining(String name);
    
    /**
     * 查找特定价格范围内的材料列表
     * @param minUnitPrice 最低单价
     * @param maxUnitPrice 最高单价
     * @return 材料列表
     */
    List<Material> findByUnitPriceBetween(Double minUnitPrice, Double maxUnitPrice);
    
    /**
     * 计算维修工单的材料总成本
     * @param repairOrderId 维修工单ID
     * @return 材料总成本
     */
    @Query("SELECT SUM(m.totalPrice) FROM Material m WHERE m.repairOrder.id = :repairOrderId")
    Double calculateTotalMaterialCost(@Param("repairOrderId") Long repairOrderId);
    
    /**
     * 获取最常用的材料统计
     * @return 统计结果（材料名称、使用次数、总数量、总成本）
     */
    @Query("SELECT m.name, COUNT(m), SUM(m.quantity), SUM(m.totalPrice) " +
           "FROM Material m " +
           "GROUP BY m.name " +
           "ORDER BY COUNT(m) DESC")
    List<Object[]> findMostUsedMaterials();
    
    /**
     * 获取特定维修工单类型最常用的材料
     * @param description 维修描述（包含关键词）
     * @return 统计结果（材料名称、使用次数）
     */
    @Query("SELECT m.name, COUNT(m) " +
           "FROM Material m " +
           "JOIN m.repairOrder r " +
           "WHERE r.description LIKE %:description% " +
           "GROUP BY m.name " +
           "ORDER BY COUNT(m) DESC")
    List<Object[]> findMostUsedMaterialsByRepairType(@Param("description") String description);
} 