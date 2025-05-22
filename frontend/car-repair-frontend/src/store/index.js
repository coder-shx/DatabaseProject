import { createStore } from 'vuex';
import { userApi, vehicleApi, repairOrderApi } from '@/api/api';

export default createStore({
  state: {
    user: null,
    token: localStorage.getItem('token') || '',
    vehicles: [],
    repairOrders: [],
    currentOrder: null,
    loading: false,
    error: null,
    role: localStorage.getItem('role') || '',
  },
  getters: {
    isAuthenticated: state => !!state.token,
    isAdmin: state => state.role === 'ADMIN',
    isMechanic: state => state.role === 'MECHANIC',
    isCustomer: state => state.role === 'CUSTOMER',
    userRole: state => state.role,
    currentUser: state => state.user,
    userVehicles: state => state.vehicles,
    userRepairOrders: state => state.repairOrders,
    currentRepairOrder: state => state.currentOrder,
    isLoading: state => state.loading,
    hasError: state => state.error !== null,
    errorMessage: state => state.error,
  },
  mutations: {
    SET_TOKEN(state, token) {
      state.token = token;
    },
    SET_USER(state, user) {
      state.user = user;
    },
    SET_ROLE(state, role) {
      state.role = role;
    },
    SET_LOADING(state, status) {
      state.loading = status;
    },
    SET_ERROR(state, error) {
      state.error = error;
    },
    CLEAR_ERROR(state) {
      state.error = null;
    },
    SET_VEHICLES(state, vehicles) {
      state.vehicles = vehicles;
    },
    ADD_VEHICLE(state, vehicle) {
      state.vehicles.push(vehicle);
    },
    UPDATE_VEHICLE(state, updatedVehicle) {
      const index = state.vehicles.findIndex(v => v.id === updatedVehicle.id);
      if (index !== -1) {
        state.vehicles.splice(index, 1, updatedVehicle);
      }
    },
    REMOVE_VEHICLE(state, vehicleId) {
      state.vehicles = state.vehicles.filter(v => v.id !== vehicleId);
    },
    SET_REPAIR_ORDERS(state, orders) {
      state.repairOrders = orders;
    },
    SET_CURRENT_ORDER(state, order) {
      state.currentOrder = order;
    },
    ADD_REPAIR_ORDER(state, order) {
      state.repairOrders.unshift(order);
    },
    UPDATE_REPAIR_ORDER(state, updatedOrder) {
      const index = state.repairOrders.findIndex(o => o.id === updatedOrder.id);
      if (index !== -1) {
        state.repairOrders.splice(index, 1, updatedOrder);
      }
      if (state.currentOrder && state.currentOrder.id === updatedOrder.id) {
        state.currentOrder = updatedOrder;
      }
    },
    LOGOUT(state) {
      state.token = '';
      state.user = null;
      state.role = '';
      state.vehicles = [];
      state.repairOrders = [];
      state.currentOrder = null;
    }
  },
  actions: {
    // 用户认证
    async login({ commit }, credentials) {
      try {
        commit('SET_LOADING', true);
        commit('CLEAR_ERROR');
        const response = await userApi.login(credentials);
        const { token, user } = response;
        localStorage.setItem('token', token);
        localStorage.setItem('role', user.role);
        commit('SET_TOKEN', token);
        commit('SET_USER', user);
        commit('SET_ROLE', user.role);
        return user;
      } catch (error) {
        commit('SET_ERROR', error.response?.data?.message || '登录失败');
        throw error;
      } finally {
        commit('SET_LOADING', false);
      }
    },
    async register({ commit }, userData) {
      try {
        commit('SET_LOADING', true);
        commit('CLEAR_ERROR');
        const response = await userApi.register(userData);
        return response;
      } catch (error) {
        commit('SET_ERROR', error.response?.data?.message || '注册失败');
        throw error;
      } finally {
        commit('SET_LOADING', false);
      }
    },
    async fetchUserInfo({ commit }) {
      try {
        commit('SET_LOADING', true);
        commit('CLEAR_ERROR');
        const user = await userApi.getUserInfo();
        commit('SET_USER', user);
        commit('SET_ROLE', user.role);
        return user;
      } catch (error) {
        commit('SET_ERROR', error.response?.data?.message || '获取用户信息失败');
        throw error;
      } finally {
        commit('SET_LOADING', false);
      }
    },
    logout({ commit }) {
      localStorage.removeItem('token');
      localStorage.removeItem('role');
      commit('LOGOUT');
    },

    // 车辆管理
    async fetchUserVehicles({ commit }) {
      try {
        commit('SET_LOADING', true);
        commit('CLEAR_ERROR');
        const vehicles = await userApi.getUserVehicles();
        commit('SET_VEHICLES', vehicles);
        return vehicles;
      } catch (error) {
        commit('SET_ERROR', error.response?.data?.message || '获取车辆列表失败');
        throw error;
      } finally {
        commit('SET_LOADING', false);
      }
    },
    async addVehicle({ commit }, vehicleData) {
      try {
        commit('SET_LOADING', true);
        commit('CLEAR_ERROR');
        const vehicle = await userApi.addVehicle(vehicleData);
        commit('ADD_VEHICLE', vehicle);
        return vehicle;
      } catch (error) {
        commit('SET_ERROR', error.response?.data?.message || '添加车辆失败');
        throw error;
      } finally {
        commit('SET_LOADING', false);
      }
    },
    async updateVehicle({ commit }, { id, vehicleData }) {
      try {
        commit('SET_LOADING', true);
        commit('CLEAR_ERROR');
        const updatedVehicle = await vehicleApi.updateVehicle(id, vehicleData);
        commit('UPDATE_VEHICLE', updatedVehicle);
        return updatedVehicle;
      } catch (error) {
        commit('SET_ERROR', error.response?.data?.message || '更新车辆信息失败');
        throw error;
      } finally {
        commit('SET_LOADING', false);
      }
    },
    async deleteVehicle({ commit }, id) {
      try {
        commit('SET_LOADING', true);
        commit('CLEAR_ERROR');
        await vehicleApi.deleteVehicle(id);
        commit('REMOVE_VEHICLE', id);
      } catch (error) {
        commit('SET_ERROR', error.response?.data?.message || '删除车辆失败');
        throw error;
      } finally {
        commit('SET_LOADING', false);
      }
    },

    // 维修工单管理
    async fetchUserOrders({ commit, getters }) {
      try {
        commit('SET_LOADING', true);
        commit('CLEAR_ERROR');
        let orders;
        if (getters.isMechanic) {
          orders = await repairOrderApi.getMechanicOrders();
        } else {
          orders = await repairOrderApi.getUserOrders();
        }
        commit('SET_REPAIR_ORDERS', orders);
        return orders;
      } catch (error) {
        commit('SET_ERROR', error.response?.data?.message || '获取维修工单失败');
        throw error;
      } finally {
        commit('SET_LOADING', false);
      }
    },
    async fetchRepairOrder({ commit }, id) {
      try {
        commit('SET_LOADING', true);
        commit('CLEAR_ERROR');
        const order = await repairOrderApi.getOrder(id);
        commit('SET_CURRENT_ORDER', order);
        return order;
      } catch (error) {
        commit('SET_ERROR', error.response?.data?.message || '获取维修工单详情失败');
        throw error;
      } finally {
        commit('SET_LOADING', false);
      }
    },
    async createRepairOrder({ commit }, orderData) {
      try {
        commit('SET_LOADING', true);
        commit('CLEAR_ERROR');
        const order = await repairOrderApi.createOrder(orderData);
        commit('ADD_REPAIR_ORDER', order);
        return order;
      } catch (error) {
        commit('SET_ERROR', error.response?.data?.message || '创建维修工单失败');
        throw error;
      } finally {
        commit('SET_LOADING', false);
      }
    },
    async updateOrderStatus({ commit }, { id, status }) {
      try {
        commit('SET_LOADING', true);
        commit('CLEAR_ERROR');
        const updatedOrder = await repairOrderApi.updateOrderStatus(id, status);
        commit('UPDATE_REPAIR_ORDER', updatedOrder);
        return updatedOrder;
      } catch (error) {
        commit('SET_ERROR', error.response?.data?.message || '更新工单状态失败');
        throw error;
      } finally {
        commit('SET_LOADING', false);
      }
    },
    async addMaterialToOrder({ commit }, { orderId, materialData }) {
      try {
        commit('SET_LOADING', true);
        commit('CLEAR_ERROR');
        const updatedOrder = await repairOrderApi.addMaterial(orderId, materialData);
        commit('UPDATE_REPAIR_ORDER', updatedOrder);
        return updatedOrder;
      } catch (error) {
        commit('SET_ERROR', error.response?.data?.message || '添加材料失败');
        throw error;
      } finally {
        commit('SET_LOADING', false);
      }
    },
    async completeOrder({ commit }, orderId) {
      try {
        commit('SET_LOADING', true);
        commit('CLEAR_ERROR');
        const updatedOrder = await repairOrderApi.completeOrder(orderId);
        commit('UPDATE_REPAIR_ORDER', updatedOrder);
        return updatedOrder;
      } catch (error) {
        commit('SET_ERROR', error.response?.data?.message || '完成维修工单失败');
        throw error;
      } finally {
        commit('SET_LOADING', false);
      }
    },
    async addOrderFeedback({ commit }, { orderId, rating, feedback }) {
      try {
        commit('SET_LOADING', true);
        commit('CLEAR_ERROR');
        const updatedOrder = await repairOrderApi.addFeedback(orderId, { rating, feedback });
        commit('UPDATE_REPAIR_ORDER', updatedOrder);
        return updatedOrder;
      } catch (error) {
        commit('SET_ERROR', error.response?.data?.message || '提交反馈失败');
        throw error;
      } finally {
        commit('SET_LOADING', false);
      }
    }
  },
  modules: {
  }
}); 