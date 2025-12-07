<template>
  <div class="level-header" :class="headerClasses">
    <!-- 返回按钮 -->
    <div v-if="showBack" class="header-back">
      <button class="back-btn" @click="handleBack">
        <span class="back-icon">←</span>
        <span v-if="backText" class="back-text">{{ backText }}</span>
      </button>
    </div>
    
    <!-- 标题区域 -->
    <div class="header-main">
      <!-- 图标 -->
      <div v-if="icon" class="header-icon">
        <span class="icon-content">{{ icon }}</span>
      </div>
      
      <!-- 标题 -->
      <div class="header-title">
        <h1 v-if="title" class="title-text">{{ title }}</h1>
        <h2 v-if="subtitle" class="subtitle-text">{{ subtitle }}</h2>
      </div>
      
      <!-- 徽章 -->
      <div v-if="badge" class="header-badge">
        <span class="badge-content" :class="badgeType">
          {{ badge }}
        </span>
      </div>
    </div>
    
    <!-- 操作区域 -->
    <div v-if="hasActions" class="header-actions">
      <!-- 搜索框 -->
      <div v-if="showSearch" class="header-search">
        <div class="search-container">
          <input
            v-model="searchQuery"
            type="text"
            :placeholder="searchPlaceholder"
            class="search-input"
            @input="handleSearch"
            @keyup.enter="handleSearch"
          />
          <button class="search-btn" @click="handleSearch">
            <span class="search-icon">🔍</span>
          </button>
        </div>
      </div>
      
      <!-- 操作按钮 -->
      <div v-if="actions.length > 0" class="action-buttons">
        <button
          v-for="action in actions"
          :key="action.id"
          class="action-btn"
          :class="action.type"
          @click="handleAction(action)"
          :disabled="action.disabled"
          :title="action.tooltip"
        >
          <span v-if="action.icon" class="action-icon">{{ action.icon }}</span>
          <span v-if="action.text" class="action-text">{{ action.text }}</span>
        </button>
      </div>
      
      <!-- 更多操作菜单 -->
      <div v-if="moreActions.length > 0" class="more-actions">
        <div class="more-dropdown" @click="toggleMoreMenu">
          <button class="more-btn">
            <span class="more-icon">⋯</span>
          </button>
          
          <div v-if="showMoreMenu" class="more-menu">
            <div
              v-for="action in moreActions"
              :key="action.id"
              class="menu-item"
              @click="handleAction(action)"
            >
              <span class="menu-icon">{{ action.icon }}</span>
              <span class="menu-text">{{ action.text }}</span>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 用户信息 -->
      <div v-if="userInfo" class="header-user">
        <div class="user-avatar" @click="toggleUserMenu">
          <img
            v-if="userInfo.avatar"
            :src="userInfo.avatar"
            :alt="userInfo.name"
            class="avatar-img"
          />
          <div v-else class="avatar-placeholder">
            {{ getUserInitials(userInfo.name) }}
          </div>
          
          <div v-if="showUserMenu" class="user-menu">
            <div class="user-info-card">
              <div class="user-card-header">
                <div class="card-avatar">
                  <img
                    v-if="userInfo.avatar"
                    :src="userInfo.avatar"
                    :alt="userInfo.name"
                  />
                  <div v-else class="card-avatar-placeholder">
                    {{ getUserInitials(userInfo.name) }}
                  </div>
                </div>
                <div class="card-user-info">
                  <div class="card-user-name">{{ userInfo.name }}</div>
                  <div v-if="userInfo.role" class="card-user-role">
                    {{ userInfo.role }}
                  </div>
                  <div v-if="userInfo.email" class="card-user-email">
                    {{ userInfo.email }}
                  </div>
                </div>
              </div>
              
              <div class="user-card-body">
                <div class="user-stats" v-if="userInfo.stats">
                  <div
                    v-for="stat in userInfo.stats"
                    :key="stat.label"
                    class="user-stat"
                  >
                    <div class="stat-value">{{ stat.value }}</div>
                    <div class="stat-label">{{ stat.label }}</div>
                  </div>
                </div>
              </div>
              
              <div class="user-card-footer">
                <button
                  v-for="action in userActions"
                  :key="action.id"
                  class="user-action-btn"
                  @click="handleUserAction(action)"
                >
                  <span class="user-action-icon">{{ action.icon }}</span>
                  <span class="user-action-text">{{ action.text }}</span>
                </button>
              </div>
            </div>
          </div>
        </div>
        
        <div class="user-name">
          {{ userInfo.name }}
          <span v-if="userInfo.role" class="user-role">
            {{ userInfo.role }}
          </span>
        </div>
      </div>
    </div>
    
    <!-- 标签页 -->
    <div v-if="tabs.length > 0" class="header-tabs">
      <div class="tabs-container">
        <div
          v-for="tab in tabs"
          :key="tab.id"
          class="tab-item"
          :class="{ 'active': activeTab === tab.id }"
          @click="handleTabClick(tab)"
        >
          <span v-if="tab.icon" class="tab-icon">{{ tab.icon }}</span>
          <span class="tab-text">{{ tab.text }}</span>
          <span v-if="tab.badge" class="tab-badge">
            {{ tab.badge }}
          </span>
        </div>
      </div>
    </div>
    
    <!-- 面包屑导航 -->
    <div v-if="breadcrumbs.length > 0" class="header-breadcrumbs">
      <div class="breadcrumbs-container">
        <span
          v-for="(crumb, index) in breadcrumbs"
          :key="crumb.id"
          class="breadcrumb-item"
        >
          <span
            class="crumb-link"
            :class="{ 'current': index === breadcrumbs.length - 1 }"
            @click="handleBreadcrumbClick(crumb, index)"
          >
            <span v-if="crumb.icon" class="crumb-icon">{{ crumb.icon }}</span>
            <span class="crumb-text">{{ crumb.text }}</span>
          </span>
          
          <span
            v-if="index < breadcrumbs.length - 1"
            class="crumb-separator"
          >
            /
          </span>
        </span>
      </div>
    </div>
    
    <!-- 进度条 -->
    <div v-if="showProgress" class="header-progress">
      <div class="progress-bar">
        <div
          class="progress-fill"
          :style="{ width: progress + '%' }"
        ></div>
      </div>
      <div class="progress-text">
        {{ progressText || `${Math.round(progress)}%` }}
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'LevelHeader',
  props: {
    // 标题配置
    title: {
      type: String,
      default: ''
    },
    subtitle: {
      type: String,
      default: ''
    },
    icon: {
      type: String,
      default: ''
    },
    
    // 返回按钮配置
    showBack: {
      type: Boolean,
      default: false
    },
    backText: {
      type: String,
      default: '返回'
    },
    
    // 徽章配置
    badge: {
      type: String,
      default: ''
    },
    badgeType: {
      type: String,
      default: 'primary',
      validator: (value) => ['primary', 'success', 'warning', 'danger', 'info'].includes(value)
    },
    
    // 搜索配置
    showSearch: {
      type: Boolean,
      default: false
    },
    searchPlaceholder: {
      type: String,
      default: '搜索...'
    },
    
    // 操作按钮配置
    actions: {
      type: Array,
      default: () => []
    },
    moreActions: {
      type: Array,
      default: () => []
    },
    
    // 用户信息配置
    userInfo: {
      type: Object,
      default: null
    },
    userActions: {
      type: Array,
      default: () => [
        { id: 'profile', text: '个人资料', icon: '👤' },
        { id: 'settings', text: '设置', icon: '⚙️' },
        { id: 'logout', text: '退出登录', icon: '🚪' }
      ]
    },
    
    // 标签页配置
    tabs: {
      type: Array,
      default: () => []
    },
    activeTab: {
      type: String,
      default: ''
    },
    
    // 面包屑配置
    breadcrumbs: {
      type: Array,
      default: () => []
    },
    
    // 进度条配置
    showProgress: {
      type: Boolean,
      default: false
    },
    progress: {
      type: Number,
      default: 0,
      validator: (value) => value >= 0 && value <= 100
    },
    progressText: {
      type: String,
      default: ''
    },
    
    // 样式配置
    type: {
      type: String,
      default: 'default',
      validator: (value) => ['default', 'primary', 'secondary', 'transparent'].includes(value)
    },
    fixed: {
      type: Boolean,
      default: false
    },
    sticky: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      searchQuery: '',
      showMoreMenu: false,
      showUserMenu: false
    }
  },
  computed: {
    headerClasses() {
      return {
        [this.type]: true,
        'fixed': this.fixed,
        'sticky': this.sticky,
        'with-tabs': this.tabs.length > 0,
        'with-breadcrumbs': this.breadcrumbs.length > 0,
        'with-progress': this.showProgress
      }
    },
    
    hasActions() {
      return this.showSearch || 
             this.actions.length > 0 || 
             this.moreActions.length > 0 || 
             this.userInfo
    }
  },
  methods: {
    handleBack() {
      this.$emit('back')
    },
    
    handleSearch() {
      this.$emit('search', this.searchQuery)
    },
    
    handleAction(action) {
      this.$emit('action', action)
      
      // 如果是更多菜单的操作，关闭菜单
      if (this.moreActions.includes(action)) {
        this.showMoreMenu = false
      }
    },
    
    toggleMoreMenu() {
      this.showMoreMenu = !this.showMoreMenu
      // 如果打开用户菜单，关闭更多菜单
      if (this.showMoreMenu && this.showUserMenu) {
        this.showUserMenu = false
      }
    },
    
    toggleUserMenu() {
      this.showUserMenu = !this.showUserMenu
      // 如果打开更多菜单，关闭用户菜单
      if (this.showUserMenu && this.showMoreMenu) {
        this.showMoreMenu = false
      }
    },
    
    getUserInitials(name) {
      if (!name) return '?'
      return name
        .split(' ')
        .map(word => word.charAt(0))
        .join('')
        .toUpperCase()
        .substring(0, 2)
    },
    
    handleUserAction(action) {
      this.$emit('user-action', action)
      this.showUserMenu = false
    },
    
    handleTabClick(tab) {
      this.$emit('tab-change', tab)
    },
    
    handleBreadcrumbClick(crumb, index) {
      if (index === this.breadcrumbs.length - 1) return
      this.$emit('breadcrumb-click', crumb)
    }
  },
  created() {
    // 点击其他地方关闭菜单
    const handleClickOutside = (event) => {
      if (this.showMoreMenu && !event.target.closest('.more-dropdown')) {
        this.showMoreMenu = false
      }
      if (this.showUserMenu && !event.target.closest('.header-user')) {
        this.showUserMenu = false
      }
    }
    
    document.addEventListener('click', handleClickOutside)
    
    this.$once('hook:beforeDestroy', () => {
      document.removeEventListener('click', handleClickOutside)
    })
  }
}
</script>

<style scoped lang="scss">
.level-header {
  background: white;
  border-bottom: 1px solid #ebeef5;
  transition: all 0.3s;
  
  &.primary {
    background: linear-gradient(135deg, #409eff, #66b1ff);
    color: white;
    border-bottom: none;
    
    .header-title .title-text,
    .header-title .subtitle-text,
    .header-breadcrumbs .breadcrumb-item,
    .header-tabs .tab-item {
      color: white;
    }
    
    .search-input,
    .action-btn {
      background: rgba(255, 255, 255, 0.1);
      border-color: rgba(255, 255, 255, 0.2);
      color: white;
      
      &:hover {
        background: rgba(255, 255, 255, 0.2);
      }
    }
  }
  
  &.secondary {
    background: #f8f9fa;
  }
  
  &.transparent {
    background: transparent;
    border-bottom: none;
  }
  
  &.fixed {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    z-index: 1000;
  }
  
  &.sticky {
    position: sticky;
    top: 0;
    z-index: 100;
  }
  
  .header-back {
    padding: 15px 20px;
    border-bottom: 1px solid #ebeef5;
    
    .back-btn {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      background: none;
      border: none;
      color: #409eff;
      font-size: 14px;
      cursor: pointer;
      padding: 4px 8px;
      border-radius: 4px;
      transition: background-color 0.3s;
      
      &:hover {
        background: #ecf5ff;
      }
      
      .back-icon {
        font-size: 16px;
      }
    }
  }
  
  .header-main {
    display: flex;
    align-items: center;
    gap: 15px;
    padding: 20px;
    
    .header-icon {
      .icon-content {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 40px;
        height: 40px;
        background: #ecf5ff;
        color: #409eff;
        border-radius: 8px;
        font-size: 20px;
      }
    }
    
    .header-title {
      flex: 1;
      
      .title-text {
        margin: 0;
        font-size: 24px;
        font-weight: 600;
        color: #303133;
        line-height: 1.2;
      }
      
      .subtitle-text {
        margin: 5px 0 0 0;
        font-size: 14px;
        color: #909399;
        font-weight: normal;
      }
    }
    
    .header-badge {
      .badge-content {
        display: inline-block;
        padding: 4px 12px;
        border-radius: 20px;
        font-size: 12px;
        font-weight: 600;
        
        &.primary {
          background: #ecf5ff;
          color: #409eff;
        }
        
        &.success {
          background: #f0f9eb;
          color: #67c23a;
        }
        
        &.warning {
          background: #fdf6ec;
          color: #e6a23c;
        }
        
        &.danger {
          background: #fef0f0;
          color: #f56c6c;
        }
        
        &.info {
          background: #f4f4f5;
          color: #909399;
        }
      }
    }
  }
  
  .header-actions {
    display: flex;
    align-items: center;
    gap: 15px;
    padding: 0 20px 20px;
    
    .header-search {
      flex: 1;
      max-width: 300px;
      
      .search-container {
        position: relative;
        
        .search-input {
          width: 100%;
          padding: 8px 40px 8px 15px;
          border: 1px solid #dcdfe6;
          border-radius: 20px;
          font-size: 14px;
          background: white;
          transition: all 0.3s;
          
          &:focus {
            outline: none;
            border-color: #409eff;
            box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
          }
        }
        
        .search-btn {
          position: absolute;
          right: 0;
          top: 0;
          height: 100%;
          padding: 0 15px;
          background: none;
          border: none;
          cursor: pointer;
          color: #909399;
          
          &:hover {
            color: #409eff;
          }
        }
      }
    }
    
    .action-buttons {
      display: flex;
      gap: 8px;
      
      .action-btn {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        padding: 8px 16px;
        border: 1px solid #dcdfe6;
        border-radius: 4px;
        background: white;
        color: #606266;
        font-size: 14px;
        cursor: pointer;
        transition: all 0.3s;
        
        &:hover:not(:disabled) {
          background: #f5f7fa;
          border-color: #c0c4cc;
        }
        
        &:disabled {
          opacity: 0.6;
          cursor: not-allowed;
        }
        
        &.primary {
          background: #409eff;
          border-color: #409eff;
          color: white;
          
          &:hover {
            background: #66b1ff;
            border-color: #66b1ff;
          }
        }
        
        .action-icon {
          font-size: 16px;
        }
      }
    }
    
    .more-actions {
      position: relative;
      
      .more-dropdown {
        .more-btn {
          width: 36px;
          height: 36px;
          display: flex;
          align-items: center;
          justify-content: center;
          background: none;
          border: 1px solid #dcdfe6;
          border-radius: 50%;
          cursor: pointer;
          transition: all 0.3s;
          
          &:hover {
            background: #f5f7fa;
          }
          
          .more-icon {
            font-size: 20px;
            line-height: 1;
          }
        }
        
        .more-menu {
          position: absolute;
          top: 100%;
          right: 0;
          min-width: 160px;
          background: white;
          border: 1px solid #ebeef5;
          border-radius: 4px;
          box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
          z-index: 1000;
          margin-top: 5px;
          
          .menu-item {
            display: flex;
            align-items: center;
            gap: 10px;
            padding: 10px 15px;
            cursor: pointer;
            transition: background-color 0.3s;
            
            &:hover {
              background: #f5f7fa;
            }
            
            .menu-icon {
              font-size: 16px;
              width: 20px;
              text-align: center;
            }
            
            .menu-text {
              font-size: 14px;
              color: #606266;
            }
          }
        }
      }
    }
    
    .header-user {
      display: flex;
      align-items: center;
      gap: 10px;
      position: relative;
      
      .user-avatar {
        position: relative;
        cursor: pointer;
        
        .avatar-img {
          width: 36px;
          height: 36px;
          border-radius: 50%;
          object-fit: cover;
          border: 2px solid #409eff;
        }
        
        .avatar-placeholder {
          width: 36px;
          height: 36px;
          border-radius: 50%;
          background: linear-gradient(135deg, #409eff, #66b1ff);
          color: white;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 14px;
          font-weight: 600;
          border: 2px solid white;
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        }
        
        .user-menu {
          position: absolute;
          top: 100%;
          right: 0;
          width: 300px;
          background: white;
          border: 1px solid #ebeef5;
          border-radius: 8px;
          box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
          z-index: 1000;
          margin-top: 10px;
          
          .user-info-card {
            padding: 20px;
            
            .user-card-header {
              display: flex;
              align-items: center;
              gap: 15px;
              margin-bottom: 20px;
              
              .card-avatar {
                img, .card-avatar-placeholder {
                  width: 60px;
                  height: 60px;
                  border-radius: 50%;
                  object-fit: cover;
                }
                
                .card-avatar-placeholder {
                  background: linear-gradient(135deg, #409eff, #66b1ff);
                  color: white;
                  display: flex;
                  align-items: center;
                  justify-content: center;
                  font-size: 20px;
                  font-weight: 600;
                }
              }
              
              .card-user-info {
                flex: 1;
                
                .card-user-name {
                  font-size: 18px;
                  font-weight: 600;
                  color: #303133;
                  margin-bottom: 4px;
                }
                
                .card-user-role {
                  font-size: 14px;
                  color: #409eff;
                  margin-bottom: 4px;
                }
                
                .card-user-email {
                  font-size: 12px;
                  color: #909399;
                }
              }
            }
            
            .user-card-body {
              .user-stats {
                display: grid;
                grid-template-columns: repeat(3, 1fr);
                gap: 15px;
                margin-bottom: 20px;
                
                .user-stat {
                  text-align: center;
                  padding: 10px;
                  background: #f8f9fa;
                  border-radius: 6px;
                  
                  .stat-value {
                    font-size: 18px;
                    font-weight: 600;
                    color: #409eff;
                    margin-bottom: 4px;
                  }
                  
                  .stat-label {
                    font-size: 12px;
                    color: #909399;
                  }
                }
              }
            }
            
            .user-card-footer {
              display: flex;
              gap: 8px;
              
              .user-action-btn {
                flex: 1;
                display: flex;
                align-items: center;
                justify-content: center;
                gap: 6px;
                padding: 8px 12px;
                background: #f5f7fa;
                border: 1px solid #dcdfe6;
                border-radius: 4px;
                color: #606266;
                font-size: 14px;
                cursor: pointer;
                transition: all 0.3s;
                
                &:hover {
                  background: #409eff;
                  border-color: #409eff;
                  color: white;
                }
              }
            }
          }
        }
      }
      
      .user-name {
        font-size: 14px;
        color: #303133;
        
        .user-role {
          display: block;
          font-size: 12px;
          color: #909399;
          margin-top: 2px;
        }
      }
    }
  }
  
  .header-tabs {
    border-top: 1px solid #ebeef5;
    
    .tabs-container {
      display: flex;
      padding: 0 20px;
      
      .tab-item {
        position: relative;
        padding: 12px 20px;
        color: #606266;
        font-size: 14px;
        cursor: pointer;
        transition: all 0.3s;
        display: flex;
        align-items: center;
        gap: 8px;
        
        &:hover {
          color: #409eff;
        }
        
        &.active {
          color: #409eff;
          font-weight: 600;
          
          &::after {
            content: '';
            position: absolute;
            bottom: 0;
            left: 0;
            right: 0;
            height: 3px;
            background: #409eff;
            border-radius: 3px 3px 0 0;
          }
        }
        
        .tab-badge {
          padding: 2px 6px;
          background: #f56c6c;
          color: white;
          border-radius: 10px;
          font-size: 12px;
          font-weight: 600;
        }
      }
    }
  }
  
  .header-breadcrumbs {
    padding: 10px 20px;
    background: #f8f9fa;
    border-top: 1px solid #ebeef5;
    
    .breadcrumbs-container {
      .breadcrumb-item {
        display: inline-block;
        
        .crumb-link {
          color: #409eff;
          cursor: pointer;
          font-size: 14px;
          transition: color 0.3s;
          
          &:hover {
            color: #66b1ff;
          }
          
          &.current {
            color: #606266;
            cursor: default;
          }
        }
        
        .crumb-separator {
          margin: 0 8px;
          color: #c0c4cc;
        }
      }
    }
  }
  
  .header-progress {
    padding: 10px 20px;
    background: #f8f9fa;
    
    .progress-bar {
      height: 6px;
      background: #ebeef5;
      border-radius: 3px;
      overflow: hidden;
      margin-bottom: 5px;
      
      .progress-fill {
        height: 100%;
        background: linear-gradient(to right, #67c23a, #85ce61);
        border-radius: 3px;
        transition: width 0.5s ease-out;
      }
    }
    
    .progress-text {
      font-size: 12px;
      color: #909399;
      text-align: center;
    }
  }
}
</style>