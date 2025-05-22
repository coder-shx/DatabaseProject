package org.com.car_repair.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.com.car_repair.entity.Mechanic;
import org.com.car_repair.entity.RepairOrder;
import org.com.car_repair.entity.RepairStatus;
import org.com.car_repair.entity.User;
import org.com.car_repair.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RepairOrderRepository extends JpaRepository<RepairOrder, Long> {
    List<RepairOrder> findByCustomer(User customer);
    
    List<RepairOrder> findByVehicle(Vehicle vehicle);
    
    List<RepairOrder> findByStatus(RepairStatus status);
    
    List<RepairOrder> findByMechanicsContains(Mechanic mechanic);
    
    List<RepairOrder> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT ro.vehicle.brand, ro.vehicle.model, COUNT(ro) as repairCount, AVG(ro.totalCost) as avgCost " +
           "FROM RepairOrder ro WHERE ro.status = 'COMPLETED' " +
           "GROUP BY ro.vehicle.brand, ro.vehicle.model ORDER BY repairCount DESC")
    List<Object[]> findVehicleRepairStatistics();
    
    @Query("SELECT FUNCTION('YEAR', ro.completedAt) as year, FUNCTION('MONTH', ro.completedAt) as month, " +
           "SUM(ro.totalLaborCost) as laborCost, SUM(ro.totalMaterialCost) as materialCost " +
           "FROM RepairOrder ro WHERE ro.status = 'COMPLETED' " +
           "GROUP BY FUNCTION('YEAR', ro.completedAt), FUNCTION('MONTH', ro.completedAt) " +
           "ORDER BY year, month")
    List<Object[]> findMonthlyCostBreakdown();
    
    @Query("SELECT COUNT(ro) FROM RepairOrder ro WHERE ro.status IN ('PENDING', 'ASSIGNED', 'IN_PROGRESS')")
    Long countUncompletedOrders();
    
    List<RepairOrder> findByCustomerRatingLessThanEqual(Integer rating);
    
    @Query("SELECT ro.customer.id, ro.customer.name, COUNT(ro) as orderCount " +
           "FROM RepairOrder ro WHERE ro.status = 'COMPLETED' " +
           "GROUP BY ro.customer.id, ro.customer.name " +
           "ORDER BY orderCount DESC")
    List<Object[]> findTopCustomers();
    
    @Query("SELECT m.mechanicType, COUNT(ro) " +
           "FROM RepairOrder ro JOIN ro.mechanics m " +
           "WHERE ro.status = 'COMPLETED' " +
           "GROUP BY m.mechanicType " +
           "ORDER BY COUNT(ro) DESC")
    List<Object[]> countCompletedRepairsByMechanicType();
    
    List<RepairOrder> findByTotalCostGreaterThan(BigDecimal cost);
} 