<template>
  <div class="home-container">
    <div class="header">
      <div class="logo-container">
        <h1 class="logo">汽车维修管理系统</h1>
      </div>
      <div class="nav-buttons">
        <el-button type="primary" @click="$router.push('/login')">登录</el-button>
        <el-button @click="$router.push('/register')">注册</el-button>
      </div>
    </div>
    
    <div class="hero-section">
      <div class="hero-content">
        <h2>高效、便捷的汽车维修服务平台</h2>
        <p>为用户和技师提供全流程的汽车维修管理解决方案</p>
        <el-button type="primary" size="large" @click="$router.push('/register')">立即注册</el-button>
      </div>
    </div>
    
    <div class="features-section">
      <h2>系统特色</h2>
      <div class="features-container">
        <div class="feature-card">
          <el-icon><Calendar /></el-icon>
          <h3>在线预约</h3>
          <p>随时随地提交维修需求，无需排队等待</p>
        </div>
        <div class="feature-card">
          <el-icon><Monitor /></el-icon>
          <h3>实时跟踪</h3>
          <p>全程监控维修进度，透明的服务流程</p>
        </div>
        <div class="feature-card">
          <el-icon><User /></el-icon>
          <h3>专业技师</h3>
          <p>根据维修需求，智能匹配最适合的专业技师</p>
        </div>
        <div class="feature-card">
          <el-icon><Document /></el-icon>
          <h3>维修记录</h3>
          <p>完整保存维修历史，方便查询和跟踪</p>
        </div>
      </div>
    </div>
    
    <div class="workflow-section">
      <h2>维修流程</h2>
      <el-steps :active="1" finish-status="success" simple>
        <el-step title="提交申请" description="描述车辆故障情况"></el-step>
        <el-step title="技师分配" description="系统匹配专业技师"></el-step>
        <el-step title="维修进行" description="技师记录维修过程"></el-step>
        <el-step title="维修完成" description="用户确认并评价"></el-step>
      </el-steps>
    </div>
    
    <div class="footer">
      <p>© 2023 汽车维修管理系统 - 为您提供专业的维修服务</p>
    </div>
  </div>
</template>

<script>
import { Calendar, Monitor, User, Document } from '@element-plus/icons-vue';

export default {
  name: 'Home',
  components: {
    Calendar,
    Monitor,
    User,
    Document
  },
  setup() {
    return {};
  }
};
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 5%;
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.logo {
  color: #409EFF;
  margin: 0;
}

.nav-buttons .el-button {
  margin-left: 10px;
}

.hero-section {
  height: 500px;
  background-image: linear-gradient(rgba(0, 0, 0, 0.5), rgba(0, 0, 0, 0.5)), url('https://images.unsplash.com/photo-1486262322291-6f4dbcd69bb9?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=1920&q=80');
  background-size: cover;
  background-position: center;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.hero-content {
  max-width: 800px;
  padding: 0 20px;
}

.hero-content h2 {
  font-size: 2.5rem;
  margin-bottom: 20px;
}

.hero-content p {
  font-size: 1.2rem;
  margin-bottom: 30px;
}

.features-section {
  padding: 60px 5%;
  text-align: center;
  background-color: #f5f7fa;
}

.features-section h2 {
  margin-bottom: 40px;
  color: #303133;
}

.features-container {
  display: flex;
  justify-content: space-around;
  flex-wrap: wrap;
  gap: 20px;
}

.feature-card {
  background: #fff;
  padding: 30px 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  width: 220px;
  transition: transform 0.3s ease;
}

.feature-card:hover {
  transform: translateY(-5px);
}

.feature-card .el-icon {
  font-size: 36px;
  color: #409EFF;
  margin-bottom: 15px;
}

.feature-card h3 {
  margin-bottom: 10px;
  color: #303133;
}

.feature-card p {
  color: #606266;
}

.workflow-section {
  padding: 60px 5%;
  text-align: center;
}

.workflow-section h2 {
  margin-bottom: 40px;
  color: #303133;
}

.footer {
  background-color: #303133;
  color: #fff;
  text-align: center;
  padding: 20px 0;
  margin-top: auto;
}
</style> 