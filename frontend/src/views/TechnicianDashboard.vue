<template>
  <div class="technician-dashboard">
    <aside class="sidebar">
      <div class="sidebar-user">
        <div class="user-avatar">
          <i class="fas fa-user-hard-hat"></i>
        </div>
        <span class="user-name">{{ user.name || user.username }}</span>
        <div class="user-dropdown-btn" @click="toggleUserMenu">
          <i class="fas fa-chevron-down"></i>
        </div>
        <div v-if="showUserMenu" class="user-dropdown">
          <a href="#" @click="activeTab = 'profile'">
            <i class="fas fa-user-edit"></i> 个人资料
          </a>
          <a href="#" @click="logout">
            <i class="fas fa-sign-out-alt"></i> 登出
          </a>
        </div>
      </div>
      <nav class="nav-menu">
        <a href="#" @click="activeTab = 'overview'" :class="{ active: activeTab === 'overview' }">
          <i class="fas fa-home"></i> 概览
        </a>
        <a href="#" @click="activeTab = 'tasks'" :class="{ active: activeTab === 'tasks' }">
          <i class="fas fa-tasks"></i> 我的任务
        </a>
        <a href="#" @click="activeTab = 'history'" :class="{ active: activeTab === 'history' }">
          <i class="fas fa-history"></i> 工作历史
        </a>
        <a href="#" @click="activeTab = 'earnings'" :class="{ active: activeTab === 'earnings' }">
          <i class="fas fa-dollar-sign"></i> 工时费统计
        </a>
      </nav>
    </aside>
    <main class="dashboard-main">
      <!-- 概览页面 -->
      <div v-if="activeTab === 'overview'" class="tab-content">
        <div class="welcome-section">
          <h1>欢迎回来，{{ user.name || user.username }}技师！</h1>
          <div class="skill-badge">
            <i class="fas fa-award"></i>
            <span>{{ getSkillTypeName(user.skillType) }}</span>
          </div>
        </div>

        <!-- 统计卡片 -->
        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f59e0b, #d97706);">
              <i class="fas fa-tasks"></i>
            </div>
            <div class="stat-content">
              <h3>{{ statistics.totalTasks }}</h3>
              <p>总任务数</p>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #10b981, #059669);">
              <i class="fas fa-check-circle"></i>
            </div>
            <div class="stat-content">
              <h3>{{ statistics.completedTasks }}</h3>
              <p>已完成任务</p>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon" style="background: linear-gradient(135deg, #3b82f6, #2563eb);">
              <i class="fas fa-clock"></i>
            </div>
            <div class="stat-content">
              <h3>{{ statistics.pendingTasks }}</h3>
              <p>进行中任务</p>
            </div>
          </div>
        </div>

        <!-- 待处理任务 -->
        <div class="pending-tasks">
          <h2>待处理任务</h2>
          <div class="task-list">
            <div v-if="pendingTasks.length === 0" class="empty-state">
              <i class="fas fa-check-circle"></i>
              <p>暂无待处理任务</p>
            </div>
            <div v-for="task in pendingTasks.slice(0, 5)" :key="task.id" class="task-item">
              <div class="task-info">
                <h4>{{ task.orderNumber || `维修单 #${task.id}` }}</h4>
                <p>{{ task.description }}</p>
                <div class="task-meta">
                  <span class="task-date">
                    <i class="fas fa-calendar"></i>
                    {{ formatDate(task.createdAt) }}
                  </span>
                  <span class="task-vehicle">
                    <i class="fas fa-car"></i>
                    {{ getVehicleDisplay(task) }}
                  </span>
                  <span class="task-customer">
                    <i class="fas fa-user"></i>
                    {{ task.user ? task.user.name : '未知客户' }}
                  </span>
                </div>
              </div>
              <div class="task-actions">
                <template v-if="task.status === 'ASSIGNED'">
                  <button class="btn btn-success" @click="startTask(task)">
                    <i class="fas fa-play"></i>
                    开始维修
                  </button>
                  <button class="btn btn-danger" @click="rejectTask(task)">
                    <i class="fas fa-times"></i>
                    拒绝订单
                  </button>
                </template>
                <template v-else-if="task.status === 'IN_PROGRESS'">
                  <button class="btn btn-primary" @click="openCompleteTaskDialog(task)">
                    <i class="fas fa-check"></i>
                    完成维修
                  </button>
                </template>
                <!-- 显示催单提醒 -->
                <div v-if="task.urgeCount > 0" class="urge-reminder">
                  <i class="fas fa-bell"></i>
                  {{ task.urgeCount>0 ?"客户已催单":"客户未催单" }}<br>
                  <span v-if="task.lastUrgedAt" class="urge-time">
                    最近催单时间：{{ formatDate(task.lastUrgedAt) }}
                  </span>
                </div>
              </div>
            </div>
          </div>
          <button v-if="pendingTasks.length > 5" class="btn btn-outline" @click="activeTab = 'tasks'">
            查看全部任务
          </button>
        </div>
      </div>

      <!-- 任务管理页面 -->
      <div v-if="activeTab === 'tasks'" class="tab-content">
        <div class="section-header">
          <h2>我的任务</h2>
          <div class="task-filters">
            <select v-model="taskFilter" class="form-input">
              <option value="">全部状态</option>
              <option value="ASSIGNED">已分配</option>
              <option value="IN_PROGRESS">进行中</option>
              <option value="COMPLETED">已完成</option>
            </select>
          </div>
        </div>

        <div class="tasks-container">
          <div v-if="filteredTasks.length === 0" class="empty-state">
            <i class="fas fa-tasks"></i>
            <h3>暂无任务</h3>
            <p>等待系统分配新任务</p>
          </div>
          <div v-for="task in filteredTasks" :key="task.id" class="task-card">
            <div class="task-header">
              <div>
                <h3>{{ task.orderNumber || `维修单 #${task.id}` }}</h3>
                <p class="task-vehicle">{{ getVehicleDisplay(task) }}</p>
              </div>
              <span :class="['status-badge', task.status.toLowerCase()]">
                {{ getStatusText(task.status) }}
              </span>
            </div>
            <div class="task-body">
              <p><strong>故障描述:</strong> {{ task.description }}</p>
              <div class="task-info">
                <div class="info-item">
                  <i class="fas fa-user"></i>
                  <span>客户: {{ task.user ? task.user.name : '未知' }}</span>
                </div>
                <div class="info-item">
                  <i class="fas fa-phone"></i>
                  <span>{{ task.user ? task.user.phone : '无联系方式' }}</span>
                </div>
                <div class="info-item">
                  <i class="fas fa-calendar"></i>
                  <span>创建: {{ formatDate(task.createdAt) }}</span>
                </div>
                <div v-if="task.completedAt" class="info-item">
                  <i class="fas fa-check-circle"></i>
                  <span>完成: {{ formatDate(task.completedAt) }}</span>
                </div>
              </div>
          
            </div>
            <div class="task-footer">
              <button v-if="task.status === 'ASSIGNED'" @click="startTask(task)" class="btn btn-primary">
                <i class="fas fa-play"></i> 开始任务
              </button>
              <button v-if="task.status === 'ASSIGNED'" @click="rejectTask(task)" class="btn btn-danger">
                <i class="fas fa-times"></i> 拒绝任务
              </button>
              <button v-if="task.status === 'IN_PROGRESS'" @click="completeTask(task)" class="btn btn-success">
                <i class="fas fa-check"></i> 完成任务
              </button>
              <button @click="viewTaskDetail(task)" class="btn btn-outline">
                <i class="fas fa-eye"></i> 查看详情
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 工作历史页面 -->
      <div v-if="activeTab === 'history'" class="tab-content">
        <div class="section-header">
          <h2>工作历史</h2>
        </div>
        
        <div class="history-container">
          <div v-if="completedTasks.length === 0" class="empty-state">
            <i class="fas fa-history"></i>
            <h3>暂无历史记录</h3>
            <p>完成任务后将显示在这里</p>
          </div>
          <div v-for="task in completedTasks" :key="task.id" class="history-card">
            <div class="history-header">
              <div>
                <h3>维修单 #{{ task.id }}</h3>
                <p>{{ task.vehiclePlate }}</p>
              </div>
              <div class="completion-date">
               完成时间： {{ formatDate(task.completedAt) }}
              </div>
            </div>
            <div class="history-body">
              <p>故障描述：{{ task.description }}</p>
              <div class="history-metrics">
                <span class="metric">
                  <i class="fas fa-dollar-sign"></i>
                  总费用: ¥{{ task.totalCost }}
                </span>
                <span class="metric" style="margin-left: 16px;">
                  <i class="fas fa-clock"></i>
                  工时费: ¥{{ task.laborCost || 0 }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 收入统计页面 -->
      <div v-if="activeTab === 'earnings'" class="tab-content">
        <div class="section-header">
          <h2>收入统计</h2>
        </div>
        <div class="earnings-dashboard">
          <div class="earnings-summary">
            <div class="summary-card">
            <div class="summary-card">
              <h3>工时费总计</h3>
              <div class="amount">¥{{ formatCurrency(earnings.laborTotal) }}</div>
            </div>
          </div>
        </div></div>
      </div>

      <!-- 个人资料页面 -->
      <div v-if="activeTab === 'profile'" class="tab-content">
        <div class="section-header">
          <h2>个人资料</h2>
        </div>
        
        <div class="profile-container">
          <form @submit.prevent="updateProfile" class="profile-form">
            <div class="form-group">
              <label class="form-label">姓名</label>
              <input v-model="profileForm.name" class="form-input" required>
            </div>
            <div class="form-group">
              <label class="form-label">员工ID</label>
              <input v-model="profileForm.employeeId" class="form-input" disabled>
            </div>
            <div class="form-group">
              <label class="form-label">用户名</label>
              <input v-model="profileForm.username" class="form-input" disabled>
            </div>
            <div class="form-group">
              <label class="form-label">电话</label>
              <input v-model="profileForm.phone" class="form-input" required>
            </div>
            <div class="form-group">
              <label class="form-label">邮箱</label>
              <input v-model="profileForm.email" type="email" class="form-input" required>
            </div>
            <div class="form-group">
              <label class="form-label">技能类型</label>
              <select v-model="profileForm.skillType" class="form-input" required>
                <option value="MECHANIC">机械维修</option>
                <option value="ELECTRICIAN">电气维修</option>
                <option value="BODY_WORK">车身维修</option>
                <option value="PAINT">喷漆</option>
                <option value="DIAGNOSTIC">诊断</option>
              </select>
            </div>
            <div class="form-group">
              <label class="form-label">时薪</label>
              <input v-model="profileForm.hourlyRate" type="number" step="0.01" class="form-input" required>
            </div>
            <button type="submit" class="btn btn-primary">
              <i class="fas fa-save"></i> 保存更改
            </button>
          </form>
        </div>
      </div>
    </main>

    <!-- 任务详情模态框 -->
    <div v-if="showTaskDetail && selectedTask" class="modal-overlay" @click="closeTaskDetail">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>{{ selectedTask.orderNumber || `维修单 #${selectedTask.id}` }}</h2>
          <button class="modal-close" @click="closeTaskDetail">
            <i class="fas fa-times"></i>
          </button>
        </div>
        <div class="modal-body">
          <div class="detail-section">
            <h3>基本信息</h3>
            <div class="detail-grid">
              <div class="detail-item">
                <label>订单号:</label>
                <span>{{ selectedTask.orderNumber || `RO-${selectedTask.id}` }}</span>
              </div>
              <div class="detail-item">
                <label>状态:</label>
                <span :class="['status-badge', selectedTask.status.toLowerCase()]">
                  {{ getStatusText(selectedTask.status) }}
                </span>
              </div>
              <div class="detail-item">
                <label>创建时间:</label>
                <span>{{ formatDate(selectedTask.createdAt) }}</span>
              </div>
              <div v-if="selectedTask.completedAt" class="detail-item">
                <label>完成时间:</label>
                <span>{{ formatDate(selectedTask.completedAt) }}</span>
              </div>
            </div>
          </div>

          <div class="detail-section">
            <h3>车辆信息</h3>
            <div class="detail-grid" v-if="selectedTask.vehicle">
              <div class="detail-item">
                <label>车牌号:</label>
                <span>{{ selectedTask.vehicle.licensePlate }}</span>
              </div>
              <div class="detail-item">
                <label>品牌型号:</label>
                <span>{{ selectedTask.vehicle.brand }} {{ selectedTask.vehicle.model }}</span>
              </div>
              <div class="detail-item">
                <label>年份:</label>
                <span>{{ selectedTask.vehicle.year }}</span>
              </div>
              <div class="detail-item">
                <label>颜色:</label>
                <span>{{ selectedTask.vehicle.color }}</span>
              </div>
            </div>
          </div>

          <div class="detail-section">
            <h3>客户信息</h3>
            <div class="detail-grid" v-if="selectedTask.user">
              <div class="detail-item">
                <label>客户姓名:</label>
                <span>{{ selectedTask.user.name }}</span>
              </div>
              <div class="detail-item">
                <label>联系电话:</label>
                <span>{{ selectedTask.user.phone }}</span>
              </div>
            </div>
          </div>

          <div class="detail-section">
            <h3>维修详情</h3>
            <div class="detail-item full-width">
              <label>故障描述:</label>
              <p class="description">{{ selectedTask.description }}</p>
            </div>
            <div class="detail-grid">
              <div class="detail-item">
                <label>工时费用:</label>
                <span class="amount">¥{{ selectedTask.laborCost || 0 }}</span>
              </div>
              
              <div class="detail-item">
                <label>材料费用:</label>
                <span class="amount">¥{{ selectedTask.materialCost || 0 }}</span>
              </div>
              <div class="detail-item">
                <label>总费用:</label>
                <span class="amount total">¥{{ selectedTask.totalCost || (selectedTask.laborCost || 0) + (selectedTask.materialCost || 0) }}</span>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button v-if="selectedTask.status === 'ASSIGNED'" @click="startTask(selectedTask)" class="btn btn-primary">
            <i class="fas fa-play"></i> 开始任务
          </button>
          <button v-if="selectedTask.status === 'IN_PROGRESS'" @click="completeTask(selectedTask)" class="btn btn-success">
            <i class="fas fa-check"></i> 完成任务
          </button>
          <button @click="closeTaskDetail" class="btn btn-outline">
            <i class="fas fa-times"></i> 关闭
          </button>
        </div>
      </div>
    </div>

    <!-- 完成任务模态框 -->
    <div v-if="showCompleteTask && selectedTask" class="modal-overlay" @click="closeCompleteTask">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>完成任务</h2>
          <button class="modal-close" @click="closeCompleteTask">
            <i class="fas fa-times"></i>
          </button>
        </div>
        <div class="modal-body">
          <div class="task-summary">
            <h3>{{ selectedTask.orderNumber || `维修单 #${selectedTask.id}` }}</h3>
            <p>{{ selectedTask.description }}</p>
            <p><strong>车辆:</strong> {{ getVehicleDisplay(selectedTask) }}</p>
          </div>
          
          <form @submit.prevent="submitCompleteTask">
            <div class="form-group">
              <label class="form-label">材料类型</label>
              <textarea v-model="completeTaskForm.material" class="form-input" rows="3"
                        placeholder="本次维修所用的材料"></textarea>
            </div>
            <div class="form-group">
              <label class="form-label">材料费用 <span class="required"></span></label>
              <input v-model="completeTaskForm.materialCost" type="number" step="0.01" min="0" 
                     class="form-input" placeholder="请输入材料费用" required>
            </div>
            
            <div class="form-group">
              <label class="form-label">工作说明</label>
              <textarea v-model="completeTaskForm.workNotes" class="form-input" rows="3"
                        placeholder="请简要说明本次维修的工作内容（可选）"></textarea>
            </div>
            
            <div class="modal-footer">
              <button type="button" @click="closeCompleteTask" class="btn btn-outline">
                取消
              </button>
              <button type="submit" class="btn btn-success" :disabled="isSubmitting">
                <i class="fas fa-check"></i> {{ isSubmitting ? '完成中...' : '完成任务' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'TechnicianDashboard',
  data() {
    return {
      user: {},
      activeTab: 'overview',
      showUserMenu: false,
      showTaskDetail: false,
      showCompleteTask: false,
      taskFilter: '',
      allTasks: [],
      selectedTask: null,
      completeTaskForm: {
        material: '',
        materialCost: '',
        workNotes: ''
      },
      statistics: {
        totalTasks: 0,
        completedTasks: 0,
        pendingTasks: 0,
        monthlyEarnings: 0
      },
      earnings: {
        monthly: 0,
        total: 0,
        laborTotal: 0
      },
      profileForm: {},
      isSubmitting: false
    }
  },
  computed: {
    pendingTasks() {
      return this.allTasks.filter(task => task.status === 'ASSIGNED' || task.status === 'IN_PROGRESS');
    },
    completedTasks() {
      return this.allTasks.filter(task => task.status === 'COMPLETED');
    },
    filteredTasks() {
      if (!this.taskFilter) return this.allTasks;
      return this.allTasks.filter(task => task.status === this.taskFilter);
    }
  },
  created() {
    this.loadUserInfo();
    this.loadData();
  },
  methods: {
    loadUserInfo() {
      const userData = localStorage.getItem('user');
      if (userData) {
        this.user = JSON.parse(userData);
        this.profileForm = { ...this.user };
        console.log('加载的技师数据:', this.user);
        
        // 检查技师ID是否存在
        if (!this.user.id) {
          console.error('技师数据中缺少ID字段:', this.user);
          this.$emit('message', '用户数据错误，请重新登录', 'error');
          this.logout();
        }
      } else {
        console.error('localStorage中没有技师数据');
        this.$emit('message', '未找到用户信息，请重新登录', 'error');
        this.logout();
      }
    },
    async loadData() {
      try {
        await Promise.all([
          this.loadTasks(),
          this.loadStatistics(),
          this.loadEarnings(),
        ]);
      } catch (error) {
        console.error('加载数据失败:', error);
        this.$emit('message', '加载数据失败', 'error');
      }
    },
    async loadTasks() {
      try {
        console.log('开始加载技师任务，技师ID:', this.user.id);
        const response = await this.$axios.get(`/repair-orders/technician/${this.user.id}`);
        console.log('技师任务API响应:', response.data);
        this.allTasks = response.data || [];
        console.log('设置技师任务数据:', this.allTasks);
        
        if (this.allTasks.length === 0) {
          console.log('没有找到分配给该技师的任务');
        } else {
          console.log(`成功加载 ${this.allTasks.length} 个任务`);
        }
      } catch (error) {
        console.error('加载技师任务失败:', error);
        console.error('错误详情:', error.response?.data);
        
        // 设置空数组避免界面错误
        this.allTasks = [];
        this.$emit('message', `加载任务失败: ${error.response?.data?.message || error.message}`, 'error');
      }
    },
    async loadStatistics() {
      try {
        console.log('开始加载技师统计数据，技师ID:', this.user.id);
        const response = await this.$axios.get(`/technicians/${this.user.id}/statistics`);
        console.log('技师统计API响应:', response.data);
        
        this.statistics = {
          totalTasks: response.data.totalTasks || 0,
          completedTasks: response.data.completedTasks || 0,
          pendingTasks: response.data.pendingTasks || 0,
          monthlyEarnings: response.data.monthlyEarnings || 0
        };
        
        console.log('设置统计数据:', this.statistics);
      } catch (error) {
        console.error('加载统计数据失败:', error);
        
        // 基于本地任务数据计算统计信息作为备用
        const totalTasks = this.allTasks.length;
        const completedTasks = this.allTasks.filter(task => task.status === 'COMPLETED').length;
        const pendingTasks = this.allTasks.filter(task => 
          task.status === 'ASSIGNED' || task.status === 'IN_PROGRESS'
        ).length;
        
        this.statistics = {
          totalTasks,
          completedTasks,
          pendingTasks,
        };
        
        console.log('使用本地计算的统计数据:', this.statistics);
      }
    },
    
    async loadEarnings() {
      try {
        console.log('开始加载技师收入数据，技师ID:', this.user.id);
        // 获取总收入
        const totalEarningsResponse = await this.$axios.get(`/technicians/${this.user.id}/earnings`);
        const totalEarnings = totalEarningsResponse.data || 0;
        // 计算工时费总计
        const laborTotal = this.completedTasks.reduce((sum, task) => sum + (task.laborCost || 0), 0);
        this.earnings = {
          monthly: monthlyEarnings,
          total: totalEarnings,
          laborTotal: Math.round(laborTotal * 100) / 100
        };
        console.log('设置收入数据:', this.earnings);
      } catch (error) {
        console.error('加载收入数据失败:', error);
        const completedTasks = this.allTasks.filter(task => task.status === 'COMPLETED');
        const laborTotal = completedTasks.reduce((sum, task) => sum + (task.laborCost || 0), 0);
        this.earnings = {
          laborTotal: Math.round(laborTotal * 100) / 100
        };
        console.log('使用估算的收入数据:', this.earnings);
      }
    },
    
    formatCurrency(amount) {
      return Math.round((amount || 0) * 100) / 100;
    },
    
    toggleUserMenu() {
      this.showUserMenu = !this.showUserMenu;
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
    formatDate(dateString) {
      return new Date(dateString).toLocaleDateString('zh-CN');
    },
    calculateDuration(startDate, endDate) {
      const start = new Date(startDate);
      const end = new Date(endDate);
      const days = Math.ceil((end - start) / (1000 * 60 * 60 * 24));
      return `${days}天`;
    },
    async startTask(task) {
      try {
        console.log('开始任务:', task.id);
        const response = await this.$axios.put(`/repair-orders/${task.id}/status`, null, {
          params: { status: 'IN_PROGRESS' }
        });
        
        console.log('开始任务API响应:', response.data);
        
        // 更新本地任务状态
        const taskIndex = this.allTasks.findIndex(t => t.id === task.id);
        if (taskIndex !== -1) {
          this.allTasks[taskIndex].status = 'IN_PROGRESS';
          this.allTasks[taskIndex].startedAt = new Date().toISOString();
          this.allTasks[taskIndex].updatedAt = new Date().toISOString();
        }
        
        this.loadStatistics(); // 重新计算统计信息
        this.$emit('message', '任务已开始', 'success');
      } catch (error) {
        console.error('开始任务失败:', error);
        this.$emit('message', '开始任务失败: ' + (error.response?.data?.message || error.message), 'error');
      }
    },
    async completeTask(task) {
      this.selectedTask = task;
      this.completeTaskForm = {
        materialCost: '',
        workNotes: ''
      };
      this.showCompleteTask = true;
    },
    viewTask(task) {
      this.selectedTask = task;
      this.showTaskDetail = true;
    },
    viewTaskDetail(task) {
      this.selectedTask = task;
      this.showTaskDetail = true;
    },
    closeTaskDetail() {
      this.showTaskDetail = false;
      this.selectedTask = null;
    },
    getVehicleDisplay(task) {
      if (task.vehicle) {
        return `${task.vehicle.licensePlate} - ${task.vehicle.brand} ${task.vehicle.model}`;
      }
      return '未知车辆';
    },
    async updateProfile() {
      try {
        // 构建完整的技师更新请求数据
        const updateData = {
          name: this.profileForm.name,
          employeeId: this.user.employeeId, // 保持原员工ID
          username: this.user.username, // 保持原用户名
          password: '', // 不更新密码时发送空字符串
          phone: this.profileForm.phone,
          email: this.profileForm.email,
          skillType: this.profileForm.skillType,
          hourlyRate: parseFloat(this.profileForm.hourlyRate)
        };
        
        const response = await this.$axios.put(`/technicians/${this.user.id}`, updateData);
        this.user = { ...this.user, ...response.data };
        this.profileForm = { ...this.user };
        localStorage.setItem('user', JSON.stringify(this.user));
        this.$emit('message', '个人资料更新成功', 'success');
      } catch (error) {
        console.error('更新资料失败:', error);
        const errorMessage = error.response?.data?.message || error.message || '更新资料失败';
        this.$emit('message', errorMessage, 'error');
      }
    },
    logout() {
      localStorage.removeItem('user');
      localStorage.removeItem('userRole');
      this.$router.push('/');
    },
    async submitCompleteTask() {
      try {
        this.isSubmitting = true;
        console.log('提交完成任务:', this.selectedTask.id);
        const response = await this.$axios.put(`/repair-orders/${this.selectedTask.id}/status`, null, {
          params: { 
            status: 'COMPLETED',
            materialCost: this.completeTaskForm.materialCost
          }
        });
        
        // 更新本地任务状态
        const taskIndex = this.allTasks.findIndex(t => t.id === this.selectedTask.id);
        if (taskIndex !== -1) {
          this.allTasks[taskIndex].status = 'COMPLETED';
          this.allTasks[taskIndex].completedAt = new Date().toISOString();
          this.allTasks[taskIndex].materialCost = this.completeTaskForm.materialCost;
          this.allTasks[taskIndex].updatedAt = new Date().toISOString();
        }
        
        this.closeCompleteTask();
        this.loadStatistics(); // 重新计算统计信息
        this.loadEarnings(); // 重新计算收入
        this.$emit('message', '任务已完成！', 'success');
      } catch (error) {
        console.error('完成任务失败:', error);
        this.$emit('message', '完成任务失败: ' + (error.response?.data?.message || error.message), 'error');
      } finally {
        this.isSubmitting = false;
      }
    },
    closeCompleteTask() {
      this.showCompleteTask = false;
      this.selectedTask = null;
      this.completeTaskForm = {
        materialCost: '',
        workNotes: ''
      };
    },      async rejectTask(task) {
      try {
        if (!confirm('确定要拒绝此任务吗？拒绝后系统将尝试重新分配给其他技师。')) {
          return;
        }
        
        console.log('拒绝任务:', {taskId: task.id, technicianId: this.user.id});
        await this.$axios.post(`/technicians/${this.user.id}/orders/${task.id}/reject`);
        
        // 更新本地任务状态
        const taskIndex = this.allTasks.findIndex(t => t.id === task.id);
        if (taskIndex !== -1) {
          // 从任务列表中移除该任务
          this.allTasks.splice(taskIndex, 1);
        }
        
        this.$emit('message', '已成功拒绝任务', 'success');
        
        // 重新加载任务列表和统计信息
        await Promise.all([
          this.loadTasks(),
          this.loadStatistics()
        ]);
      } catch (error) {
        console.error('拒绝任务失败:', error);
        this.$emit('message', error.response?.data?.message || '拒绝任务失败', 'error');
      }
    },
    openCompleteTaskDialog(task) {
      this.selectedTask = task;
      this.completeTaskForm.materialCost = task.materialCost || '';
      this.showCompleteTask = true;
    }
  }
}
</script>

<style scoped>
.technician-dashboard {
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
  margin-top: 0;
  padding: 1.5rem 1rem 1rem 1rem;
  border-bottom: 1px solid rgba(255,255,255,0.1);
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 0.5rem;
  background: inherit;
}
.user-avatar {
  font-size: 2rem;
  margin-bottom: 0.5rem;
}
.user-name {
  font-weight: bold;
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

.tab-content {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.welcome-section {
  text-align: center;
  margin-bottom: 2rem;
}

.welcome-section h1 {
  font-size: 2rem;
  color: #1f2937;
  margin-bottom: 1rem;
}

.skill-badge {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background: linear-gradient(135deg, #f59e0b, #d97706);
  color: white;
  border-radius: 9999px;
  font-weight: 500;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.stat-card {
  background: white;
  padding: 1.5rem;
  border-radius: 1rem;
  box-shadow: 0 4px 6px rgba(0,0,0,0.05);
  display: flex;
  align-items: center;
  gap: 1rem;
}

.stat-icon {
  width: 3rem;
  height: 3rem;
  border-radius: 0.75rem;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.25rem;
}

.stat-content h3 {
  font-size: 1.5rem;
  font-weight: 700;
  margin: 0;
  color: #1f2937;
}

.stat-content p {
  margin: 0;
  color: #6b7280;
  font-size: 0.875rem;
}

.info-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.technician-card {
  background: white;
  padding: 1.5rem;
  border-radius: 1rem;
  box-shadow: 0 4px 6px rgba(0,0,0,0.05);
  display: flex;
  gap: 1rem;
}

.tech-avatar {
  width: 4rem;
  height: 4rem;
  background: linear-gradient(135deg, #f59e0b, #d97706);
  border-radius: 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.5rem;
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
  color: #f59e0b;
  width: 1rem;
}

.earnings-overview {
  background: white;
  padding: 1.5rem;
  border-radius: 1rem;
  box-shadow: 0 4px 6px rgba(0,0,0,0.05);
}

.earnings-overview h3 {
  margin: 0 0 1rem 0;
  color: #1f2937;
}

.earnings-stats {
  margin-bottom: 1rem;
}

.earnings-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.5rem;
}

.earnings-label {
  color: #6b7280;
  font-size: 0.875rem;
}

.earnings-value {
  font-weight: 600;
  color: #1f2937;
}

.pending-tasks {
  margin-bottom: 2rem;
}

.pending-tasks h2 {
  margin-bottom: 1rem;
  color: #1f2937;
}

.task-list {
  background: white;
  border-radius: 1rem;
  padding: 1.5rem;
  box-shadow: 0 4px 6px rgba(0,0,0,0.05);
  margin-bottom: 1rem;
}

.task-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 0;
  border-bottom: 1px solid #e5e7eb;
}

.task-item:last-child {
  border-bottom: none;
}

.task-info h4 {
  margin: 0 0 0.25rem 0;
  color: #1f2937;
}

.task-info p {
  margin: 0 0 0.5rem 0;
  color: #6b7280;
  font-size: 0.875rem;
}

.task-meta {
  display: flex;
  gap: 1rem;
  font-size: 0.75rem;
  color: #9ca3af;
}

.task-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.task-status {
  padding: 0.25rem 0.75rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 500;
  text-transform: uppercase;
}

.task-status.pending {
  background: #fef3c7;
  color: #d97706;
}

.task-status.in_progress {
  background: #dbeafe;
  color: #2563eb;
}

.task-status.completed {
  background: #d1fae5;
  color: #059669;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.section-header h2 {
  font-size: 1.5rem;
  color: #1f2937;
  margin: 0;
}

.task-filters {
  display: flex;
  gap: 1rem;
}

.tasks-container,
.history-container {
  display: grid;
  gap: 1.5rem;
}

.task-card,
.history-card {
  background: white;
  padding: 1.5rem;
  border-radius: 1rem;
  box-shadow: 0 4px 6px rgba(0,0,0,0.05);
}

.task-header,
.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.status-badge {
  padding: 0.5rem 1rem;
  border-radius: 9999px;
  font-size: 0.875rem;
  font-weight: 500;
}

.task-timeline {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin: 1rem 0;
}

.timeline-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
  color: #6b7280;
}

.timeline-item i {
  color: #f59e0b;
}

.task-footer {
  display: flex;
  gap: 1rem;
  margin-top: 1rem;
}

.btn-sm {
  padding: 0.5rem 1rem;
  font-size: 0.875rem;
}

.history-metrics {
  display: flex;
  gap: 1rem;
  margin-top: 0.5rem;
}

.metric {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  font-size: 0.875rem;
  color: #6b7280;
}

.metric i {
  color: #f59e0b;
}

.earnings-dashboard {
  max-width: 800px;
}

.earnings-summary {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.summary-card {
  background: white;
  padding: 1.5rem;
  border-radius: 1rem;
  box-shadow: 0 4px 6px rgba(0,0,0,0.05);
  text-align: center;
}

.summary-card h3 {
  margin: 0 0 1rem 0;
  color: #6b7280;
  font-size: 0.875rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.amount {
  font-size: 2rem;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 0.5rem;
}

.change {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.25rem;
  font-size: 0.875rem;
  color: #6b7280;
}

.change.positive {
  color: #059669;
}

.tasks-count {
  font-size: 0.875rem;
  color: #6b7280;
}

.profile-container {
  max-width: 600px;
}

.profile-form {
  background: white;
  padding: 2rem;
  border-radius: 1rem;
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
  
  .info-section {
    grid-template-columns: 1fr;
  }
  
  .section-header {
    flex-direction: column;
    gap: 1rem;
    align-items: stretch;
  }
  
  .task-item {
    flex-direction: column;
    align-items: stretch;
    gap: 1rem;
  }
  
  .task-actions {
    justify-content: space-between;
  }
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
  max-width: 800px;
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

.detail-section {
  margin-bottom: 2rem;
}

.detail-section:last-child {
  margin-bottom: 0;
}

.detail-section h3 {
  margin: 0 0 1rem 0;
  color: #1f2937;
  font-size: 1.125rem;
  font-weight: 600;
  border-bottom: 2px solid #f59e0b;
  padding-bottom: 0.5rem;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.detail-item.full-width {
  grid-column: 1 / -1;
}

.detail-item label {
  font-weight: 600;
  color: #374151;
  font-size: 0.875rem;
}

.detail-item span {
  color: #6b7280;
}

.detail-item .description {
  background: #f9fafb;
  padding: 1rem;
  border-radius: 0.5rem;
  margin: 0;
  color: #374151;
  line-height: 1.6;
}

.detail-item .amount {
  font-weight: 600;
  color: #059669;
}

.detail-item .amount.total {
  color: #dc2626;
  font-size: 1.125rem;
}

.task-info {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin: 1rem 0;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
  color: #6b7280;
}

.info-item i {
  color: #f59e0b;
  width: 16px;
}

.status-badge.assigned {
  background: #fef3c7;
  color: #d97706;
}

.status-badge.in_progress {
  background: #dbeafe;
  color: #2563eb;
}

.status-badge.completed {
  background: #d1fae5;
  color: #059669;
}

.status-badge.cancelled {
  background: #fee2e2;
  color: #dc2626;
}

.btn-danger {
  background-color: #dc2626;
  color: white;
  border: none;
  transition: background-color 0.2s;
}

.btn-danger:hover {
  background-color: #b91c1c;
}

@media (max-width: 768px) {
  .modal-content {
    margin: 1rem;
    max-height: calc(100vh - 2rem);
  }
  
  .detail-grid {
    grid-template-columns: 1fr;
  }
  
  .modal-footer {
    flex-direction: column;
  }
}
</style>