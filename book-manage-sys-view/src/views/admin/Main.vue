<template>
  <div class="admin-layout">
    <!-- 顶部栏 -->
    <div class="admin-header">
      <div class="header-left">
        <button @click="toggleMenu" class="menu-btn">☰</button>
        <h1>智慧图书管理系统</h1>
      </div>
      <div class="header-right">
        <span>欢迎，{{ userName }}</span>
        <button @click="logout" class="logout-btn">退出</button>
      </div>
    </div>

    <div class="admin-body">
      <!-- 侧边栏 -->
      <div v-show="menuVisible" class="admin-sidebar">
        <div class="sidebar-user">
          <img :src="userAvatar" class="avatar">
          <p>{{ userName }}</p>
        </div>
        
        <div class="sidebar-menu">
          <router-link to="/admin/dashboard" class="menu-item">
            📊 仪表盘
          </router-link>
          <router-link to="/admin/users" class="menu-item">
            👥 用户管理
          </router-link>
          <router-link to="/admin/notices" class="menu-item">
            📢 通知管理
          </router-link>
          <router-link to="/admin/notices/create" class="menu-item">
            ✏️ 创建通知
          </router-link>
        </div>
      </div>

      <!-- 主内容区 -->
      <div class="admin-content">
        <router-view></router-view>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const menuVisible = ref(true)

// 用户信息
const userName = computed(() => {
  return localStorage.getItem('username') || '管理员'
})

const userAvatar = computed(() => {
  return localStorage.getItem('avatar') || '/default-avatar.png'
})

// 方法
const toggleMenu = () => {
  menuVisible.value = !menuVisible.value
}

const logout = () => {
  localStorage.removeItem('token')
  router.push('/login')
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.admin-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 0 20px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.menu-btn {
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  width: 40px;
  height: 40px;
  border-radius: 5px;
  cursor: pointer;
  font-size: 20px;
}

h1 {
  margin: 0;
  font-size: 20px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.logout-btn {
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  padding: 5px 15px;
  border-radius: 5px;
  cursor: pointer;
}

.admin-body {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.admin-sidebar {
  width: 200px;
  background: #2c3e50;
  color: white;
  overflow-y: auto;
}

.sidebar-user {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.avatar {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  margin-bottom: 10px;
}

.sidebar-menu {
  padding: 10px 0;
}

.menu-item {
  display: block;
  padding: 12px 20px;
  color: #ecf0f1;
  text-decoration: none;
  transition: background 0.3s;
}

.menu-item:hover {
  background: #34495e;
}

.menu-item.router-link-active {
  background: #1abc9c;
  border-left: 4px solid #16a085;
}

.admin-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  background: #ecf0f1;
}
</style>