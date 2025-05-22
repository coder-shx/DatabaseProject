<template>
  <div class="admin-mechanics-container">
    <el-card class="admin-card">
      <template #header>
        <div class="card-header">
          <h2>技工管理</h2>
          <el-button type="primary">添加技工</el-button>
        </div>
      </template>
      
      <div class="placeholder-message">
        <el-empty description="技工管理页面正在开发中" :image-size="200">
          <template #description>
            <p>该功能将在下一个版本中推出</p>
          </template>
        </el-empty>
      </div>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'AdminMechanicsView',
  setup() {
    return {};
  }
};
</script>

<style scoped>
.admin-mechanics-container {
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

.placeholder-message {
  padding: 40px 0;
  display: flex;
  justify-content: center;
}
</style> 