<template>
  <div class="home-container">
    <!-- 左侧身份选择区 -->
    <div class="identity-panel">
      <div class="panel-header">
        <h1 class="system-title">
          汽车维修管理系统
        </h1>
       </div>

      <div class="identity-list">
        <div
            v-for="role in roles"
            :key="role.key"
            class="identity-item"
            :class="{ 'active': selectedRole === role.key }"
            @click="selectRole(role.key)"
        ><div class="item-info">
          <h3 class="item-title">{{ role.title }}</h3>
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
        },
        {
          key: 'technician',
          title: '技师',
        },
        {
          key: 'admin',
          title: '系统管理员',
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
  width: 40%;
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