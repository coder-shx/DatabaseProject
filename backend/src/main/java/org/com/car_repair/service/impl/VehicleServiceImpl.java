package org.com.car_repair.service.impl;

import java.util.NoSuchElementException;

import org.com.car_repair.entity.Vehicle;
import org.com.car_repair.repository.VehicleRepository;
import org.com.car_repair.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;

    @Autowired
    public VehicleServiceImpl(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public Vehicle getVehicleById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("车辆不存在"));
    }

    @Override
    @Transactional
    public Vehicle updateVehicle(Vehicle vehicle) {
        // 确保车辆存在
        if (!vehicleRepository.existsById(vehicle.getId())) {
            throw new NoSuchElementException("车辆不存在");
        }
        return vehicleRepository.save(vehicle);
    }

    @Override
    @Transactional
    public void deleteVehicle(Long id) {
        // 确保车辆存在
        if (!vehicleRepository.existsById(id)) {
            throw new NoSuchElementException("车辆不存在");
        }
        vehicleRepository.deleteById(id);
    }
} 