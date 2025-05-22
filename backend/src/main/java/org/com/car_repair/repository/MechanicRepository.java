package org.com.car_repair.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.com.car_repair.entity.Mechanic;
import org.com.car_repair.entity.MechanicType;
import org.com.car_repair.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MechanicRepository extends JpaRepository<Mechanic, Long> {
    Optional<Mechanic> findByUser(User user);
    
    List<Mechanic> findByMechanicType(MechanicType mechanicType);
    
    @Query("SELECT m FROM Mechanic m WHERE m.mechanicType = ?1 AND " +
           "(SELECT COUNT(r) FROM m.repairOrders r WHERE r.status IN ('ASSIGNED', 'IN_PROGRESS')) < ?2")
    List<Mechanic> findAvailableMechanicsByType(MechanicType mechanicType, int maxWorkload);
    
    @Query("SELECT m.mechanicType, COUNT(m) FROM Mechanic m GROUP BY m.mechanicType")
    List<Object[]> countMechanicsByType();
    
    @Query("SELECT m.mechanicType, AVG(m.hourlyRate) FROM Mechanic m GROUP BY m.mechanicType")
    List<Object[]> findAverageHourlyRateByType();
    
    List<Mechanic> findByHourlyRateGreaterThan(BigDecimal rate);
} 