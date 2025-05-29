package org.com.repair.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.com.repair.DTO.NewVehicleRequest;
import org.com.repair.DTO.VehicleResponse;
import org.com.repair.entity.User;
import org.com.repair.entity.Vehicle;
import org.com.repair.repository.UserRepository;
import org.com.repair.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VehicleService {
    
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    
    public VehicleService(VehicleRepository vehicleRepository, UserRepository userRepository) {
        this.vehicleRepository = vehicleRepository;
        this.userRepository = userRepository;
    }
    
    @Transactional
    public VehicleResponse addVehicle(NewVehicleRequest request) {
        if (vehicleRepository.existsByLicensePlate(request.licensePlate())) {
            throw new RuntimeException("车牌号已存在");
        }
        
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        Vehicle vehicle = new Vehicle();
        vehicle.setLicensePlate(request.licensePlate());
        vehicle.setBrand(request.brand());
        vehicle.setModel(request.model());
        vehicle.setYear(request.year());
        vehicle.setColor(request.color());
        vehicle.setVin(request.vin());
        vehicle.setUser(user);
        
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        return new VehicleResponse(savedVehicle);
    }
    
    public Optional<VehicleResponse> getVehicleById(Long id) {
        return vehicleRepository.findById(id)
                .map(VehicleResponse::new);
    }
    
    public List<VehicleResponse> getVehiclesByUserId(Long userId) {
        return vehicleRepository.findByUserId(userId).stream()
                .map(VehicleResponse::new)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public VehicleResponse updateVehicle(Long id, NewVehicleRequest request) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("车辆不存在"));
        
        if (!vehicle.getLicensePlate().equals(request.licensePlate()) &&
            vehicleRepository.existsByLicensePlate(request.licensePlate())) {
            throw new RuntimeException("车牌号已存在");
        }
        
        if (!vehicle.getUser().getId().equals(request.userId())) {
            User newUser = userRepository.findById(request.userId())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            vehicle.setUser(newUser);
        }
        
        vehicle.setLicensePlate(request.licensePlate());
        vehicle.setBrand(request.brand());
        vehicle.setModel(request.model());
        vehicle.setYear(request.year());
        vehicle.setColor(request.color());
        vehicle.setVin(request.vin());
        
        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        return new VehicleResponse(updatedVehicle);
    }
    
    @Transactional
    public boolean deleteVehicle(Long id) {
        if (vehicleRepository.existsById(id)) {
            vehicleRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    public List<VehicleResponse> getAllVehicles() {
        return vehicleRepository.findAll().stream()
                .map(VehicleResponse::new)
                .collect(Collectors.toList());
    }
    
    public Optional<VehicleResponse> getVehicleByLicensePlate(String licensePlate) {
        return vehicleRepository.findByLicensePlate(licensePlate)
                .map(VehicleResponse::new);
    }
    
    public List<Object[]> getRepairStatisticsByModel() {
        return vehicleRepository.getRepairStatisticsByModel();
    }
} 