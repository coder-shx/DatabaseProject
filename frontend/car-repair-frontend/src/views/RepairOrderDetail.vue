<template>
  <div class="repair-order-detail-container">
    <el-card class="repair-order-card">
      <template #header>
        <div class="card-header">
          <h2>维修工单详情</h2>
          <el-button @click="$router.back()">返回</el-button>
        </div>
      </template>
      
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="6" animated />
      </div>
      
      <div v-else-if="!order" class="empty-container">
        <el-empty description="未找到工单信息" :image-size="200">
          <el-button @click="$router.push('/repair-orders')">查看所有工单</el-button>
        </el-empty>
      </div>
      
      <div v-else>
        <el-descriptions title="基本信息" :column="2" border>
          <el-descriptions-item label="工单号">{{ order.id }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDate(order.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="车辆信息">
            {{ order.vehicle.brand }} {{ order.vehicle.model }} ({{ order.vehicle.licensePlate }})
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusType(order.status)">{{ getStatusText(order.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="故障描述" :span="2">{{ order.description }}</el-descriptions-item>
        </el-descriptions>
        
        <!-- 技工信息 -->
        <el-descriptions v-if="order.mechanic" title="技工信息" :column="2" border class="section">
          <el-descriptions-item label="技工姓名">{{ order.mechanic.name }}</el-descriptions-item>
          <el-descriptions-item label="专业类型">{{ getMechanicTypeText(order.mechanic.mechanicType) }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ order.mechanic.phoneNumber }}</el-descriptions-item>
          <el-descriptions-item label="时薪">¥{{ order.mechanic.hourlyRate }}/小时</el-descriptions-item>
        </el-descriptions>
        
        <!-- 维修进度 -->
        <div class="section">
          <h3>维修进度</h3>
          <el-steps :active="getStepByStatus(order.status)" finish-status="success">
            <el-step title="提交申请" :description="formatDate(order.createdAt)"></el-step>
            <el-step title="技工分配" :description="order.assignedAt ? formatDate(order.assignedAt) : '待分配'"></el-step>
            <el-step title="维修进行" :description="order.startedAt ? formatDate(order.startedAt) : '待开始'"></el-step>
            <el-step title="维修完成" :description="order.completedAt ? formatDate(order.completedAt) : '待完成'"></el-step>
          </el-steps>
        </div>
        
        <!-- 材料清单 -->
        <div class="section">
          <div class="section-header">
            <h3>维修材料</h3>
            <el-button 
              v-if="isMechanic && order.status === 'IN_PROGRESS'" 
              type="primary" 
              size="small" 
              @click="showAddMaterialDialog">
              添加材料
            </el-button>
          </div>
          
          <el-table v-if="order.materials && order.materials.length > 0" :data="order.materials" style="width: 100%">
            <el-table-column prop="name" label="材料名称"></el-table-column>
            <el-table-column prop="description" label="描述" show-overflow-tooltip></el-table-column>
            <el-table-column prop="unitPrice" label="单价">
              <template #default="scope">¥{{ scope.row.unitPrice }}</template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量"></el-table-column>
            <el-table-column prop="totalPrice" label="小计">
              <template #default="scope">¥{{ scope.row.totalPrice }}</template>
            </el-table-column>
          </el-table>
          
          <div v-else class="empty-data">
            <p>暂无材料记录</p>
          </div>
          
          <div v-if="order.materials && order.materials.length > 0" class="cost-summary">
            <p><strong>材料费用合计：</strong>¥{{ order.totalMaterialCost || calculateTotalMaterialCost() }}</p>
            <p v-if="order.totalLaborCost"><strong>工时费用：</strong>¥{{ order.totalLaborCost }}</p>
            <p v-if="order.totalCost"><strong>总计费用：</strong>¥{{ order.totalCost }}</p>
          </div>
        </div>
        
        <!-- 客户评价 -->
        <div v-if="order.status === 'COMPLETED'" class="section">
          <h3>客户评价</h3>
          
          <div v-if="order.customerRating" class="feedback-container">
            <div class="rating">
              <span>评分：</span>
              <el-rate v-model="order.customerRating" disabled text-color="#ff9900"></el-rate>
            </div>
            <div v-if="order.customerFeedback" class="feedback">
              <p>{{ order.customerFeedback }}</p>
            </div>
          </div>
          
          <div v-else-if="isCustomer" class="empty-feedback">
            <p>您还没有评价此次维修服务</p>
            <el-button type="primary" size="small" @click="showAddFeedbackDialog">立即评价</el-button>
          </div>
          
          <div v-else class="empty-feedback">
            <p>客户暂未评价</p>
          </div>
        </div>
        
        <!-- 操作区域 -->
        <div class="action-section">
          <!-- 客户操作 -->
          <template v-if="isCustomer">
            <el-button 
              v-if="order.status === 'PENDING'" 
              type="danger" 
              @click="cancelOrder">
              取消申请
            </el-button>
            <el-button 
              v-if="order.status === 'COMPLETED' && !order.customerRating" 
              type="primary" 
              @click="showAddFeedbackDialog">
              评价服务
            </el-button>
          </template>
          
          <!-- 技工操作 -->
          <template v-if="isMechanic">
            <el-button 
              v-if="order.status === 'ASSIGNED'" 
              type="success" 
              @click="startRepair">
              开始维修
            </el-button>
            <el-button 
              v-if="order.status === 'IN_PROGRESS'" 
              type="primary" 
              @click="showAddMaterialDialog">
              添加材料
            </el-button>
            <el-button 
              v-if="order.status === 'IN_PROGRESS'" 
              type="success" 
              @click="completeRepair">
              完成维修
            </el-button>
          </template>
          
          <!-- 管理员操作 -->
          <template v-if="isAdmin">
            <el-dropdown @command="handleStatusChange" trigger="click">
              <el-button type="primary">
                更改状态<el-icon class="el-icon--right"><arrow-down /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item :disabled="order.status === 'PENDING'" command="PENDING">待处理</el-dropdown-item>
                  <el-dropdown-item :disabled="order.status === 'ASSIGNED'" command="ASSIGNED">已分配</el-dropdown-item>
                  <el-dropdown-item :disabled="order.status === 'IN_PROGRESS'" command="IN_PROGRESS">维修中</el-dropdown-item>
                  <el-dropdown-item :disabled="order.status === 'COMPLETED'" command="COMPLETED">已完成</el-dropdown-item>
                  <el-dropdown-item :disabled="order.status === 'CANCELED'" command="CANCELED">已取消</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button type="primary" @click="assignMechanic">分配技工</el-button>
          </template>
        </div>
      </div>
    </el-card>
    
    <!-- 添加材料对话框 -->
    <el-dialog
      v-model="materialDialogVisible"
      title="添加维修材料"
      width="500px"
      :close-on-click-modal="false">
      <el-form :model="materialForm" :rules="materialRules" ref="materialFormRef" label-width="100px">
        <el-form-item label="材料名称" prop="name">
          <el-input v-model="materialForm.name" placeholder="请输入材料名称"></el-input>
        </el-form-item>
        <el-form-item label="材料描述" prop="description">
          <el-input
            v-model="materialForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入材料描述">
          </el-input>
        </el-form-item>
        <el-form-item label="单价" prop="unitPrice">
          <el-input-number
            v-model="materialForm.unitPrice"
            :precision="2"
            :min="0"
            :step="0.01"
            style="width: 100%">
          </el-input-number>
        </el-form-item>
        <el-form-item label="数量" prop="quantity">
          <el-input-number
            v-model="materialForm.quantity"
            :min="1"
            :step="1"
            style="width: 100%">
          </el-input-number>
        </el-form-item>
        <el-form-item label="总价">
          <span>{{ (materialForm.unitPrice * materialForm.quantity).toFixed(2) }} 元</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="materialDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="saveMaterial">保存</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 添加评价对话框 -->
    <el-dialog
      v-model="feedbackDialogVisible"
      title="评价维修服务"
      width="500px"
      :close-on-click-modal="false">
      <el-form :model="feedbackForm" :rules="feedbackRules" ref="feedbackFormRef" label-width="100px">
        <el-form-item label="服务评分" prop="rating">
          <el-rate
            v-model="feedbackForm.rating"
            :texts="['极差', '失望', '一般', '满意', '很满意']"
            show-text>
          </el-rate>
        </el-form-item>
        <el-form-item label="反馈意见" prop="feedback">
          <el-input
            v-model="feedbackForm.feedback"
            type="textarea"
            :rows="4"
            placeholder="请输入您对本次维修服务的意见或建议">
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="feedbackDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="saveFeedback">提交评价</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue';
import { useStore } from 'vuex';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ArrowDown } from '@element-plus/icons-vue';
import { repairOrderApi } from '@/api/api';

export default {
  name: 'RepairOrderDetailView',
  components: {
    ArrowDown
  },
  setup() {
    const store = useStore();
    const router = useRouter();
    const route = useRoute();
    const materialFormRef = ref(null);
    const feedbackFormRef = ref(null);
    
    // 获取路由参数中的工单ID
    const orderId = computed(() => Number(route.params.id));
    
    // 状态变量
    const loading = ref(true);
    const saving = ref(false);
    const order = ref(null);
    const materialDialogVisible = ref(false);
    const feedbackDialogVisible = ref(false);
    
    // 用户角色
    const isCustomer = computed(() => store.getters.isCustomer);
    const isMechanic = computed(() => store.getters.isMechanic);
    const isAdmin = computed(() => store.getters.isAdmin);
    
    // 添加材料表单
    const materialForm = reactive({
      name: '',
      description: '',
      unitPrice: 0,
      quantity: 1
    });
    
    // 材料表单验证规则
    const materialRules = {
      name: [
        { required: true, message: '请输入材料名称', trigger: 'blur' }
      ],
      description: [
        { required: true, message: '请输入材料描述', trigger: 'blur' }
      ],
      unitPrice: [
        { required: true, message: '请输入单价', trigger: 'blur' },
        { type: 'number', min: 0.01, message: '单价必须大于0', trigger: 'blur' }
      ],
      quantity: [
        { required: true, message: '请输入数量', trigger: 'blur' },
        { type: 'number', min: 1, message: '数量必须大于0', trigger: 'blur' }
      ]
    };
    
    // 评价表单
    const feedbackForm = reactive({
      rating: 5,
      feedback: ''
    });
    
    // 评价表单验证规则
    const feedbackRules = {
      rating: [
        { required: true, message: '请选择评分', trigger: 'change' }
      ],
      feedback: [
        { required: true, message: '请输入反馈意见', trigger: 'blur' }
      ]
    };
    
    // 获取工单详情
    const fetchOrderDetail = async () => {
      loading.value = true;
      
      try {
        // 从store获取工单详情
        await store.dispatch('fetchRepairOrder', orderId.value);
        order.value = store.state.currentOrder;
      } catch (error) {
        console.error('Failed to fetch repair order:', error);
        ElMessage.error('获取工单详情失败');
      } finally {
        loading.value = false;
      }
    };
    
    // 根据状态获取步骤
    const getStepByStatus = (status) => {
      const stepMap = {
        'PENDING': 0,
        'ASSIGNED': 1,
        'IN_PROGRESS': 2,
        'COMPLETED': 3,
        'CANCELED': 0
      };
      return stepMap[status] || 0;
    };
    
    // 计算材料总价
    const calculateTotalMaterialCost = () => {
      if (!order.value || !order.value.materials) return 0;
      
      return order.value.materials.reduce((sum, material) => {
        return sum + (material.totalPrice || 0);
      }, 0).toFixed(2);
    };
    
    // 显示添加材料对话框
    const showAddMaterialDialog = () => {
      resetMaterialForm();
      materialDialogVisible.value = true;
    };
    
    // 保存材料
    const saveMaterial = async () => {
      if (!materialFormRef.value) return;
      
      try {
        await materialFormRef.value.validate();
        saving.value = true;
        
        const materialData = {
          name: materialForm.name,
          description: materialForm.description,
          unitPrice: materialForm.unitPrice,
          quantity: materialForm.quantity,
          totalPrice: materialForm.unitPrice * materialForm.quantity
        };
        
        await store.dispatch('addMaterialToOrder', {
          orderId: orderId.value,
          materialData
        });
        
        // 刷新工单详情
        await fetchOrderDetail();
        
        ElMessage.success('材料已添加');
        materialDialogVisible.value = false;
      } catch (error) {
        console.error('Failed to add material:', error);
        ElMessage.error('添加材料失败');
      } finally {
        saving.value = false;
      }
    };
    
    // 显示添加评价对话框
    const showAddFeedbackDialog = () => {
      resetFeedbackForm();
      feedbackDialogVisible.value = true;
    };
    
    // 保存评价
    const saveFeedback = async () => {
      if (!feedbackFormRef.value) return;
      
      try {
        await feedbackFormRef.value.validate();
        saving.value = true;
        
        await store.dispatch('addOrderFeedback', {
          orderId: orderId.value,
          rating: feedbackForm.rating,
          feedback: feedbackForm.feedback
        });
        
        // 刷新工单详情
        await fetchOrderDetail();
        
        ElMessage.success('评价已提交');
        feedbackDialogVisible.value = false;
      } catch (error) {
        console.error('Failed to add feedback:', error);
        ElMessage.error('提交评价失败');
      } finally {
        saving.value = false;
      }
    };
    
    // 开始维修（技工）
    const startRepair = async () => {
      try {
        await store.dispatch('updateOrderStatus', {
          id: orderId.value,
          status: 'IN_PROGRESS'
        });
        
        // 刷新工单详情
        await fetchOrderDetail();
        
        ElMessage.success('维修已开始');
      } catch (error) {
        console.error('Failed to start repair:', error);
        ElMessage.error('操作失败');
      }
    };
    
    // 完成维修（技工）
    const completeRepair = async () => {
      try {
        await store.dispatch('completeOrder', orderId.value);
        
        // 刷新工单详情
        await fetchOrderDetail();
        
        ElMessage.success('维修已完成');
      } catch (error) {
        console.error('Failed to complete repair:', error);
        ElMessage.error('操作失败');
      }
    };
    
    // 取消工单（客户）
    const cancelOrder = () => {
      ElMessageBox.confirm(
        '确定要取消此维修申请吗？',
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }
      ).then(async () => {
        try {
          await store.dispatch('updateOrderStatus', {
            id: orderId.value,
            status: 'CANCELED'
          });
          
          // 刷新工单详情
          await fetchOrderDetail();
          
          ElMessage.success('维修申请已取消');
        } catch (error) {
          console.error('Failed to cancel order:', error);
          ElMessage.error('操作失败');
        }
      }).catch(() => {});
    };
    
    // 分配技工（管理员）
    const assignMechanic = () => {
      // 这里应该弹出分配技工的对话框
      // 由于暂时没有实现，我们简单提示
      ElMessage.info('技工分配功能将在下一版本实现');
    };
    
    // 更改状态（管理员）
    const handleStatusChange = async (status) => {
      try {
        await store.dispatch('updateOrderStatus', {
          id: orderId.value,
          status: status
        });
        
        // 刷新工单详情
        await fetchOrderDetail();
        
        ElMessage.success(`工单状态已更改为"${getStatusText(status)}"`);
      } catch (error) {
        console.error('Failed to update order status:', error);
        ElMessage.error('操作失败');
      }
    };
    
    // 重置材料表单
    const resetMaterialForm = () => {
      materialForm.name = '';
      materialForm.description = '';
      materialForm.unitPrice = 0;
      materialForm.quantity = 1;
      
      if (materialFormRef.value) {
        materialFormRef.value.resetFields();
      }
    };
    
    // 重置评价表单
    const resetFeedbackForm = () => {
      feedbackForm.rating = 5;
      feedbackForm.feedback = '';
      
      if (feedbackFormRef.value) {
        feedbackFormRef.value.resetFields();
      }
    };
    
    // 格式化日期
    const formatDate = (dateStr) => {
      if (!dateStr) return '-';
      const date = new Date(dateStr);
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      });
    };
    
    // 获取状态类型对应的标签样式
    const getStatusType = (status) => {
      const typeMap = {
        'PENDING': 'info',
        'ASSIGNED': 'warning',
        'IN_PROGRESS': 'primary',
        'COMPLETED': 'success',
        'CANCELED': 'danger'
      };
      return typeMap[status] || 'info';
    };
    
    // 获取状态文本
    const getStatusText = (status) => {
      const textMap = {
        'PENDING': '待处理',
        'ASSIGNED': '已分配',
        'IN_PROGRESS': '维修中',
        'COMPLETED': '已完成',
        'CANCELED': '已取消'
      };
      return textMap[status] || status;
    };
    
    // 获取技工类型文本
    const getMechanicTypeText = (type) => {
      const textMap = {
        'PAINTER': '漆工',
        'WELDER': '焊工',
        'MECHANIC': '机修',
        'ELECTRIC': '电工',
        'TIRE': '轮胎工',
        'OTHER': '其他'
      };
      return textMap[type] || type;
    };
    
    onMounted(() => {
      fetchOrderDetail();
    });
    
    return {
      loading,
      saving,
      order,
      isCustomer,
      isMechanic,
      isAdmin,
      materialDialogVisible,
      feedbackDialogVisible,
      materialForm,
      materialFormRef,
      materialRules,
      feedbackForm,
      feedbackFormRef,
      feedbackRules,
      formatDate,
      getStatusType,
      getStatusText,
      getMechanicTypeText,
      getStepByStatus,
      calculateTotalMaterialCost,
      showAddMaterialDialog,
      saveMaterial,
      showAddFeedbackDialog,
      saveFeedback,
      startRepair,
      completeRepair,
      cancelOrder,
      assignMechanic,
      handleStatusChange
    };
  }
};
</script>

<style scoped>
.repair-order-detail-container {
  padding: 20px;
}

.repair-order-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
}

.loading-container,
.empty-container {
  padding: 20px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.section {
  margin-top: 25px;
}

.section h3 {
  margin-top: 0;
  margin-bottom: 15px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.section-header h3 {
  margin: 0;
}

.empty-data {
  text-align: center;
  color: #909399;
  padding: 20px 0;
}

.cost-summary {
  margin-top: 15px;
  text-align: right;
  padding-right: 20px;
}

.action-section {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  gap: 15px;
}

.feedback-container {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
}

.rating {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.empty-feedback {
  text-align: center;
  color: #909399;
  padding: 15px 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style> 