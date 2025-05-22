<template>
  <div class="dashboard-container">
    <el-container>
      <el-aside width="240px" class="aside">
        <div class="logo-container">
          <h2 class="logo">汽车维修系统</h2>
        </div>
        <el-menu
          :default-active="activeMenu"
          class="menu"
          :router="true"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF">
          
          <!-- 所有用户都能看到的菜单 -->
          <el-menu-item index="/dashboard">
            <el-icon><House /></el-icon>
            <span>仪表盘</span>
          </el-menu-item>
          
          <el-menu-item index="/profile">
            <el-icon><User /></el-icon>
            <span>个人资料</span>
          </el-menu-item>
          
          <!-- 客户菜单 -->
          <template v-if="isCustomer">
            <el-menu-item index="/vehicles">
              <el-icon><Van /></el-icon>
              <span>我的车辆</span>
            </el-menu-item>
            
            <el-menu-item index="/repair-orders">
              <el-icon><Tickets /></el-icon>
              <span>维修工单</span>
            </el-menu-item>
            
            <el-menu-item index="/create-repair-order">
              <el-icon><Plus /></el-icon>
              <span>提交维修申请</span>
            </el-menu-item>
          </template>
          
          <!-- 技工菜单 -->
          <template v-if="isMechanic">
            <el-menu-item index="/repair-orders">
              <el-icon><Tickets /></el-icon>
              <span>维修工单</span>
            </el-menu-item>
          </template>
          
          <!-- 管理员菜单 -->
          <template v-if="isAdmin">
            <el-sub-menu index="admin">
              <template #title>
                <el-icon><Setting /></el-icon>
                <span>系统管理</span>
              </template>
              <el-menu-item index="/admin/users">用户管理</el-menu-item>
              <el-menu-item index="/admin/mechanics">技工管理</el-menu-item>
              <el-menu-item index="/admin/vehicles">车辆管理</el-menu-item>
              <el-menu-item index="/admin/orders">工单管理</el-menu-item>
              <el-menu-item index="/admin/statistics">统计分析</el-menu-item>
            </el-sub-menu>
          </template>
        </el-menu>
      </el-aside>
      
      <el-container>
        <el-header height="60px" class="header">
          <div class="header-left">
            <el-icon class="toggle-icon" @click="toggleCollapse"><Expand /></el-icon>
          </div>
          <div class="header-right">
            <el-dropdown trigger="click">
              <div class="avatar-container">
                <el-avatar :size="32" class="avatar"></el-avatar>
                <span class="username">{{ user?.name || '用户' }}</span>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="$router.push('/profile')">个人资料</el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>
        
        <el-main class="main">
          <!-- 根据用户角色显示不同内容 -->
          <div v-if="isCustomer" class="dashboard-content customer-dashboard">
            <el-row :gutter="20">
              <el-col :span="8">
                <el-card class="dashboard-card">
                  <template #header>
                    <div class="card-header">
                      <span>我的车辆</span>
                      <el-button text @click="$router.push('/vehicles')">查看全部</el-button>
                    </div>
                  </template>
                  <div v-if="vehicles.length > 0">
                    <div v-for="vehicle in vehicles.slice(0, 2)" :key="vehicle.id" class="vehicle-item">
                      <p><strong>{{ vehicle.brand }} {{ vehicle.model }}</strong></p>
                      <p>车牌号: {{ vehicle.licensePlate }}</p>
                    </div>
                    <el-button type="primary" size="small" @click="$router.push('/create-repair-order')">
                      申请维修
                    </el-button>
                  </div>
                  <div v-else class="empty-data">
                    <p>暂无车辆信息</p>
                    <el-button type="primary" size="small" @click="$router.push('/vehicles')">
                      添加车辆
                    </el-button>
                  </div>
                </el-card>
              </el-col>
              
              <el-col :span="16">
                <el-card class="dashboard-card">
                  <template #header>
                    <div class="card-header">
                      <span>最近维修工单</span>
                      <el-button text @click="$router.push('/repair-orders')">查看全部</el-button>
                    </div>
                  </template>
                  <el-table v-if="repairOrders.length > 0" :data="repairOrders.slice(0, 3)" style="width: 100%">
                    <el-table-column prop="id" label="工单号" width="80"></el-table-column>
                    <el-table-column prop="vehicle.licensePlate" label="车牌号"></el-table-column>
                    <el-table-column prop="createdAt" label="创建时间">
                      <template #default="scope">
                        {{ formatDate(scope.row.createdAt) }}
                      </template>
                    </el-table-column>
                    <el-table-column prop="status" label="状态">
                      <template #default="scope">
                        <el-tag :type="getStatusType(scope.row.status)">
                          {{ getStatusText(scope.row.status) }}
                        </el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column label="操作">
                      <template #default="scope">
                        <el-button link type="primary" @click="$router.push(`/repair-orders/${scope.row.id}`)">
                          查看
                        </el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                  <div v-else class="empty-data">
                    <p>暂无维修工单</p>
                    <el-button type="primary" size="small" @click="$router.push('/create-repair-order')">
                      申请维修
                    </el-button>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>
          
          <div v-if="isMechanic" class="dashboard-content mechanic-dashboard">
            <el-card class="dashboard-card">
              <template #header>
                <div class="card-header">
                  <span>我的维修工单</span>
                  <el-button text @click="$router.push('/repair-orders')">查看全部</el-button>
                </div>
              </template>
              <el-tabs>
                <el-tab-pane label="待处理工单">
                  <el-table v-if="pendingOrders.length > 0" :data="pendingOrders" style="width: 100%">
                    <el-table-column prop="id" label="工单号" width="80"></el-table-column>
                    <el-table-column prop="vehicle.licensePlate" label="车牌号"></el-table-column>
                    <el-table-column prop="description" label="故障描述" show-overflow-tooltip></el-table-column>
                    <el-table-column prop="createdAt" label="创建时间">
                      <template #default="scope">
                        {{ formatDate(scope.row.createdAt) }}
                      </template>
                    </el-table-column>
                    <el-table-column label="操作">
                      <template #default="scope">
                        <el-button link type="primary" @click="$router.push(`/repair-orders/${scope.row.id}`)">
                          处理
                        </el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                  <div v-else class="empty-data">
                    <p>暂无待处理工单</p>
                  </div>
                </el-tab-pane>
                <el-tab-pane label="进行中工单">
                  <el-table v-if="inProgressOrders.length > 0" :data="inProgressOrders" style="width: 100%">
                    <el-table-column prop="id" label="工单号" width="80"></el-table-column>
                    <el-table-column prop="vehicle.licensePlate" label="车牌号"></el-table-column>
                    <el-table-column prop="description" label="故障描述" show-overflow-tooltip></el-table-column>
                    <el-table-column prop="startedAt" label="开始时间">
                      <template #default="scope">
                        {{ formatDate(scope.row.startedAt) }}
                      </template>
                    </el-table-column>
                    <el-table-column label="操作">
                      <template #default="scope">
                        <el-button link type="primary" @click="$router.push(`/repair-orders/${scope.row.id}`)">
                          继续处理
                        </el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                  <div v-else class="empty-data">
                    <p>暂无进行中工单</p>
                  </div>
                </el-tab-pane>
              </el-tabs>
            </el-card>
          </div>
          
          <div v-if="isAdmin" class="dashboard-content admin-dashboard">
            <el-row :gutter="20">
              <el-col :span="6">
                <el-card class="stat-card">
                  <h3>用户数量</h3>
                  <div class="stat-value">{{ adminStats.userCount || 0 }}</div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card class="stat-card">
                  <h3>车辆数量</h3>
                  <div class="stat-value">{{ adminStats.vehicleCount || 0 }}</div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card class="stat-card">
                  <h3>技工数量</h3>
                  <div class="stat-value">{{ adminStats.mechanicCount || 0 }}</div>
                </el-card>
              </el-col>
              <el-col :span="6">
                <el-card class="stat-card">
                  <h3>待处理工单</h3>
                  <div class="stat-value">{{ adminStats.pendingOrderCount || 0 }}</div>
                </el-card>
              </el-col>
            </el-row>
            
            <el-row :gutter="20" class="mt-20">
              <el-col :span="12">
                <el-card class="dashboard-card">
                  <template #header>
                    <div class="card-header">
                      <span>最近订单</span>
                      <el-button text @click="$router.push('/admin/orders')">管理工单</el-button>
                    </div>
                  </template>
                  <el-table v-if="adminOrders.length > 0" :data="adminOrders.slice(0, 5)" style="width: 100%">
                    <el-table-column prop="id" label="工单号" width="80"></el-table-column>
                    <el-table-column prop="customer.name" label="客户"></el-table-column>
                    <el-table-column prop="vehicle.licensePlate" label="车牌号"></el-table-column>
                    <el-table-column prop="status" label="状态">
                      <template #default="scope">
                        <el-tag :type="getStatusType(scope.row.status)">
                          {{ getStatusText(scope.row.status) }}
                        </el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column label="操作">
                      <template #default="scope">
                        <el-button link type="primary" @click="$router.push(`/repair-orders/${scope.row.id}`)">
                          查看
                        </el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                  <div v-else class="empty-data">
                    <p>暂无维修工单</p>
                  </div>
                </el-card>
              </el-col>
              
              <el-col :span="12">
                <el-card class="dashboard-card">
                  <template #header>
                    <div class="card-header">
                      <span>快捷操作</span>
                    </div>
                  </template>
                  <div class="quick-actions">
                    <el-button type="primary" @click="$router.push('/admin/users')">用户管理</el-button>
                    <el-button type="primary" @click="$router.push('/admin/mechanics')">技工管理</el-button>
                    <el-button type="primary" @click="$router.push('/admin/vehicles')">车辆管理</el-button>
                    <el-button type="primary" @click="$router.push('/admin/statistics')">统计分析</el-button>
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
import { ref, computed, onMounted, reactive } from 'vue';
import { useStore } from 'vuex';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { House, User, Van, Tickets, Plus, Setting, Expand } from '@element-plus/icons-vue';

export default {
  name: 'Dashboard',
  components: {
    House,
    User,
    Van, 
    Tickets,
    Plus,
    Setting,
    Expand
  },
  setup() {
    const store = useStore();
    const router = useRouter();
    const collapsed = ref(false);

    // 用户信息
    const user = computed(() => store.state.user);
    const isAdmin = computed(() => store.getters.isAdmin);
    const isMechanic = computed(() => store.getters.isMechanic);
    const isCustomer = computed(() => store.getters.isCustomer);
    
    // 菜单状态
    const activeMenu = computed(() => router.currentRoute.value.path);

    // 车辆和维修工单数据
    const vehicles = computed(() => store.state.vehicles);
    const repairOrders = computed(() => store.state.repairOrders);
    
    // 为技工过滤订单
    const pendingOrders = computed(() => {
      return repairOrders.value.filter(order => 
        order.status === 'ASSIGNED' || order.status === 'PENDING'
      );
    });
    
    const inProgressOrders = computed(() => {
      return repairOrders.value.filter(order => 
        order.status === 'IN_PROGRESS'
      );
    });
    
    // 管理员仪表盘数据
    const adminOrders = ref([]);
    const adminStats = reactive({
      userCount: 0,
      vehicleCount: 0,
      mechanicCount: 0,
      pendingOrderCount: 0
    });

    // 加载用户数据
    const loadUserData = async () => {
      try {
        // 获取用户信息
        if (!user.value) {
          await store.dispatch('fetchUserInfo');
        }
        
        // 获取车辆列表
        if (isCustomer.value && vehicles.value.length === 0) {
          await store.dispatch('fetchUserVehicles');
        }
        
        // 获取维修工单
        await store.dispatch('fetchUserOrders');
        
        // 如果是管理员，获取管理员特有的仪表盘数据
        if (isAdmin.value) {
          // 这里应该调用API获取管理员仪表盘数据
          // 由于暂时没有实现，我们模拟一些数据
          adminStats.userCount = 120;
          adminStats.vehicleCount = 85;
          adminStats.mechanicCount = 18;
          adminStats.pendingOrderCount = 12;
          adminOrders.value = repairOrders.value;
        }
      } catch (error) {
        ElMessage.error('加载数据失败');
      }
    };

    // 切换侧边栏折叠状态
    const toggleCollapse = () => {
      collapsed.value = !collapsed.value;
    };

    // 处理用户登出
    const handleLogout = () => {
      ElMessageBox.confirm('确定要退出登录吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }).then(() => {
        store.dispatch('logout');
        router.push('/login');
        ElMessage.success('已成功退出登录');
      }).catch(() => {});
    };

    // 格式化日期
    const formatDate = (dateStr) => {
      if (!dateStr) return '';
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

    onMounted(() => {
      loadUserData();
    });

    return {
      user,
      isAdmin,
      isMechanic,
      isCustomer,
      activeMenu,
      collapsed,
      vehicles,
      repairOrders,
      pendingOrders,
      inProgressOrders,
      adminOrders,
      adminStats,
      toggleCollapse,
      handleLogout,
      formatDate,
      getStatusType,
      getStatusText
    };
  }
};
</script>

<style scoped>
.dashboard-container {
  height: 100vh;
  overflow: hidden;
}

.aside {
  background-color: #304156;
  transition: width 0.3s;
  overflow-x: hidden;
}

.logo-container {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #304156;
}

.logo {
  color: #fff;
  margin: 0;
  font-size: 1.2rem;
}

.menu {
  border-right: none;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.header-left {
  display: flex;
  align-items: center;
}

.toggle-icon {
  font-size: 20px;
  cursor: pointer;
  color: #606266;
}

.header-right {
  display: flex;
  align-items: center;
}

.avatar-container {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.avatar {
  margin-right: 8px;
}

.username {
  font-size: 14px;
  color: #606266;
}

.main {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}

.dashboard-content {
  margin-bottom: 20px;
}

.dashboard-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.vehicle-item {
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.vehicle-item:last-child {
  border-bottom: none;
}

.empty-data {
  text-align: center;
  color: #909399;
  padding: 20px 0;
}

.stat-card {
  text-align: center;
  padding: 20px;
}

.stat-card h3 {
  margin-top: 0;
  color: #606266;
  font-size: 16px;
  font-weight: normal;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #409EFF;
  margin: 15px 0;
}

.mt-20 {
  margin-top: 20px;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
  padding: 10px;
}

.quick-actions .el-button {
  width: 100%;
}
</style> 