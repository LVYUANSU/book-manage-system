<template>
  <div class="level-menu" :class="menuClasses">
    <!-- 菜单头部 -->
    <div v-if="showHeader" class="menu-header">
      <div v-if="title" class="menu-title">{{ title }}</div>
      <div v-if="subtitle" class="menu-subtitle">{{ subtitle }}</div>
      
      <!-- 头部操作 -->
      <div v-if="headerActions.length > 0" class="header-actions">
        <button
          v-for="action in headerActions"
          :key="action.id"
          class="header-action-btn"
          :class="action.type"
          @click="handleHeaderAction(action)"
          :title="action.tooltip"
        >
          <span v-if="action.icon" class="action-icon">{{ action.icon }}</span>
          <span v-if="action.text" class="action-text">{{ action.text }}</span>
        </button>
      </div>
    </div>
    
    <!-- 搜索框 -->
    <div v-if="showSearch" class="menu-search">
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
    <div class="menu-content" :style="{ height: contentHeight + 'px' }">
      <!-- 空状态 -->
      <div v-if="filteredItems.length === 0" class="empty-state">
        <div class="empty-icon">📁</div>
        <div class="empty-text">{{ emptyText }}</div>
        <button v-if="showCreateButton" class="create-btn" @click="handleCreate">
          {{ createButtonText }}
        </button>
      </div>
      
      <!-- 树形菜单 -->
      <div v-else class="menu-tree">
        <div
          v-for="item in filteredItems"
          :key="item.id"
          class="menu-item"
          :class="getItemClasses(item)"
          @click="handleItemClick(item)"
          @mouseenter="hoveredItem = item.id"
          @mouseleave="hoveredItem = null"
        >
          <!-- 缩进线 -->
          <div class="item-indent" v-if="showIndentLines">
            <div
              v-for="level in getItemLevel(item)"
              :key="level"
              class="indent-line"
              :class="{ 'active': isLineActive(item, level) }"
            ></div>
          </div>
          
          <!-- 展开/折叠按钮 -->
          <div
            v-if="item.children && item.children.length > 0"
            class="item-expander"
            @click.stop="toggleExpand(item)"
          >
            <span class="expander-icon">
              {{ item.expanded ? '▼' : '▶' }}
            </span>
          </div>
          <div v-else class="item-placeholder"></div>
          
          <!-- 图标 -->
          <div class="item-icon">
            <span class="icon-content">
              {{ getItemIcon(item) }}
            </span>
          </div>
          
          <!-- 文本内容 -->
          <div class="item-content">
            <div class="item-title">
              {{ item.title }}
              <span v-if="item.badge" class="item-badge" :class="item.badgeType">
                {{ item.badge }}
              </span>
            </div>
            
            <div v-if="item.description" class="item-description">
              {{ item.description }}
            </div>
            
            <!-- 子信息 -->
            <div v-if="item.meta" class="item-meta">
              <span v-for="(meta, key) in item.meta" :key="key" class="meta-item">
                {{ meta }}
              </span>
            </div>
          </div>
          
          <!-- 操作按钮 -->
          <div v-if="showItemActions" class="item-actions">
            <button
              v-for="action in getItemActions(item)"
              :key="action.id"
              class="action-btn"
              :class="action.type"
              @click.stop="handleItemAction(item, action)"
              :title="action.tooltip"
            >
              <span class="action-icon">{{ action.icon }}</span>
            </button>
          </div>
          
          <!-- 选中标记 -->
          <div v-if="showCheckbox" class="item-checkbox">
            <input
              type="checkbox"
              :checked="isItemChecked(item)"
              @click.stop="toggleCheck(item)"
              class="checkbox-input"
            />
          </div>
        </div>
      </div>
    </div>
    
    <!-- 菜单底部 -->
    <div v-if="showFooter" class="menu-footer">
      <div class="footer-info">
        <span v-if="selectedCount > 0" class="selected-count">
          已选 {{ selectedCount }} 项
        </span>
        <span v-if="totalCount > 0" class="total-count">
          共 {{ totalCount }} 项
        </span>
      </div>
      
      <div v-if="footerActions.length > 0" class="footer-actions">
        <button
          v-for="action in footerActions"
          :key="action.id"
          class="footer-btn"
          :class="action.type"
          @click="handleFooterAction(action)"
          :disabled="action.disabled"
        >
          <span v-if="action.icon" class="btn-icon">{{ action.icon }}</span>
          <span class="btn-text">{{ action.text }}</span>
        </button>
      </div>
    </div>
    
    <!-- 上下文菜单 -->
    <div
      v-if="showContextMenu && contextMenu.visible"
      class="context-menu"
      :style="contextMenuStyle"
      @click.stop
    >
      <div class="context-menu-header">
        <div class="context-title">{{ contextMenu.item?.title }}</div>
        <button class="context-close" @click="closeContextMenu">×</button>
      </div>
      
      <div class="context-menu-items">
        <div
          v-for="action in contextActions"
          :key="action.id"
          class="context-item"
          @click="handleContextAction(action)"
        >
          <span class="context-icon">{{ action.icon }}</span>
          <span class="context-text">{{ action.text }}</span>
          <span v-if="action.shortcut" class="context-shortcut">
            {{ action.shortcut }}
          </span>
        </div>
      </div>
    </div>
    
    <!-- 创建菜单项模态框 -->
    <div v-if="showCreateModal" class="create-modal">
      <div class="modal-content">
        <h3 class="modal-title">{{ createModalTitle }}</h3>
        
        <div class="form-group">
          <label class="form-label">标题 *</label>
          <input
            v-model="newItem.title"
            type="text"
            class="form-input"
            placeholder="请输入标题"
            @keyup.enter="createItem"
          />
        </div>
        
        <div class="form-group">
          <label class="form-label">描述</label>
          <textarea
            v-model="newItem.description"
            class="form-textarea"
            placeholder="请输入描述"
            rows="3"
          ></textarea>
        </div>
        
        <div class="form-group">
          <label class="form-label">父级菜单</label>
          <select
            v-model="newItem.parentId"
            class="form-select"
          >
            <option value="">无 (顶级菜单)</option>
            <option
              v-for="parent in availableParents"
              :key="parent.id"
              :value="parent.id"
            >
              {{ getParentPath(parent) }}
            </option>
          </select>
        </div>
        
        <div class="form-group">
          <label class="form-label">图标</label>
          <div class="icon-selector">
            <div
              v-for="icon in availableIcons"
              :key="icon"
              class="icon-option"
              :class="{ 'selected': newItem.icon === icon }"
              @click="newItem.icon = icon"
            >
              {{ icon }}
            </div>
          </div>
        </div>
        
        <div class="modal-actions">
          <button class="modal-btn cancel" @click="closeCreateModal">
            取消
          </button>
          <button
            class="modal-btn confirm"
            @click="createItem"
            :disabled="!canCreateItem"
          >
            创建
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'LevelMenu',
  props: {
    // 基本配置
    items: {
      type: Array,
      required: true,
      default: () => []
    },
    title: {
      type: String,
      default: ''
    },
    subtitle: {
      type: String,
      default: ''
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
    showCheckbox: {
      type: Boolean,
      default: false
    },
    showItemActions: {
      type: Boolean,
      default: true
    },
    showIndentLines: {
      type: Boolean,
      default: true
    },
    showCreateButton: {
      type: Boolean,
      default: false
    },
    
    // 搜索配置
    searchPlaceholder: {
      type: String,
      default: '搜索菜单项...'
    },
    
    // 空状态配置
    emptyText: {
      type: String,
      default: '暂无菜单项'
    },
    createButtonText: {
      type: String,
      default: '创建新项'
    },
    
    // 操作配置
    headerActions: {
      type: Array,
      default: () => []
    },
    itemActions: {
      type: Array,
      default: () => [
        { id: 'edit', text: '编辑', icon: '✏️', type: 'primary' },
        { id: 'delete', text: '删除', icon: '🗑️', type: 'danger' }
      ]
    },
    footerActions: {
      type: Array,
      default: () => [
        { id: 'refresh', text: '刷新', icon: '🔄', type: 'default' },
        { id: 'expand-all', text: '展开全部', icon: '📂', type: 'default' },
        { id: 'collapse-all', text: '折叠全部', icon: '📁', type: 'default' }
      ]
    },
    contextActions: {
      type: Array,
      default: () => [
        { id: 'open', text: '打开', icon: '📂', shortcut: 'Enter' },
        { id: 'edit', text: '编辑', icon: '✏️', shortcut: 'Ctrl+E' },
        { id: 'copy', text: '复制', icon: '📋', shortcut: 'Ctrl+C' },
        { id: 'move', text: '移动', icon: '➡️', shortcut: 'Ctrl+M' },
        { id: 'delete', text: '删除', icon: '🗑️', shortcut: 'Del' }
      ]
    },
    
    // 选择配置
    multiple: {
      type: Boolean,
      default: false
    },
    selectedItems: {
      type: Array,
      default: () => []
    },
    
    // 创建配置
    showCreateModal: {
      type: Boolean,
      default: false
    },
    createModalTitle: {
      type: String,
      default: '创建菜单项'
    },
    availableIcons: {
      type: Array,
      default: () => ['📁', '📄', '📝', '📊', '🔧', '⚙️', '🔍', '➕', '⭐', '🔔']
    },
    
    // 样式配置
    type: {
      type: String,
      default: 'default',
      validator: (value) => ['default', 'primary', 'secondary', 'dark'].includes(value)
    },
    contentHeight: {
      type: Number,
      default: 400
    },
    indentWidth: {
      type: Number,
      default: 20
    }
  },
  data() {
    return {
      searchQuery: '',
      expandedItems: new Set(),
      checkedItems: new Set(this.selectedItems.map(item => item.id)),
      hoveredItem: null,
      contextMenu: {
        visible: false,
        x: 0,
        y: 0,
        item: null
      },
      newItem: {
        title: '',
        description: '',
        parentId: '',
        icon: '📁'
      }
    }
  },
  computed: {
    menuClasses() {
      return {
        [this.type]: true,
        'with-search': this.showSearch,
        'with-footer': this.showFooter
      }
    },
    
    filteredItems() {
      if (!this.searchQuery) {
        return this.getFlattenedItems(this.items)
      }
      
      const query = this.searchQuery.toLowerCase()
      return this.searchItems(this.items, query)
    },
    
    totalCount() {
      return this.countItems(this.items)
    },
    
    selectedCount() {
      return this.checkedItems.size
    },
    
    availableParents() {
      return this.getAvailableParents(this.items)
    },
    
    canCreateItem() {
      return this.newItem.title.trim().length > 0
    },
    
    contextMenuStyle() {
      return {
        left: this.contextMenu.x + 'px',
        top: this.contextMenu.y + 'px',
        display: this.contextMenu.visible ? 'block' : 'none'
      }
    }
  },
  watch: {
    selectedItems(newItems) {
      this.checkedItems = new Set(newItems.map(item => item.id))
    },
    
    checkedItems: {
      handler(newSet) {
        const selected = this.items
          .flatMap(item => this.getAllItems(item))
          .filter(item => newSet.has(item.id))
        
        this.$emit('selection-change', selected)
      },
      deep: true
    }
  },
  mounted() {
    // 初始化展开状态
    this.items.forEach(item => {
      if (item.expanded) {
        this.expandedItems.add(item.id)
      }
    })
    
    // 添加右键菜单监听
    this.$el.addEventListener('contextmenu', this.handleContextMenu)
    
    // 添加全局点击监听以关闭上下文菜单
    document.addEventListener('click', this.closeContextMenu)
    
    // 添加键盘快捷键
    document.addEventListener('keydown', this.handleKeydown)
  },
  beforeDestroy() {
    this.$el.removeEventListener('contextmenu', this.handleContextMenu)
    document.removeEventListener('click', this.closeContextMenu)
    document.removeEventListener('keydown', this.handleKeydown)
  },
  methods: {
    getFlattenedItems(items, level = 0, parentId = null) {
      let result = []
      
      items.forEach(item => {
        const flattenedItem = {
          ...item,
          level: level,
          parentId: parentId,
          expanded: this.expandedItems.has(item.id)
        }
        
        result.push(flattenedItem)
        
        if (item.children && item.children.length > 0 && this.expandedItems.has(item.id)) {
          const childItems = this.getFlattenedItems(item.children, level + 1, item.id)
          result = result.concat(childItems)
        }
      })
      
      return result
    },
    
    searchItems(items, query, level = 0, parentId = null) {
      let result = []
      
      items.forEach(item => {
        const matches = item.title.toLowerCase().includes(query) ||
                       (item.description && item.description.toLowerCase().includes(query))
        
        if (matches) {
          // 展开父级菜单以显示匹配项
          this.expandParents(item)
          
          result.push({
            ...item,
            level: level,
            parentId: parentId,
            expanded: this.expandedItems.has(item.id)
          })
        }
        
        if (item.children && item.children.length > 0) {
          const childResults = this.searchItems(item.children, query, level + 1, item.id)
          if (childResults.length > 0) {
            this.expandedItems.add(item.id)
            
            result.push({
              ...item,
              level: level,
              parentId: parentId,
              expanded: true
            })
            
            result = result.concat(childResults)
          }
        }
      })
      
      return result
    },
    
    expandParents(item) {
      let parentId = item.parentId
      while (parentId) {
        this.expandedItems.add(parentId)
        const parent = this.findItemById(this.items, parentId)
        parentId = parent?.parentId
      }
    },
    
    findItemById(items, id) {
      for (const item of items) {
        if (item.id === id) return item
        if (item.children && item.children.length > 0) {
          const found = this.findItemById(item.children, id)
          if (found) return found
        }
      }
      return null
    },
    
    getAllItems(items) {
      let result = []
      items.forEach(item => {
        result.push(item)
        if (item.children && item.children.length > 0) {
          result = result.concat(this.getAllItems(item.children))
        }
      })
      return result
    },
    
    countItems(items) {
      let count = 0
      items.forEach(item => {
        count++
        if (item.children && item.children.length > 0) {
          count += this.countItems(item.children)
        }
      })
      return count
    },
    
    getAvailableParents(items, level = 0) {
      let result = []
      
      items.forEach(item => {
        // 不能选择自己或自己的子项作为父级
        if (item.id !== this.newItem.id) {
          result.push({
            ...item,
            level: level,
            path: this.getParentPath(item)
          })
          
          if (item.children && item.children.length > 0) {
            const childParents = this.getAvailableParents(item.children, level + 1)
            result = result.concat(childParents)
          }
        }
      })
      
      return result
    },
    
    getParentPath(item) {
      let path = item.title
      let current = item
      
      while (current.parentId) {
        const parent = this.findItemById(this.items, current.parentId)
        if (parent) {
          path = parent.title + ' / ' + path
          current = parent
        } else {
          break
        }
      }
      
      return path
    },
    
    getItemLevel(item) {
      return Array.from({ length: item.level || 0 })
    },
    
    isLineActive(item, level) {
      // 检查该层级的缩进线是否应该高亮
      return item.level > level
    },
    
    getItemIcon(item) {
      if (item.icon) return item.icon
      
      // 默认图标
      if (item.children && item.children.length > 0) {
        return item.expanded ? '📂' : '📁'
      }
      
      return '📄'
    },
    
    getItemClasses(item) {
      return {
        'selected': this.isItemSelected(item),
        'checked': this.isItemChecked(item),
        'hovered': this.hoveredItem === item.id,
        'has-children': item.children && item.children.length > 0,
        'expanded': item.expanded,
        [`level-${item.level}`]: true
      }
    },
    
    isItemSelected(item) {
      // 这里可以根据需要实现选中逻辑
      return false
    },
    
    isItemChecked(item) {
      return this.checkedItems.has(item.id)
    },
    
    getItemActions(item) {
      // 合并默认操作和项特定的操作
      return [...this.itemActions, ...(item.actions || [])]
    },
    
    handleSearch() {
      this.$emit('search', this.searchQuery)
    },
    
    handleItemClick(item) {
      if (item.children && item.children.length > 0) {
        this.toggleExpand(item)
      }
      
      this.$emit('item-click', item)
      
      // 如果是单选模式，清空其他选择
      if (!this.multiple && !item.children) {
        this.checkedItems.clear()
        this.checkedItems.add(item.id)
      }
    },
    
    toggleExpand(item) {
      if (this.expandedItems.has(item.id)) {
        this.expandedItems.delete(item.id)
      } else {
        this.expandedItems.add(item.id)
      }
      
      this.$emit('expand-change', {
        item: item,
        expanded: this.expandedItems.has(item.id)
      })
    },
    
    toggleCheck(item) {
      if (this.checkedItems.has(item.id)) {
        this.checkedItems.delete(item.id)
      } else {
        if (!this.multiple) {
          this.checkedItems.clear()
        }
        this.checkedItems.add(item.id)
      }
      
      this.$emit('check-change', {
        item: item,
        checked: this.checkedItems.has(item.id)
      })
    },
    
    handleHeaderAction(action) {
      this.$emit('header-action', action)
    },
    
    handleItemAction(item, action) {
      this.$emit('item-action', { item, action })
    },
    
    handleFooterAction(action) {
      switch (action.id) {
        case 'refresh':
          this.$emit('refresh')
          break
        case 'expand-all':
          this.expandAll()
          break
        case 'collapse-all':
          this.collapseAll()
          break
        default:
          this.$emit('footer-action', action)
      }
    },
    
    handleCreate() {
      this.$emit('create')
    },
    
    expandAll() {
      const allItems = this.getAllItems(this.items)
      allItems.forEach(item => {
        if (item.children && item.children.length > 0) {
          this.expandedItems.add(item.id)
        }
      })
      this.$emit('expand-all')
    },
    
    collapseAll() {
      this.expandedItems.clear()
      this.$emit('collapse-all')
    },
    
    handleContextMenu(event) {
      const target = event.target.closest('.menu-item')
      if (target) {
        event.preventDefault()
        
        const itemId = target.dataset.itemId
        const item = this.findItemById(this.items, itemId)
        
        if (item) {
          this.contextMenu = {
            visible: true,
            x: event.clientX,
            y: event.clientY,
            item: item
          }
        }
      }
    },
    
    handleContextAction(action) {
      if (this.contextMenu.item) {
        this.$emit('context-action', {
          item: this.contextMenu.item,
          action: action
        })
      }
      this.closeContextMenu()
    },
    
    closeContextMenu() {
      this.contextMenu.visible = false
    },
    
    handleKeydown(event) {
      if (this.contextMenu.visible) {
        switch (event.key) {
          case 'Escape':
            this.closeContextMenu()
            break
          case 'Enter':
            // 执行默认的上下文操作
            if (this.contextActions[0]) {
              this.handleContextAction(this.contextActions[0])
            }
            break
        }
      }
    },
    
    openCreateModal() {
      this.newItem = {
        title: '',
        description: '',
        parentId: '',
        icon: '📁'
      }
      this.$emit('open-create-modal')
    },
    
    closeCreateModal() {
      this.$emit('close-create-modal')
    },
    
    createItem() {
      if (!this.canCreateItem) return
      
      this.$emit('create-item', { ...this.newItem })
      this.closeCreateModal()
    }
  }
}
</script>

<style scoped lang="scss">
.level-menu {
  background: white;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: all 0.3s;
  
  &.primary {
    border-color: #409eff;
    
    .menu-header {
      background: linear-gradient(135deg, #409eff, #66b1ff);
      color: white;
    }
    
    .menu-item:hover {
      background: #ecf5ff;
    }
    
    .menu-item.selected {
      background: #409eff;
      color: white;
    }
  }
  
  &.secondary {
    background: #f8f9fa;
    border-color: #ebeef5;
  }
  
  &.dark {
    background: #2c3e50;
    color: white;
    border-color: #34495e;
    
    .menu-header {
      background: #34495e;
    }
    
    .menu-item {
      color: white;
      
      &:hover {
        background: #3a5069;
      }
      
      &.selected {
        background: #409eff;
      }
    }
  }
  
  .menu-header {
    padding: 20px;
    background: #f8f9fa;
    border-bottom: 1px solid #ebeef5;
    
    .menu-title {
      font-size: 20px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 5px;
    }
    
    .menu-subtitle {
      font-size: 14px;
      color: #909399;
      margin-bottom: 15px;
    }
    
    .header-actions {
      display: flex;
      gap: 8px;
      
      .header-action-btn {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        padding: 6px 12px;
        border: 1px solid #dcdfe6;
        border-radius: 4px;
        background: white;
        color: #606266;
        font-size: 14px;
        cursor: pointer;
        transition: all 0.3s;
        
        &:hover {
          background: #f5f7fa;
          border-color: #c0c4cc;
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
          font-size: 14px;
        }
      }
    }
  }
  
  .menu-search {
    padding: 15px 20px;
    border-bottom: 1px solid #ebeef5;
    
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
  
  .menu-content {
    flex: 1;
    overflow-y: auto;
    min-height: 200px;
    
    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 100%;
      padding: 40px 20px;
      color: #909399;
      
      .empty-icon {
        font-size: 48px;
        margin-bottom: 15px;
        opacity: 0.5;
      }
      
      .empty-text {
        font-size: 16px;
        margin-bottom: 20px;
        text-align: center;
      }
      
      .create-btn {
        padding: 8px 20px;
        background: #409eff;
        color: white;
        border: none;
        border-radius: 4px;
        font-size: 14px;
        cursor: pointer;
        transition: background-color 0.3s;
        
        &:hover {
          background: #66b1ff;
        }
      }
    }
    
    .menu-tree {
      .menu-item {
        display: flex;
        align-items: center;
        padding: 10px 20px;
        border-bottom: 1px solid #f5f7fa;
        cursor: pointer;
        transition: all 0.3s;
        position: relative;
        
        &:hover {
          background: #f8f9fa;
        }
        
        &.selected {
          background: #ecf5ff;
          border-left: 3px solid #409eff;
        }
        
        &.checked {
          background: #f0f9eb;
        }
        
        &.hovered {
          background: #f5f7fa;
        }
        
        .item-indent {
          display: flex;
          
          .indent-line {
            width: 20px;
            height: 100%;
            border-left: 1px solid #dcdfe6;
            margin-right: 20px;
            
            &.active {
              border-left-color: #409eff;
            }
          }
        }
        
        .item-expander,
        .item-placeholder {
          width: 20px;
          display: flex;
          align-items: center;
          justify-content: center;
          
          .expander-icon {
            font-size: 12px;
            color: #909399;
            transition: transform 0.3s;
          }
        }
        
        .item-icon {
          width: 24px;
          height: 24px;
          display: flex;
          align-items: center;
          justify-content: center;
          margin-right: 10px;
          
          .icon-content {
            font-size: 16px;
          }
        }
        
        .item-content {
          flex: 1;
          min-width: 0;
          
          .item-title {
            font-size: 14px;
            color: #303133;
            font-weight: 500;
            margin-bottom: 4px;
            display: flex;
            align-items: center;
            gap: 8px;
            
            .item-badge {
              padding: 2px 6px;
              border-radius: 10px;
              font-size: 11px;
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
            }
          }
          
          .item-description {
            font-size: 12px;
            color: #909399;
            line-height: 1.4;
            margin-bottom: 4px;
          }
          
          .item-meta {
            display: flex;
            gap: 10px;
            
            .meta-item {
              font-size: 11px;
              color: #c0c4cc;
              
              &:not(:last-child)::after {
                content: '•';
                margin-left: 10px;
              }
            }
          }
        }
        
        .item-actions {
          display: flex;
          gap: 4px;
          opacity: 0;
          transition: opacity 0.3s;
          
          .action-btn {
            width: 28px;
            height: 28px;
            display: flex;
            align-items: center;
            justify-content: center;
            background: white;
            border: 1px solid #dcdfe6;
            border-radius: 4px;
            color: #606266;
            font-size: 12px;
            cursor: pointer;
            transition: all 0.3s;
            
            &:hover {
              background: #f5f7fa;
              border-color: #c0c4cc;
              
              &.primary {
                background: #409eff;
                border-color: #409eff;
                color: white;
              }
              
              &.danger {
                background: #f56c6c;
                border-color: #f56c6c;
                color: white;
              }
            }
          }
        }
        
        .item-checkbox {
          margin-left: 10px;
          
          .checkbox-input {
            width: 16px;
            height: 16px;
            cursor: pointer;
          }
        }
        
        &:hover .item-actions {
          opacity: 1;
        }
      }
    }
  }
  
  .menu-footer {
    padding: 15px 20px;
    background: #f8f9fa;
    border-top: 1px solid #ebeef5;
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .footer-info {
      font-size: 12px;
      color: #909399;
      
      .selected-count {
        margin-right: 10px;
        color: #409eff;
      }
    }
    
    .footer-actions {
      display: flex;
      gap: 8px;
      
      .footer-btn {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        padding: 6px 12px;
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
        
        .btn-icon {
          font-size: 14px;
        }
      }
    }
  }
  
  .context-menu {
    position: fixed;
    background: white;
    border: 1px solid #ebeef5;
    border-radius: 6px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
    z-index: 1000;
    min-width: 200px;
    
    .context-menu-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 15px;
      border-bottom: 1px solid #ebeef5;
      background: #f8f9fa;
      
      .context-title {
        font-size: 14px;
        font-weight: 600;
        color: #303133;
        max-width: 150px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      
      .context-close {
        width: 20px;
        height: 20px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: none;
        border: none;
        color: #909399;
        cursor: pointer;
        font-size: 16px;
        line-height: 1;
        
        &:hover {
          color: #f56c6c;
        }
      }
    }
    
    .context-menu-items {
      padding: 8px 0;
      
      .context-item {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 8px 15px;
        cursor: pointer;
        transition: background-color 0.3s;
        
        &:hover {
          background: #f5f7fa;
        }
        
        .context-icon {
          font-size: 14px;
          margin-right: 10px;
          width: 20px;
          text-align: center;
        }
        
        .context-text {
          flex: 1;
          font-size: 14px;
          color: #606266;
        }
        
        .context-shortcut {
          font-size: 11px;
          color: #909399;
          margin-left: 10px;
        }
      }
    }
  }
  
  .create-modal {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 2000;
    
    .modal-content {
      background: white;
      border-radius: 8px;
      padding: 30px;
      width: 90%;
      max-width: 500px;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
      
      .modal-title {
        margin: 0 0 20px 0;
        color: #303133;
        font-size: 18px;
        font-weight: 600;
      }
      
      .form-group {
        margin-bottom: 20px;
        
        .form-label {
          display: block;
          margin-bottom: 8px;
          color: #606266;
          font-size: 14px;
          font-weight: 500;
          
          &::after {
            content: '*';
            color: #f56c6c;
            margin-left: 4px;
          }
        }
        
        .form-input,
        .form-textarea,
        .form-select {
          width: 100%;
          padding: 10px 15px;
          border: 1px solid #dcdfe6;
          border-radius: 4px;
          font-size: 14px;
          color: #303133;
          background: white;
          transition: all 0.3s;
          
          &:focus {
            outline: none;
            border-color: #409eff;
            box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
          }
        }
        
        .form-textarea {
          resize: vertical;
          min-height: 80px;
          font-family: inherit;
        }
        
        .icon-selector {
          display: flex;
          flex-wrap: wrap;
          gap: 8px;
          
          .icon-option {
            width: 36px;
            height: 36px;
            display: flex;
            align-items: center;
            justify-content: center;
            background: #f5f7fa;
            border: 1px solid #dcdfe6;
            border-radius: 4px;
            font-size: 16px;
            cursor: pointer;
            transition: all 0.3s;
            
            &:hover {
              background: #ecf5ff;
              border-color: #c6e2ff;
            }
            
            &.selected {
              background: #409eff;
              border-color: #409eff;
              color: white;
            }
          }
        }
      }
      
      .modal-actions {
        display: flex;
        justify-content: flex-end;
        gap: 10px;
        margin-top: 30px;
        
        .modal-btn {
          padding: 8px 20px;
          border: none;
          border-radius: 4px;
          font-size: 14px;
          cursor: pointer;
          transition: all 0.3s;
          
          &.cancel {
            background: #f5f7fa;
            color: #606266;
            border: 1px solid #dcdfe6;
            
            &:hover {
              background: #ebeef5;
            }
          }
          
          &.confirm {
            background: #409eff;
            color: white;
            
            &:hover:not(:disabled) {
              background: #66b1ff;
            }
            
            &:disabled {
              opacity: 0.6;
              cursor: not-allowed;
            }
          }
        }
      }
    }
  }
}
</style>