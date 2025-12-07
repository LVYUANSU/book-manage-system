<template>
  <div class="vertical-menu" :class="menuClasses" :style="menuStyle">
    <!-- 菜单头部 -->
    <div v-if="showHeader" class="menu-header" @click="toggleCollapse" v-if="collapsible">
      <div class="header-content">
        <div v-if="logo" class="header-logo">
          <img v-if="logo.image" :src="logo.image" :alt="logo.alt" class="logo-image" />
          <span v-else-if="logo.icon" class="logo-icon">{{ logo.icon }}</span>
          <span v-else class="logo-text">{{ getInitials(title) }}</span>
        </div>
        
        <div v-if="!collapsed" class="header-info">
          <h3 v-if="title" class="header-title">{{ title }}</h3>
          <div v-if="subtitle" class="header-subtitle">{{ subtitle }}</div>
        </div>
      </div>
      
      <button class="collapse-btn" :class="{ 'collapsed': collapsed }">
        <span class="collapse-icon">{{ collapsed ? '→' : '←' }}</span>
      </button>
    </div>
    
    <!-- 搜索框 -->
    <div v-if="showSearch && !collapsed" class="menu-search">
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
    
    <!-- 菜单内容 -->
    <div class="menu-content" :style="contentStyle">
      <!-- 空状态 -->
      <div v-if="filteredItems.length === 0" class="empty-menu">
        <div class="empty-icon">📁</div>
        <div class="empty-text">{{ emptyText }}</div>
      </div>
      
      <!-- 菜单项 -->
      <div class="menu-items">
        <!-- 分组菜单 -->
        <template v-for="item in filteredItems" :key="item.id">
          <!-- 分组标题 -->
          <div
            v-if="item.type === 'group'"
            class="menu-group"
            :class="{ 'collapsed': collapsedGroups.has(item.id) }"
          >
            <div
              class="group-header"
              @click="toggleGroup(item.id)"
              v-if="!collapsed"
            >
              <span v-if="item.icon" class="group-icon">{{ item.icon }}</span>
              <span class="group-title">{{ item.title }}</span>
              <span v-if="item.badge" class="group-badge">{{ item.badge }}</span>
              <span class="group-arrow">{{ collapsedGroups.has(item.id) ? '▶' : '▼' }}</span>
            </div>
            
            <!-- 分组内容 -->
            <div
              v-if="!collapsedGroups.has(item.id) || collapsed"
              class="group-content"
            >
              <div
                v-for="child in item.children"
                :key="child.id"
                class="menu-item child-item"
                :class="getItemClasses(child)"
                @click="handleItemClick(child)"
                @mouseenter="hoveredItem = child.id"
                @mouseleave="hoveredItem = null"
              >
                <div class="item-indicator" v-if="showIndicators"></div>
                
                <div class="item-content">
                  <span v-if="child.icon" class="item-icon">{{ child.icon }}</span>
                  <span v-if="!collapsed" class="item-text">{{ child.title }}</span>
                </div>
                
                <div class="item-meta" v-if="!collapsed">
                  <span v-if="child.badge" class="item-badge" :class="child.badgeType">
                    {{ child.badge }}
                  </span>
                  <span v-if="child.hot && showHot" class="item-hot">HOT</span>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 普通菜单项 -->
          <div
            v-else
            class="menu-item"
            :class="getItemClasses(item)"
            @click="handleItemClick(item)"
            @mouseenter="hoveredItem = item.id"
            @mouseleave="hoveredItem = null"
          >
            <div class="item-indicator" v-if="showIndicators"></div>
            
            <div class="item-content">
              <span v-if="item.icon" class="item-icon">{{ item.icon }}</span>
              <span v-if="!collapsed" class="item-text">{{ item.title }}</span>
              <span v-if="collapsed && item.title" class="item-tooltip">{{ item.title }}</span>
            </div>
            
            <div class="item-meta" v-if="!collapsed">
              <span v-if="item.badge" class="item-badge" :class="item.badgeType">
                {{ item.badge }}
              </span>
              <span v-if="item.hot && showHot" class="item-hot">HOT</span>
              <span v-if="item.children && item.children.length > 0" class="item-arrow">
                {{ expandedItems.has(item.id) ? '▼' : '▶' }}
              </span>
            </div>
            
            <!-- 子菜单 -->
            <div
              v-if="item.children && item.children.length > 0 && expandedItems.has(item.id) && !collapsed"
              class="sub-menu"
            >
              <div
                v-for="child in item.children"
                :key="child.id"
                class="sub-item"
                :class="getItemClasses(child)"
                @click.stop="handleItemClick(child)"
              >
                <span class="sub-icon">{{ child.icon || '○' }}</span>
                <span class="sub-text">{{ child.title }}</span>
                <span v-if="child.badge" class="sub-badge">{{ child.badge }}</span>
              </div>
            </div>
          </div>
        </template>
      </div>
    </div>
    
    <!-- 菜单底部 -->
    <div v-if="showFooter && !collapsed" class="menu-footer">
      <div class="user-info" v-if="userInfo" @click="handleUserClick">
        <div class="user-avatar">
          <img
            v-if="userInfo.avatar"
            :src="userInfo.avatar"
            :alt="userInfo.name"
            class="avatar-image"
          />
          <div v-else class="avatar-placeholder">
            {{ getUserInitials(userInfo.name) }}
          </div>
        </div>
        <div class="user-details">
          <div class="user-name">{{ userInfo.name }}</div>
          <div v-if="userInfo.role" class="user-role">{{ userInfo.role }}</div>
        </div>
      </div>
      
      <div class="footer-actions" v-if="footerActions.length > 0">
        <button
          v-for="action in footerActions"
          :key="action.id"
          class="footer-action"
          @click="handleFooterAction(action)"
          :title="action.tooltip"
        >
          <span class="action-icon">{{ action.icon }}</span>
          <span class="action-text">{{ action.text }}</span>
        </button>
      </div>
    </div>
    
    <!-- 展开/折叠按钮（底部） -->
    <div v-if="collapsible && showCollapseButton" class="collapse-bottom">
      <button class="collapse-bottom-btn" @click="toggleCollapse">
        <span class="collapse-icon">{{ collapsed ? '→' : '←' }}</span>
        <span v-if="!collapsed" class="collapse-text">收起菜单</span>
      </button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'VerticalMenu',
  props: {
    // 菜单配置
    items: {
      type: Array,
      required: true,
      default: () => []
    },
    
    // 标题配置
    title: {
      type: String,
      default: '菜单'
    },
    subtitle: {
      type: String,
      default: ''
    },
    logo: {
      type: Object,
      default: null
    },
    
    // 用户信息
    userInfo: {
      type: Object,
      default: null
    },
    
    // 显示配置
    showHeader: {
      type: Boolean,
      default: true
    },
    showSearch: {
      type: Boolean,
      default: true
    },
    showFooter: {
      type: Boolean,
      default: true
    },
    showIndicators: {
      type: Boolean,
      default: true
    },
    showHot: {
      type: Boolean,
      default: true
    },
    showCollapseButton: {
      type: Boolean,
      default: true
    },
    collapsible: {
      type: Boolean,
      default: true
    },
    collapsed: {
      type: Boolean,
      default: false
    },
    
    // 搜索配置
    searchPlaceholder: {
      type: String,
      default: '搜索菜单...'
    },
    searchable: {
      type: Boolean,
      default: true
    },
    
    // 空状态配置
    emptyText: {
      type: String,
      default: '暂无菜单项'
    },
    
    // 底部操作
    footerActions: {
      type: Array,
      default: () => [
        { id: 'settings', text: '设置', icon: '⚙️', tooltip: '系统设置' },
        { id: 'help', text: '帮助', icon: '❓', tooltip: '帮助中心' },
        { id: 'logout', text: '退出', icon: '🚪', tooltip: '退出登录' }
      ]
    },
    
    // 样式配置
    width: {
      type: [String, Number],
      default: 240
    },
    collapsedWidth: {
      type: [String, Number],
      default: 64
    },
    theme: {
      type: String,
      default: 'light', // 'light', 'dark', 'primary'
      validator: (value) => ['light', 'dark', 'primary'].includes(value)
    },
    position: {
      type: String,
      default: 'left', // 'left', 'right'
      validator: (value) => ['left', 'right'].includes(value)
    },
    fixed: {
      type: Boolean,
      default: false
    },
    bordered: {
      type: Boolean,
      default: true
    },
    
    // 激活项
    activeItem: {
      type: String,
      default: ''
    },
    
    // 默认展开的组
    defaultExpandedGroups: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      collapsedLocal: this.collapsed,
      searchQuery: '',
      hoveredItem: null,
      expandedItems: new Set(),
      collapsedGroups: new Set(),
      filteredItems: []
    }
  },
  computed: {
    menuClasses() {
      return {
        [this.theme]: true,
        [this.position]: true,
        'collapsed': this.collapsedLocal,
        'fixed': this.fixed,
        'bordered': this.bordered,
        'has-header': this.showHeader,
        'has-footer': this.showFooter,
        'has-search': this.showSearch
      }
    },
    
    menuStyle() {
      const width = this.collapsedLocal ? this.collapsedWidth : this.width
      return {
        width: typeof width === 'number' ? width + 'px' : width
      }
    },
    
    contentStyle() {
      return {
        maxHeight: this.fixed ? 'calc(100vh - 120px)' : 'none'
      }
    },
    
    showSearch() {
      return this.searchable && !this.collapsedLocal
    }
  },
  watch: {
    collapsed(newVal) {
      this.collapsedLocal = newVal
    },
    
    items: {
      handler(newItems) {
        this.filteredItems = this.searchQuery ? this.filterMenuItems(newItems) : newItems
      },
      deep: true
    },
    
    searchQuery() {
      this.filteredItems = this.searchQuery ? this.filterMenuItems(this.items) : this.items
    },
    
    defaultExpandedGroups: {
      handler(groups) {
        groups.forEach(groupId => {
          this.expandedItems.add(groupId)
        })
      },
      immediate: true
    }
  },
  mounted() {
    this.filteredItems = [...this.items]
    
    // 初始化展开的组
    this.items.forEach(item => {
      if (item.type === 'group' && item.defaultExpanded !== false) {
        this.expandedItems.add(item.id)
      }
    })
  },
  methods: {
    getInitials(text) {
      if (!text) return 'M'
      return text
        .split(' ')
        .map(word => word.charAt(0))
        .join('')
        .toUpperCase()
        .substring(0, 2)
    },
    
    getUserInitials(name) {
      return this.getInitials(name)
    },
    
    filterMenuItems(items) {
      const query = this.searchQuery.toLowerCase()
      const result = []
      
      items.forEach(item => {
        if (item.type === 'group') {
          // 过滤分组内的子项
          const filteredChildren = item.children?.filter(child =>
            child.title.toLowerCase().includes(query)
          )
          
          if (filteredChildren && filteredChildren.length > 0) {
            result.push({
              ...item,
              children: filteredChildren
            })
          } else if (item.title.toLowerCase().includes(query)) {
            result.push(item)
          }
        } else if (item.title.toLowerCase().includes(query)) {
          result.push(item)
        }
      })
      
      return result
    },
    
    getItemClasses(item) {
      return {
        'active': this.activeItem === item.id || this.activeItem === item.path,
        'hovered': this.hoveredItem === item.id,
        'has-children': item.children && item.children.length > 0,
        'expanded': this.expandedItems.has(item.id),
        [`level-${item.level || 0}`]: true,
        [item.type]: true
      }
    },
    
    handleItemClick(item) {
      if (item.children && item.children.length > 0) {
        // 如果有子菜单，切换展开状态
        if (this.expandedItems.has(item.id)) {
          this.expandedItems.delete(item.id)
        } else {
          this.expandedItems.add(item.id)
        }
      } else {
        // 触发点击事件
        this.$emit('item-click', item)
        
        // 如果菜单项有路径，更新激活状态
        if (item.path) {
          this.$emit('update:activeItem', item.id)
        }
      }
    },
    
    toggleGroup(groupId) {
      if (this.collapsedGroups.has(groupId)) {
        this.collapsedGroups.delete(groupId)
      } else {
        this.collapsedGroups.add(groupId)
      }
    },
    
    toggleCollapse() {
      this.collapsedLocal = !this.collapsedLocal
      this.$emit('update:collapsed', this.collapsedLocal)
      this.$emit('collapse-change', this.collapsedLocal)
    },
    
    handleSearch() {
      this.$emit('search', this.searchQuery)
    },
    
    handleUserClick() {
      this.$emit('user-click', this.userInfo)
    },
    
    handleFooterAction(action) {
      this.$emit('footer-action', action)
    }
  }
}
</script>

<style scoped lang="scss">
.vertical-menu {
  display: flex;
  flex-direction: column;
  background: white;
  transition: all 0.3s ease;
  height: 100%;
  position: relative;
  
  &.fixed {
    position: fixed;
    top: 0;
    height: 100vh;
    z-index: 1000;
    
    &.left {
      left: 0;
    }
    
    &.right {
      right: 0;
    }
  }
  
  &.bordered {
    border-right: 1px solid #dcdfe6;
    
    &.right {
      border-right: none;
      border-left: 1px solid #dcdfe6;
    }
  }
  
  // 主题
  &.light {
    background: white;
    color: #303133;
  }
  
  &.dark {
    background: #2c3e50;
    color: white;
    
    .menu-header,
    .menu-search,
    .menu-footer {
      background: #34495e;
    }
    
    .menu-item {
      &.active {
        background: #409eff;
        color: white;
      }
      
      &:hover:not(.active) {
        background: #3a5069;
      }
    }
  }
  
  &.primary {
    background: linear-gradient(135deg, #409eff, #66b1ff);
    color: white;
    
    .menu-header,
    .menu-search,
    .menu-footer {
      background: rgba(255, 255, 255, 0.1);
    }
    
    .menu-item {
      &.active {
        background: rgba(255, 255, 255, 0.2);
      }
      
      &:hover:not(.active) {
        background: rgba(255, 255, 255, 0.1);
      }
    }
  }
  
  // 折叠状态
  &.collapsed {
    .item-text,
    .group-title,
    .header-info,
    .user-details,
    .footer-action .action-text,
    .collapse-text {
      display: none;
    }
    
    .menu-search {
      display: none;
    }
    
    .menu-group {
      margin-bottom: 8px;
      
      .group-header {
        justify-content: center;
        padding: 12px;
        
        .group-icon,
        .group-badge,
        .group-arrow {
          display: none;
        }
      }
      
      .group-content {
        display: none;
      }
    }
    
    .menu-item {
      justify-content: center;
      padding: 12px;
      
      .item-content {
        justify-content: center;
      }
      
      .item-meta {
        display: none;
      }
      
      .item-tooltip {
        position: absolute;
        left: 100%;
        top: 50%;
        transform: translateY(-50%);
        background: #303133;
        color: white;
        padding: 6px 12px;
        border-radius: 4px;
        font-size: 12px;
        white-space: nowrap;
        opacity: 0;
        visibility: hidden;
        transition: all 0.3s;
        z-index: 1001;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
        
        &::before {
          content: '';
          position: absolute;
          right: 100%;
          top: 50%;
          transform: translateY(-50%);
          border: 4px solid transparent;
          border-right-color: #303133;
        }
      }
      
      &:hover .item-tooltip {
        opacity: 1;
        visibility: visible;
        margin-left: 10px;
      }
    }
  }
  
  .menu-header {
    padding: 20px;
    border-bottom: 1px solid #ebeef5;
    cursor: pointer;
    user-select: none;
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .header-content {
      display: flex;
      align-items: center;
      gap: 12px;
      
      .header-logo {
        width: 40px;
        height: 40px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #409eff;
        color: white;
        font-weight: 700;
        flex-shrink: 0;
        
        .logo-image {
          width: 100%;
          height: 100%;
          object-fit: cover;
          border-radius: 8px;
        }
        
        .logo-icon {
          font-size: 20px;
        }
        
        .logo-text {
          font-size: 14px;
        }
      }
      
      .header-info {
        .header-title {
          margin: 0;
          font-size: 18px;
          font-weight: 600;
          line-height: 1.2;
        }
        
        .header-subtitle {
          font-size: 12px;
          opacity: 0.7;
          margin-top: 4px;
        }
      }
    }
    
    .collapse-btn {
      width: 24px;
      height: 24px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #f5f7fa;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      color: #606266;
      transition: all 0.3s;
      
      &:hover {
        background: #409eff;
        color: white;
      }
      
      &.collapsed {
        transform: rotate(180deg);
      }
      
      .collapse-icon {
        font-size: 12px;
        line-height: 1;
      }
    }
  }
  
  .menu-search {
    padding: 16px;
    border-bottom: 1px solid #ebeef5;
    
    .search-container {
      position: relative;
      
      .search-input {
        width: 100%;
        padding: 8px 36px 8px 12px;
        border: 1px solid #dcdfe6;
        border-radius: 4px;
        font-size: 14px;
        background: white;
        color: #303133;
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
        padding: 0 12px;
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
  
  .menu-content {
    flex: 1;
    overflow-y: auto;
    overflow-x: hidden;
    
    .empty-menu {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 40px 20px;
      color: #909399;
      
      .empty-icon {
        font-size: 48px;
        margin-bottom: 16px;
        opacity: 0.5;
      }
      
      .empty-text {
        font-size: 14px;
        text-align: center;
      }
    }
    
    .menu-items {
      padding: 8px 0;
      
      .menu-group {
        margin-bottom: 16px;
        
        .group-header {
          padding: 12px 16px;
          font-size: 12px;
          color: #909399;
          text-transform: uppercase;
          letter-spacing: 1px;
          display: flex;
          align-items: center;
          gap: 8px;
          cursor: pointer;
          user-select: none;
          transition: all 0.3s;
          
          &:hover {
            color: #409eff;
          }
          
          .group-icon {
            font-size: 14px;
          }
          
          .group-title {
            flex: 1;
            font-weight: 600;
          }
          
          .group-badge {
            padding: 2px 6px;
            background: #f56c6c;
            color: white;
            border-radius: 10px;
            font-size: 10px;
            font-weight: 600;
          }
          
          .group-arrow {
            font-size: 10px;
            transition: transform 0.3s;
          }
        }
        
        &.collapsed .group-arrow {
          transform: rotate(-90deg);
        }
        
        .group-content {
          .child-item {
            padding-left: 40px;
          }
        }
      }
      
      .menu-item {
        position: relative;
        padding: 12px 16px;
        display: flex;
        align-items: center;
        gap: 12px;
        cursor: pointer;
        transition: all 0.3s;
        user-select: none;
        
        &:hover:not(.active) {
          background: #f5f7fa;
        }
        
        &.active {
          background: #ecf5ff;
          color: #409eff;
          font-weight: 600;
          
          .item-indicator {
            opacity: 1;
            transform: scaleY(1);
          }
        }
        
        .item-indicator {
          position: absolute;
          left: 0;
          top: 50%;
          transform: translateY(-50%) scaleY(0.8);
          width: 3px;
          height: 20px;
          background: #409eff;
          border-radius: 0 3px 3px 0;
          opacity: 0;
          transition: all 0.3s;
        }
        
        .item-content {
          flex: 1;
          display: flex;
          align-items: center;
          gap: 12px;
          min-width: 0;
          
          .item-icon {
            font-size: 16px;
            width: 20px;
            text-align: center;
            flex-shrink: 0;
          }
          
          .item-text {
            flex: 1;
            font-size: 14px;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }
        }
        
        .item-meta {
          display: flex;
          align-items: center;
          gap: 8px;
          flex-shrink: 0;
          
          .item-badge {
            padding: 2px 6px;
            border-radius: 10px;
            font-size: 10px;
            font-weight: 600;
            white-space: nowrap;
            
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
          
          .item-hot {
            padding: 2px 6px;
            background: #f56c6c;
            color: white;
            border-radius: 10px;
            font-size: 10px;
            font-weight: 600;
          }
          
          .item-arrow {
            font-size: 10px;
            color: #909399;
            transition: transform 0.3s;
          }
        }
        
        &.expanded .item-arrow {
          transform: rotate(90deg);
        }
        
        .sub-menu {
          background: #f8f9fa;
          border-radius: 4px;
          margin: 8px 0 8px 20px;
          overflow: hidden;
          
          .sub-item {
            padding: 8px 12px 8px 32px;
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 13px;
            color: #606266;
            cursor: pointer;
            transition: all 0.3s;
            
            &:hover {
              background: #ecf5ff;
              color: #409eff;
            }
            
            &.active {
              background: #ecf5ff;
              color: #409eff;
              font-weight: 600;
            }
            
            .sub-icon {
              font-size: 12px;
              width: 16px;
              text-align: center;
            }
            
            .sub-text {
              flex: 1;
            }
            
            .sub-badge {
              padding: 1px 4px;
              background: #f56c6c;
              color: white;
              border-radius: 8px;
              font-size: 9px;
              font-weight: 600;
            }
          }
        }
      }
    }
  }
  
  .menu-footer {
    padding: 16px;
    border-top: 1px solid #ebeef5;
    
    .user-info {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px;
      border-radius: 6px;
      cursor: pointer;
      transition: all 0.3s;
      
      &:hover {
        background: #f5f7fa;
      }
      
      .user-avatar {
        width: 36px;
        height: 36px;
        border-radius: 50%;
        overflow: hidden;
        flex-shrink: 0;
        
        .avatar-image {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
        
        .avatar-placeholder {
          width: 100%;
          height: 100%;
          display: flex;
          align-items: center;
          justify-content: center;
          background: linear-gradient(135deg, #409eff, #66b1ff);
          color: white;
          font-weight: 600;
          font-size: 14px;
        }
      }
      
      .user-details {
        flex: 1;
        min-width: 0;
        
        .user-name {
          font-size: 14px;
          font-weight: 600;
          color: #303133;
          margin-bottom: 2px;
          white-space: nowrap;
          overflow: hidden;
          text-overflow: ellipsis;
        }
        
        .user-role {
          font-size: 12px;
          color: #909399;
        }
      }
    }
    
    .footer-actions {
      display: flex;
      justify-content: space-around;
      margin-top: 16px;
      padding-top: 16px;
      border-top: 1px solid #ebeef5;
      
      .footer-action {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 4px;
        background: none;
        border: none;
        cursor: pointer;
        color: #606266;
        font-size: 12px;
        transition: all 0.3s;
        
        &:hover {
          color: #409eff;
        }
        
        .action-icon {
          font-size: 18px;
        }
        
        .action-text {
          white-space: nowrap;
        }
      }
    }
  }
  
  .collapse-bottom {
    padding: 12px;
    border-top: 1px solid #ebeef5;
    
    .collapse-bottom-btn {
      width: 100%;
      padding: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
      background: #f5f7fa;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      color: #606266;
      font-size: 14px;
      transition: all 0.3s;
      
      &:hover {
        background: #409eff;
        color: white;
      }
      
      .collapse-icon {
        font-size: 12px;
      }
    }
  }
}
</style>