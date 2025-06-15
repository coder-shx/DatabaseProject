<template>
  <div class="admin-dashboard">
    <aside class="sidebar">
      <div class="sidebar-user">
        <div class="user-avatar">
          <i class="fas fa-user-shield"></i>
        </div>
        <span class="user-name">{{ user.name || user.username }}</span>
        <span class="user-role">{{ getRoleName(user.role) }}</span>
        <div class="user-dropdown-btn" @click="toggleUserMenu">
          <i class="fas fa-chevron-down"></i>
        </div>
        <div v-if="showUserMenu" class="user-dropdown">
          <a href="#" @click="logout">
            <i class="fas fa-sign-out-alt"></i> 登出
          </a>
        </div>
      </div>
      <nav class="nav-menu">
        <a href="#" @click="activeTab = 'overview'" :class="{ active: activeTab === 'overview' }">
          <i class="fas fa-home"></i> 概览
        </a>
        <a v-if="isManager || isSuperAdmin" href="#" @click="activeTab = 'orders'" :class="{ active: activeTab === 'orders' }">
          <i class="fas fa-clipboard-list"></i> 订单管理
        </a>
        <a v-if="isManager || isSuperAdmin" href="#" @click="activeTab = 'technicians'" :class="{ active: activeTab === 'technicians' }">
          <i class="fas fa-users-cog"></i> 技师管理
        </a>
        <a v-if="isSuperAdmin" href="#" @click="activeTab = 'statistics'" :class="{ active: activeTab === 'statistics' }">
          <i class="fas fa-chart-bar"></i> 统计分析
        </a>
        <a v-if="isSuperAdmin" href="#" @click="activeTab = 'users'" :class="{ active: activeTab === 'users' }">
          <i class="fas fa-users"></i> 用户管理
        </a>
      </nav>
    </aside>
    <main class="dashboard-main">
      <div class="dashboard-header">
        <h1 class="dashboard-title">管理后台</h1>
        <div class="dashboard-userinfo">
          <span class="dashboard-username"><i class="fas fa-user"></i> {{ user.name || user.username }}</span>
          <span class="dashboard-role">({{ getRoleName(user.role) }})</span>
        </div>
      </div>
      <div class="dashboard-content">
        <transition name="fade" mode="out-in">
          <div v-if="activeTab === 'overview'" key="overview" class="tab-content overview-tab">
            <div class="overview-cards">
              <div class="overview-card">
                <div class="overview-card-title">总订单数</div>
                <div class="overview-card-value">{{ dashboardStats.totalOrders }}</div>
              </div>
              <div class="overview-card">
                <div class="overview-card-title">已完成订单</div>
                <div class="overview-card-value">{{ dashboardStats.completedOrders }}</div>
              </div>
            </div>
            <button class="btn btn-outline refresh-btn" @click="refreshData" :disabled="isLoading">
              <i class="fas fa-sync-alt" :class="{ 'fa-spin': isLoading }"></i>
              {{ isLoading ? 'loading...' : '刷新数据' }}
            </button>
          </div>
          <!-- 其它tab内容... -->
          <div v-else-if="activeTab === 'orders'" key="orders" class="tab-content">
            <div class="section-header">
              <h2>订单管理</h2>
              <div class="order-filters">
                <select v-model="orderFilter" class="form-input">
                  <option value="">全部状态</option>
                  <option value="PENDING">待处理</option>
                  <option value="ASSIGNED">已分配</option>
                  <option value="IN_PROGRESS">进行中</option>
                  <option value="COMPLETED">已完成</option>
                  <option value="CANCELLED">已取消</option>
                  <option value="REJECTED">已拒绝</option>
                </select>

              </div>
            </div>

            <div class="orders-container">
              <div v-if="filteredOrders.length === 0" class="empty-state">
                <i class="fas fa-clipboard-list"></i>
                <h3>暂无订单</h3>
                <p>还没有维修订单</p>
              </div>
              <div v-for="order in filteredOrders" :key="order.id" class="order-card">
                <div class="order-header">
                  <div>
                    <h3>{{ order.orderNumber }}</h3>
                    <p>{{ order.description }}</p>
                  </div>
                  <span :class="['status-badge', order.status.toLowerCase()]">
                {{ getStatusText(order.status) }}
              </span>
                </div>
                <div class="order-body">
                  <div class="order-info">
                    <div class="info-item">
                      <i class="fas fa-user"></i>
                      <span>客户: {{ order.user?.name || '未知' }}</span>
                    </div>
                    <div class="info-item">
                      <i class="fas fa-car"></i>
                      <span>车辆: {{ getVehicleDisplay(order) }}</span>
                    </div>
                    <div class="info-item">
                      <i class="fas fa-calendar"></i>
                      <span>创建: {{ formatDate(order.createdAt) }}</span>
                    </div>
                    <div class="info-item">
                      <i class="fas fa-dollar-sign"></i>
                      <span>费用: ¥{{ order.totalCost || 0 }}</span>
                    </div>
                  </div>
                  <div v-if="order.technicians && order.technicians.length > 0" class="assigned-technicians">
                    <h4>分配技师:</h4>
                    <div class="technician-list">
                      <span v-for="tech in order.technicians" :key="tech.id" class="technician-tag">
                        {{ tech.name }} ({{ getSkillTypeName(tech.skillType) }})
                      </span>
                    </div>
                  </div>
                </div>
                <div class="order-footer">
                  <button class="btn btn-outline" @click="viewOrderDetail(order)">
                    <i class="fas fa-eye"></i> 查看详情
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- 技师管理页面 -->
          <div v-if="activeTab === 'technicians'" key="technicians" class="tab-content">
            <div class="section-header">
              <h2>技师管理</h2>
            </div>

            <div class="technicians-container">
              <div v-if="technicians.length === 0" class="empty-state">
                <i class="fas fa-users-cog"></i>
                <h3>暂无技师</h3>
              </div>
              <div v-for="technician in technicians" :key="technician.id" class="technician-card">
                <div class="tech-avatar">
                  <i class="fas fa-user-hard-hat"></i>
                </div>
                <div class="tech-info">
                  <h3>{{ technician.name }}</h3>
                  <p class="tech-id">员工ID: {{ technician.employeeId }}</p>
                  <div class="tech-details">
                    <div class="detail-item">
                      <span>工种：{{ getSkillTypeName(technician.skillType) }}</span>
                    </div>
                    <div class="detail-item">
                      <span>¥{{ technician.hourlyRate }}/小时</span>
                    </div>
                    <div class="detail-item">
                      <span>电话：{{ technician.phone }}</span>
                    </div>
                  </div>
                </div>
                <div class="tech-actions">
                  <button class="btn btn-danger btn-sm" @click="deleteTechnician(technician)">
                     删除
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- 统计分析页面 (仅超级管理员) -->
          <div v-if="activeTab === 'statistics' && isSuperAdmin" key="statistics" class="tab-content">
            <div class="section-header">
              <h2>统计分析</h2>
            </div>

            <div class="statistics-container">
              <!-- 详细数据表格 -->
              <div class="stat-section">
                <h3>技师工作量详细统计</h3>
                <div class="technician-stats">
                  <table>
                    <thead>
                    <tr>
                      <th>技师</th>
                      <th>技能类型</th>
                      <th>完成任务数</th>
                      <th>总收入</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr v-for="stat in statistics.technicianStats" :key="stat.technicianId">
                      <td>{{ stat.technicianName }}</td>
                      <td>{{ getSkillTypeName(stat.skillType) }}</td>
                      <td>{{ stat.completedTasks }}</td>
                      <td>¥{{ stat.totalEarnings }}</td>
                    </tr>
                    </tbody>
                  </table>
                </div>
              </div>

              <!-- 全部反馈统计 -->
              <div class="stat-section">
                <h3>用户反馈统计</h3>
                <div class="feedback-stats">
                  <div v-if="statistics.allFeedback && statistics.allFeedback.length > 0">
                    <table>
                      <thead>
                      <tr>
                        <th>订单号</th>
                        <th>评分</th>
                        <th>反馈内容</th>
                        <th>用户</th>
                        <th>日期</th>
                      </tr>
                      </thead>
                      <tbody>
                      <tr v-for="feedback in statistics.allFeedback.slice(0, 10)" :key="feedback.id">
                        <td>{{ feedback.repairOrder?.orderNumber || '未知' }}</td>
                        <td>
                          <div class="rating-stars">
                              <span v-for="star in 5" :key="star"
                                    :class="star <= feedback.rating ? 'star active' : 'star'">
                                ★
                              </span>
                            <span class="rating-text">({{ feedback.rating }}分)</span>
                          </div>
                        </td>
                        <td class="feedback-comment">{{ feedback.comment }}</td>
                        <td>{{ feedback.user?.name || '未知用户' }}</td>
                        <td>{{ formatDate(feedback.createdAt) }}</td>
                      </tr>
                      </tbody>
                    </table>
                    <div v-if="statistics.allFeedback.length > 10" class="feedback-pagination">
                      <p>显示前10条，共{{ statistics.allFeedback.length }}条反馈</p>
                    </div>
                  </div>
                  <div v-else class="no-feedback">
                    <i class="fas fa-comments"></i>
                    <p>暂无用户反馈</p>
                  </div>
                </div>
              </div>

              <!-- 车辆品牌维修统计详细数据 -->
              <div class="stat-section">
                <h3>车辆品牌维修数量排行</h3>
                <div class="brand-stats">
                  <div v-if="statistics.vehicleBrandStats && statistics.vehicleBrandStats.length > 0">
                    <table>
                      <thead>
                      <tr>
                        <th>排名</th>
                        <th>车辆品牌</th>
                        <th>维修次数</th>
                        <th>占比</th>
                      </tr>
                      </thead>
                      <tbody>
                      <tr v-for="(stat, index) in statistics.vehicleBrandStats" :key="stat.brand">
                        <td>{{ index + 1 }}</td>
                        <td>{{ stat.brand }}</td>
                        <td>{{ stat.repairCount }}</td>
                        <td>{{ ((stat.repairCount / allOrders.length) * 100).toFixed(1) }}%</td>
                      </tr>
                      </tbody>
                    </table>
                  </div>
                  <div v-else class="no-data">
                    <i class="fas fa-car"></i>
                    <p>暂无车辆品牌统计数据</p>
                  </div>
                </div>
              </div>

              <!-- 维修工种类型统计详细数据 -->
              <div class="stat-section">
                <h3>维修工种类型需求统计</h3>
                <div class="skill-type-stats">
                  <div v-if="statistics.skillTypeStats && statistics.skillTypeStats.length > 0">
                    <table>
                      <thead>
                      <tr>
                        <th>排名</th>
                        <th>工种类型</th>
                        <th>订单数量</th>
                        <th>占比</th>
                      </tr>
                      </thead>
                      <tbody>
                      <tr v-for="(stat, index) in statistics.skillTypeStats" :key="stat.skillType">
                        <td>{{ index + 1 }}</td>
                        <td>{{ stat.skillType }}</td>
                        <td>{{ stat.orderCount }}</td>
                        <td>{{ ((stat.orderCount / allOrders.length) * 100).toFixed(1) }}%</td>
                      </tr>
                      </tbody>
                    </table>
                  </div>
                  <div v-else class="no-data">
                    <i class="fas fa-tools"></i>
                    <p>暂无工种类型统计数据</p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 用户管理页面 (仅超级管理员) -->
          <div v-if="activeTab === 'users' && isSuperAdmin" key="users" class="tab-content">
            <div class="section-header">
              <h2>用户管理</h2>
            </div>

            <div class="users-container">
              <table class="users-table">
                <thead>
                <tr>
                  <th>ID</th>
                  <th>用户名</th>
                  <th>姓名</th>
                  <th>电话</th>
                  <th>邮箱</th>
                  <th>车辆数</th>
                  <th>订单数</th>
                  <th>操作</th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="user in filteredUsers" :key="user.id">
                  <td>{{ user.id }}</td>
                  <td>{{ user.username }}</td>
                  <td>{{ user.name }}</td>
                  <td>{{ user.phone }}</td>
                  <td>{{ user.email }}</td>
                  <td>{{ user.vehicleCount || 0 }}</td>
                  <td>{{ user.orderCount || 0 }}</td>
                  <td>
                    <button class="btn btn-outline btn-sm" @click="viewUserDetail(user)">
                      查看
                    </button>
                  </td>
                </tr>
                </tbody>
              </table>
            </div>
          </div>
        </transition>
      </div>
      <!-- 订单详情模态框 -->
      <div v-if="showOrderDetailModal && selectedOrderDetail" class="modal-overlay" @click="closeOrderDetailModal">
        <div class="modal-content" @click.stop>
          <div class="modal-header">
            <h2>订单详情: {{ selectedOrderDetail.orderNumber }}</h2>
            <button class="modal-close" @click="closeOrderDetailModal">
              <i class="fas fa-times"></i>
            </button>
          </div>
          <div class="modal-body">
            <div class="order-detail-sections">
              <div class="detail-section">
                <h3>基本信息</h3>
                <div class="detail-grid">
                  <div class="detail-item">
                    <label>订单号:</label>
                    <span>{{ selectedOrderDetail.orderNumber }}</span>
                  </div>
                  <div class="detail-item">
                    <label>状态:</label>
                    <span :class="['status-badge', selectedOrderDetail.status.toLowerCase()]">
                      {{ getStatusText(selectedOrderDetail.status) }}
                    </span>
                  </div>
                  <div class="detail-item">
                    <label>描述:</label>
                    <span>{{ selectedOrderDetail.description }}</span>
                  </div>
                  <div class="detail-item">
                    <label>创建时间:</label>
                    <span>{{ formatDate(selectedOrderDetail.createdAt) }}</span>
                  </div>
                  <div class="detail-item">
                    <label>更新时间:</label>
                    <span>{{ formatDate(selectedOrderDetail.updatedAt) }}</span>
                  </div>
                  <div v-if="selectedOrderDetail.completedAt" class="detail-item">
                    <label>完成时间:</label>
                    <span>{{ formatDate(selectedOrderDetail.completedAt) }}</span>
                  </div>
                </div>
              </div>

              <div class="detail-section">
                <h3>客户信息</h3>
                <div class="detail-grid">
                  <div class="detail-item">
                    <label>客户姓名:</label>
                    <span>{{ selectedOrderDetail.user?.name || '未知' }}</span>
                  </div>
                  <div class="detail-item">
                    <label>联系电话:</label>
                    <span>{{ selectedOrderDetail.user?.phone || '未知' }}</span>
                  </div>
                  <div class="detail-item">
                    <label>邮箱:</label>
                    <span>{{ selectedOrderDetail.user?.email || '未知' }}</span>
                  </div>
                </div>
              </div>

              <div class="detail-section">
                <h3>车辆信息</h3>
                <div class="detail-grid">
                  <div class="detail-item">
                    <label>车牌号:</label>
                    <span>{{ selectedOrderDetail.vehicle?.licensePlate || '未知' }}</span>
                  </div>
                  <div class="detail-item">
                    <label>品牌:</label>
                    <span>{{ selectedOrderDetail.vehicle?.brand || '未知' }}</span>
                  </div>
                  <div class="detail-item">
                    <label>型号:</label>
                    <span>{{ selectedOrderDetail.vehicle?.model || '未知' }}</span>
                  </div>
                  <div class="detail-item">
                    <label>年份:</label>
                    <span>{{ selectedOrderDetail.vehicle?.year || '未知' }}</span>
                  </div>
                </div>
              </div>

              <div v-if="selectedOrderDetail.technicians && selectedOrderDetail.technicians.length > 0" class="detail-section">
                <h3>分配技师</h3>
                <div class="technician-list">
                  <div v-for="tech in selectedOrderDetail.technicians" :key="tech.id" class="technician-item">
                    <div class="tech-info">
                      <span class="tech-name">{{ tech.name }}</span>
                      <span class="tech-skill">{{ getSkillTypeName(tech.skillType) }}</span>
                      <span class="tech-rate">¥{{ tech.hourlyRate }}/小时</span>
                    </div>
                  </div>
                </div>
              </div>

              <div class="detail-section">
                <h3>费用信息</h3>
                <div class="detail-grid">
                  <div class="detail-item">
                    <label>工时费:</label>
                    <span>¥{{ selectedOrderDetail.laborCost || 0 }}</span>
                  </div>
                  <div class="detail-item">
                    <label>材料费:</label>
                    <span>¥{{ selectedOrderDetail.materialCost || 0 }}</span>
                  </div>
                  <div class="detail-item">
                    <label>总费用:</label>
                    <span class="total-cost">¥{{ selectedOrderDetail.totalCost || 0 }}</span>
                  </div>
                </div>
              </div>


            </div>
          </div>
          <div class="modal-footer">
            <button @click="closeOrderDetailModal" class="btn btn-outline">
              <i class="fas fa-times"></i> 关闭
            </button>
          </div>
        </div>
      </div>

      <!-- 添加技师模态框 -->
    </main>

    <!-- 订单详情模态框 -->
    <div v-if="showOrderDetailModal && selectedOrderDetail" class="modal-overlay" @click="closeOrderDetailModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>订单详情: {{ selectedOrderDetail.orderNumber }}</h2>
          <button class="modal-close" @click="closeOrderDetailModal">
            <i class="fas fa-times"></i>
          </button>
        </div>
        <div class="modal-body">
          <div class="order-detail-sections">
            <div class="detail-section">
              <h3>基本信息</h3>
              <div class="detail-grid">
                <div class="detail-item">
                  <label>订单号:</label>
                  <span>{{ selectedOrderDetail.orderNumber }}</span>
                </div>
                <div class="detail-item">
                  <label>状态:</label>
                  <span :class="['status-badge', selectedOrderDetail.status.toLowerCase()]">
                    {{ getStatusText(selectedOrderDetail.status) }}
                  </span>
                </div>
                <div class="detail-item">
                  <label>描述:</label>
                  <span>{{ selectedOrderDetail.description }}</span>
                </div>
                <div class="detail-item">
                  <label>创建时间:</label>
                  <span>{{ formatDate(selectedOrderDetail.createdAt) }}</span>
                </div>
                <div class="detail-item">
                  <label>更新时间:</label>
                  <span>{{ formatDate(selectedOrderDetail.updatedAt) }}</span>
                </div>
                <div v-if="selectedOrderDetail.completedAt" class="detail-item">
                  <label>完成时间:</label>
                  <span>{{ formatDate(selectedOrderDetail.completedAt) }}</span>
                </div>
              </div>
            </div>

            <div class="detail-section">
              <h3>客户信息</h3>
              <div class="detail-grid">
                <div class="detail-item">
                  <label>客户姓名:</label>
                  <span>{{ selectedOrderDetail.user?.name || '未知' }}</span>
                </div>
                <div class="detail-item">
                  <label>联系电话:</label>
                  <span>{{ selectedOrderDetail.user?.phone || '未知' }}</span>
                </div>
                <div class="detail-item">
                  <label>邮箱:</label>
                  <span>{{ selectedOrderDetail.user?.email || '未知' }}</span>
                </div>
              </div>
            </div>

            <div class="detail-section">
              <h3>车辆信息</h3>
              <div class="detail-grid">
                <div class="detail-item">
                  <label>车牌号:</label>
                  <span>{{ selectedOrderDetail.vehicle?.licensePlate || '未知' }}</span>
                </div>
                <div class="detail-item">
                  <label>品牌:</label>
                  <span>{{ selectedOrderDetail.vehicle?.brand || '未知' }}</span>
                </div>
                <div class="detail-item">
                  <label>型号:</label>
                  <span>{{ selectedOrderDetail.vehicle?.model || '未知' }}</span>
                </div>
                <div class="detail-item">
                  <label>年份:</label>
                  <span>{{ selectedOrderDetail.vehicle?.year || '未知' }}</span>
                </div>
              </div>
            </div>

            <div v-if="selectedOrderDetail.technicians && selectedOrderDetail.technicians.length > 0" class="detail-section">
              <h3>分配技师</h3>
              <div class="technician-list">
                <div v-for="tech in selectedOrderDetail.technicians" :key="tech.id" class="technician-item">
                  <div class="tech-info">
                    <span class="tech-name">{{ tech.name }}</span>
                    <span class="tech-skill">{{ getSkillTypeName(tech.skillType) }}</span>
                    <span class="tech-rate">¥{{ tech.hourlyRate }}/小时</span>
                  </div>
                </div>
              </div>
            </div>

            <div class="detail-section">
              <h3>费用信息</h3>
              <div class="detail-grid">
                <div class="detail-item">
                  <label>工时费:</label>
                  <span>¥{{ selectedOrderDetail.laborCost || 0 }}</span>
                </div>
                <div class="detail-item">
                  <label>材料费:</label>
                  <span>¥{{ selectedOrderDetail.materialCost || 0 }}</span>
                </div>
                <div class="detail-item">
                  <label>总费用:</label>
                  <span class="total-cost">¥{{ selectedOrderDetail.totalCost || 0 }}</span>
                </div>
              </div>
            </div>


          </div>
        </div>
        <div class="modal-footer">
          <button @click="closeOrderDetailModal" class="btn btn-outline">
            <i class="fas fa-times"></i> 关闭
          </button>
        </div>
      </div>
    </div>

    <!-- 添加技师模态框 -->
  </div>
</template>

<script>
import Chart from '@/components/Chart.vue';

export default {
  name: 'AdminDashboard',
  components: {
    Chart
  },
  data() {
    return {
      user: {},
      activeTab: 'overview',
      showUserMenu: false,
      showOrderDetailModal: false,
      showCreateTechnician: false,
      showEditTechnician: false,
      showAssignModal: false,
      selectedOrder: null,
      selectedOrderDetail: null,
      editingTechnician: null,
      orderFilter: '',
      orderSortBy: 'createdAt',
      orderSortDesc: true,
      technicianForm: {
        name: '',
        employeeId: '',
        username: '',
        password: '',
        phone: '',
        email: '',
        skillType: '',
        hourlyRate: ''
      },
      assignForm: {
        technicianIds: [],
        laborCost: 0,
        materialCost: 0
      },
      statistics: {
        totalRevenue: 0,
        totalOrders: 0,
        activeTechnicians: 0,
        avgOrderValue: 0,
        monthlyGrowth: 0,
        monthlyRevenue: [],
        vehicleBrandStats: [],
        skillTypeStats: [],
        allFeedback: []
      },
      allOrders: [],
      technicians: [],
      users: [],
      dashboardStats: {
        totalOrders: 0,
        pendingOrders: 0,
        completedOrders: 0,
        activeTechnicians: 0
      },
      statisticsDateRange: {
        start: '',
        end: ''
      }
    }
  },
  computed: {
    isManager() {
      return this.user.role === 'MANAGER';
    },
    isSuperAdmin() {
      return this.user.role === 'SUPER_ADMIN';
    },
    filteredOrders() {
      if (!this.orderFilter) return this.allOrders;
      return this.allOrders.filter(order => order.status === this.orderFilter);
    },
    filteredUsers() {
      if (!this.userSearchTerm) return this.users;
      return this.users.filter(user =>
          user.name.toLowerCase().includes(this.userSearchTerm.toLowerCase()) ||
          user.username.toLowerCase().includes(this.userSearchTerm.toLowerCase())
      );
    },
    recentOrders() {
      return this.allOrders.slice(0, 10);
    },
    availableTechnicians() {
      return this.technicians.filter(tech => tech.status !== 'INACTIVE');
    },
    totalCost() {
      // 修复浮点数计算精度问题
      const laborCost = parseFloat(this.assignForm.laborCost) || 0;
      const materialCost = parseFloat(this.assignForm.materialCost) || 0;
      return Math.round((laborCost + materialCost) * 100) / 100; // 保留两位小数
    },
    // 图表数据计算属性
    revenueChartData() {
      const laborCost = this.statistics.laborCost || 0;
      const materialCost = this.statistics.materialCost || 0;

      if (laborCost === 0 && materialCost === 0) {
        return [{ label: '暂无数据', value: 1 }];
      }

      return [
        { label: '工时费', value: laborCost },
        { label: '材料费', value: materialCost }
      ];
    },
    technicianTasksChartData() {
      if (!this.statistics.technicianStats || this.statistics.technicianStats.length === 0) {
        return [{ label: '暂无数据', value: 0 }];
      }

      return this.statistics.technicianStats.map(tech => ({
        label: tech.technicianName,
        value: tech.completedTasks
      }));
    },
    technicianEarningsChartData() {
      if (!this.statistics.technicianStats || this.statistics.technicianStats.length === 0) {
        return [{ label: '暂无数据', value: 0 }];
      }

      return this.statistics.technicianStats.map(tech => ({
        label: tech.technicianName,
        value: tech.totalEarnings
      }));
    },
    orderStatusChartData() {
      const statusCounts = {
        PENDING: 0,
        ASSIGNED: 0,
        IN_PROGRESS: 0,
        COMPLETED: 0,
        CANCELLED: 0
      };

      this.allOrders.forEach(order => {
        if (statusCounts.hasOwnProperty(order.status)) {
          statusCounts[order.status]++;
        }
      });

      const data = Object.entries(statusCounts)
          .filter(([status, count]) => count > 0)
          .map(([status, count]) => ({
            label: this.getStatusText(status),
            value: count
          }));

      return data.length > 0 ? data : [{ label: '暂无数据', value: 1 }];
    },
    // 车辆品牌维修统计图表数据
    vehicleBrandChartData() {
      if (!this.statistics.vehicleBrandStats || this.statistics.vehicleBrandStats.length === 0) {
        return [{ label: '暂无数据', value: 0 }];
      }

      return this.statistics.vehicleBrandStats.map(stat => ({
        label: stat.brand,
        value: stat.repairCount
      }));
    },
    // 维修工种类型统计图表数据
    skillTypeChartData() {
      if (!this.statistics.skillTypeStats || this.statistics.skillTypeStats.length === 0) {
        return [{ label: '暂无数据', value: 0 }];
      }

      return this.statistics.skillTypeStats.map(stat => ({
        label: stat.skillType,
        value: stat.orderCount
      }));
    }
  },
  created() {
    this.loadUserInfo();
    this.loadData();
    // 定时自动刷新数据
    this._refreshTimer = setInterval(() => {
      this.loadData();
    }, 30000); // 30秒刷新一次
  },
  beforeDestroy() {
    if (this._refreshTimer) {
      clearInterval(this._refreshTimer);
    }
  },
  methods: {
    loadUserInfo() {
      const userData = localStorage.getItem('user');
      if (userData) {
        this.user = JSON.parse(userData);
        console.log('加载的管理员数据:', this.user);

        if (!this.user.id) {
          console.error('管理员数据中缺少ID字段:', this.user);
          this.$emit('message', '用户数据错误，请重新登录', 'error');
          this.logout();
        }
      } else {
        console.error('localStorage中没有管理员数据');
        this.$emit('message', '未找到用户信息，请重新登录', 'error');
        this.logout();
      }
    },
    async loadData() {
      try {
        this.isLoading = true;

        // 先加载基础数据
        await this.loadOrders();
        await this.loadTechnicians();

        if (this.isSuperAdmin) {
          await this.loadUsers();
        }

        // 然后基于基础数据计算统计信息
        await this.loadDashboardStats();

        // 如果是超级管理员，初始化统计分析数据
        if (this.isSuperAdmin) {
          this.calculateLocalStatistics();
        }
      } catch (error) {
        console.error('加载数据失败:', error);
        this.$emit('message', '加载数据失败', 'error');
      } finally {
        this.isLoading = false;
      }
    },

    async refreshData() {
      this.$emit('message', '正在刷新数据...', 'info');
      this.isLoading = true;
      try {
        await this.loadData();

        // 重新加载订单数据
        await this.loadOrders();

        // 强制重新计算统计数据
        await this.loadDashboardStats();
        if (this.isSuperAdmin) {
          await this.loadDetailedStatistics();
        }
        this.$emit('message', '数据刷新完成', 'success');
      } catch (error) {
        console.error('刷新数据失败:', error);
        this.$emit('message', '刷新数据失败: ' + error.message, 'error');
      } finally {
        this.isLoading = false;
      }
    },
    async loadOrders() {
      try {
        const response = await this.$axios.get('/repair-orders');
        this.allOrders = Array.isArray(response.data) ? response.data : [];
        console.log('从API加载的订单数据:', this.allOrders);
        console.log('订单数量:', this.allOrders.length);
      } catch (error) {
        console.error('加载订单失败:', error);
        console.error('错误详情:', error.response?.data || error.message);
        this.allOrders = [];
        this.$emit('message', '加载订单数据失败: ' + (error.response?.data?.message || error.message), 'error');
      }
    },
    async loadTechnicians() {
      try {
        const response = await this.$axios.get('/technicians');
        this.technicians = Array.isArray(response.data) ? response.data : [];
        console.log('从API加载的技师数据:', this.technicians);
        console.log('技师数量:', this.technicians.length);
      } catch (error) {
        console.error('加载技师失败:', error);
        console.error('错误详情:', error.response?.data || error.message);
        this.technicians = [];
        this.$emit('message', '加载技师数据失败: ' + (error.response?.data?.message || error.message), 'error');
      }
    },
    async loadUsers() {
      try {
        const response = await this.$axios.get('/users/with-stats');
        this.users = Array.isArray(response.data) ? response.data : [];
        console.log('加载的用户数据（包含统计信息）:', this.users);
      } catch (error) {
        console.error('加载用户失败:', error);
        console.error('错误详情:', error.response?.data);
        this.users = [];
        this.$emit('message', `加载用户数据失败: ${error.response?.data?.message || error.message}`, 'error');
      }
    },
    async loadDashboardStats() {
      try {
        const response = await this.$axios.get('/admins/dashboard-stats');
        this.dashboardStats = response.data;
        console.log('加载统计数据成功:', this.dashboardStats);
      } catch (error) {
        console.error('加载统计数据失败，使用本地计算:', error);
        // 基于本地数据计算统计信息
        const orders = Array.isArray(this.allOrders) ? this.allOrders : [];
        const techs = Array.isArray(this.technicians) ? this.technicians : [];

        this.dashboardStats = {
          totalOrders: orders.length,
          pendingOrders: orders.filter(o => o.status === 'PENDING').length,
          completedOrders: orders.filter(o => o.status === 'COMPLETED').length,
          activeTechnicians: techs.length
        };
      }
    },
    async loadDetailedStatistics() {
      if (!this.isSuperAdmin) return;

      try {
        const params = {};
        if (this.statisticsDateRange.start) {
          params.startDate = this.statisticsDateRange.start;
        }
        if (this.statisticsDateRange.end) {
          params.endDate = this.statisticsDateRange.end;
        }

        const response = await this.$axios.get('/admins/detailed-statistics', { params });
        this.statistics = response.data;
        console.log('加载详细统计数据成功:', this.statistics);
      } catch (error) {
        console.error('加载详细统计数据失败，使用本地计算:', error);
        this.calculateLocalStatistics();
      }
    },

    async loadStatistics() {
      if (!this.isSuperAdmin) return;

      try {
        // 尝试从API加载各种统计数据
        const [revenueStats, techStats, feedbackStats] = await Promise.all([
          this.$axios.get('/repair-orders/analysis/monthly', {
            params: { year: 2024, month: 12 }
          }).catch(() => null),
          this.$axios.get('/repair-orders/task-statistics', {
            params: {
              startDate: this.statisticsDateRange.start,
              endDate: this.statisticsDateRange.end
            }
          }).catch(() => null),
          this.$axios.get('/feedbacks').catch(() => null)
        ]);

        // 如果API调用失败，基于本地数据计算统计信息
        if (!revenueStats || !techStats || !feedbackStats) {
          this.calculateLocalStatistics();
        } else {
          // 获取新的统计数据
          await this.loadVehicleBrandStatistics();
          await this.loadSkillTypeStatistics();

          this.statistics = {
            totalRevenue: revenueStats.data?.total_cost || 0,
            laborCost: revenueStats.data?.total_labor_cost || 0,
            materialCost: revenueStats.data?.total_material_cost || 0,
            technicianStats: techStats.data || [],
            allFeedback: feedbackStats.data || [],
            vehicleBrandStats: this.statistics.vehicleBrandStats || [],
            skillTypeStats: this.statistics.skillTypeStats || [],
            monthlyRevenue: this.statistics.monthlyRevenue || []
          };
        }
      } catch (error) {
        console.error('加载统计数据失败，使用本地计算:', error);
        this.calculateLocalStatistics();
      }
    },

    async calculateLocalStatistics() {
      // 确保数据已初始化
      if (!Array.isArray(this.allOrders)) {
        this.allOrders = [];
      }
      if (!Array.isArray(this.technicians)) {
        this.technicians = [];
      }

      // 基于本地订单数据计算统计信息
      const completedOrders = this.allOrders.filter(o => o.status === 'COMPLETED');

      // 计算收入统计
      const totalRevenue = completedOrders.reduce((sum, order) => sum + (order.totalCost || 0), 0);
      const laborCost = completedOrders.reduce((sum, order) => sum + (order.laborCost || 0), 0);
      const materialCost = completedOrders.reduce((sum, order) => sum + (order.materialCost || 0), 0);

      // 计算技师统计
      const technicianStats = this.technicians.map(tech => {
        const techOrders = completedOrders.filter(order =>
            order.technicians && order.technicians.some(t => t.id === tech.id)
        );

        return {
          technicianId: tech.id,
          technicianName: tech.name,
          skillType: tech.skillType,
          completedTasks: techOrders.length,
          totalEarnings: techOrders.reduce((sum, order) => sum + (order.laborCost || 0), 0)
        };
      });

      // 获取真实的反馈数据
      await this.loadAllFeedback();

      // 加载新的统计数据
      await this.loadVehicleBrandStatistics();
      await this.loadSkillTypeStatistics();

      this.statistics = {
        totalRevenue,
        laborCost,
        materialCost,
        technicianStats,
        allFeedback: this.statistics.allFeedback || [],
        vehicleBrandStats: this.statistics.vehicleBrandStats || [],
        skillTypeStats: this.statistics.skillTypeStats || [],
        monthlyRevenue: this.statistics.monthlyRevenue || []
      };
    },

    async loadAllFeedback() {
      try {
        const response = await this.$axios.get('/feedbacks');
        this.statistics.allFeedback = response.data;
      } catch (error) {
        console.error('加载反馈失败:', error);
        this.statistics.allFeedback = [];
      }
    },
    async viewOrderDetail(order) {
      try {
        // 使用新的详细查询端点
        const response = await this.$axios.get(`/repair-orders/${order.id}/details`);
        this.selectedOrderDetail = response.data;
        console.log('订单详情:', this.selectedOrderDetail);
        this.showOrderDetailModal = true;
      } catch (error) {
        console.error('获取订单详情失败:', error);
        // 如果详细端点不可用，回退到基本查询
        try {
          const fallbackResponse = await this.$axios.get(`/repair-orders/${order.id}`);
          this.selectedOrderDetail = fallbackResponse.data;
          this.showOrderDetailModal = true;
        } catch (fallbackError) {
          console.error('获取订单详情失败（备用方案也失败）:', fallbackError);
          this.$emit('message', '获取订单详情失败', 'error');
        }
      }
    },
    closeOrderDetailModal() {
      this.showOrderDetailModal = false;
      this.selectedOrderDetail = null;
    },
    editOrder(order) {
      // 实现订单编辑
      this.$emit('message', '订单编辑功能开发中', 'info');
    },
    async rejectOrder(order) {
      try {
        if (!confirm(`确定要拒绝订单 ${order.orderNumber} 吗？拒绝后系统将尝试重新分配给其他技师。`)) {
          return;
        }
        
        console.log('管理员拒绝订单:', order.id);
        await this.$axios.put(`/api/repair-orders/${order.id}/reject`);
        
        // 重新加载订单数据
        await this.loadOrders();
        await this.loadDashboardStats();
        
        this.$emit('message', '订单已成功拒绝并重新分配', 'success');
      } catch (error) {
        console.error('拒绝订单失败:', error);
        this.$emit('message', '拒绝订单失败: ' + (error.response?.data?.message || error.message), 'error');
      }
    },
    editTechnician(technician) {
      // 实现技师编辑
      this.$emit('message', '技师编辑功能开发中', 'info');
    },
    async deleteTechnician(technician) {
      if (confirm(`确定要删除技师 ${technician.name} 吗？此操作不可撤销。`)) {
        try {
          await this.$axios.delete(`/technicians/${technician.id}`);
          this.$emit('message', '技师删除成功', 'success');
          // 重新加载技师列表
          await this.loadTechnicians();
          // 重新加载统计数据
          await this.loadDashboardStats();
        } catch (error) {
          console.error('删除技师失败:', error);
          this.$emit('message', '删除技师失败: ' + (error.response?.data?.message || error.message), 'error');
        }
      }
    },
    viewUserDetail(user) {
      // 实现用户详情查看
      this.$emit('message', '用户详情功能开发中', 'info');
    },
    toggleUserMenu() {
      this.showUserMenu = !this.showUserMenu;
    },
    getRoleName(role) {
      const roleMap = {
        'MANAGER': '经理',
        'SUPER_ADMIN': '超级管理员'
      };
      return roleMap[role] || role;
    },
    getStatusText(status) {
      const statusMap = {
        'PENDING': '待处理',
        'ASSIGNED': '已分配',
        'IN_PROGRESS': '进行中',
        'COMPLETED': '已完成',
        'CANCELLED': '已取消',
        'REJECTED': '已拒绝'
      };
      return statusMap[status] || status;
    },
    getSkillTypeName(skillType) {
      const skillMap = {
        'MECHANIC': '机械维修',
        'ELECTRICIAN': '电气维修',
        'BODY_WORK': '车身维修',
        'PAINT': '喷漆',
        'DIAGNOSTIC': '诊断'
      };
      return skillMap[skillType] || skillType;
    },
    getVehicleDisplay(order) {
      if (order.vehicle) {
        const { licensePlate, brand, model, year } = order.vehicle;
        if (licensePlate && brand && model) {
          return `${licensePlate} - ${brand} ${model}${year ? ` (${year})` : ''}`;
        } else if (licensePlate) {
          return licensePlate;
        }
      }
      return '未知车辆';
    },
    formatDate(dateString) {
      return new Date(dateString).toLocaleDateString('zh-CN');
    },
    logout() {
      localStorage.removeItem('user');
      localStorage.removeItem('userRole');
      this.$router.push('/');
    },
    async reassignPendingOrders() {
      try {
        const response = await this.$axios.post('/admins/reassign-pending-orders');

        if (response.data.reassignedCount > 0) {
          this.$emit('message',
              `成功重新分配 ${response.data.reassignedCount} 个订单`,
              'success'
          );
          console.log('重新分配的订单:', response.data.reassignedOrders);
        } else {
          console.log('没有需要重新分配的订单');
        }
      } catch (error) {
        console.error('重新分配订单失败:', error);
        // 重新分配失败不影响整体刷新流程，只记录错误
        console.warn('重新分配订单失败，但数据刷新将继续');
      }
    },
    async manualReassignPendingOrders() {
      try {
        this.isLoading = true;
        this.$emit('message', '正在重新分配待分配订单...', 'info');

        const response = await this.$axios.post('/admins/reassign-pending-orders');

        if (response.data.reassignedCount > 0) {
          this.$emit('message',
              `成功重新分配 ${response.data.reassignedCount} 个订单`,
              'success'
          );
          console.log('重新分配的订单:', response.data.reassignedOrders);

          // 重新加载订单数据以反映最新状态
          await this.loadOrders();
          await this.loadDashboardStats();
        } else {
          this.$emit('message', '当前没有需要重新分配的订单', 'info');
        }
      } catch (error) {
        console.error('手动重新分配订单失败:', error);
        this.$emit('message', '重新分配失败: ' + (error.response?.data?.error || error.message), 'error');
      } finally {
        this.isLoading = false;
      }
    },
    async loadVehicleBrandStatistics() {
      try {
        const response = await this.$axios.get('/admins/vehicle-brand-statistics');
        this.statistics.vehicleBrandStats = response.data;
        console.log('加载车辆品牌统计数据成功:', this.statistics.vehicleBrandStats);
      } catch (error) {
        console.error('加载车辆品牌统计失败:', error);
        this.statistics.vehicleBrandStats = [];
      }
    },
    async loadSkillTypeStatistics() {
      try {
        const response = await this.$axios.get('/admins/skill-type-statistics');
        this.statistics.skillTypeStats = response.data;
        console.log('加载工种类型统计数据成功:', this.statistics.skillTypeStats);
      } catch (error) {
        console.error('加载工种类型统计失败:', error);
        this.statistics.skillTypeStats = [];
      }
    }
  }
}
</script>

<style scoped>
.admin-dashboard {
  display: flex;
  min-height: 100vh;
  background: #f4f7fa;
}

.sidebar {
  width: 220px;
  background: linear-gradient(180deg, #1e293b 80%, #2563eb 100%);
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: stretch;
  padding: 0;
  box-shadow: 2px 0 8px rgba(30,41,59,0.08);
}

.sidebar-user {
  margin-top: auto;
  padding: 1.5rem 1rem 1rem 1rem;
  border-top: 1px solid rgba(255,255,255,0.1);
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 0.5rem;
}

.user-avatar {
  font-size: 2rem;
  margin-bottom: 0.5rem;
}

.user-name {
  font-weight: bold;
}

.user-role {
  font-size: 0.9rem;
  opacity: 0.8;
}

.user-dropdown-btn {
  margin-top: 0.5rem;
  cursor: pointer;
}

.user-dropdown {
  background: #fff;
  color: #222;
  border-radius: 0.5rem;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  margin-top: 0.5rem;
  padding: 0.5rem 0;
  width: 100%;
}

.user-dropdown a {
  display: block;
  padding: 0.5rem 1rem;
  color: #222;
  text-decoration: none;
  border-radius: 0.5rem;
}

.user-dropdown a:hover {
  background: #f1f5f9;
}

.nav-menu {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-top: 1rem;
}

.nav-menu a {
  color: #fff;
  padding: 0.75rem 1.5rem;
  border-radius: 0.5rem 0 0 0.5rem;
  text-decoration: none;
  font-size: 1.1rem;
  transition: background 0.2s;
}

.nav-menu a.active, .nav-menu a:hover {
  background: #2563eb;
  color: #fff;
}

.dashboard-main {
  flex: 1;
  padding: 2rem 2.5rem;
  background: #f8fafc;
  min-height: 100vh;
}

.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid #e5e7eb;
  margin-bottom: 2rem;
}
.dashboard-title {
  font-size: 2rem;
  font-weight: 700;
  color: #2563eb;
  letter-spacing: 2px;
}
.dashboard-userinfo {
  display: flex;
  align-items: center;
  gap: 1rem;
  font-size: 1.1rem;
  color: #374151;
}
.dashboard-username {
  font-weight: 600;
}
.dashboard-role {
  color: #6b7280;
}
.dashboard-content {
  margin-top: 1rem;
}
.overview-cards {
  display: flex;
  gap: 2rem;
  margin-bottom: 2rem;
}
.overview-card {
  flex: 1;
  background: linear-gradient(135deg, #e0e7ff 60%, #2563eb 100%);
  color: #1e293b;
  border-radius: 1rem;
  box-shadow: 0 2px 8px rgba(30,41,59,0.08);
  padding: 2rem 1.5rem;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  min-width: 180px;
  transition: box-shadow 0.2s, transform 0.2s;
}
.overview-card:hover {
  box-shadow: 0 8px 24px rgba(37,99,235,0.12);
  transform: translateY(-2px) scale(1.03);
}
.overview-card-title {
  font-size: 1.1rem;
  color: #2563eb;
  margin-bottom: 0.5rem;
  font-weight: 500;
}
.overview-card-value {
  font-size: 2.2rem;
  font-weight: 700;
  color: #1e293b;
}
.refresh-btn {
  margin-top: 1rem;
  padding: 0.5rem 1.5rem;
  font-size: 1rem;
}
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s;
}
.fade-enter, .fade-leave-to {
  opacity: 0;
}

.orders-container,
.technicians-container {
  display: grid;
  gap: 1.5rem;
}

.order-card {
  background: white;
  padding: 1.5rem;
  border-radius: 1rem;
  box-shadow: 0 4px 6px rgba(0,0,0,0.05);
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.order-header h3 {
  margin: 0;
  color: #1f2937;
}

.order-header p {
  margin: 0.25rem 0 0 0;
  color: #6b7280;
  font-size: 0.875rem;
}

.status-badge {
  padding: 0.5rem 1rem;
  border-radius: 9999px;
  font-size: 0.875rem;
  font-weight: 500;
}

.status-badge.pending {
  background: #fef3c7;
  color: #d97706;
}

.status-badge.assigned {
  background: #dbeafe;
  color: #2563eb;
}

.status-badge.in_progress {
  background: #e0e7ff;
  color: #6366f1;
}

.status-badge.completed {
  background: #d1fae5;
  color: #059669;
}

.order-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  margin-bottom: 1rem;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
  color: #6b7280;
}

.info-item i {
  color: #3b82f6;
  width: 16px;
}

.assigned-technicians {
  margin-bottom: 1rem;
}

.assigned-technicians h4 {
  margin: 0 0 0.5rem 0;
  color: #374151;
  font-size: 0.875rem;
}

.technician-list {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.technician-tag {
  background: #f3f4f6;
  padding: 0.25rem 0.75rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  color: #374151;
}

.order-footer {
  display: flex;
  gap: 1rem;
}

.technician-card {
  background: white;
  padding: 1.5rem;
  border-radius: 1rem;
  box-shadow: 0 4px 6px rgba(0,0,0,0.05);
  display: flex;
  gap: 1rem;
  align-items: center;
}

.tech-avatar {
  width: 4rem;
  height: 4rem;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  border-radius: 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.5rem;
}

.tech-info {
  flex: 1;
}

.tech-info h3 {
  margin: 0 0 0.5rem 0;
  color: #1f2937;
}

.tech-id {
  color: #6b7280;
  font-size: 0.875rem;
  margin-bottom: 1rem;
}

.tech-details {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0.5rem;
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
  color: #374151;
}

.detail-item i {
  color: #3b82f6;
  width: 1rem;
}

.tech-actions {
  display: flex;
  gap: 0.5rem;
}

.statistics-container {
  display: grid;
  gap: 2rem;
}

.charts-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 2rem;
  margin-bottom: 2rem;
}

.chart-card {
  background: white;
  border-radius: 1rem;
  box-shadow: 0 4px 6px rgba(0,0,0,0.05);
  overflow: hidden;
}

.no-feedback {
  text-align: center;
  padding: 2rem;
  color: #6b7280;
}

.no-feedback i {
  font-size: 2rem;
  margin-bottom: 1rem;
  opacity: 0.5;
}

.brand-stats table,
.skill-type-stats table {
  width: 100%;
  border-collapse: collapse;
}

.brand-stats th,
.brand-stats td,
.skill-type-stats th,
.skill-type-stats td {
  padding: 0.75rem;
  text-align: left;
  border-bottom: 1px solid #e5e7eb;
}

.brand-stats th,
.skill-type-stats th {
  background: #f9fafb;
  font-weight: 600;
  color: #374151;
}

.no-data {
  text-align: center;
  padding: 2rem;
  color: #6b7280;
}

.no-data i {
  font-size: 2rem;
  margin-bottom: 1rem;
  opacity: 0.5;
}

.no-data p {
  margin: 0;
}

.stat-section {
  background: white;
  padding: 1.5rem;
  border-radius: 1rem;
  box-shadow: 0 4px 6px rgba(0,0,0,0.05);
}

.stat-section h3 {
  margin: 0 0 1rem 0;
  color: #1f2937;
  border-bottom: 2px solid #3b82f6;
  padding-bottom: 0.5rem;
}

.revenue-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.revenue-card {
  text-align: center;
  padding: 1rem;
  background: #f9fafb;
  border-radius: 0.5rem;
}

.revenue-card h4 {
  margin: 0 0 0.5rem 0;
  color: #6b7280;
  font-size: 0.875rem;
}

.amount {
  font-size: 1.5rem;
  font-weight: 700;
  color: #1f2937;
}

.technician-stats table,
.feedback-stats table,
.users-table {
  width: 100%;
  border-collapse: collapse;
}

.technician-stats th,
.technician-stats td,
.feedback-stats th,
.feedback-stats td,
.users-table th,
.users-table td {
  padding: 0.75rem;
  text-align: left;
  border-bottom: 1px solid #e5e7eb;
}

.technician-stats th,
.feedback-stats th,
.users-table th {
  background: #f9fafb;
  font-weight: 600;
  color: #374151;
}

.users-container {
  background: white;
  border-radius: 1rem;
  overflow: hidden;
  box-shadow: 0 4px 6px rgba(0,0,0,0.05);
}

.empty-state {
  text-align: center;
  padding: 3rem 1rem;
  color: #6b7280;
}

.empty-state i {
  font-size: 3rem;
  margin-bottom: 1rem;
  opacity: 0.5;
}

.empty-state h3 {
  margin-bottom: 0.5rem;
  color: #374151;
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 1rem;
}

.modal-content {
  background: white;
  border-radius: 1rem;
  max-width: 600px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #e5e7eb;
}

.modal-header h2 {
  margin: 0;
  color: #1f2937;
  font-size: 1.5rem;
}

.modal-close {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: #6b7280;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 0.5rem;
  transition: all 0.2s;
}

.modal-close:hover {
  background: #f3f4f6;
  color: #374151;
}

.modal-body {
  padding: 1.5rem;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  padding: 1.5rem;
  border-top: 1px solid #e5e7eb;
}

.assign-form {
  display: grid;
  gap: 1rem;
}

.technician-selection {
  display: grid;
  gap: 0.5rem;
  max-height: 200px;
  overflow-y: auto;
  border: 1px solid #e5e7eb;
  border-radius: 0.5rem;
  padding: 1rem;
}

.technician-option {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.technician-label {
  flex: 1;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 0.5rem;
  transition: background-color 0.2s;
}

.technician-label:hover {
  background: #f9fafb;
}

.tech-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tech-name {
  font-weight: 600;
  color: #1f2937;
}

.tech-skill {
  color: #6b7280;
  font-size: 0.875rem;
}

.tech-rate {
  color: #059669;
  font-weight: 500;
  font-size: 0.875rem;
}

/* 反馈相关样式 */
.rating-stars {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.star {
  color: #d1d5db;
  font-size: 1rem;
}

.star.active {
  color: #f59e0b;
}

.rating-text {
  font-size: 0.875rem;
  color: #6b7280;
  margin-left: 0.5rem;
}

.feedback-comment {
  max-width: 200px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.feedback-pagination {
  text-align: center;
  margin-top: 1rem;
  padding: 1rem;
  background: #f9fafb;
  border-radius: 0.5rem;
  color: #6b7280;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .dashboard-header {
    padding: 0 1rem;
    flex-direction: column;
    gap: 1rem;
  }

  .header-left {
    flex-direction: column;
    gap: 1rem;
  }

  .nav-menu {
    overflow-x: auto;
  }

  .dashboard-main {
    padding: 1rem;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .action-grid {
    grid-template-columns: 1fr;
  }

  .section-header {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }

  .order-info {
    grid-template-columns: 1fr;
  }

  .tech-details {
    grid-template-columns: 1fr;
  }

  .revenue-stats {
    grid-template-columns: 1fr;
  }

  .modal-content {
    margin: 1rem;
    max-height: calc(100vh - 2rem);
  }

  .modal-footer {
    flex-direction: column;
  }

  .welcome-content {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
  }
}

.btn-secondary {
  background: #6b7280;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 0.5rem;
  cursor: pointer;
  font-size: 0.875rem;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.btn-secondary:hover:not(:disabled) {
  background: #4b5563;
  transform: translateY(-1px);
}

.btn-secondary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-secondary .fa-spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>