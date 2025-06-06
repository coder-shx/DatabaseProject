<!--<template>-->
<!--  <div class="identity-selection">-->
<!--    <div class="hero-section">-->
<!--      <h1 class="hero-title">-->
<!--        <i class="fas fa-car icon-large"></i>-->
<!--        汽车维修管理系统-->
<!--      </h1>-->
<!--      <p class="hero-subtitle">选择您的身份开始使用系统</p>-->
<!--    </div>-->

<!--    <div class="identity-grid">-->
<!--      <div -->
<!--        v-for="role in roles" -->
<!--        :key="role.key"-->
<!--        class="identity-card"-->
<!--        @click="selectRole(role.key)"-->
<!--      >-->
<!--        <div class="card-icon">-->
<!--          <i :class="role.icon"></i>-->
<!--        </div>-->
<!--        <h3 class="card-title">{{ role.title }}</h3>-->
<!--        <p class="card-description">{{ role.description }}</p>-->
<!--        <div class="card-features">-->
<!--          <ul>-->
<!--            <li v-for="feature in role.features" :key="feature">-->
<!--              <i class="fas fa-check-circle"></i>-->
<!--              {{ feature }}-->
<!--            </li>-->
<!--          </ul>-->
<!--        </div>-->
<!--        <button class="select-btn">-->
<!--          选择 {{ role.title }}-->
<!--          <i class="fas fa-arrow-right"></i>-->
<!--        </button>-->
<!--      </div>-->
<!--    </div>-->
<!--  </div>-->
<!--</template>-->

<!--<script>-->
<!--export default {-->
<!--  name: 'IdentitySelection',-->
<!--  data() {-->
<!--    return {-->
<!--      roles: [-->
<!--        {-->
<!--          key: 'customer',-->
<!--          title: '顾客',-->
<!--          icon: 'fas fa-user',-->
<!--          description: '车主用户，管理车辆和维修服务',-->
<!--          features: [-->
<!--            '管理车辆信息',-->
<!--            '预约维修服务',-->
<!--            '查看维修记录',-->
<!--            '提交服务反馈'-->
<!--          ]-->
<!--        },-->
<!--        {-->
<!--          key: 'technician',-->
<!--          title: '技师',-->
<!--          icon: 'fas fa-tools',-->
<!--          description: '专业技师，负责车辆维修工作',-->
<!--          features: [-->
<!--            '接收维修任务',-->
<!--            '记录维修进度',-->
<!--            '管理工作时间',-->
<!--            '查看收入统计'-->
<!--          ]-->
<!--        },-->
<!--        {-->
<!--          key: 'admin',-->
<!--          title: '管理员',-->
<!--          icon: 'fas fa-user-cog',-->
<!--          description: '系统管理员，负责系统运营管理',-->
<!--          features: [-->
<!--            '用户权限管理',-->
<!--            '系统数据统计',-->
<!--            '业务流程监控',-->
<!--            '系统配置管理'-->
<!--          ]-->
<!--        }-->
<!--      ]-->
<!--    }-->
<!--  },-->
<!--  methods: {-->
<!--    selectRole(role) {-->
<!--      this.$emit('role-selected', role);-->
<!--    }-->
<!--  }-->
<!--}-->
<!--</script>-->

<!--<style scoped>-->
<!--.identity-selection {-->
<!--  min-height: 100vh;-->
<!--  display: flex;-->
<!--  flex-direction: column;-->
<!--  align-items: center;-->
<!--  padding: 40px 20px;-->
<!--  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);-->
<!--}-->

<!--.hero-section {-->
<!--  text-align: center;-->
<!--  margin-bottom: 60px;-->
<!--  color: white;-->
<!--}-->

<!--.hero-title {-->
<!--  font-size: 3rem;-->
<!--  font-weight: 700;-->
<!--  margin-bottom: 16px;-->
<!--  display: flex;-->
<!--  align-items: center;-->
<!--  justify-content: center;-->
<!--  gap: 20px;-->
<!--}-->

<!--.icon-large {-->
<!--  font-size: 3.5rem;-->
<!--  color: #42b983;-->
<!--}-->

<!--.hero-subtitle {-->
<!--  font-size: 1.25rem;-->
<!--  opacity: 0.9;-->
<!--  font-weight: 300;-->
<!--}-->

<!--.identity-grid {-->
<!--  display: grid;-->
<!--  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));-->
<!--  gap: 30px;-->
<!--  max-width: 1200px;-->
<!--  width: 100%;-->
<!--  margin-bottom: 60px;-->
<!--}-->

<!--.identity-card {-->
<!--  background: white;-->
<!--  border-radius: 20px;-->
<!--  padding: 40px 30px;-->
<!--  text-align: center;-->
<!--  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);-->
<!--  cursor: pointer;-->
<!--  transition: all 0.3s ease;-->
<!--  position: relative;-->
<!--  overflow: hidden;-->
<!--}-->

<!--.identity-card::before {-->
<!--  content: '';-->
<!--  position: absolute;-->
<!--  top: 0;-->
<!--  left: -100%;-->
<!--  width: 100%;-->
<!--  height: 4px;-->
<!--  background: linear-gradient(90deg, #42b983, #369970);-->
<!--  transition: left 0.3s ease;-->
<!--}-->

<!--.identity-card:hover::before {-->
<!--  left: 0;-->
<!--}-->

<!--.identity-card:hover {-->
<!--  transform: translateY(-10px);-->
<!--  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);-->
<!--}-->

<!--.card-icon {-->
<!--  width: 80px;-->
<!--  height: 80px;-->
<!--  margin: 0 auto 20px;-->
<!--  background: linear-gradient(135deg, #42b983, #369970);-->
<!--  border-radius: 50%;-->
<!--  display: flex;-->
<!--  align-items: center;-->
<!--  justify-content: center;-->
<!--  color: white;-->
<!--  font-size: 2rem;-->
<!--}-->

<!--.card-title {-->
<!--  font-size: 1.5rem;-->
<!--  font-weight: 600;-->
<!--  color: #1f2937;-->
<!--  margin-bottom: 12px;-->
<!--}-->

<!--.card-description {-->
<!--  color: #6b7280;-->
<!--  margin-bottom: 24px;-->
<!--  line-height: 1.6;-->
<!--}-->

<!--.card-features {-->
<!--  margin-bottom: 30px;-->
<!--}-->

<!--.card-features ul {-->
<!--  list-style: none;-->
<!--  text-align: left;-->
<!--}-->

<!--.card-features li {-->
<!--  display: flex;-->
<!--  align-items: center;-->
<!--  gap: 8px;-->
<!--  margin-bottom: 8px;-->
<!--  color: #374151;-->
<!--  font-size: 14px;-->
<!--}-->

<!--.card-features i {-->
<!--  color: #42b983;-->
<!--  font-size: 12px;-->
<!--}-->

<!--.select-btn {-->
<!--  width: 100%;-->
<!--  padding: 16px 24px;-->
<!--  background: linear-gradient(135deg, #42b983, #369970);-->
<!--  color: white;-->
<!--  border: none;-->
<!--  border-radius: 12px;-->
<!--  font-size: 16px;-->
<!--  font-weight: 600;-->
<!--  cursor: pointer;-->
<!--  transition: all 0.3s ease;-->
<!--  display: flex;-->
<!--  align-items: center;-->
<!--  justify-content: center;-->
<!--  gap: 8px;-->
<!--}-->

<!--.select-btn:hover {-->
<!--  transform: translateY(-2px);-->
<!--  box-shadow: 0 8px 20px rgba(66, 185, 131, 0.3);-->
<!--}-->

<!--.system-info {-->
<!--  display: grid;-->
<!--  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));-->
<!--  gap: 20px;-->
<!--  max-width: 900px;-->
<!--  width: 100%;-->
<!--}-->

<!--.info-card {-->
<!--  background: rgba(255, 255, 255, 0.1);-->
<!--  backdrop-filter: blur(10px);-->
<!--  border: 1px solid rgba(255, 255, 255, 0.2);-->
<!--  border-radius: 16px;-->
<!--  padding: 24px;-->
<!--  text-align: center;-->
<!--  color: white;-->
<!--}-->

<!--.info-card h3 {-->
<!--  font-size: 1.1rem;-->
<!--  margin-bottom: 8px;-->
<!--  display: flex;-->
<!--  align-items: center;-->
<!--  justify-content: center;-->
<!--  gap: 8px;-->
<!--}-->

<!--.info-card p {-->
<!--  font-size: 14px;-->
<!--  opacity: 0.9;-->
<!--  line-height: 1.5;-->
<!--}-->

<!--/* 响应式设计 */-->
<!--@media (max-width: 768px) {-->
<!--  .hero-title {-->
<!--    font-size: 2.5rem;-->
<!--    flex-direction: column;-->
<!--    gap: 16px;-->
<!--  }-->
<!--  -->
<!--  .icon-large {-->
<!--    font-size: 3rem;-->
<!--  }-->
<!--  -->
<!--  .identity-grid {-->
<!--    grid-template-columns: 1fr;-->
<!--    gap: 20px;-->
<!--  }-->
<!--  -->
<!--  .identity-card {-->
<!--    padding: 30px 20px;-->
<!--  }-->
<!--  -->
<!--  .system-info {-->
<!--    grid-template-columns: 1fr;-->
<!--    gap: 16px;-->
<!--  }-->
<!--}-->

<!--@media (max-width: 480px) {-->
<!--  .identity-selection {-->
<!--    padding: 20px 16px;-->
<!--  }-->
<!--  -->
<!--  .hero-title {-->
<!--    font-size: 2rem;-->
<!--  }-->
<!--  -->
<!--  .hero-subtitle {-->
<!--    font-size: 1.1rem;-->
<!--  }-->
<!--}-->
<!--</style>-->

<template>
  <div class="home-container">
    <!-- 左侧身份选择区 -->
    <div class="identity-panel">
      <div class="panel-header">
        <h1 class="system-title">
          <i class="fas fa-car"></i>
          汽车维修管理系统
        </h1>
        <p class="welcome-text">请选择您的身份开始使用系统</p>
      </div>

      <div class="identity-list">
        <div
            v-for="role in roles"
            :key="role.key"
            class="identity-item"
            :class="{ 'active': selectedRole === role.key }"
            @click="selectRole(role.key)"
        >
          <div class="item-icon">
            <i :class="role.icon"></i>
          </div>
          <div class="item-info">
            <h3 class="item-title">{{ role.title }}</h3>
            <p class="item-desc">{{ role.description }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 右侧登录/注册区 -->
    <div class="auth-panel">
      <AuthForm :role="selectedRole" @auth-success="handleAuthSuccess"/>
    </div>
  </div>
</template>

<script>
import AuthForm from './AuthForm.vue';

export default {
  name: 'IdentitySelection',
  components: { AuthForm },
  data() {
    return {
      selectedRole: 'customer',
      roles: [
        {
          key: 'customer',
          title: '顾客',
          icon: 'fas fa-user',
          description: '车主用户，管理车辆和维修服务'
        },
        {
          key: 'technician',
          title: '技师',
          icon: 'fas fa-tools',
          description: '专业技师，负责车辆维修工作'
        },
        {
          key: 'admin',
          title: '管理员',
          icon: 'fas fa-user-cog',
          description: '系统管理员，负责系统运营管理'
        }
      ]
    }
  },
  methods: {
    selectRole(role) {
      this.selectedRole = role;
    },
    handleAuthSuccess(userData) {
      // 处理认证成功逻辑
      console.log('认证成功:', userData);
      // 根据用户角色跳转到不同页面
      const roleRoutes = {
        'customer': '/customer',
        'technician': '/technician',
        'admin': '/admin'
      };
      this.$router.push(roleRoutes[userData.role] || '/');
    }
  }
}
</script>

<style scoped>
.home-container {
  display: flex;
  min-height: 100vh;
  background: #f8f9fa;
}

.identity-panel {
  width: 35%;
  background: linear-gradient(135deg, #2c3e50 0%, #1a2530 100%);
  color: white;
  padding: 40px 30px;
  display: flex;
  flex-direction: column;
}

.auth-panel {
  width: 65%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: #f8f9fa;
}

.panel-header {
  margin-bottom: 50px;
  padding-bottom: 30px;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}

.system-title {
  font-size: 2rem;
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 15px;
}

.system-title i {
  color: #42b983;
}

.welcome-text {
  font-size: 1.1rem;
  opacity: 0.8;
  margin: 0;
}

.identity-list {
  flex: 1;
}

.identity-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px;
  border-radius: 15px;
  margin-bottom: 15px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: rgba(255,255,255,0.05);
  border: 1px solid transparent;
}

.identity-item:hover {
  background: rgba(255,255,255,0.1);
  border-color: rgba(255,255,255,0.2);
}

.identity-item.active {
  background: rgba(66, 185, 131, 0.15);
  border-color: #42b983;
}

.item-icon {
  width: 60px;
  height: 60px;
  background: rgba(255,255,255,0.1);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  color: #42b983;
}

.identity-item.active .item-icon {
  background: rgba(66, 185, 131, 0.2);
  color: white;
}

.item-info {
  flex: 1;
}

.item-title {
  margin: 0 0 5px 0;
  font-size: 1.2rem;
}

.item-desc {
  margin: 0;
  font-size: 0.9rem;
  opacity: 0.7;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .home-container {
    flex-direction: column;
  }

  .identity-panel, .auth-panel {
    width: 100%;
  }

  .identity-panel {
    padding: 30px 20px;
  }

  .identity-list {
    display: flex;
    flex-wrap: wrap;
    gap: 15px;
  }

  .identity-item {
    flex: 1;
    min-width: 200px;
  }
}

@media (max-width: 576px) {
  .identity-item {
    min-width: 100%;
  }

  .panel-header {
    margin-bottom: 30px;
  }

  .system-title {
    font-size: 1.6rem;
  }
}
</style>