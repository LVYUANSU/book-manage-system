<template>
  <div class="login-container">
    <div class="login-wrapper">
      <!-- 左侧功能介绍区域 -->
      <div class="feature-section">
        <h1 class="welcome-title">欢迎使用智慧图书管理系统</h1>
        <p class="welcome-desc">
          一个集图书管理、借阅追踪、会员管理、数据分析于一体的现代化图书管理系统。
          帮助图书馆、学校和企业实现图书资源的智能化管理。
        </p>
        
        <ul class="feature-list">
          <li class="feature-item">
            <span class="feature-icon">📚</span>
            <span>智能图书管理，支持扫码入库</span>
          </li>
          <li class="feature-item">
            <span class="feature-icon">🔍</span>
            <span>快速检索系统，支持多条件查询</span>
          </li>
          <li class="feature-item">
            <span class="feature-icon">👥</span>
            <span>会员管理系统，记录借阅历史</span>
          </li>
          <li class="feature-item">
            <span class="feature-icon">📊</span>
            <span>数据统计分析，生成可视化报表</span>
          </li>
          <li class="feature-item">
            <span class="feature-icon">🔔</span>
            <span>智能提醒功能，逾期自动通知</span>
          </li>
        </ul>
      </div>

      <!-- 右侧登录区域 -->
      <div class="login-section">
        <div class="login-card">
          <h2 class="login-title">管理员登录</h2>
          
          <form @submit.prevent="handleLogin" class="login-form">
            <div class="form-group">
              <label class="form-label">用户名</label>
              <input
                v-model="loginForm.username"
                type="text"
                placeholder="请输入管理员账号"
                class="form-input"
                required
              />
            </div>
            
            <div class="form-group">
              <label class="form-label">密码</label>
              <input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入登录密码"
                class="form-input"
                required
                @keyup.enter="handleLogin"
              />
            </div>
            
            <button
              type="submit"
              class="login-btn"
              :disabled="loading"
            >
              {{ loading ? '登录中...' : '登录系统' }}
            </button>
            
            <div class="register-link">
              没有账户？
              <a href="#" @click.prevent="goToRegister" class="register-text">立即注册</a>
            </div>
          </form>
          
          <div class="copyright">
            © 2025 智慧图书管理系统 版权所有<br/>
            技术支持：张许烨、陈丽、许梓晗
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const loginForm = reactive({
  username: '',
  password: ''
})

const loading = ref(false)

const handleLogin = async () => {
  if (!loginForm.username.trim() || !loginForm.password.trim()) {
    alert('请填写用户名和密码')
    return
  }
  
  loading.value = true
  
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 800))
    
    // 存储token并跳转到首页
    localStorage.setItem('token', 'mock-admin-token')
    localStorage.setItem('user', loginForm.username)
    
    // 跳转到首页
    router.push('/home')
  } catch (error) {
    alert('登录失败：' + error.message)
  } finally {
    loading.value = false
  }
}

const goToRegister = () => {
  // 这里可以跳转到注册页面
  alert('跳转到注册页面')
  // router.push('/register')
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px;
}

.login-wrapper {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  min-height: calc(100vh - 80px);
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

/* 左侧区域 */
.feature-section {
  flex: 1;
  padding: 60px 40px;
  background: linear-gradient(135deg, #6a11cb 0%, #2575fc 100%);
  color: white;
}

.welcome-title {
  font-size: 32px;
  margin-bottom: 20px;
  font-weight: 600;
  line-height: 1.3;
}

.welcome-desc {
  font-size: 16px;
  line-height: 1.6;
  margin-bottom: 40px;
  opacity: 0.9;
}

.feature-list {
  list-style: none;
  padding: 0;
}

.feature-item {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  font-size: 16px;
  padding: 12px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.feature-icon {
  font-size: 20px;
  margin-right: 15px;
  width: 24px;
  text-align: center;
}

/* 右侧区域 */
.login-section {
  flex: 0 0 500px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: #f8f9fa;
}

.login-card {
  width: 100%;
  max-width: 400px;
  background: white;
  padding: 40px 30px;
  border-radius: 15px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.08);
}

.login-title {
  text-align: center;
  color: #333;
  font-size: 28px;
  margin-bottom: 40px;
  font-weight: 600;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-group {
  width: 100%;
}

.form-label {
  display: block;
  margin-bottom: 8px;
  color: #555;
  font-size: 14px;
  font-weight: 500;
}

.form-input {
  width: 100%;
  padding: 14px 16px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  font-size: 16px;
  box-sizing: border-box;
  transition: all 0.3s;
  background: #f8f9fa;
}

.form-input:focus {
  outline: none;
  border-color: #6a11cb;
  background: white;
  box-shadow: 0 0 0 3px rgba(106, 17, 203, 0.1);
}

.form-input::placeholder {
  color: #999;
}

.login-btn {
  width: 100%;
  padding: 16px;
  background: linear-gradient(135deg, #6a11cb 0%, #2575fc 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  margin-top: 10px;
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(106, 17, 203, 0.3);
}

.login-btn:active:not(:disabled) {
  transform: translateY(0);
}

.login-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.register-link {
  text-align: center;
  margin-top: 20px;
  color: #666;
  font-size: 14px;
}

.register-text {
  color: #6a11cb;
  text-decoration: none;
  font-weight: 500;
  margin-left: 5px;
  cursor: pointer;
  transition: color 0.3s;
}

.register-text:hover {
  color: #2575fc;
  text-decoration: underline;
}

.copyright {
  text-align: center;
  margin-top: 50px;
  padding-top: 20px;
  border-top: 1px solid #eee;
  color: #888;
  font-size: 12px;
  line-height: 1.6;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .login-wrapper {
    flex-direction: column;
    max-width: 600px;
  }
  
  .feature-section {
    padding: 40px 30px;
  }
  
  .login-section {
    flex: none;
    padding: 30px;
  }
}

@media (max-width: 480px) {
  .login-container {
    padding: 20px;
  }
  
  .welcome-title {
    font-size: 24px;
  }
  
  .login-title {
    font-size: 24px;
  }
  
  .login-card {
    padding: 30px 20px;
  }
}
</style>