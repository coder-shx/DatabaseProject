package org.com.repair.DTO;

import org.com.repair.entity.Technician;
import org.com.repair.entity.Technician.SkillType;

public record TechnicianResponse(
    Long id,
    String name,
    String employeeId,
    String username,
    String phone,
    SkillType skillType,
    Double hourlyRate
) {
    public TechnicianResponse(Technician technician) {
        this(technician.getId(), technician.getName(), technician.getEmployeeId(), 
             technician.getUsername(), technician.getPhone(),
             technician.getSkillType(), technician.getHourlyRate());
    }
} 