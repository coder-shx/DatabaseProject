package org.com.repair.DTO;

import java.util.Date;

import org.com.repair.entity.Feedback;
import org.com.repair.entity.RepairOrder;
import org.com.repair.entity.User;

public record FeedbackResponse(
    Long id,
    String comment,
    Date createdAt,
    RepairOrder repairOrder,
    User user
) {
    public FeedbackResponse(Feedback feedback) {
        this(feedback.getId(), feedback.getComment(), 
             feedback.getCreatedAt(), feedback.getRepairOrder(), feedback.getUser());
    }
} 