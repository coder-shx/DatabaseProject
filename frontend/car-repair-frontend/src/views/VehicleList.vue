<template>
  <div class="vehicle-list-container">
    <el-card class="vehicle-list-card">
      <template #header>
        <div class="card-header">
          <h2>我的车辆</h2>
          <el-button type="primary" @click="showAddVehicleDialog">添加车辆</el-button>
        </div>
      </template>
      
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="3" animated />
      </div>
      
      <div v-else-if="vehicles.length === 0" class="empty-container">
        <el-empty description="暂无车辆信息" :image-size="200">
          <el-button type="primary" @click="showAddVehicleDialog">添加车辆</el-button>
        </el-empty>
      </div>
      
      <div v-else class="vehicle-grid">
        <el-card v-for="vehicle in vehicles" :key="vehicle.id" class="vehicle-card">
          <div class="vehicle-header">
            <h3>{{ vehicle.brand }} {{ vehicle.model }}</h3>
            <div class="vehicle-actions">
              <el-button-group>
                <el-button size="small" type="primary" @click="handleEdit(vehicle)">编辑</el-button>
                <el-button size="small" type="danger" @click="handleDelete(vehicle)">删除</el-button>
              </el-button-group>
            </div>
          </div>
          <div class="vehicle-content">
            <p><strong>车牌号：</strong>{{ vehicle.licensePlate }}</p>
            <p><strong>生产年份：</strong>{{ vehicle.year || '未知' }}</p>
            <p><strong>颜色：</strong>{{ vehicle.color || '未知' }}</p>
            <p><strong>车辆识别码：</strong>{{ vehicle.vinNumber || '未知' }}</p>
          </div>
          <div class="vehicle-footer">
            <el-button type="primary" plain @click="createRepairOrder(vehicle)">申请维修</el-button>
            <el-button type="info" plain @click="viewVehicleHistory(vehicle)">维修历史</el-button>
          </div>
        </el-card>
      </div>
    </el-card>
    
    <!-- 添加/编辑车辆对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑车辆' : '添加车辆'"
      width="500px"
      :close-on-click-modal="false">
      <el-form :model="vehicleForm" :rules="rules" ref="vehicleFormRef" label-width="100px">
        <el-form-item label="车牌号" prop="licensePlate">
          <el-input v-model="vehicleForm.licensePlate" placeholder="请输入车牌号"></el-input>
        </el-form-item>
        <el-form-item label="品牌" prop="brand">
          <el-input v-model="vehicleForm.brand" placeholder="请输入品牌"></el-input>
        </el-form-item>
        <el-form-item label="型号" prop="model">
          <el-input v-model="vehicleForm.model" placeholder="请输入型号"></el-input>
        </el-form-item>
        <el-form-item label="生产年份" prop="year">
          <el-date-picker
            v-model="vehicleForm.year"
            type="year"
            placeholder="选择年份"
            format="YYYY"
            value-format="YYYY"
            style="width: 100%">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="颜色" prop="color">
          <el-input v-model="vehicleForm.color" placeholder="请输入颜色"></el-input>
        </el-form-item>
        <el-form-item label="车辆识别码" prop="vinNumber">
          <el-input v-model="vehicleForm.vinNumber" placeholder="请输入车辆识别码"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="saveVehicle">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue';
import { useStore } from 'vuex';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';

export default {
  name: 'VehicleList',
  setup() {
    const store = useStore();
    const router = useRouter();
    const vehicleFormRef = ref(null);
    
    // 数据
    const vehicles = computed(() => store.state.vehicles);
    const loading = computed(() => store.state.loading);
    const saving = ref(false);
    const dialogVisible = ref(false);
    const isEdit = ref(false);
    
    // 车辆表单
    const vehicleForm = reactive({
      id: null,
      licensePlate: '',
      brand: '',
      model: '',
      year: '',
      color: '',
      vinNumber: ''
    });
    
    // 表单验证规则
    const rules = {
      licensePlate: [
        { required: true, message: '请输入车牌号', trigger: 'blur' },
        { pattern: /^[\u4e00-\u9fa5]{1}[A-Z]{1}[A-Z0-9]{5}$/, message: '请输入正确的车牌号格式', trigger: 'blur' }
      ],
      brand: [
        { required: true, message: '请输入品牌', trigger: 'blur' }
      ],
      model: [
        { required: true, message: '请输入型号', trigger: 'blur' }
      ]
    };
    
    // 获取车辆列表
    const fetchVehicles = async () => {
      try {
        await store.dispatch('fetchUserVehicles');
      } catch (error) {
        console.error('Failed to fetch vehicles:', error);
      }
    };
    
    // 显示添加车辆对话框
    const showAddVehicleDialog = () => {
      isEdit.value = false;
      resetForm();
      dialogVisible.value = true;
    };
    
    // 显示编辑车辆对话框
    const handleEdit = (vehicle) => {
      isEdit.value = true;
      Object.assign(vehicleForm, vehicle);
      dialogVisible.value = true;
    };
    
    // 删除车辆
    const handleDelete = (vehicle) => {
      ElMessageBox.confirm(
        `确定要删除车辆 ${vehicle.brand} ${vehicle.model} (${vehicle.licensePlate}) 吗？`,
        '警告',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }
      ).then(async () => {
        try {
          await store.dispatch('deleteVehicle', vehicle.id);
          ElMessage.success('删除成功');
        } catch (error) {
          console.error('Failed to delete vehicle:', error);
        }
      }).catch(() => {});
    };
    
    // 保存车辆
    const saveVehicle = async () => {
      if (!vehicleFormRef.value) return;
      
      try {
        await vehicleFormRef.value.validate();
        saving.value = true;
        
        if (isEdit.value) {
          // 更新车辆
          await store.dispatch('updateVehicle', {
            id: vehicleForm.id,
            vehicleData: { ...vehicleForm }
          });
          ElMessage.success('车辆信息已更新');
        } else {
          // 添加车辆
          await store.dispatch('addVehicle', { ...vehicleForm });
          ElMessage.success('车辆已添加');
        }
        
        dialogVisible.value = false;
      } catch (error) {
        console.error('Failed to save vehicle:', error);
      } finally {
        saving.value = false;
      }
    };
    
    // 申请维修
    const createRepairOrder = (vehicle) => {
      router.push({
        path: '/create-repair-order',
        query: { vehicleId: vehicle.id }
      });
    };
    
    // 查看车辆维修历史
    const viewVehicleHistory = (vehicle) => {
      router.push({
        path: '/repair-orders',
        query: { vehicleId: vehicle.id }
      });
    };
    
    // 重置表单
    const resetForm = () => {
      vehicleForm.id = null;
      vehicleForm.licensePlate = '';
      vehicleForm.brand = '';
      vehicleForm.model = '';
      vehicleForm.year = '';
      vehicleForm.color = '';
      vehicleForm.vinNumber = '';
      
      if (vehicleFormRef.value) {
        vehicleFormRef.value.resetFields();
      }
    };
    
    onMounted(() => {
      fetchVehicles();
    });
    
    return {
      vehicles,
      loading,
      saving,
      dialogVisible,
      isEdit,
      vehicleForm,
      vehicleFormRef,
      rules,
      showAddVehicleDialog,
      handleEdit,
      handleDelete,
      saveVehicle,
      createRepairOrder,
      viewVehicleHistory
    };
  }
};
</script>

<style scoped>
.vehicle-list-container {
  padding: 20px;
}

.vehicle-list-card {
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

.loading-container {
  padding: 20px 0;
}

.empty-container {
  padding: 40px 0;
}

.vehicle-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.vehicle-card {
  transition: all 0.3s;
}

.vehicle-card:hover {
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  transform: translateY(-5px);
}

.vehicle-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.vehicle-header h3 {
  margin: 0;
  font-size: 18px;
}

.vehicle-content {
  margin-bottom: 20px;
}

.vehicle-content p {
  margin: 8px 0;
}

.vehicle-footer {
  display: flex;
  justify-content: space-between;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style> 