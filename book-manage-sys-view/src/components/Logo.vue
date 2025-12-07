<template>
  <div class="logo-container" :class="logoClasses" :style="logoStyle">
    <!-- 图标型Logo -->
    <div v-if="type === 'icon'" class="logo-icon" @click="handleClick">
      <div class="icon-wrapper" :style="iconStyle">
        <span v-if="icon" class="icon-content">{{ icon }}</span>
        <img v-else-if="image" :src="image" :alt="alt" class="icon-image" />
        <div v-else class="default-icon">
          <span class="default-icon-text">{{ getInitials(name) }}</span>
        </div>
      </div>
      <span v-if="showName && name" class="icon-name">{{ name }}</span>
    </div>
    
    <!-- 文字型Logo -->
    <div v-else-if="type === 'text'" class="logo-text" @click="handleClick">
      <span class="text-content" :style="textStyle">{{ name }}</span>
      <span v-if="slogan" class="text-slogan">{{ slogan }}</span>
    </div>
    
    <!-- 组合型Logo -->
    <div v-else class="logo-combined" @click="handleClick">
      <div class="combined-icon" :style="iconStyle">
        <span v-if="icon" class="icon-content">{{ icon }}</span>
        <img v-else-if="image" :src="image" :alt="alt" class="icon-image" />
        <div v-else class="default-icon">
          <span class="default-icon-text">{{ getInitials(name) }}</span>
        </div>
      </div>
      <div class="combined-text">
        <span class="combined-name" :style="textStyle">{{ name }}</span>
        <span v-if="slogan" class="combined-slogan">{{ slogan }}</span>
      </div>
    </div>
    
    <!-- 徽章 -->
    <div v-if="badge" class="logo-badge" :class="badgeType">
      {{ badge }}
    </div>
    
    <!-- 版本号 -->
    <div v-if="version" class="logo-version">
      v{{ version }}
    </div>
  </div>
</template>

<script>
export default {
  name: 'BrandLogo',
  props: {
    // Logo类型
    type: {
      type: String,
      default: 'combined', // 'icon', 'text', 'combined'
      validator: (value) => ['icon', 'text', 'combined'].includes(value)
    },
    
    // 品牌名称
    name: {
      type: String,
      default: '品牌名称'
    },
    
    // 口号/副标题
    slogan: {
      type: String,
      default: ''
    },
    
    // 图标
    icon: {
      type: String,
      default: ''
    },
    
    // 图片Logo
    image: {
      type: String,
      default: ''
    },
    
    // 图片alt文本
    alt: {
      type: String,
      default: 'Logo'
    },
    
    // 徽章
    badge: {
      type: String,
      default: ''
    },
    
    // 徽章类型
    badgeType: {
      type: String,
      default: 'primary',
      validator: (value) => ['primary', 'success', 'warning', 'danger', 'info'].includes(value)
    },
    
    // 版本号
    version: {
      type: String,
      default: ''
    },
    
    // 显示配置
    showName: {
      type: Boolean,
      default: true
    },
    clickable: {
      type: Boolean,
      default: true
    },
    
    // 样式配置
    size: {
      type: String,
      default: 'medium', // 'small', 'medium', 'large', 'xlarge'
      validator: (value) => ['small', 'medium', 'large', 'xlarge'].includes(value)
    },
    color: {
      type: String,
      default: 'primary' // 'primary', 'secondary', 'success', 'warning', 'danger', 'info', 'dark', 'light'
    },
    theme: {
      type: String,
      default: 'light', // 'light', 'dark'
      validator: (value) => ['light', 'dark'].includes(value)
    },
    rounded: {
      type: Boolean,
      default: false
    },
    shadow: {
      type: Boolean,
      default: false
    },
    
    // 自定义样式
    customStyle: {
      type: Object,
      default: () => ({})
    },
    iconStyle: {
      type: Object,
      default: () => ({})
    },
    textStyle: {
      type: Object,
      default: () => ({})
    }
  },
  computed: {
    logoClasses() {
      return {
        [this.size]: true,
        [this.color]: true,
        [this.theme]: true,
        'rounded': this.rounded,
        'shadow': this.shadow,
        'clickable': this.clickable,
        'has-badge': this.badge,
        'has-version': this.version
      }
    },
    
    logoStyle() {
      return {
        ...this.customStyle
      }
    }
  },
  methods: {
    getInitials(name) {
      if (!name) return 'LO'
      
      return name
        .split(' ')
        .map(word => word.charAt(0))
        .join('')
        .toUpperCase()
        .substring(0, 2)
    },
    
    handleClick() {
      if (this.clickable) {
        this.$emit('click')
      }
    }
  }
}
</script>

<style scoped lang="scss">
.logo-container {
  display: inline-flex;
  align-items: center;
  transition: all 0.3s;
  position: relative;
  
  &.clickable {
    cursor: pointer;
    
    &:hover {
      transform: translateY(-2px);
    }
    
    &:active {
      transform: translateY(0);
    }
  }
  
  &.rounded {
    .icon-wrapper,
    .default-icon,
    .combined-icon {
      border-radius: 50%;
    }
  }
  
  &.shadow {
    .icon-wrapper,
    .default-icon,
    .combined-icon {
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }
  }
  
  // 尺寸
  &.small {
    .icon-wrapper,
    .default-icon,
    .combined-icon {
      width: 32px;
      height: 32px;
      font-size: 16px;
    }
    
    .icon-name,
    .text-content,
    .combined-name {
      font-size: 14px;
    }
    
    .text-slogan,
    .combined-slogan {
      font-size: 10px;
    }
    
    .logo-badge {
      font-size: 10px;
      padding: 2px 6px;
      top: -5px;
      right: -5px;
    }
    
    .logo-version {
      font-size: 10px;
      bottom: -15px;
    }
  }
  
  &.medium {
    .icon-wrapper,
    .default-icon,
    .combined-icon {
      width: 48px;
      height: 48px;
      font-size: 24px;
    }
    
    .icon-name,
    .text-content,
    .combined-name {
      font-size: 18px;
    }
    
    .text-slogan,
    .combined-slogan {
      font-size: 12px;
    }
    
    .logo-badge {
      font-size: 11px;
      padding: 3px 8px;
      top: -8px;
      right: -8px;
    }
    
    .logo-version {
      font-size: 11px;
      bottom: -18px;
    }
  }
  
  &.large {
    .icon-wrapper,
    .default-icon,
    .combined-icon {
      width: 64px;
      height: 64px;
      font-size: 32px;
    }
    
    .icon-name,
    .text-content,
    .combined-name {
      font-size: 24px;
    }
    
    .text-slogan,
    .combined-slogan {
      font-size: 14px;
    }
    
    .logo-badge {
      font-size: 12px;
      padding: 4px 10px;
      top: -10px;
      right: -10px;
    }
    
    .logo-version {
      font-size: 12px;
      bottom: -20px;
    }
  }
  
  &.xlarge {
    .icon-wrapper,
    .default-icon,
    .combined-icon {
      width: 96px;
      height: 96px;
      font-size: 48px;
    }
    
    .icon-name,
    .text-content,
    .combined-name {
      font-size: 32px;
    }
    
    .text-slogan,
    .combined-slogan {
      font-size: 16px;
    }
    
    .logo-badge {
      font-size: 14px;
      padding: 6px 12px;
      top: -12px;
      right: -12px;
    }
    
    .logo-version {
      font-size: 14px;
      bottom: -24px;
    }
  }
  
  // 颜色主题
  &.primary {
    .icon-wrapper,
    .default-icon {
      background: linear-gradient(135deg, #409eff, #66b1ff);
      color: white;
    }
    
    .icon-name,
    .text-content,
    .combined-name {
      color: #409eff;
    }
    
    .text-slogan,
    .combined-slogan {
      color: #909399;
    }
  }
  
  &.secondary {
    .icon-wrapper,
    .default-icon {
      background: linear-gradient(135deg, #909399, #a6a9ad);
      color: white;
    }
    
    .icon-name,
    .text-content,
    .combined-name {
      color: #909399;
    }
  }
  
  &.success {
    .icon-wrapper,
    .default-icon {
      background: linear-gradient(135deg, #67c23a, #85ce61);
      color: white;
    }
    
    .icon-name,
    .text-content,
    .combined-name {
      color: #67c23a;
    }
  }
  
  &.warning {
    .icon-wrapper,
    .default-icon {
      background: linear-gradient(135deg, #e6a23c, #ebb563);
      color: white;
    }
    
    .icon-name,
    .text-content,
    .combined-name {
      color: #e6a23c;
    }
  }
  
  &.danger {
    .icon-wrapper,
    .default-icon {
      background: linear-gradient(135deg, #f56c6c, #f78989);
      color: white;
    }
    
    .icon-name,
    .text-content,
    .combined-name {
      color: #f56c6c;
    }
  }
  
  &.info {
    .icon-wrapper,
    .default-icon {
      background: linear-gradient(135deg, #909399, #a6a9ad);
      color: white;
    }
    
    .icon-name,
    .text-content,
    .combined-name {
      color: #909399;
    }
  }
  
  &.dark {
    .icon-wrapper,
    .default-icon {
      background: linear-gradient(135deg, #303133, #606266);
      color: white;
    }
    
    .icon-name,
    .text-content,
    .combined-name {
      color: #303133;
    }
  }
  
  &.light {
    .icon-wrapper,
    .default-icon {
      background: linear-gradient(135deg, #ffffff, #f5f7fa);
      color: #303133;
      border: 1px solid #dcdfe6;
    }
    
    .icon-name,
    .text-content,
    .combined-name {
      color: #303133;
    }
  }
  
  // 明暗主题
  &.light {
    .logo-version {
      color: #909399;
    }
  }
  
  &.dark {
    .logo-version {
      color: #a6a9ad;
    }
  }
  
  .logo-icon {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    
    .icon-wrapper {
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 12px;
      overflow: hidden;
      
      .icon-content {
        line-height: 1;
      }
      
      .icon-image {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
      
      .default-icon {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-weight: 700;
        
        .default-icon-text {
          line-height: 1;
        }
      }
    }
    
    .icon-name {
      font-weight: 600;
      white-space: nowrap;
    }
  }
  
  .logo-text {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    
    .text-content {
      font-weight: 700;
      line-height: 1;
    }
    
    .text-slogan {
      color: inherit;
      opacity: 0.7;
      margin-top: 4px;
    }
  }
  
  .logo-combined {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .combined-icon {
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 12px;
      overflow: hidden;
      flex-shrink: 0;
      
      .icon-content {
        line-height: 1;
      }
      
      .icon-image {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
      
      .default-icon {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-weight: 700;
        
        .default-icon-text {
          line-height: 1;
        }
      }
    }
    
    .combined-text {
      display: flex;
      flex-direction: column;
      
      .combined-name {
        font-weight: 700;
        line-height: 1;
      }
      
      .combined-slogan {
        color: inherit;
        opacity: 0.7;
        margin-top: 4px;
        white-space: nowrap;
      }
    }
  }
  
  .logo-badge {
    position: absolute;
    border-radius: 20px;
    font-weight: 600;
    white-space: nowrap;
    
    &.primary {
      background: #409eff;
      color: white;
    }
    
    &.success {
      background: #67c23a;
      color: white;
    }
    
    &.warning {
      background: #e6a23c;
      color: white;
    }
    
    &.danger {
      background: #f56c6c;
      color: white;
    }
    
    &.info {
      background: #909399;
      color: white;
    }
  }
  
  .logo-version {
    position: absolute;
    left: 50%;
    transform: translateX(-50%);
    white-space: nowrap;
    opacity: 0.7;
  }
}
</style>