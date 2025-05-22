import axios from 'axios';

// 创建axios实例
const api = axios.create({
  baseURL: 'http://localhost:8080', // 后端API地址
  timeout: 10000, // 请求超时时间
  headers: {
    'Content-Type': 'application/json'
  }
});

// 请求拦截器
api.interceptors.request.use(
  config => {
    // 从localStorage获取token
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// 响应拦截器
api.interceptors.response.use(
  response => {
    return response.data;
  },
  error => {
    if (error.response && error.response.status === 401) {
      // 如果返回401未授权，清除token并重定向到登录页
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// 用户API
export const userApi = {
  // 用户注册
  register: (userData) => api.post('/api/users/register', userData),
  // 用户登录
  login: (credentials) => api.post('/api/users/login', credentials),
  // 获取用户信息
  getUserInfo: () => api.get('/api/users/info'),
  // 更新用户信息
  updateUser: (userData) => api.put('/api/users/update', userData),
  // 获取用户车辆列表
  getUserVehicles: () => api.get('/api/users/vehicles'),
  // 添加车辆
  addVehicle: (vehicleData) => api.post('/api/users/vehicles', vehicleData),
};

// 车辆API
export const vehicleApi = {
  // 获取车辆详情
  getVehicle: (id) => api.get(`/api/vehicles/${id}`),
  // 更新车辆信息
  updateVehicle: (id, vehicleData) => api.put(`/api/vehicles/${id}`, vehicleData),
  // 删除车辆
  deleteVehicle: (id) => api.delete(`/api/vehicles/${id}`),
};

// 维修工单API
export const repairOrderApi = {
  // 创建维修工单
  createOrder: (orderData) => api.post('/api/repair-orders', orderData),
  // 获取工单详情
  getOrder: (id) => api.get(`/api/repair-orders/${id}`),
  // 获取用户的工单列表
  getUserOrders: () => api.get('/api/repair-orders/user'),
  // 获取技工的工单列表
  getMechanicOrders: () => api.get('/api/repair-orders/mechanic'),
  // 更新工单状态
  updateOrderStatus: (id, status) => api.put(`/api/repair-orders/${id}/status`, { status }),
  // 为工单分配技工
  assignMechanic: (orderId, mechanicId) => api.post(`/api/repair-orders/${orderId}/mechanics/${mechanicId}`),
  // 从工单移除技工
  removeMechanic: (orderId, mechanicId) => api.delete(`/api/repair-orders/${orderId}/mechanics/${mechanicId}`),
  // 添加材料到工单
  addMaterial: (orderId, materialData) => api.post(`/api/repair-orders/${orderId}/materials`, materialData),
  // 获取工单的材料列表
  getOrderMaterials: (orderId) => api.get(`/api/repair-orders/${orderId}/materials`),
  // 完成工单
  completeOrder: (orderId) => api.put(`/api/repair-orders/${orderId}/complete`),
  // 添加客户反馈
  addFeedback: (orderId, feedback) => api.post(`/api/repair-orders/${orderId}/feedback`, feedback),
};

// 技工API
export const mechanicApi = {
  // 获取技工详情
  getMechanic: (id) => api.get(`/api/mechanics/${id}`),
  // 更新技工信息
  updateMechanic: (id, mechanicData) => api.put(`/api/mechanics/${id}`, mechanicData),
  // 获取特定类型的技工列表
  getMechanicsByType: (type) => api.get(`/api/mechanics/type/${type}`),
  // 获取可用技工
  getAvailableMechanics: (type) => api.get(`/api/mechanics/available/${type}`),
};

// 管理员API
export const adminApi = {
  // 获取所有用户
  getAllUsers: () => api.get('/api/admin/users'),
  // 获取所有技工
  getAllMechanics: () => api.get('/api/admin/mechanics'),
  // 获取所有车辆
  getAllVehicles: () => api.get('/api/admin/vehicles'),
  // 获取所有维修工单
  getAllOrders: () => api.get('/api/admin/repair-orders'),
  // 统计数据
  getVehicleRepairStats: () => api.get('/api/admin/stats/vehicle-repairs'),
  getMonthlyCostBreakdown: () => api.get('/api/admin/stats/monthly-costs'),
  getUncompletedOrders: () => api.get('/api/admin/stats/uncompleted-orders'),
  getNegativeFeedbackOrders: () => api.get('/api/admin/stats/negative-feedback'),
  getRepairsByMechanicType: () => api.get('/api/admin/stats/repairs-by-mechanic-type'),
};

export default api; 