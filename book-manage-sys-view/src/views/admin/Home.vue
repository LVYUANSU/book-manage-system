<template>
  <div class="admin-dashboard">
    <h2>📊 系统概览</h2>
    
    <!-- 统计卡片 -->
    <div class="stats">
      <div class="stat-card">
        <div class="stat-icon">👥</div>
        <div class="stat-content">
          <h3>{{ stats.totalUsers }}</h3>
          <p>总用户数</p>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">📢</div>
        <div class="stat-content">
          <h3>{{ stats.totalNotices }}</h3>
          <p>总通知数</p>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">✅</div>
        <div class="stat-content">
          <h3>{{ stats.activeUsers }}</h3>
          <p>活跃用户</p>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon">🔔</div>
        <div class="stat-content">
          <h3>{{ stats.unreadNotices }}</h3>
          <p>未读通知</p>
        </div>
      </div>
    </div>

    <!-- 快速操作 -->
    <div class="quick-actions">
      <h3>🚀 快速操作</h3>
      <div class="action-buttons">
        <button @click="goToUsers" class="action-btn">👥 管理用户</button>
        <button @click="goToCreateNotice" class="action-btn">📢 发布通知</button>
        <button @click="refresh" class="action-btn">🔄 刷新数据</button>
      </div>
    </div>

    <!-- 最近活动 -->
    <div class="recent-activity">
      <h3>📋 最近活动</h3>
      <div class="activity-list">
        <div v-for="activity in activities" :key="activity.id" class="activity-item">
          <span class="activity-icon">{{ activity.icon }}</span>
          <div class="activity-content">
            <p>{{ activity.text }}</p>
            <small>{{ activity.time }}</small>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 数据
const stats = ref({
  totalUsers: 0,
  totalNotices: 0,
  activeUsers: 0,
  unreadNotices: 0
})

const activities = ref([
  { id: 1, icon: '👤', text: '新用户注册', time: '10分钟前' },
  { id: 2, icon: '📢', text: '系统通知发布', time: '30分钟前' },
  { id: 3, icon: '🔧', text: '系统维护完成', time: '2小时前' }
])

// 方法
const loadStats = async () => {
  // 模拟API调用
  stats.value = {
    totalUsers: 1234,
    totalNotices: 567,
    activeUsers: 890,
    unreadNotices: 12
  }
}

const goToUsers = () => {
  router.push('/admin/users')
}

const goToCreateNotice = () => {
  router.push('/admin/notices/create')
}

const refresh = () => {
  loadStats()
}

// 生命周期
onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.admin-dashboard {
  padding: 20px;
}

h2 {
  margin-top: 0;
  color: #2c3e50;
}

.stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin: 20px 0;
}

.stat-card {
  background: white;
  border-radius: 10px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 15px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.stat-icon {
  font-size: 40px;
}

.stat-content h3 {
  margin: 0;
  font-size: 24px;
  color: #2c3e50;
}

.stat-content p {
  margin: 5px 0 0 0;
  color: #7f8c8d;
}

.quick-actions {
  background: white;
  border-radius: 10px;
  padding: 20px;
  margin: 20px 0;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.action-buttons {
  display: flex;
  gap: 15px;
  margin-top: 15px;
}

.action-btn {
  padding: 10px 20px;
  background: #3498db;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.action-btn:hover {
  background: #2980b9;
}

.recent-activity {
  background: white;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.activity-list {
  margin-top: 15px;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 10px;
  border-bottom: 1px solid #eee;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-icon {
  font-size: 20px;
}

.activity-content p {
  margin: 0;
  color: #2c3e50;
}

.activity-content small {
  color: #7f8c8d;
  font-size: 12px;
}
</style>