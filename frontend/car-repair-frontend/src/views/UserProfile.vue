<template>
  <div class="user-profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <h2>个人资料</h2>
          <el-button type="primary" @click="enableEdit" v-if="!isEditing">编辑资料</el-button>
          <div v-else>
            <el-button type="primary" :loading="saving" @click="saveProfile">保存</el-button>
            <el-button @click="cancelEdit">取消</el-button>
          </div>
        </div>
      </template>
      
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="4" animated />
      </div>
      
      <div v-else>
        <el-form
          ref="profileFormRef"
          :model="profileForm"
          :rules="rules"
          label-width="100px"
          :disabled="!isEditing">
          
          <el-form-item label="用户名">
            <el-input v-model="profileForm.username" disabled></el-input>
          </el-form-item>
          
          <el-form-item label="用户角色">
            <el-tag :type="getRoleType(profileForm.role)">{{ getRoleText(profileForm.role) }}</el-tag>
          </el-form-item>
          
          <el-form-item label="姓名" prop="name">
            <el-input v-model="profileForm.name" placeholder="请输入姓名"></el-input>
          </el-form-item>
          
          <el-form-item label="手机号码" prop="phoneNumber">
            <el-input v-model="profileForm.phoneNumber" placeholder="请输入手机号码"></el-input>
          </el-form-item>
          
          <el-form-item label="电子邮箱" prop="email">
            <el-input v-model="profileForm.email" placeholder="请输入电子邮箱"></el-input>
          </el-form-item>
          
          <!-- 技工特有信息 -->
          <template v-if="isMechanic">
            <el-divider>技工信息</el-divider>
            
            <el-form-item label="工种">
              <el-tag type="success">{{ getMechanicTypeText(mechanic.mechanicType) }}</el-tag>
            </el-form-item>
            
            <el-form-item label="时薪">
              <span>¥{{ mechanic.hourlyRate }}/小时</span>
            </el-form-item>
          </template>
          
          <!-- 修改密码 -->
          <template v-if="isEditing">
            <el-divider>修改密码（可选）</el-divider>
            
            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="profileForm.newPassword"
                type="password"
                placeholder="留空表示不修改密码"
                show-password>
              </el-input>
            </el-form-item>
            
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="profileForm.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                show-password>
              </el-input>
            </el-form-item>
          </template>
        </el-form>
      </div>
    </el-card>
    
    <!-- 用户的其他信息卡片 -->
    <el-row :gutter="20" class="info-cards">
      <el-col :xs="24" :sm="12" v-if="isCustomer">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <h3>我的车辆</h3>
              <el-button text @click="$router.push('/vehicles')">管理车辆</el-button>
            </div>
          </template>
          <div v-if="vehicles.length > 0">
            <div v-for="vehicle in vehicles.slice(0, 3)" :key="vehicle.id" class="vehicle-item">
              <div class="vehicle-info">
                <strong>{{ vehicle.brand }} {{ vehicle.model }}</strong>
                <span class="license-plate">{{ vehicle.licensePlate }}</span>
              </div>
            </div>
            <div v-if="vehicles.length > 3" class="more-items">
              还有 {{ vehicles.length - 3 }} 辆车...
            </div>
          </div>
          <div v-else class="empty-info">
            <p>暂无车辆信息</p>
            <el-button type="primary" size="small" @click="$router.push('/vehicles')">添加车辆</el-button>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <h3>最近维修工单</h3>
              <el-button text @click="$router.push('/repair-orders')">全部工单</el-button>
            </div>
          </template>
          <div v-if="repairOrders.length > 0">
            <div v-for="order in repairOrders.slice(0, 3)" :key="order.id" class="order-item">
              <div class="order-info">
                <div>
                  <strong>工单号：</strong>{{ order.id }}
                  <el-tag :type="getStatusType(order.status)" size="small" class="status-tag">
                    {{ getStatusText(order.status) }}
                  </el-tag>
                </div>
                <div><strong>车牌号：</strong>{{ order.vehicle.licensePlate }}</div>
                <div><strong>创建时间：</strong>{{ formatDate(order.createdAt) }}</div>
              </div>
              <el-button
                link
                type="primary"
                @click="$router.push(`/repair-orders/${order.id}`)">
                查看
              </el-button>
            </div>
            <div v-if="repairOrders.length > 3" class="more-items">
              还有 {{ repairOrders.length - 3 }} 条工单...
            </div>
          </div>
          <div v-else class="empty-info">
            <p>暂无维修工单</p>
            <el-button
              v-if="isCustomer"
              type="primary"
              size="small"
              @click="$router.push('/create-repair-order')">
              申请维修
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { useStore } from 'vuex';
import { ElMessage } from 'element-plus';

export default {
  name: 'UserProfile',
  setup() {
    const store = useStore();
    const profileFormRef = ref(null);
    
    // 状态变量
    const loading = computed(() => store.state.loading);
    const saving = ref(false);
    const isEditing = ref(false);
    
    // 用户数据
    const user = computed(() => store.state.user);
    const vehicles = computed(() => store.state.vehicles);
    const repairOrders = computed(() => store.state.repairOrders);
    const mechanic = ref({
      mechanicType: '',
      hourlyRate: 0
    });
    
    // 用户角色
    const isCustomer = computed(() => store.getters.isCustomer);
    const isMechanic = computed(() => store.getters.isMechanic);
    const isAdmin = computed(() => store.getters.isAdmin);
    
    // 表单数据
    const profileForm = reactive({
      id: '',
      username: '',
      name: '',
      phoneNumber: '',
      email: '',
      role: '',
      newPassword: '',
      confirmPassword: ''
    });
    
    // 自定义校验密码一致性的验证器
    const validatePass = (rule, value, callback) => {
      if (profileForm.newPassword && value !== profileForm.newPassword) {
        callback(new Error('两次输入的密码不一致'));
      } else {
        callback();
      }
    };
    
    // 表单验证规则
    const rules = {
      name: [
        { required: true, message: '请输入姓名', trigger: 'blur' }
      ],
      phoneNumber: [
        { required: true, message: '请输入手机号码', trigger: 'blur' },
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
      ],
      email: [
        { required: true, message: '请输入电子邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入正确的电子邮箱格式', trigger: 'blur' }
      ],
      newPassword: [
        { min: 6, max: 20, message: '密码长度在6到20个字符之间', trigger: 'blur' }
      ],
      confirmPassword: [
        { validator: validatePass, trigger: 'blur' }
      ]
    };
    
    // 启用编辑模式
    const enableEdit = () => {
      isEditing.value = true;
    };
    
    // 取消编辑
    const cancelEdit = () => {
      isEditing.value = false;
      resetForm();
    };
    
    // 保存个人资料
    const saveProfile = async () => {
      if (!profileFormRef.value) return;
      
      try {
        await profileFormRef.value.validate();
        saving.value = true;
        
        const userData = {
          id: profileForm.id,
          name: profileForm.name,
          phoneNumber: profileForm.phoneNumber,
          email: profileForm.email
        };
        
        // 如果输入了新密码，则包含密码字段
        if (profileForm.newPassword) {
          userData.password = profileForm.newPassword;
        }
        
        await store.dispatch('updateUser', userData);
        
        ElMessage.success('个人资料已更新');
        isEditing.value = false;
        
        // 清空密码字段
        profileForm.newPassword = '';
        profileForm.confirmPassword = '';
      } catch (error) {
        console.error('Failed to update profile:', error);
        ElMessage.error('更新个人资料失败');
      } finally {
        saving.value = false;
      }
    };
    
    // 重置表单为当前用户数据
    const resetForm = () => {
      if (user.value) {
        profileForm.id = user.value.id;
        profileForm.username = user.value.username;
        profileForm.name = user.value.name;
        profileForm.phoneNumber = user.value.phoneNumber;
        profileForm.email = user.value.email;
        profileForm.role = user.value.role;
      }
      
      profileForm.newPassword = '';
      profileForm.confirmPassword = '';
      
      if (profileFormRef.value) {
        profileFormRef.value.clearValidate();
      }
    };
    
    // 加载用户数据
    const loadUserData = async () => {
      try {
        // 获取用户信息
        if (!user.value) {
          await store.dispatch('fetchUserInfo');
        }
        
        // 重置表单为当前用户数据
        resetForm();
        
        // 如果是普通用户，获取车辆列表
        if (isCustomer.value && vehicles.value.length === 0) {
          await store.dispatch('fetchUserVehicles');
        }
        
        // 获取维修工单
        if (repairOrders.value.length === 0) {
          await store.dispatch('fetchUserOrders');
        }
        
        // 如果是技工，获取技工信息
        if (isMechanic.value) {
          // 这里应该调用获取技工信息的API
          // 由于暂时没有实现，我们模拟一些数据
          mechanic.value = {
            mechanicType: 'MECHANIC', // 可能的值: PAINTER, WELDER, MECHANIC, ELECTRIC, TIRE, OTHER
            hourlyRate: 100
          };
        }
      } catch (error) {
        console.error('Failed to load user data:', error);
        ElMessage.error('加载用户数据失败');
      }
    };
    
    // 获取角色标签类型
    const getRoleType = (role) => {
      const typeMap = {
        'CUSTOMER': '',
        'MECHANIC': 'success',
        'ADMIN': 'danger'
      };
      return typeMap[role] || '';
    };
    
    // 获取角色文本
    const getRoleText = (role) => {
      const textMap = {
        'CUSTOMER': '普通用户',
        'MECHANIC': '维修人员',
        'ADMIN': '系统管理员'
      };
      return textMap[role] || role;
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
    
    // 监听用户数据变化，更新表单
    watch(user, () => {
      resetForm();
    });
    
    onMounted(() => {
      loadUserData();
    });
    
    return {
      loading,
      saving,
      isEditing,
      profileForm,
      profileFormRef,
      rules,
      user,
      vehicles,
      repairOrders,
      mechanic,
      isCustomer,
      isMechanic,
      isAdmin,
      enableEdit,
      cancelEdit,
      saveProfile,
      getRoleType,
      getRoleText,
      getMechanicTypeText,
      getStatusType,
      getStatusText,
      formatDate
    };
  }
};
</script>

<style scoped>
.user-profile-container {
  padding: 20px;
}

.profile-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2, .card-header h3 {
  margin: 0;
}

.loading-container {
  padding: 20px 0;
}

.info-cards {
  margin-top: 20px;
}

.info-card {
  margin-bottom: 20px;
  height: 100%;
}

.vehicle-item, .order-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #ebeef5;
}

.vehicle-item:last-child, .order-item:last-child {
  border-bottom: none;
}

.vehicle-info, .order-info {
  flex: 1;
}

.license-plate {
  margin-left: 10px;
  color: #909399;
}

.status-tag {
  margin-left: 5px;
}

.more-items {
  text-align: center;
  color: #909399;
  padding: 10px 0;
}

.empty-info {
  text-align: center;
  color: #909399;
  padding: 20px 0;
}
</style> 