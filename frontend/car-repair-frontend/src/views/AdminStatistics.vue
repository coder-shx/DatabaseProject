<template>
  <div class="admin-statistics-container">
    <el-card class="admin-card">
      <template #header>
        <div class="card-header">
          <h2>统计分析</h2>
          <div class="header-actions">
            <el-select v-model="selectedPeriod" placeholder="选择时间段">
              <el-option label="最近7天" value="week"></el-option>
              <el-option label="最近30天" value="month"></el-option>
              <el-option label="最近90天" value="quarter"></el-option>
              <el-option label="最近一年" value="year"></el-option>
            </el-select>
            <el-button type="primary">导出报表</el-button>
          </div>
        </div>
      </template>
      
      <div class="placeholder-message">
        <el-empty description="统计分析页面正在开发中" :image-size="200">
          <template #description>
            <p>该功能将在下一个版本中推出</p>
          </template>
        </el-empty>
      </div>
    </el-card>
  </div>
</template>

<script>
import { ref } from 'vue';

export default {
  name: 'AdminStatisticsView',
  setup() {
    const selectedPeriod = ref('month');
    
    return {
      selectedPeriod
    };
  }
};
</script>

<style scoped>
.admin-statistics-container {
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

.placeholder-message {
  padding: 40px 0;
  display: flex;
  justify-content: center;
}
</style> 