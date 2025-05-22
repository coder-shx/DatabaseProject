<template>
  <div class="repair-order-table">
    <el-table
      v-if="orders && orders.length > 0"
      :data="orders"
      style="width: 100%"
      :row-class-name="tableRowClassName">
      <el-table-column prop="id" label="工单号" width="90"></el-table-column>
      <el-table-column label="车辆信息" width="220">
        <template #default="scope">
          <div>
            <div><strong>车牌号：</strong>{{ scope.row.vehicle.licensePlate }}</div>
            <div><strong>车型：</strong>{{ scope.row.vehicle.brand }} {{ scope.row.vehicle.model }}</div>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="故障描述" show-overflow-tooltip></el-table-column>
      <el-table-column label="时间" width="180">
        <template #default="scope">
          <div>
            <div><strong>创建时间：</strong>{{ formatDate(scope.row.createdAt) }}</div>
            <div v-if="scope.row.completedAt">
              <strong>完成时间：</strong>{{ formatDate(scope.row.completedAt) }}
            </div>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="120">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="费用" width="200" v-if="scope.row.status === 'COMPLETED'">
        <template #default="scope">
          <div v-if="scope.row.status === 'COMPLETED'">
            <div><strong>工时费：</strong>¥{{ scope.row.totalLaborCost || 0 }}</div>
            <div><strong>材料费：</strong>¥{{ scope.row.totalMaterialCost || 0 }}</div>
            <div><strong>总计：</strong>¥{{ scope.row.totalCost || 0 }}</div>
          </div>
          <div v-else>-</div>
        </template>
      </el-table-column>
      <el-table-column label="评价" width="120" v-if="scope.row.status === 'COMPLETED'">
        <template #default="scope">
          <div v-if="scope.row.customerRating">
            <el-rate
              v-model="scope.row.customerRating"
              disabled
              text-color="#ff9900">
            </el-rate>
          </div>
          <div v-else>未评价</div>
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="180">
        <template #default="scope">
          <div class="action-buttons">
            <el-button link type="primary" @click="handleView(scope.row)">查看</el-button>
            
            <!-- 技工可接受或拒绝待处理工单 -->
            <template v-if="isMechanic && (scope.row.status === 'PENDING' || scope.row.status === 'ASSIGNED')">
              <el-button link type="success" @click="handleAccept(scope.row)">接受</el-button>
              <el-button link type="danger" @click="handleReject(scope.row)">拒绝</el-button>
            </template>
            
            <!-- 技工可为进行中工单添加材料 -->
            <template v-if="isMechanic && scope.row.status === 'IN_PROGRESS'">
              <el-button link type="warning" @click="handleAddMaterial(scope.row)">添加材料</el-button>
              <el-button link type="success" @click="handleUpdateStatus(scope.row, 'COMPLETED')">完成</el-button>
            </template>
            
            <!-- 管理员可以更新工单状态 -->
            <template v-if="isAdmin && scope.row.status !== 'COMPLETED' && scope.row.status !== 'CANCELED'">
              <el-dropdown trigger="click" @command="(cmd) => handleUpdateStatus(scope.row, cmd)">
                <el-button link type="primary">
                  更新状态<el-icon class="el-icon--right"><arrow-down /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item :disabled="scope.row.status === 'PENDING'" command="PENDING">待处理</el-dropdown-item>
                    <el-dropdown-item :disabled="scope.row.status === 'ASSIGNED'" command="ASSIGNED">已分配</el-dropdown-item>
                    <el-dropdown-item :disabled="scope.row.status === 'IN_PROGRESS'" command="IN_PROGRESS">维修中</el-dropdown-item>
                    <el-dropdown-item command="COMPLETED">已完成</el-dropdown-item>
                    <el-dropdown-item command="CANCELED">已取消</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
            
            <!-- 用户可以为已完成工单添加评价 -->
            <template v-if="isCustomer && scope.row.status === 'COMPLETED' && !scope.row.customerRating">
              <el-button link type="primary" @click="handleAddFeedback(scope.row)">评价</el-button>
            </template>
          </div>
        </template>
      </el-table-column>
    </el-table>
    
    <div v-else class="empty-data">
      <el-empty description="暂无工单数据"></el-empty>
    </div>
  </div>
</template>

<script>
import { computed } from 'vue';
import { useStore } from 'vuex';
import { ArrowDown } from '@element-plus/icons-vue';

export default {
  name: 'RepairOrderTable',
  components: {
    ArrowDown
  },
  props: {
    orders: {
      type: Array,
      required: true,
      default: () => []
    },
    loading: {
      type: Boolean,
      default: false
    }
  },
  setup(props, { emit }) {
    const store = useStore();
    
    // 用户角色
    const isCustomer = computed(() => store.getters.isCustomer);
    const isMechanic = computed(() => store.getters.isMechanic);
    const isAdmin = computed(() => store.getters.isAdmin);
    
    // 设置表格行的样式
    const tableRowClassName = ({ row }) => {
      if (row.status === 'COMPLETED') {
        return 'success-row';
      } else if (row.status === 'CANCELED') {
        return 'canceled-row';
      } else if (row.status === 'IN_PROGRESS') {
        return 'active-row';
      }
      return '';
    };
    
    // 获取状态对应的标签样式
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
    
    // 操作处理函数
    const handleView = (order) => {
      emit('view', order);
    };
    
    const handleAccept = (order) => {
      emit('accept', order);
    };
    
    const handleReject = (order) => {
      emit('reject', order);
    };
    
    const handleUpdateStatus = (order, status) => {
      emit('update-status', order, status);
    };
    
    const handleAddMaterial = (order) => {
      emit('add-material', order);
    };
    
    const handleAddFeedback = (order) => {
      emit('add-feedback', order);
    };
    
    return {
      isCustomer,
      isMechanic,
      isAdmin,
      tableRowClassName,
      getStatusType,
      getStatusText,
      formatDate,
      handleView,
      handleAccept,
      handleReject,
      handleUpdateStatus,
      handleAddMaterial,
      handleAddFeedback
    };
  }
};
</script>

<style scoped>
.repair-order-table {
  width: 100%;
}

.empty-data {
  padding: 30px 0;
}

.action-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

:deep(.success-row) {
  background-color: rgba(103, 194, 58, 0.1);
}

:deep(.canceled-row) {
  background-color: rgba(245, 108, 108, 0.1);
}

:deep(.active-row) {
  background-color: rgba(64, 158, 255, 0.1);
}
</style> 