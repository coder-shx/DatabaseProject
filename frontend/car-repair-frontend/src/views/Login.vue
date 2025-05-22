<template>
  <div class="login-container">
    <el-card class="login-card">
      <div class="card-header">
        <h2>汽车维修管理系统</h2>
        <h3>用户登录</h3>
      </div>
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleLogin">登录</el-button>
          <el-button @click="$router.push('/register')">注册</el-button>
        </el-form-item>
      </el-form>
      <el-alert v-if="error" :title="error" type="error" :closable="false"></el-alert>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue';
import { useStore } from 'vuex';
import { useRouter, useRoute } from 'vue-router';
import { ElMessage } from 'element-plus';

export default {
  name: 'Login',
  setup() {
    const store = useStore();
    const router = useRouter();
    const route = useRoute();
    const loginFormRef = ref(null);

    // 表单绑定数据
    const loginForm = reactive({
      username: '',
      password: ''
    });

    // 表单验证规则
    const rules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '用户名长度在3到20个字符之间', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度在6到20个字符之间', trigger: 'blur' }
      ]
    };

    // 从store获取状态
    const loading = computed(() => store.state.loading);
    const error = computed(() => store.state.error);

    // 登录表单提交
    const handleLogin = async () => {
      if (!loginFormRef.value) return;
      
      try {
        await loginFormRef.value.validate();
        
        await store.dispatch('login', loginForm);
        
        ElMessage.success('登录成功');
        
        // 如果有重定向路径，则重定向到该路径，否则进入用户首页
        const redirectPath = route.query.redirect || '/dashboard';
        router.push(redirectPath);
      } catch (error) {
        // 登录失败，错误已在store中设置
        console.error('Login failed:', error);
      }
    };

    // 如果用户已登录，直接跳转到首页
    onMounted(() => {
      if (store.getters.isAuthenticated) {
        router.push('/dashboard');
      }
    });

    return {
      loginForm,
      loginFormRef,
      rules,
      loading,
      error,
      handleLogin
    };
  }
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f7fa;
}

.login-card {
  width: 400px;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  text-align: center;
  margin-bottom: 30px;
}

.card-header h2 {
  margin-bottom: 10px;
  color: #409EFF;
}

.card-header h3 {
  font-weight: normal;
  color: #606266;
}

.el-form-item:last-child {
  margin-bottom: 0;
  text-align: center;
}

.el-button {
  width: 120px;
  margin: 0 10px;
}

.el-alert {
  margin-top: 20px;
}
</style> 