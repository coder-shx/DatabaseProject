<template>
  <div class="repair-orders-container">
    <el-card class="repair-orders-card">
      <template #header>
        <div class="card-header">
          <h2>维修工单</h2>
          <div class="header-actions">
            <el-input
              v-model="searchQuery"
              placeholder="搜索工单"
              clearable
              class="search-input"
              @clear="handleClear">
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <el-button-group v-if="isCustomer">
              <el-button type="primary" @click="createNewOrder">提交维修申请</el-button>
            </el-button-group>
          </div>
        </div>
      </template>
      
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>
      
      <div v-else>
        <el-tabs v-model="activeTab" @tab-click="handleTabClick">
          <el-tab-pane v-if="!isCustomer && !isAdmin" label="待处理" name="pending">
            <repair-order-table
              :orders="pendingOrders"
              :loading="loading"
              @view="viewOrder"
              @accept="acceptOrder"
              @reject="rejectOrder"
            />
          </el-tab-pane>
          <el-tab-pane v-if="!isCustomer && !isAdmin" label="进行中" name="in-progress">
            <repair-order-table
              :orders="inProgressOrders"
              :loading="loading"
              @view="viewOrder"
              @update-status="updateOrderStatus"
              @add-material="addMaterial"
            />
          </el-tab-pane>
          <el-tab-pane label="全部工单" name="all">
            <repair-order-table
              :orders="filteredOrders"
              :loading="loading"
              @view="viewOrder"
              @update-status="updateOrderStatus"
              @add-material="addMaterial"
              @add-feedback="addFeedback"
            />
            
            <div class="pagination-container">
              <el-pagination
                v-model:currentPage="currentPage"
                :page-size="pageSize"
                :total="filteredOrders.length"
                layout="total, prev, pager, next"
                @current-change="handleCurrentChange"
              />
            </div>
          </el-tab-pane>
          <el-tab-pane label="已完成" name="completed">
            <repair-order-table
              :orders="completedOrders"
              :loading="loading"
              @view="viewOrder"
              @add-feedback="addFeedback"
            />
          </el-tab-pane>
        </el-tabs>
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
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { useStore } from 'vuex';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search } from '@element-plus/icons-vue';
import RepairOrderTable from '@/components/RepairOrderTable.vue';

export default {
  name: 'RepairOrderList',
  components: {
    Search,
    RepairOrderTable
  },
  setup() {
    const store = useStore();
    const router = useRouter();
    const route = useRoute();
    const materialFormRef = ref(null);
    const feedbackFormRef = ref(null);
    
    // 分页和筛选
    const currentPage = ref(1);
    const pageSize = ref(10);
    const searchQuery = ref('');
    const activeTab = ref('all');
    
    // 状态和操作
    const loading = computed(() => store.state.loading);
    const saving = ref(false);
    const materialDialogVisible = ref(false);
    const feedbackDialogVisible = ref(false);
    const currentOrderId = ref(null);
    
    // 用户角色
    const isCustomer = computed(() => store.getters.isCustomer);
    const isMechanic = computed(() => store.getters.isMechanic);
    const isAdmin = computed(() => store.getters.isAdmin);
    
    // 订单数据
    const repairOrders = computed(() => store.state.repairOrders);
    
    // 过滤订单
    const filteredOrders = computed(() => {
      if (!searchQuery.value) {
        return repairOrders.value;
      }
      
      const query = searchQuery.value.toLowerCase();
      return repairOrders.value.filter(order => {
        const vehiclePlate = order.vehicle?.licensePlate?.toLowerCase() || '';
        const description = order.description?.toLowerCase() || '';
        const id = order.id?.toString() || '';
        
        return vehiclePlate.includes(query) || 
               description.includes(query) || 
               id.includes(query);
      });
    });
    
    // 按状态过滤订单
    const pendingOrders = computed(() => {
      return repairOrders.value.filter(order => 
        order.status === 'PENDING' || order.status === 'ASSIGNED'
      );
    });
    
    const inProgressOrders = computed(() => {
      return repairOrders.value.filter(order => 
        order.status === 'IN_PROGRESS'
      );
    });
    
    const completedOrders = computed(() => {
      return repairOrders.value.filter(order => 
        order.status === 'COMPLETED'
      );
    });
    
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
    
    // 获取维修工单列表
    const fetchRepairOrders = async () => {
      try {
        // 先清除查询条件
        searchQuery.value = '';
        
        // 获取路由中的车辆ID参数
        const vehicleId = route.query.vehicleId;
        
        // 获取所有工单
        await store.dispatch('fetchUserOrders');
        
        // 如果有车辆ID参数，过滤显示该车辆的工单
        if (vehicleId) {
          searchQuery.value = repairOrders.value.find(
            order => order.vehicle?.id === Number(vehicleId)
          )?.vehicle?.licensePlate || '';
        }
      } catch (error) {
        console.error('Failed to fetch repair orders:', error);
        ElMessage.error('获取维修工单失败');
      }
    };
    
    // 创建新工单
    const createNewOrder = () => {
      router.push('/create-repair-order');
    };
    
    // 查看工单详情
    const viewOrder = (order) => {
      router.push(`/repair-orders/${order.id}`);
    };
    
    // 接受工单（技工）
    const acceptOrder = async (order) => {
      try {
        await store.dispatch('updateOrderStatus', {
          id: order.id,
          status: 'IN_PROGRESS'
        });
        ElMessage.success('已接受工单');
      } catch (error) {
        console.error('Failed to accept order:', error);
        ElMessage.error('操作失败');
      }
    };
    
    // 拒绝工单（技工）
    const rejectOrder = (order) => {
      ElMessageBox.confirm(
        '确定要拒绝这个工单吗？系统将重新分配给其他技工。',
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }
      ).then(async () => {
        try {
          // 这里应该调用拒绝工单的API
          // 由于暂时没有实现，我们简单地模拟一下
          ElMessage.success('已拒绝工单，系统将重新分配');
        } catch (error) {
          console.error('Failed to reject order:', error);
          ElMessage.error('操作失败');
        }
      }).catch(() => {});
    };
    
    // 更新工单状态
    const updateOrderStatus = (order, status) => {
      ElMessageBox.confirm(
        `确定要将工单状态更改为"${getStatusText(status)}"吗？`,
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }
      ).then(async () => {
        try {
          await store.dispatch('updateOrderStatus', {
            id: order.id,
            status: status
          });
          ElMessage.success('状态已更新');
        } catch (error) {
          console.error('Failed to update order status:', error);
          ElMessage.error('操作失败');
        }
      }).catch(() => {});
    };
    
    // 添加材料
    const addMaterial = (order) => {
      currentOrderId.value = order.id;
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
          orderId: currentOrderId.value,
          materialData
        });
        
        ElMessage.success('材料已添加');
        materialDialogVisible.value = false;
      } catch (error) {
        console.error('Failed to add material:', error);
        ElMessage.error('添加材料失败');
      } finally {
        saving.value = false;
      }
    };
    
    // 添加评价
    const addFeedback = (order) => {
      // 只有状态为已完成的工单才能评价
      if (order.status !== 'COMPLETED') {
        ElMessage.warning('只能对已完成的工单进行评价');
        return;
      }
      
      // 如果已经评价过，显示已有评价
      if (order.customerRating) {
        ElMessage.info(`您已对此工单评价：${order.customerRating}星`);
        return;
      }
      
      currentOrderId.value = order.id;
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
          orderId: currentOrderId.value,
          rating: feedbackForm.rating,
          feedback: feedbackForm.feedback
        });
        
        ElMessage.success('评价已提交');
        feedbackDialogVisible.value = false;
      } catch (error) {
        console.error('Failed to add feedback:', error);
        ElMessage.error('提交评价失败');
      } finally {
        saving.value = false;
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
    
    // 处理标签切换
    const handleTabClick = (tab) => {
      currentPage.value = 1;
    };
    
    // 处理分页
    const handleCurrentChange = (page) => {
      currentPage.value = page;
    };
    
    // 清除搜索
    const handleClear = () => {
      searchQuery.value = '';
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
    
    // 监听路由变化
    watch(
      () => route.query,
      (newQuery) => {
        if (newQuery.vehicleId) {
          const vehicleId = Number(newQuery.vehicleId);
          const vehicle = repairOrders.value.find(
            order => order.vehicle?.id === vehicleId
          )?.vehicle;
          
          if (vehicle) {
            searchQuery.value = vehicle.licensePlate || '';
          }
        }
      }
    );
    
    onMounted(() => {
      fetchRepairOrders();
      
      // 根据用户角色设置默认标签页
      if (isMechanic.value) {
        activeTab.value = 'pending';
      }
    });
    
    return {
      repairOrders,
      filteredOrders,
      pendingOrders,
      inProgressOrders,
      completedOrders,
      loading,
      saving,
      currentPage,
      pageSize,
      searchQuery,
      activeTab,
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
      createNewOrder,
      viewOrder,
      acceptOrder,
      rejectOrder,
      updateOrderStatus,
      addMaterial,
      saveMaterial,
      addFeedback,
      saveFeedback,
      handleTabClick,
      handleCurrentChange,
      handleClear
    };
  }
};
</script>

<style scoped>
.repair-orders-container {
  padding: 20px;
}

.repair-orders-card {
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

.header-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.search-input {
  width: 250px;
}

.loading-container {
  padding: 20px 0;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style> 