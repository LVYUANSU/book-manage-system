<template>
  <div class="main-layout">
    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ collapsed: isCollapsed }">
      <div class="sidebar-header">
        <h2 class="logo" @click="toggleSidebar">
          📚 {{ isCollapsed ? '图书' : '智慧图书管理系统' }}
        </h2>
        <button class="toggle-btn" @click="toggleSidebar">
          {{ isCollapsed ? '→' : '←' }}
        </button>
      </div>
      
      <nav class="nav-menu">
        <div class="user-profile">
          <img :src="userAvatar" alt="用户头像" class="profile-avatar">
          <div v-if="!isCollapsed" class="profile-info">
            <h4>{{ username }}</h4>
            <p>管理员</p>
          </div>
        </div>
        
        <ul class="menu-list">
          <li 
            v-for="item in menuItems" 
            :key="item.path"
            :class="{ active: activeMenu === item.path }"
            @click="navigateTo(item.path)"
          >
            <span class="menu-icon">{{ item.icon }}</span>
            <span v-if="!isCollapsed" class="menu-text">{{ item.text }}</span>
          </li>
        </ul>
      </nav>
    </aside>

    <!-- 主内容区 -->
    <div class="main-content">
      <!-- 顶部工具栏 -->
      <header class="topbar">
        <div class="topbar-left">
          <div class="breadcrumb">
            <span v-for="(crumb, index) in breadcrumbs" :key="index">
              <span v-if="index > 0"> / </span>
              <span :class="{ active: index === breadcrumbs.length - 1 }">
                {{ crumb }}
              </span>
            </span>
          </div>
        </div>
        
        <div class="topbar-right">
          <div class="search-box">
            <input 
              type="text" 
              v-model="searchQuery"
              placeholder="搜索图书、作者..."
              @keyup.enter="handleSearch"
            >
            <button class="search-btn" @click="handleSearch">🔍</button>
          </div>
          
          <div class="toolbar-actions">
            <button class="action-btn" @click="refreshPage">🔄</button>
            <button class="action-btn" @click="showNotifications">
              🔔<span class="badge" v-if="notificationCount > 0">{{ notificationCount }}</span>
            </button>
            <button class="action-btn logout-btn" @click="logout">🚪</button>
          </div>
        </div>
      </header>

      <!-- 路由视图区域 -->
      <div class="content-view">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

// 侧边栏状态
const isCollapsed = ref(false)

// 用户信息
const username = ref('管理员')
const userAvatar = ref('https://via.placeholder.com/40')

// 搜索相关
const searchQuery = ref('')

// 通知数量
const notificationCount = ref(3)

// 菜单项配置
const menuItems = ref([
  { icon: '🏠', text: '首页', path: '/home' },
  { icon: '📚', text: '图书管理', path: '/books' },
  { icon: '👥', text: '会员管理', path: '/members' },
  { icon: '📖', text: '借阅管理', path: '/borrow' },
  { icon: '📊', text: '数据统计', path: '/stats' },
  { icon: '⚙️', text: '系统设置', path: '/settings' },
  { icon: '❓', text: '帮助中心', path: '/help' }
])

// 计算面包屑
const breadcrumbs = computed(() => {
  const path = route.path
  const parts = path.split('/').filter(p => p)
  
  if (parts.length === 0) return ['首页']
  
  return parts.map(part => {
    const item = menuItems.value.find(item => 
      item.path.slice(1) === part || item.path === path
    )
    return item ? item.text : part
  })
})

// 当前激活菜单
const activeMenu = computed(() => route.path)

// 切换侧边栏
const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}

// 导航到页面
const navigateTo = (path) => {
  router.push(path)
}

// 搜索处理
const handleSearch = () => {
  if (searchQuery.value.trim()) {
    alert(`搜索: ${searchQuery.value}`)
    // router.push(`/search?q=${searchQuery.value}`)
    searchQuery.value = ''
  }
}

// 刷新页面
const refreshPage = () => {
  window.location.reload()
}

// 显示通知
const showNotifications = () => {
  alert(`您有 ${notificationCount.value} 条新通知`)
  notificationCount.value = 0
}

// 退出登录
const logout = () => {
  if (confirm('确定要退出登录吗？')) {
    localStorage.removeItem('token')
    router.push('/login')
  }
}

// 初始化
onMounted(() => {
  const savedUser = localStorage.getItem('user')
  if (savedUser) {
    username.value = savedUser
  }
})
</script>

<style scoped>
.main-layout {
  display: flex;
  min-height: 100vh;
  background: #f5f7fa;
}

/* 侧边栏 */
.sidebar {
  width: 250px;
  background: linear-gradient(135deg, #2c3e50 0%, #1a1a2e 100%);
  color: white;
  transition: width 0.3s;
  position: sticky;
  top: 0;
  height: 100vh;
  overflow-y: auto;
}

.sidebar.collapsed {
  width: 70px;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  font-size: 18px;
  margin: 0;
  cursor: pointer;
  user-select: none;
  white-space: nowrap;
  overflow: hidden;
}

.toggle-btn {
  background: rgba(255, 255, 255, 0.1);
  border: none;
  color: white;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.3s;
}

.toggle-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

/* 用户资料 */
.user-profile {
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 15px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.profile-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  border: 2px solid #6a11cb;
}

.profile-info h4 {
  margin: 0 0 5px 0;
  font-size: 16px;
}

.profile-info p {
  margin: 0;
  font-size: 12px;
  opacity: 0.7;
}

.sidebar.collapsed .profile-info {
  display: none;
}

/* 菜单列表 */
.menu-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.menu-list li {
  padding: 15px 20px;
  display: flex;
  align-items: center;
  cursor: pointer;
  transition: background 0.3s;
  border-left: 4px solid transparent;
}

.menu-list li:hover {
  background: rgba(255, 255, 255, 0.1);
}

.menu-list li.active {
  background: rgba(106, 17, 203, 0.2);
  border-left-color: #6a11cb;
}

.menu-icon {
  font-size: 20px;
  margin-right: 15px;
  width: 24px;
  text-align: center;
}

.menu-text {
  font-size: 14px;
  white-space: nowrap;
}

.sidebar.collapsed .menu-text {
  display: none;
}

/* 主内容区 */
.main-content {
  flex: 1;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

/* 顶部工具栏 */
.topbar {
  background: white;
  padding: 15px 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 99;
}

.breadcrumb {
  font-size: 14px;
  color: #666;
}

.breadcrumb .active {
  color: #6a11cb;
  font-weight: 500;
}

.search-box {
  display: flex;
  align-items: center;
  background: #f5f7fa;
  border-radius: 8px;
  padding: 5px 10px;
  margin-right: 20px;
}

.search-box input {
  border: none;
  background: transparent;
  padding: 8px;
  width: 200px;
  font-size: 14px;
  outline: none;
}

.search-btn {
  background: transparent;
  border: none;
  cursor: pointer;
  font-size: 16px;
  color: #666;
  padding: 5px;
}

.toolbar-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.action-btn {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  border: 1px solid #e0e0e0;
  background: white;
  cursor: pointer;
  font-size: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  transition: all 0.3s;
}

.action-btn:hover {
  background: #f5f7fa;
  border-color: #6a11cb;
}

.badge {
  position: absolute;
  top: -5px;
  right: -5px;
  background: #ff4757;
  color: white;
  font-size: 12px;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logout-btn {
  background: linear-gradient(135deg, #ff4757 0%, #ff3838 100%);
  color: white;
  border: none;
}

/* 内容区域 */
.content-view {
  flex: 1;
  padding: 30px;
  overflow-y: auto;
}

/* 响应式 */
@media (max-width: 1024px) {
  .sidebar {
    width: 70px;
  }
  
  .sidebar:not(.collapsed) {
    width: 250px;
    position: fixed;
    z-index: 1000;
    height: 100vh;
  }
  
  .search-box input {
    width: 150px;
  }
}

@media (max-width: 768px) {
  .topbar {
    flex-direction: column;
    gap: 15px;
    padding: 15px;
  }
  
  .topbar-left, .topbar-right {
    width: 100%;
  }
  
  .breadcrumb {
    margin-bottom: 10px;
  }
  
  .search-box {
    margin-right: 0;
    flex: 1;
  }
  
  .search-box input {
    width: 100%;
  }
  
  .content-view {
    padding: 20px;
  }
}
</style>