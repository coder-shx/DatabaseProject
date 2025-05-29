package org.com.repair.service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.com.repair.DTO.FeedbackResponse;
import org.com.repair.DTO.NewFeedbackRequest;
import org.com.repair.entity.Feedback;
import org.com.repair.entity.RepairOrder;
import org.com.repair.entity.User;
import org.com.repair.repository.FeedbackRepository;
import org.com.repair.repository.RepairOrderRepository;
import org.com.repair.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeedbackService {
    
    private final FeedbackRepository feedbackRepository;
    private final RepairOrderRepository repairOrderRepository;
    private final UserRepository userRepository;
    
    public FeedbackService(
            FeedbackRepository feedbackRepository,
            RepairOrderRepository repairOrderRepository,
            UserRepository userRepository) {
        this.feedbackRepository = feedbackRepository;
        this.repairOrderRepository = repairOrderRepository;
        this.userRepository = userRepository;
    }
    
    @Transactional
    public FeedbackResponse addFeedback(NewFeedbackRequest request) {
        RepairOrder repairOrder = repairOrderRepository.findById(request.repairOrderId())
                .orElseThrow(() -> new RuntimeException("维修工单不存在"));
        
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 验证用户是否是工单的所有者
        if (!repairOrder.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("您无权对此工单进行评价");
        }
        
        Feedback feedback = new Feedback();
        feedback.setComment(request.comment());
        feedback.setCreatedAt(new Date());
        feedback.setRepairOrder(repairOrder);
        feedback.setUser(user);
        
        Feedback savedFeedback = feedbackRepository.save(feedback);
        return new FeedbackResponse(savedFeedback);
    }
    
    public Optional<FeedbackResponse> getFeedbackById(Long id) {
        return feedbackRepository.findById(id)
                .map(FeedbackResponse::new);
    }
    
    public List<FeedbackResponse> getFeedbacksByRepairOrderId(Long repairOrderId) {
        return feedbackRepository.findByRepairOrderId(repairOrderId).stream()
                .map(FeedbackResponse::new)
                .collect(Collectors.toList());
    }
    
    public List<FeedbackResponse> getFeedbacksByUserId(Long userId) {
        return feedbackRepository.findByUserId(userId).stream()
                .map(FeedbackResponse::new)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public FeedbackResponse updateFeedback(Long id, NewFeedbackRequest request) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("反馈不存在"));
        
        // 验证用户是否是反馈的所有者
        if (!feedback.getUser().getId().equals(request.userId())) {
            throw new RuntimeException("您无权修改此反馈");
        }
        
        feedback.setComment(request.comment());
        
        Feedback updatedFeedback = feedbackRepository.save(feedback);
        return new FeedbackResponse(updatedFeedback);
    }
    
    @Transactional
    public boolean deleteFeedback(Long id, Long userId) {
        Optional<Feedback> feedbackOpt = feedbackRepository.findById(id);
        if (feedbackOpt.isPresent()) {
            Feedback feedback = feedbackOpt.get();
            
            // 验证用户是否是反馈的所有者
            if (!feedback.getUser().getId().equals(userId)) {
                throw new RuntimeException("您无权删除此反馈");
            }
            
            feedbackRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    public List<FeedbackResponse> getAllFeedbacks() {
        return feedbackRepository.findAll().stream()
                .map(FeedbackResponse::new)
                .collect(Collectors.toList());
    }
    

    
    public List<FeedbackResponse> getFeedbacksByKeyword(String keyword) {
        return feedbackRepository.findByCommentContaining(keyword).stream()
                .map(FeedbackResponse::new)
                .collect(Collectors.toList());
    }
    

} 