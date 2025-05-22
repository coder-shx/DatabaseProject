<template>
  <div class="register-container">
    <el-card class="register-card">
      <div class="card-header">
        <h2>汽车维修管理系统</h2>
        <h3>用户注册</h3>
      </div>
      <el-form :model="registerForm" :rules="rules" ref="registerFormRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="registerForm.username" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请再次输入密码"></el-input>
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="registerForm.name" placeholder="请输入您的姓名"></el-input>
        </el-form-item>
        <el-form-item label="手机号码" prop="phoneNumber">
          <el-input v-model="registerForm.phoneNumber" placeholder="请输入手机号码"></el-input>
        </el-form-item>
        <el-form-item label="电子邮箱" prop="email">
          <el-input v-model="registerForm.email" placeholder="请输入电子邮箱"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleRegister">注册</el-button>
          <el-button @click="$router.push('/login')">返回登录</el-button>
        </el-form-item>
      </el-form>
      <el-alert v-if="error" :title="error" type="error" :closable="false"></el-alert>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, computed } from 'vue';
import { useStore } from 'vuex';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';

export default {
  name: 'Register',
  setup() {
    const store = useStore();
    const router = useRouter();
    const registerFormRef = ref(null);

    // 表单绑定数据
    const registerForm = reactive({
      username: '',
      password: '',
      confirmPassword: '',
      name: '',
      phoneNumber: '',
      email: '',
      role: 'CUSTOMER' // 默认注册为普通用户角色
    });

    // 自定义校验密码一致性的验证器
    const validatePass = (rule, value, callback) => {
      if (value !== registerForm.password) {
        callback(new Error('两次输入的密码不一致'));
      } else {
        callback();
      }
    };

    // 表单验证规则
    const rules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '用户名长度在3到20个字符之间', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度在6到20个字符之间', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请再次输入密码', trigger: 'blur' },
        { validator: validatePass, trigger: 'blur' }
      ],
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
      ]
    };

    // 从store获取状态
    const loading = computed(() => store.state.loading);
    const error = computed(() => store.state.error);

    // 注册表单提交
    const handleRegister = async () => {
      if (!registerFormRef.value) return;
      
      try {
        await registerFormRef.value.validate();
        
        // 提交注册信息
        const userData = {
          username: registerForm.username,
          password: registerForm.password,
          name: registerForm.name,
          phoneNumber: registerForm.phoneNumber,
          email: registerForm.email,
          role: registerForm.role
        };

        await store.dispatch('register', userData);
        
        ElMessage.success('注册成功，请登录');
        router.push('/login');
      } catch (error) {
        // 注册失败，错误已在store中设置
        console.error('Registration failed:', error);
      }
    };

    return {
      registerForm,
      registerFormRef,
      rules,
      loading,
      error,
      handleRegister
    };
  }
};
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 20px 0;
  background-color: #f5f7fa;
}

.register-card {
  width: 500px;
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