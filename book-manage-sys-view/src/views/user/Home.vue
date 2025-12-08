<template>
  <div class="home-container">
    <!-- 顶部导航栏 -->
    <header class="navbar">
      <div class="nav-left">
        <h1 class="logo">📚 智慧图书管理系统</h1>
      </div>
      <div class="nav-right">
        <div class="user-info">
          <span class="welcome-text">欢迎，{{ username }}</span>
          <div class="user-dropdown">
            <img :src="userAvatar" alt="用户头像" class="user-avatar">
            <div class="dropdown-content">
              <a href="#" @click="goToProfile">个人中心</a>
              <a href="#" @click="logout">退出登录</a>
            </div>
          </div>
        </div>
      </div>
    </header>

    <!-- 主要内容区域 -->
    <main class="main-content">
      <!-- 欢迎横幅 -->
      <section class="welcome-banner">
        <div class="banner-content">
          <h2>欢迎使用智慧图书管理系统</h2>
          <p>已收录图书 {{ bookCount }} 册，今日借阅 {{ todayBorrowCount }} 次</p>
        </div>
      </section>

      <!-- 快捷操作 -->
      <section class="quick-actions">
        <h3 class="section-title">快捷操作</h3>
        <div class="actions-grid">
          <div class="action-card" @click="goToBookSearch">
            <div class="action-icon">🔍</div>
            <h4>图书检索</h4>
            <p>快速查找所需图书</p>
          </div>
          <div class="action-card" @click="goToBorrowBook">
            <div class="action-icon">📖</div>
            <h4>借阅图书</h4>
            <p>借阅新的图书</p>
          </div>
          <div class="action-card" @click="goToReturnBook">
            <div class="action-icon">📚</div>
            <h4>归还图书</h4>
            <p>归还已借阅图书</p>
          </div>
          <div class="action-card" @click="goToBorrowHistory">
            <div class="action-icon">📅</div>
            <h4>借阅记录</h4>
            <p>查看历史借阅信息</p>
          </div>
        </div>
      </section>

      <!-- 借阅统计 -->
      <section class="borrow-stats">
        <h3 class="section-title">我的借阅统计</h3>
        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-number">{{ currentBorrowCount }}</div>
            <div class="stat-label">当前借阅</div>
          </div>
          <div class="stat-card">
            <div class="stat-number">{{ totalBorrowCount }}</div>
            <div class="stat-label">累计借阅</div>
          </div>
          <div class="stat-card">
            <div class="stat-number">{{ overdueCount }}</div>
            <div class="stat-label">逾期未还</div>
          </div>
          <div class="stat-card">
            <div class="stat-number">{{ favoriteCount }}</div>
            <div class="stat-label">收藏图书</div>
          </div>
        </div>
      </section>

      <!-- 最新图书推荐 -->
      <section class="book-recommendations">
        <div class="section-header">
          <h3 class="section-title">最新上架图书</h3>
          <a href="#" class="view-all" @click.prevent="goToAllBooks">查看全部 →</a>
        </div>
        <div class="books-grid">
          <div class="book-card" v-for="book in newBooks" :key="book.id">
            <img :src="book.cover" :alt="book.title" class="book-cover">
            <div class="book-info">
              <h4 class="book-title">{{ book.title }}</h4>
              <p class="book-author">{{ book.author }}</p>
              <div class="book-status" :class="book.available ? 'available' : 'unavailable'">
                {{ book.available ? '可借阅' : '已借出' }}
              </div>
            </div>
          </div>
        </div>
      </section>
    </main>

    <!-- 页脚 -->
    <footer class="footer">
      <p>© 2025 智慧图书管理系统 版权所有 | 技术支持：张许烨、陈丽、许梓晗</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 用户信息
const username = ref('管理员')
const userAvatar = ref('https://via.placeholder.com/40')

// 统计数据
const bookCount = ref(12543)
const todayBorrowCount = ref(24)
const currentBorrowCount = ref(3)
const totalBorrowCount = ref(156)
const overdueCount = ref(1)
const favoriteCount = ref(12)

// 最新图书数据
const newBooks = ref([
  { id: 1, title: 'Vue.js设计与实现', author: '霍春阳', cover: 'https://via.placeholder.com/80x100', available: true },
  { id: 2, title: 'JavaScript高级程序设计', author: 'Nicholas C. Zakas', cover: 'https://via.placeholder.com/80x100', available: true },
  { id: 3, title: 'Python数据分析', author: 'Wes McKinney', cover: 'https://via.placeholder.com/80x100', available: false },
  { id: 4, title: '深入浅出Node.js', author: '朴灵', cover: 'https://via.placeholder.com/80x100', available: true },
  { id: 5, title: '设计模式之美', author: '王争', cover: 'https://placeholder.com/80x100', available: true }
])

// 导航方法
const goToBookSearch = () => {
  alert('跳转到图书检索页面')
  // router.push('/search')
}

const goToBorrowBook = () => {
  alert('跳转到借阅图书页面')
  // router.push('/borrow')
}

const goToReturnBook = () => {
  alert('跳转到归还图书页面')
  // router.push('/return')
}

const goToBorrowHistory = () => {
  alert('跳转到借阅记录页面')
  // router.push('/history')
}

const goToProfile = () => {
  alert('跳转到个人中心')
  // router.push('/profile')
}

const goToAllBooks = () => {
  alert('查看全部图书')
  // router.push('/books')
}

const logout = () => {
  if (confirm('确定要退出登录吗？')) {
    localStorage.removeItem('token')
    router.push('/login')
  }
}

// 模拟数据加载
onMounted(() => {
  // 实际项目中这里可以调用API获取用户数据和统计信息
  const savedUser = localStorage.getItem('user')
  if (savedUser) {
    username.value = savedUser
  }
})
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: #f5f7fa;
}

/* 顶部导航栏 */
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 40px;
  height: 60px;
  background: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  position: sticky;
  top: 0;
  z-index: 100;
}

.logo {
  font-size: 20px;
  color: #333;
  margin: 0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.welcome-text {
  color: #666;
  font-size: 14px;
}

.user-dropdown {
  position: relative;
  cursor: pointer;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 2px solid #6a11cb;
}

.dropdown-content {
  display: none;
  position: absolute;
  right: 0;
  background: white;
  min-width: 120px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.15);
  border-radius: 8px;
  overflow: hidden;
  z-index: 100;
}

.user-dropdown:hover .dropdown-content {
  display: block;
}

.dropdown-content a {
  display: block;
  padding: 12px 16px;
  color: #333;
  text-decoration: none;
  transition: background 0.3s;
}

.dropdown-content a:hover {
  background: #f5f7fa;
}

/* 主要内容 */
.main-content {
  padding: 30px 40px;
}

/* 欢迎横幅 */
.welcome-banner {
  background: linear-gradient(135deg, #6a11cb 0%, #2575fc 100%);
  color: white;
  padding: 30px;
  border-radius: 15px;
  margin-bottom: 30px;
}

.banner-content h2 {
  margin: 0 0 10px 0;
  font-size: 28px;
}

.banner-content p {
  margin: 0;
  opacity: 0.9;
}

/* 快捷操作 */
.quick-actions {
  margin-bottom: 40px;
}

.section-title {
  font-size: 20px;
  color: #333;
  margin-bottom: 20px;
  font-weight: 600;
}

.actions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.action-card {
  background: white;
  padding: 25px 20px;
  border-radius: 12px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.action-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.action-icon {
  font-size: 36px;
  margin-bottom: 15px;
}

.action-card h4 {
  margin: 0 0 8px 0;
  color: #333;
}

.action-card p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 20px;
  margin-bottom: 40px;
}

.stat-card {
  background: white;
  padding: 25px 20px;
  border-radius: 12px;
  text-align: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.stat-number {
  font-size: 36px;
  font-weight: 600;
  color: #6a11cb;
  margin-bottom: 8px;
}

.stat-label {
  color: #666;
  font-size: 14px;
}

/* 图书推荐 */
.book-recommendations {
  margin-bottom: 40px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.view-all {
  color: #6a11cb;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s;
}

.view-all:hover {
  color: #2575fc;
}

.books-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 20px;
}

.book-card {
  background: white;
  padding: 15px;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  transition: transform 0.3s;
}

.book-card:hover {
  transform: translateY(-3px);
}

.book-cover {
  width: 100%;
  height: 150px;
  object-fit: cover;
  border-radius: 6px;
  margin-bottom: 12px;
  background: #f0f0f0;
}

.book-title {
  font-size: 14px;
  font-weight: 600;
  margin: 0 0 5px 0;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.book-author {
  font-size: 12px;
  color: #666;
  margin: 0 0 10px 0;
}

.book-status {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.book-status.available {
  background: #e8f5e8;
  color: #2ecc71;
}

.book-status.unavailable {
  background: #ffeaea;
  color: #ff4757;
}

/* 页脚 */
.footer {
  padding: 20px;
  text-align: center;
  background: white;
  color: #666;
  font-size: 14px;
  border-top: 1px solid #eee;
  margin-top: auto;
}

/* 响应式 */
@media (max-width: 768px) {
  .navbar {
    padding: 0 20px;
  }
  
  .main-content {
    padding: 20px;
  }
  
  .welcome-banner {
    padding: 20px;
  }
  
  .actions-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .books-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .actions-grid {
    grid-template-columns: 1fr;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .books-grid {
    grid-template-columns: 1fr;
  }
}
</style>