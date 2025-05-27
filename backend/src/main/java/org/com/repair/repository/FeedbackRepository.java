package org.com.repair.repository;

import java.util.Date;
import java.util.List;

import org.com.repair.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    
    /**
     * 根据维修工单ID查找反馈列表
     * @param repairOrderId 维修工单ID
     * @return 反馈列表
     */
    List<Feedback> findByRepairOrderId(Long repairOrderId);
    
    /**
     * 根据用户ID查找反馈列表
     * @param userId 用户ID
     * @return 反馈列表
     */
    List<Feedback> findByUserId(Long userId);
    

    
    /**
     * 查找指定日期范围内的反馈列表
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 反馈列表
     */
    List<Feedback> findByCreatedAtBetween(Date startDate, Date endDate);
    

    
    /**
     * 查找包含特定关键词的反馈列表
     * @param keyword 关键词
     * @return 反馈列表
     */
    List<Feedback> findByCommentContaining(String keyword);
    

} 