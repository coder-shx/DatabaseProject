<template>
  <div class="vehicle-detail-container">
    <el-card class="vehicle-detail-card">
      <template #header>
        <div class="card-header">
          <h2>车辆详情</h2>
          <div class="header-actions">
            <el-button type="primary" @click="handleEdit">编辑</el-button>
            <el-button type="danger" @click="handleDelete">删除</el-button>
          </div>
        </div>
      </template>
      
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>
      
      <div v-else-if="!vehicle" class="empty-container">
        <el-empty description="未找到车辆信息" :image-size="200">
          <el-button @click="$router.push('/vehicles')">返回车辆列表</el-button>
        </el-empty>
      </div>
      
      <div v-else class="vehicle-info">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="车牌号">{{ vehicle.licensePlate }}</el-descriptions-item>
          <el-descriptions-item label="品牌">{{ vehicle.brand }}</el-descriptions-item>
          <el-descriptions-item label="型号">{{ vehicle.model }}</el-descriptions-item>
          <el-descriptions-item label="生产年份">{{ vehicle.year || '未知' }}</el-descriptions-item>
          <el-descriptions-item label="颜色">{{ vehicle.color || '未知' }}</el-descriptions-item>
          <el-descriptions-item label="车辆识别码">{{ vehicle.vinNumber || '未知' }}</el-descriptions-item>
        </el-descriptions>
        
        <div class="action-buttons">
          <el-button type="primary" @click="createRepairOrder">申请维修</el-button>
        </div>
      </div>
    </el-card>
    
    <el-card class="repair-history-card">
      <template #header>
        <div class="card-header">
          <h3>维修记录</h3>
        </div>
      </template>
      
      <div v-if="loadingHistory" class="loading-container">
        <el-skeleton :rows="3" animated />
      </div>
      
      <div v-else>
        <el-table v-if="repairHistory.length > 0" :data="repairHistory" style="width: 100%">
          <el-table-column prop="id" label="工单号" width="90"></el-table-column>
          <el-table-column prop="createdAt" label="维修日期">
            <template #default="scope">
              {{ formatDate(scope.row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column prop="description" label="故障描述" show-overflow-tooltip></el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="totalCost" label="费用" width="100">
            <template #default="scope">
              {{ scope.row.totalCost ? `¥${scope.row.totalCost}` : '-' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="scope">
              <el-button link type="primary" @click="viewOrderDetail(scope.row)">详情</el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div v-else class="empty-data">
          <p>暂无维修记录</p>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue';
import { useStore } from 'vuex';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { vehicleApi } from '@/api/api';

export default {
  name: 'VehicleDetailView',
  setup() {
    const store = useStore();
    const router = useRouter();
    const route = useRoute();
    
    // 获取路由参数中的车辆ID
    const vehicleId = computed(() => Number(route.params.id));
    
    // 状态变量
    const loading = ref(true);
    const loadingHistory = ref(true);
    const vehicle = ref(null);
    const repairHistory = ref([]);
    
    // 从store获取车辆信息
    const vehicles = computed(() => store.state.vehicles);
    
    // 获取车辆详情
    const fetchVehicleDetail = async () => {
      loading.value = true;
      
      try {
        // 先尝试从store中获取车辆信息
        if (vehicles.value.length === 0) {
          await store.dispatch('fetchUserVehicles');
        }
        
        // 在store中查找车辆
        const foundVehicle = vehicles.value.find(v => v.id === vehicleId.value);
        
        if (foundVehicle) {
          vehicle.value = foundVehicle;
        } else {
          // 如果在store中找不到，尝试单独获取
          try {
            const response = await vehicleApi.getVehicle(vehicleId.value);
            vehicle.value = response;
          } catch (error) {
            console.error('Failed to fetch vehicle:', error);
            ElMessage.error('获取车辆信息失败');
          }
        }
      } finally {
        loading.value = false;
      }
    };
    
    // 获取维修记录
    const fetchRepairHistory = async () => {
      loadingHistory.value = true;
      
      try {
        // 这里应该调用获取车辆维修记录的API
        // 由于暂时没有实现，我们模拟一些数据
        await new Promise(resolve => setTimeout(resolve, 500)); // 模拟API调用
        
        repairHistory.value = [
          {
            id: 1001,
            createdAt: '2023-10-10T08:30:00',
            description: '发动机异响，怠速不稳',
            status: 'COMPLETED',
            totalCost: 1560.50
          },
          {
            id: 1022,
            createdAt: '2023-06-15T14:20:00',
            description: '更换制动片',
            status: 'COMPLETED',
            totalCost: 860.00
          },
          {
            id: 1045,
            createdAt: '2023-03-01T10:15:00',
            description: '定期保养，更换机油和滤清器',
            status: 'COMPLETED',
            totalCost: 650.50
          }
        ];
      } finally {
        loadingHistory.value = false;
      }
    };
    
    // 编辑车辆
    const handleEdit = () => {
      router.push(`/vehicles/${vehicleId.value}/edit`);
    };
    
    // 删除车辆
    const handleDelete = () => {
      ElMessageBox.confirm(
        `确定要删除车辆 ${vehicle.value.brand} ${vehicle.value.model} (${vehicle.value.licensePlate}) 吗？`,
        '警告',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }
      ).then(async () => {
        try {
          await store.dispatch('deleteVehicle', vehicleId.value);
          ElMessage.success('删除成功');
          router.push('/vehicles');
        } catch (error) {
          console.error('Failed to delete vehicle:', error);
          ElMessage.error('删除失败');
        }
      }).catch(() => {});
    };
    
    // 申请维修
    const createRepairOrder = () => {
      router.push({
        path: '/create-repair-order',
        query: { vehicleId: vehicleId.value }
      });
    };
    
    // 查看工单详情
    const viewOrderDetail = (order) => {
      router.push(`/repair-orders/${order.id}`);
    };
    
    // 格式化日期
    const formatDate = (dateStr) => {
      if (!dateStr) return '-';
      const date = new Date(dateStr);
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
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
    
    onMounted(() => {
      fetchVehicleDetail();
      fetchRepairHistory();
    });
    
    return {
      loading,
      loadingHistory,
      vehicle,
      repairHistory,
      handleEdit,
      handleDelete,
      createRepairOrder,
      viewOrderDetail,
      formatDate,
      getStatusType,
      getStatusText
    };
  }
};
</script>

<style scoped>
.vehicle-detail-container {
  padding: 20px;
}

.vehicle-detail-card,
.repair-history-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2,
.card-header h3 {
  margin: 0;
}

.loading-container,
.empty-container {
  padding: 20px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.vehicle-info {
  margin-bottom: 20px;
}

.action-buttons {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 10px;
}

.empty-data {
  text-align: center;
  color: #909399;
  padding: 20px 0;
}
</style> 