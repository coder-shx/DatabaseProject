package org.com.car_repair.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "repair_orders")
public class RepairOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description; // 故障描述

    @Column
    private LocalDateTime createdAt; // 创建时间

    @Column
    private LocalDateTime startedAt; // 开始维修时间

    @Column
    private LocalDateTime completedAt; // 完成时间

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RepairStatus status;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToMany
    @JoinTable(
        name = "order_mechanics",
        joinColumns = @JoinColumn(name = "order_id"),
        inverseJoinColumns = @JoinColumn(name = "mechanic_id")
    )
    private Set<Mechanic> mechanics = new HashSet<>();

    @OneToMany(mappedBy = "repairOrder", cascade = CascadeType.ALL)
    private Set<Material> materials = new HashSet<>();

    @Column
    private BigDecimal totalLaborCost; // 总工时费

    @Column
    private BigDecimal totalMaterialCost; // 总材料费

    @Column
    private BigDecimal totalCost; // 总费用

    @Column
    private Integer customerRating; // 客户评分 1-5

    @Column
    private String customerFeedback; // 客户反馈

    // 构造函数
    public RepairOrder() {
        this.createdAt = LocalDateTime.now();
        this.status = RepairStatus.PENDING;
        this.totalLaborCost = BigDecimal.ZERO;
        this.totalMaterialCost = BigDecimal.ZERO;
        this.totalCost = BigDecimal.ZERO;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public RepairStatus getStatus() {
        return status;
    }

    public void setStatus(RepairStatus status) {
        this.status = status;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public User getCustomer() {
        return customer;
    }

    public void setCustomer(User customer) {
        this.customer = customer;
    }

    public Set<Mechanic> getMechanics() {
        return mechanics;
    }

    public void setMechanics(Set<Mechanic> mechanics) {
        this.mechanics = mechanics;
    }

    public Set<Material> getMaterials() {
        return materials;
    }

    public void setMaterials(Set<Material> materials) {
        this.materials = materials;
    }

    public BigDecimal getTotalLaborCost() {
        return totalLaborCost;
    }

    public void setTotalLaborCost(BigDecimal totalLaborCost) {
        this.totalLaborCost = totalLaborCost;
    }

    public BigDecimal getTotalMaterialCost() {
        return totalMaterialCost;
    }

    public void setTotalMaterialCost(BigDecimal totalMaterialCost) {
        this.totalMaterialCost = totalMaterialCost;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public Integer getCustomerRating() {
        return customerRating;
    }

    public void setCustomerRating(Integer customerRating) {
        this.customerRating = customerRating;
    }

    public String getCustomerFeedback() {
        return customerFeedback;
    }

    public void setCustomerFeedback(String customerFeedback) {
        this.customerFeedback = customerFeedback;
    }

    // 业务方法
    public void addMechanic(Mechanic mechanic) {
        this.mechanics.add(mechanic);
        mechanic.getRepairOrders().add(this);
    }

    public void removeMechanic(Mechanic mechanic) {
        this.mechanics.remove(mechanic);
        mechanic.getRepairOrders().remove(this);
    }

    public void calculateTotalCost() {
        this.totalCost = this.totalLaborCost.add(this.totalMaterialCost);
    }
} 