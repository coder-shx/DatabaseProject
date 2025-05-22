<template>
  <div class="admin-users-container">
    <el-card class="admin-card">
      <template #header>
        <div class="card-header">
          <h2>用户管理</h2>
          <div class="header-actions">
            <el-input
              v-model="searchQuery"
              placeholder="搜索用户"
              clearable
              class="search-input"
              @clear="handleClear">
              <template #prefix>
                <el-icon><search /></el-icon>
              </template>
            </el-input>
            <el-button type="primary" @click="showAddUserDialog">添加用户</el-button>
          </div>
        </div>
      </template>
      
      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="5" animated />
      </div>
      
      <div v-else>
        <el-table
          v-if="filteredUsers.length > 0"
          :data="paginatedUsers"
          style="width: 100%"
          border>
          <el-table-column prop="id" label="ID" width="80" sortable></el-table-column>
          <el-table-column prop="username" label="用户名" sortable></el-table-column>
          <el-table-column prop="name" label="姓名"></el-table-column>
          <el-table-column prop="phoneNumber" label="电话"></el-table-column>
          <el-table-column prop="email" label="邮箱" show-overflow-tooltip></el-table-column>
          <el-table-column prop="role" label="角色" width="120">
            <template #default="scope">
              <el-tag :type="getRoleType(scope.row.role)">
                {{ getRoleText(scope.row.role) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button link type="primary" @click="handleEditUser(scope.row)">编辑</el-button>
              <el-button link type="primary" @click="viewUserVehicles(scope.row)">车辆</el-button>
              <el-button link type="primary" @click="viewUserOrders(scope.row)">工单</el-button>
              <el-button 
                v-if="scope.row.role !== 'ADMIN'"
                link 
                type="danger" 
                @click="handleDeleteUser(scope.row)">
                删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div v-else class="empty-data">
          <el-empty description="暂无用户数据" :image-size="200"></el-empty>
        </div>
        
        <div class="pagination-container">
          <el-pagination
            v-model:currentPage="currentPage"
            :page-size="pageSize"
            :total="filteredUsers.length"
            layout="total, prev, pager, next"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>
    
    <!-- 添加/编辑用户对话框 -->
    <el-dialog
      v-model="userDialogVisible"
      :title="isEdit ? '编辑用户' : '添加用户'"
      width="600px"
      :close-on-click-modal="false">
      <el-form :model="userForm" :rules="rules" ref="userFormRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" :disabled="isEdit" placeholder="请输入用户名"></el-input>
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="userForm.name" placeholder="请输入姓名"></el-input>
        </el-form-item>
        <el-form-item label="手机号码" prop="phoneNumber">
          <el-input v-model="userForm.phoneNumber" placeholder="请输入手机号码"></el-input>
        </el-form-item>
        <el-form-item label="电子邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入电子邮箱"></el-input>
        </el-form-item>
        <el-form-item label="用户角色" prop="role">
          <el-select v-model="userForm.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="普通用户" value="CUSTOMER"></el-option>
            <el-option label="维修人员" value="MECHANIC"></el-option>
            <el-option label="系统管理员" value="ADMIN"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码" show-password></el-input>
        </el-form-item>
        <el-form-item v-if="!isEdit" label="确认密码" prop="confirmPassword">
          <el-input v-model="userForm.confirmPassword" type="password" placeholder="请再次输入密码" show-password></el-input>
        </el-form-item>
        <el-form-item v-if="isEdit" label="密码">
          <el-button type="primary" plain @click="showResetPasswordDialog">重置密码</el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="userDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="saveUser">保存</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 重置密码对话框 -->
    <el-dialog
      v-model="resetPasswordDialogVisible"
      title="重置密码"
      width="500px"
      :close-on-click-modal="false">
      <el-form :model="resetPasswordForm" :rules="resetPasswordRules" ref="resetPasswordFormRef" label-width="100px">
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="resetPasswordForm.newPassword" type="password" placeholder="请输入新密码" show-password></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="resetPasswordForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="resetPasswordDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="savingPassword" @click="resetPassword">确定</el-button>
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
import { Search } from '@element-plus/icons-vue';
import { adminApi } from '@/api/api';

export default {
  name: 'AdminUsersView',
  components: {
    Search
  },
  setup() {
    const store = useStore();
    const router = useRouter();
    const userFormRef = ref(null);
    const resetPasswordFormRef = ref(null);
    
    // 状态变量
    const loading = ref(true);
    const saving = ref(false);
    const savingPassword = ref(false);
    const userDialogVisible = ref(false);
    const resetPasswordDialogVisible = ref(false);
    const isEdit = ref(false);
    const currentUserId = ref(null);
    
    // 分页和搜索
    const currentPage = ref(1);
    const pageSize = ref(10);
    const searchQuery = ref('');
    
    // 用户数据
    const users = ref([]);
    
    // 过滤用户
    const filteredUsers = computed(() => {
      if (!searchQuery.value) {
        return users.value;
      }
      
      const query = searchQuery.value.toLowerCase();
      return users.value.filter(user => {
        return user.username.toLowerCase().includes(query) ||
               user.name.toLowerCase().includes(query) ||
               user.email.toLowerCase().includes(query);
      });
    });
    
    // 分页用户
    const paginatedUsers = computed(() => {
      const start = (currentPage.value - 1) * pageSize.value;
      const end = start + pageSize.value;
      return filteredUsers.value.slice(start, end);
    });
    
    // 用户表单
    const userForm = reactive({
      id: '',
      username: '',
      name: '',
      phoneNumber: '',
      email: '',
      role: 'CUSTOMER',
      password: '',
      confirmPassword: ''
    });
    
    // 自定义校验密码一致性的验证器
    const validatePass = (rule, value, callback) => {
      if (userForm.password && value !== userForm.password) {
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
      role: [
        { required: true, message: '请选择用户角色', trigger: 'change' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度在6到20个字符之间', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请再次输入密码', trigger: 'blur' },
        { validator: validatePass, trigger: 'blur' }
      ]
    };
    
    // 重置密码表单
    const resetPasswordForm = reactive({
      newPassword: '',
      confirmPassword: ''
    });
    
    // 自定义校验重置密码一致性的验证器
    const validateResetPass = (rule, value, callback) => {
      if (resetPasswordForm.newPassword && value !== resetPasswordForm.newPassword) {
        callback(new Error('两次输入的密码不一致'));
      } else {
        callback();
      }
    };
    
    // 重置密码表单验证规则
    const resetPasswordRules = {
      newPassword: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度在6到20个字符之间', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请再次输入新密码', trigger: 'blur' },
        { validator: validateResetPass, trigger: 'blur' }
      ]
    };
    
    // 获取用户列表
    const fetchUsers = async () => {
      try {
        loading.value = true;
        const response = await adminApi.getAllUsers();
        users.value = response;
      } catch (error) {
        console.error('Failed to fetch users:', error);
        ElMessage.error('获取用户列表失败');
      } finally {
        loading.value = false;
      }
    };
    
    // 显示添加用户对话框
    const showAddUserDialog = () => {
      isEdit.value = false;
      currentUserId.value = null;
      resetUserForm();
      userDialogVisible.value = true;
    };
    
    // 显示编辑用户对话框
    const handleEditUser = (user) => {
      isEdit.value = true;
      currentUserId.value = user.id;
      
      // 填充表单数据
      Object.assign(userForm, {
        id: user.id,
        username: user.username,
        name: user.name,
        phoneNumber: user.phoneNumber,
        email: user.email,
        role: user.role
      });
      
      userDialogVisible.value = true;
    };
    
    // 保存用户
    const saveUser = async () => {
      if (!userFormRef.value) return;
      
      try {
        await userFormRef.value.validate();
        saving.value = true;
        
        const userData = {
          username: userForm.username,
          name: userForm.name,
          phoneNumber: userForm.phoneNumber,
          email: userForm.email,
          role: userForm.role
        };
        
        if (!isEdit.value) {
          // 添加用户
          userData.password = userForm.password;
          // 这里应该调用添加用户的API
          // await adminApi.createUser(userData);
          // 由于暂时没有实现，我们模拟添加
          const newUser = {
            id: users.value.length + 1,
            ...userData
          };
          users.value.unshift(newUser);
          ElMessage.success('用户已添加');
        } else {
          // 更新用户
          userData.id = currentUserId.value;
          // 这里应该调用更新用户的API
          // await adminApi.updateUser(userData);
          // 由于暂时没有实现，我们模拟更新
          const index = users.value.findIndex(u => u.id === currentUserId.value);
          if (index !== -1) {
            users.value[index] = { ...users.value[index], ...userData };
          }
          ElMessage.success('用户信息已更新');
        }
        
        userDialogVisible.value = false;
      } catch (error) {
        console.error('Failed to save user:', error);
        ElMessage.error('保存用户信息失败');
      } finally {
        saving.value = false;
      }
    };
    
    // 删除用户
    const handleDeleteUser = (user) => {
      ElMessageBox.confirm(
        `确定要删除用户 ${user.name} (${user.username}) 吗？`,
        '警告',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        }
      ).then(async () => {
        try {
          // 这里应该调用删除用户的API
          // await adminApi.deleteUser(user.id);
          // 由于暂时没有实现，我们模拟删除
          users.value = users.value.filter(u => u.id !== user.id);
          ElMessage.success('用户已删除');
        } catch (error) {
          console.error('Failed to delete user:', error);
          ElMessage.error('删除用户失败');
        }
      }).catch(() => {});
    };
    
    // 显示重置密码对话框
    const showResetPasswordDialog = () => {
      resetPasswordForm.newPassword = '';
      resetPasswordForm.confirmPassword = '';
      
      if (resetPasswordFormRef.value) {
        resetPasswordFormRef.value.resetFields();
      }
      
      resetPasswordDialogVisible.value = true;
    };
    
    // 重置密码
    const resetPassword = async () => {
      if (!resetPasswordFormRef.value) return;
      
      try {
        await resetPasswordFormRef.value.validate();
        savingPassword.value = true;
        
        // 这里应该调用重置密码的API
        // await adminApi.resetUserPassword(currentUserId.value, resetPasswordForm.newPassword);
        // 由于暂时没有实现，我们仅显示成功消息
        
        ElMessage.success('密码已重置');
        resetPasswordDialogVisible.value = false;
      } catch (error) {
        console.error('Failed to reset password:', error);
        ElMessage.error('重置密码失败');
      } finally {
        savingPassword.value = false;
      }
    };
    
    // 查看用户车辆
    const viewUserVehicles = (user) => {
      // 这里应该导航到用户车辆管理页面
      // 由于暂时没有实现，我们简单提示
      ElMessage.info(`查看用户 ${user.name} 的车辆`);
    };
    
    // 查看用户工单
    const viewUserOrders = (user) => {
      // 这里应该导航到用户工单管理页面
      // 由于暂时没有实现，我们简单提示
      ElMessage.info(`查看用户 ${user.name} 的工单`);
    };
    
    // 处理分页
    const handleCurrentChange = (page) => {
      currentPage.value = page;
    };
    
    // 清除搜索
    const handleClear = () => {
      searchQuery.value = '';
    };
    
    // 重置用户表单
    const resetUserForm = () => {
      userForm.id = '';
      userForm.username = '';
      userForm.name = '';
      userForm.phoneNumber = '';
      userForm.email = '';
      userForm.role = 'CUSTOMER';
      userForm.password = '';
      userForm.confirmPassword = '';
      
      if (userFormRef.value) {
        userFormRef.value.resetFields();
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
    
    onMounted(async () => {
      await fetchUsers();
      
      // 模拟一些用户数据，因为API可能还没实现
      if (users.value.length === 0) {
        users.value = [
          {
            id: 1,
            username: 'admin',
            name: '系统管理员',
            phoneNumber: '13800138000',
            email: 'admin@example.com',
            role: 'ADMIN'
          },
          {
            id: 2,
            username: 'mechanic1',
            name: '张技工',
            phoneNumber: '13900139000',
            email: 'mechanic1@example.com',
            role: 'MECHANIC'
          },
          {
            id: 3,
            username: 'customer1',
            name: '李明',
            phoneNumber: '13700137000',
            email: 'customer1@example.com',
            role: 'CUSTOMER'
          },
          {
            id: 4,
            username: 'customer2',
            name: '王芳',
            phoneNumber: '13600136000',
            email: 'customer2@example.com',
            role: 'CUSTOMER'
          }
        ];
      }
    });
    
    return {
      loading,
      saving,
      savingPassword,
      users,
      filteredUsers,
      paginatedUsers,
      currentPage,
      pageSize,
      searchQuery,
      userDialogVisible,
      resetPasswordDialogVisible,
      userForm,
      userFormRef,
      rules,
      resetPasswordForm,
      resetPasswordFormRef,
      resetPasswordRules,
      isEdit,
      showAddUserDialog,
      handleEditUser,
      saveUser,
      handleDeleteUser,
      showResetPasswordDialog,
      resetPassword,
      viewUserVehicles,
      viewUserOrders,
      handleCurrentChange,
      handleClear,
      getRoleType,
      getRoleText
    };
  }
};
</script>

<style scoped>
.admin-users-container {
  padding: 20px;
}

.admin-card {
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

.header-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}

.search-input {
  width: 250px;
}

.loading-container {
  padding: 20px 0;
}

.empty-data {
  padding: 40px 0;
  display: flex;
  justify-content: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style> 