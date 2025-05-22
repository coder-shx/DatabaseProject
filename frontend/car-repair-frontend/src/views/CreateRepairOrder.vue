<template>
  <div class="create-repair-order-container">
    <el-card class="create-order-card">
      <template #header>
        <div class="card-header">
          <h2>提交维修申请</h2>
        </div>
      </template>
      
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>
      
      <div v-else>
        <el-form :model="orderForm" :rules="rules" ref="orderFormRef" label-width="100px">
          <!-- 选择车辆 -->
          <el-form-item label="车辆" prop="vehicleId">
            <el-select
              v-model="orderForm.vehicleId"
              filterable
              placeholder="请选择车辆"
              style="width: 100%"
              @change="handleVehicleChange">
              <el-option
                v-for="vehicle in vehicles"
                :key="vehicle.id"
                :label="`${vehicle.brand} ${vehicle.model} (${vehicle.licensePlate})`"
                :value="vehicle.id">
              </el-option>
            </el-select>
            <div class="add-vehicle-tip" v-if="vehicles.length === 0">
              <span>您还没有添加车辆，</span>
              <el-button type="text" @click="$router.push('/vehicles')">点击添加车辆</el-button>
            </div>
          </el-form-item>
          
          <!-- 车辆信息预览 -->
          <div v-if="selectedVehicle" class="vehicle-preview">
            <el-descriptions :column="2" border size="small">
              <el-descriptions-item label="车牌号">{{ selectedVehicle.licensePlate }}</el-descriptions-item>
              <el-descriptions-item label="品牌车型">{{ selectedVehicle.brand }} {{ selectedVehicle.model }}</el-descriptions-item>
              <el-descriptions-item label="生产年份">{{ selectedVehicle.year || '未知' }}</el-descriptions-item>
              <el-descriptions-item label="颜色">{{ selectedVehicle.color || '未知' }}</el-descriptions-item>
            </el-descriptions>
          </div>
          
          <!-- 故障描述 -->
          <el-form-item label="故障描述" prop="description">
            <el-input
              v-model="orderForm.description"
              type="textarea"
              :rows="4"
              placeholder="请详细描述车辆故障情况">
            </el-input>
          </el-form-item>
          
          <!-- 期望的维修类型 -->
          <el-form-item label="维修类型" prop="repairType">
            <el-select
              v-model="orderForm.repairType"
              placeholder="请选择维修类型"
              style="width: 100%">
              <el-option label="常规保养" value="REGULAR_MAINTENANCE"></el-option>
              <el-option label="发动机维修" value="ENGINE_REPAIR"></el-option>
              <el-option label="底盘维修" value="CHASSIS_REPAIR"></el-option>
              <el-option label="电气系统维修" value="ELECTRICAL_REPAIR"></el-option>
              <el-option label="轮胎更换" value="TIRE_REPLACEMENT"></el-option>
              <el-option label="外观维修" value="BODY_REPAIR"></el-option>
              <el-option label="空调系统" value="AC_REPAIR"></el-option>
              <el-option label="其他" value="OTHER"></el-option>
            </el-select>
          </el-form-item>
          
          <!-- 期望预约时间 -->
          <el-form-item label="预约时间" prop="appointmentTime">
            <el-date-picker
              v-model="orderForm.appointmentTime"
              type="datetime"
              placeholder="选择预约时间"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm"
              :disabled-date="disabledDate"
              :disabled-hours="disabledHours"
              style="width: 100%">
            </el-date-picker>
          </el-form-item>
          
          <!-- 备注信息 -->
          <el-form-item label="备注" prop="notes">
            <el-input
              v-model="orderForm.notes"
              type="textarea"
              :rows="2"
              placeholder="其他需要补充的信息（选填）">
            </el-input>
          </el-form-item>
          
          <!-- 紧急程度 -->
          <el-form-item label="紧急程度" prop="urgency">
            <el-radio-group v-model="orderForm.urgency">
              <el-radio label="LOW">一般</el-radio>
              <el-radio label="MEDIUM">较急</el-radio>
              <el-radio label="HIGH">紧急</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <!-- 提交按钮 -->
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="submitOrder">提交申请</el-button>
            <el-button @click="$router.back()">取消</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue';
import { useStore } from 'vuex';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';

export default {
  name: 'CreateRepairOrderView',
  setup() {
    const store = useStore();
    const router = useRouter();
    const route = useRoute();
    const orderFormRef = ref(null);
    
    // 状态变量
    const loading = ref(true);
    const saving = ref(false);
    
    // 表单数据
    const orderForm = reactive({
      vehicleId: '',
      description: '',
      repairType: '',
      appointmentTime: '',
      notes: '',
      urgency: 'MEDIUM'
    });
    
    // 表单验证规则
    const rules = {
      vehicleId: [
        { required: true, message: '请选择车辆', trigger: 'change' }
      ],
      description: [
        { required: true, message: '请描述故障情况', trigger: 'blur' },
        { min: 10, message: '故障描述不能少于10个字符', trigger: 'blur' }
      ],
      repairType: [
        { required: true, message: '请选择维修类型', trigger: 'change' }
      ],
      appointmentTime: [
        { required: true, message: '请选择预约时间', trigger: 'change' }
      ]
    };
    
    // 从store获取车辆数据
    const vehicles = computed(() => store.state.vehicles);
    const selectedVehicle = computed(() => {
      if (!orderForm.vehicleId) return null;
      return vehicles.value.find(v => v.id === orderForm.vehicleId);
    });
    
    // 日期选择器限制
    const disabledDate = (time) => {
      // 禁用今天之前的日期
      return time.getTime() < Date.now() - 8.64e7; // 减去一天的毫秒数
    };
    
    const disabledHours = () => {
      // 禁用8点之前和18点之后的时间
      const hours = [];
      for (let i = 0; i < 24; i++) {
        if (i < 8 || i > 18) {
          hours.push(i);
        }
      }
      return hours;
    };
    
    // 处理车辆选择变化
    const handleVehicleChange = (vehicleId) => {
      orderForm.vehicleId = vehicleId;
    };
    
    // 获取车辆列表
    const fetchVehicles = async () => {
      try {
        // 如果store中没有车辆数据，则获取
        if (vehicles.value.length === 0) {
          await store.dispatch('fetchUserVehicles');
        }
        
        // 如果路由参数中有车辆ID，则自动选择
        const vehicleId = route.query.vehicleId;
        if (vehicleId) {
          orderForm.vehicleId = Number(vehicleId);
        }
      } catch (error) {
        console.error('Failed to fetch vehicles:', error);
        ElMessage.error('获取车辆列表失败');
      } finally {
        loading.value = false;
      }
    };
    
    // 提交维修工单
    const submitOrder = async () => {
      if (!orderFormRef.value) return;
      
      try {
        await orderFormRef.value.validate();
        saving.value = true;
        
        const orderData = {
          vehicleId: orderForm.vehicleId,
          description: orderForm.description,
          repairType: orderForm.repairType,
          appointmentTime: orderForm.appointmentTime,
          notes: orderForm.notes,
          urgency: orderForm.urgency
        };
        
        const newOrder = await store.dispatch('createRepairOrder', orderData);
        
        ElMessage.success('维修申请已提交');
        
        // 跳转到工单详情页
        router.push(`/repair-orders/${newOrder.id}`);
      } catch (error) {
        console.error('Failed to create repair order:', error);
        ElMessage.error('提交维修申请失败');
      } finally {
        saving.value = false;
      }
    };
    
    onMounted(() => {
      fetchVehicles();
    });
    
    return {
      loading,
      saving,
      orderForm,
      orderFormRef,
      rules,
      vehicles,
      selectedVehicle,
      disabledDate,
      disabledHours,
      handleVehicleChange,
      submitOrder
    };
  }
};
</script>

<style scoped>
.create-repair-order-container {
  padding: 20px;
}

.create-order-card {
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

.add-vehicle-tip {
  margin-top: 10px;
  color: #909399;
}

.vehicle-preview {
  margin: 15px 0 25px 100px;
}

.el-form-item:last-child {
  margin-bottom: 0;
}
</style> 