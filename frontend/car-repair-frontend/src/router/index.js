import { createRouter, createWebHistory } from 'vue-router';
import store from '@/store';

// 页面组件
const Home = () => import('@/views/Home.vue');
const Login = () => import('@/views/Login.vue');
const Register = () => import('@/views/Register.vue');
const Dashboard = () => import('@/views/Dashboard.vue');
const UserProfile = () => import('@/views/UserProfile.vue');
const VehicleList = () => import('@/views/VehicleList.vue');
const VehicleDetail = () => import('@/views/VehicleDetail.vue');
const RepairOrderList = () => import('@/views/RepairOrderList.vue');
const RepairOrderDetail = () => import('@/views/RepairOrderDetail.vue');
const CreateRepairOrder = () => import('@/views/CreateRepairOrder.vue');
const AdminUsers = () => import('@/views/AdminUsers.vue');
const AdminMechanics = () => import('@/views/AdminMechanics.vue');
const AdminVehicles = () => import('@/views/AdminVehicles.vue');
const AdminOrders = () => import('@/views/AdminOrders.vue');
const AdminStatistics = () => import('@/views/AdminStatistics.vue');
const NotFound = () => import('@/views/NotFound.vue');

// 路由配置
const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home,
    meta: { requiresAuth: false }
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: { requiresAuth: false }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: Dashboard,
    meta: { requiresAuth: true }
  },
  {
    path: '/profile',
    name: 'UserProfile',
    component: UserProfile,
    meta: { requiresAuth: true }
  },
  {
    path: '/vehicles',
    name: 'VehicleList',
    component: VehicleList,
    meta: { requiresAuth: true, roles: ['CUSTOMER', 'ADMIN'] }
  },
  {
    path: '/vehicles/:id',
    name: 'VehicleDetail',
    component: VehicleDetail,
    meta: { requiresAuth: true, roles: ['CUSTOMER', 'ADMIN'] }
  },
  {
    path: '/repair-orders',
    name: 'RepairOrderList',
    component: RepairOrderList,
    meta: { requiresAuth: true }
  },
  {
    path: '/repair-orders/:id',
    name: 'RepairOrderDetail',
    component: RepairOrderDetail,
    meta: { requiresAuth: true }
  },
  {
    path: '/create-repair-order',
    name: 'CreateRepairOrder',
    component: CreateRepairOrder,
    meta: { requiresAuth: true, roles: ['CUSTOMER'] }
  },
  {
    path: '/admin/users',
    name: 'AdminUsers',
    component: AdminUsers,
    meta: { requiresAuth: true, roles: ['ADMIN'] }
  },
  {
    path: '/admin/mechanics',
    name: 'AdminMechanics',
    component: AdminMechanics,
    meta: { requiresAuth: true, roles: ['ADMIN'] }
  },
  {
    path: '/admin/vehicles',
    name: 'AdminVehicles',
    component: AdminVehicles,
    meta: { requiresAuth: true, roles: ['ADMIN'] }
  },
  {
    path: '/admin/orders',
    name: 'AdminOrders',
    component: AdminOrders,
    meta: { requiresAuth: true, roles: ['ADMIN'] }
  },
  {
    path: '/admin/statistics',
    name: 'AdminStatistics',
    component: AdminStatistics,
    meta: { requiresAuth: true, roles: ['ADMIN'] }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: NotFound,
    meta: { requiresAuth: false }
  }
];

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
});

// 导航守卫
router.beforeEach((to, from, next) => {
  const isAuthenticated = store.getters.isAuthenticated;
  const userRole = store.getters.userRole;
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth);
  const requiredRoles = to.matched.find(record => record.meta.roles)?.meta.roles;

  // 如果路由需要认证但用户未登录，重定向到登录页
  if (requiresAuth && !isAuthenticated) {
    next({ name: 'Login', query: { redirect: to.fullPath } });
    return;
  }

  // 如果路由需要特定角色但用户不具备该角色，重定向到首页
  if (requiredRoles && !requiredRoles.includes(userRole)) {
    next({ name: 'Dashboard' });
    return;
  }

  // 如果用户已登录且尝试访问登录/注册页，重定向到首页
  if (isAuthenticated && (to.name === 'Login' || to.name === 'Register')) {
    next({ name: 'Dashboard' });
    return;
  }

  next();
});

export default router; 