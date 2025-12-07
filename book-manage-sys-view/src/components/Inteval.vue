<template>
  <div class="interval-selector" :class="selectorClasses">
    <!-- 标题 -->
    <div v-if="title" class="interval-title">{{ title }}</div>
    
    <!-- 区间选择器 -->
    <div class="interval-container">
      <!-- 最小值输入 -->
      <div class="interval-input-group">
        <label class="input-label">{{ minLabel }}</label>
        <div class="input-wrapper">
          <input
            type="number"
            v-model="localMin"
            :min="absoluteMin"
            :max="localMax"
            :step="step"
            class="interval-input"
            :placeholder="minPlaceholder"
            @input="handleMinInput"
            @blur="validateMin"
          />
          <span v-if="unit" class="input-unit">{{ unit }}</span>
        </div>
      </div>
      
      <!-- 分隔符 -->
      <div class="interval-separator">
        <span class="separator-text">{{ separator }}</span>
      </div>
      
      <!-- 最大值输入 -->
      <div class="interval-input-group">
        <label class="input-label">{{ maxLabel }}</label>
        <div class="input-wrapper">
          <input
            type="number"
            v-model="localMax"
            :min="localMin"
            :max="absoluteMax"
            :step="step"
            class="interval-input"
            :placeholder="maxPlaceholder"
            @input="handleMaxInput"
            @blur="validateMax"
          />
          <span v-if="unit" class="input-unit">{{ unit }}</span>
        </div>
      </div>
    </div>
    
    <!-- 滑块选择器 -->
    <div v-if="showSlider" class="interval-slider">
      <div class="slider-track" ref="sliderTrack">
        <div
          class="slider-range"
          :style="rangeStyle"
        ></div>
        
        <!-- 最小值滑块 -->
        <div
          class="slider-handle min-handle"
          :style="{ left: minPosition + '%' }"
          @mousedown="startDrag('min')"
          @touchstart="startDrag('min')"
        >
          <div class="handle-tooltip">
            {{ localMin }}{{ unit }}
          </div>
        </div>
        
        <!-- 最大值滑块 -->
        <div
          class="slider-handle max-handle"
          :style="{ left: maxPosition + '%' }"
          @mousedown="startDrag('max')"
          @touchstart="startDrag('max')"
        >
          <div class="handle-tooltip">
            {{ localMax }}{{ unit }}
          </div>
        </div>
      </div>
      
      <!-- 刻度标记 -->
      <div v-if="showMarks" class="slider-marks">
        <div
          v-for="mark in marks"
          :key="mark.value"
          class="slider-mark"
          :style="{ left: mark.position + '%' }"
          :class="{ 'active': isMarkActive(mark.value) }"
        >
          <span class="mark-line"></span>
          <span class="mark-label">{{ mark.label }}</span>
        </div>
      </div>
    </div>
    
    <!-- 预设选项 -->
    <div v-if="presets.length > 0" class="interval-presets">
      <div class="presets-label">{{ presetsLabel }}</div>
      <div class="presets-list">
        <button
          v-for="preset in presets"
          :key="preset.label"
          class="preset-btn"
          :class="{ 'active': isPresetActive(preset) }"
          @click="applyPreset(preset)"
        >
          {{ preset.label }}
        </button>
      </div>
    </div>
    
    <!-- 值显示 -->
    <div v-if="showValueDisplay" class="interval-display">
      <div class="display-label">当前区间：</div>
      <div class="display-value">
        <span class="value-min">{{ localMin }}{{ unit }}</span>
        <span class="value-separator">至</span>
        <span class="value-max">{{ localMax }}{{ unit }}</span>
        <span v-if="rangeValue !== null" class="value-range">
          (区间值: {{ rangeValue }}{{ unit }})
        </span>
      </div>
    </div>
    
    <!-- 错误提示 -->
    <div v-if="error" class="interval-error">
      <span class="error-icon">⚠️</span>
      {{ error }}
    </div>
    
    <!-- 操作按钮 -->
    <div v-if="showActions" class="interval-actions">
      <button
        v-if="showReset"
        class="action-btn reset-btn"
        @click="resetInterval"
      >
        重置
      </button>
      <button
        class="action-btn apply-btn"
        @click="applyInterval"
        :disabled="!isValid"
      >
        {{ applyButtonText }}
      </button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'IntervalSelector',
  props: {
    // 基本配置
    title: {
      type: String,
      default: ''
    },
    min: {
      type: Number,
      required: true
    },
    max: {
      type: Number,
      required: true
    },
    value: {
      type: Array,
      default: () => [0, 100]
    },
    
    // 标签配置
    minLabel: {
      type: String,
      default: '最小值'
    },
    maxLabel: {
      type: String,
      default: '最大值'
    },
    separator: {
      type: String,
      default: '至'
    },
    unit: {
      type: String,
      default: ''
    },
    
    // 输入框配置
    minPlaceholder: {
      type: String,
      default: '请输入最小值'
    },
    maxPlaceholder: {
      type: String,
      default: '请输入最大值'
    },
    step: {
      type: Number,
      default: 1
    },
    
    // 滑块配置
    showSlider: {
      type: Boolean,
      default: true
    },
    sliderHeight: {
      type: Number,
      default: 6
    },
    handleSize: {
      type: Number,
      default: 20
    },
    showMarks: {
      type: Boolean,
      default: false
    },
    marks: {
      type: Array,
      default: () => []
    },
    
    // 预设配置
    presets: {
      type: Array,
      default: () => []
    },
    presetsLabel: {
      type: String,
      default: '常用区间'
    },
    
    // 显示配置
    showValueDisplay: {
      type: Boolean,
      default: true
    },
    showActions: {
      type: Boolean,
      default: false
    },
    showReset: {
      type: Boolean,
      default: true
    },
    applyButtonText: {
      type: String,
      default: '应用'
    },
    
    // 验证配置
    minDistance: {
      type: Number,
      default: 0
    },
    maxDistance: {
      type: Number,
      default: null
    },
    required: {
      type: Boolean,
      default: false
    },
    disabled: {
      type: Boolean,
      default: false
    },
    readonly: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      localMin: this.value[0],
      localMax: this.value[1],
      error: '',
      isDragging: false,
      dragType: null, // 'min' 或 'max'
      dragStartX: 0,
      dragStartValue: 0,
      absoluteMin: this.min,
      absoluteMax: this.max
    }
  },
  computed: {
    selectorClasses() {
      return {
        'disabled': this.disabled,
        'readonly': this.readonly,
        'has-error': this.error,
        'dragging': this.isDragging
      }
    },
    
    minPosition() {
      return ((this.localMin - this.absoluteMin) / (this.absoluteMax - this.absoluteMin)) * 100
    },
    
    maxPosition() {
      return ((this.localMax - this.absoluteMin) / (this.absoluteMax - this.absoluteMin)) * 100
    },
    
    rangeStyle() {
      return {
        left: this.minPosition + '%',
        width: (this.maxPosition - this.minPosition) + '%'
      }
    },
    
    rangeValue() {
      return this.localMax - this.localMin
    },
    
    isValid() {
      if (this.error) return false
      
      if (this.required && (this.localMin === null || this.localMax === null)) {
        return false
      }
      
      if (this.localMin > this.localMax) {
        return false
      }
      
      if (this.minDistance !== null && this.rangeValue < this.minDistance) {
        return false
      }
      
      if (this.maxDistance !== null && this.rangeValue > this.maxDistance) {
        return false
      }
      
      return true
    }
  },
  watch: {
    value(newVal) {
      if (newVal[0] !== this.localMin || newVal[1] !== this.localMax) {
        this.localMin = newVal[0]
        this.localMax = newVal[1]
        this.validate()
      }
    },
    
    localMin(newVal) {
      this.$emit('input', [newVal, this.localMax])
      this.validate()
    },
    
    localMax(newVal) {
      this.$emit('input', [this.localMin, newVal])
      this.validate()
    }
  },
  mounted() {
    this.validate()
    
    // 添加全局事件监听
    document.addEventListener('mousemove', this.handleDrag)
    document.addEventListener('mouseup', this.stopDrag)
    document.addEventListener('touchmove', this.handleDrag)
    document.addEventListener('touchend', this.stopDrag)
  },
  beforeDestroy() {
    // 移除全局事件监听
    document.removeEventListener('mousemove', this.handleDrag)
    document.removeEventListener('mouseup', this.stopDrag)
    document.removeEventListener('touchmove', this.handleDrag)
    document.removeEventListener('touchend', this.stopDrag)
  },
  methods: {
    handleMinInput(event) {
      const value = parseFloat(event.target.value)
      if (isNaN(value)) return
      
      this.localMin = value
      this.$emit('min-change', value)
    },
    
    handleMaxInput(event) {
      const value = parseFloat(event.target.value)
      if (isNaN(value)) return
      
      this.localMax = value
      this.$emit('max-change', value)
    },
    
    validateMin() {
      let value = this.localMin
      
      // 验证是否为数字
      if (isNaN(value)) {
        this.error = '请输入有效的数字'
        return false
      }
      
      // 验证最小值
      if (value < this.absoluteMin) {
        value = this.absoluteMin
        this.localMin = value
      }
      
      // 验证最大值
      if (value > this.localMax) {
        value = this.localMax
        this.localMin = value
      }
      
      // 验证步进
      if (this.step > 0) {
        const remainder = (value - this.absoluteMin) % this.step
        if (remainder !== 0) {
          value = Math.round((value - this.absoluteMin) / this.step) * this.step + this.absoluteMin
          this.localMin = value
        }
      }
      
      this.error = ''
      return true
    },
    
    validateMax() {
      let value = this.localMax
      
      // 验证是否为数字
      if (isNaN(value)) {
        this.error = '请输入有效的数字'
        return false
      }
      
      // 验证最大值
      if (value > this.absoluteMax) {
        value = this.absoluteMax
        this.localMax = value
      }
      
      // 验证最小值
      if (value < this.localMin) {
        value = this.localMin
        this.localMax = value
      }
      
      // 验证步进
      if (this.step > 0) {
        const remainder = (value - this.absoluteMin) % this.step
        if (remainder !== 0) {
          value = Math.round((value - this.absoluteMin) / this.step) * this.step + this.absoluteMin
          this.localMax = value
        }
      }
      
      this.error = ''
      return true
    },
    
    validate() {
      const minValid = this.validateMin()
      const maxValid = this.validateMax()
      
      if (!minValid || !maxValid) {
        return false
      }
      
      // 验证区间距离
      if (this.minDistance !== null && this.rangeValue < this.minDistance) {
        this.error = `区间距离不能小于 ${this.minDistance}${this.unit}`
        return false
      }
      
      if (this.maxDistance !== null && this.rangeValue > this.maxDistance) {
        this.error = `区间距离不能大于 ${this.maxDistance}${this.unit}`
        return false
      }
      
      this.error = ''
      return true
    },
    
    startDrag(type) {
      if (this.disabled || this.readonly) return
      
      this.isDragging = true
      this.dragType = type
      this.dragStartValue = type === 'min' ? this.localMin : this.localMax
      
      this.$emit('drag-start', { type })
    },
    
    handleDrag(event) {
      if (!this.isDragging || !this.dragType) return
      
      event.preventDefault()
      
      const sliderTrack = this.$refs.sliderTrack
      if (!sliderTrack) return
      
      const rect = sliderTrack.getBoundingClientRect()
      const clientX = event.touches ? event.touches[0].clientX : event.clientX
      
      // 计算鼠标位置相对于轨道的百分比
      let percentage = (clientX - rect.left) / rect.width
      percentage = Math.max(0, Math.min(1, percentage))
      
      // 计算对应的值
      const value = this.absoluteMin + percentage * (this.absoluteMax - this.absoluteMin)
      
      // 应用步进
      let steppedValue = value
      if (this.step > 0) {
        steppedValue = Math.round((value - this.absoluteMin) / this.step) * this.step + this.absoluteMin
      }
      
      // 更新对应的值
      if (this.dragType === 'min') {
        // 确保最小值不超过最大值
        const newMin = Math.min(steppedValue, this.localMax)
        this.localMin = newMin
      } else {
        // 确保最大值不小于最小值
        const newMax = Math.max(steppedValue, this.localMin)
        this.localMax = newMax
      }
      
      this.$emit('dragging', {
        type: this.dragType,
        value: this.dragType === 'min' ? this.localMin : this.localMax
      })
    },
    
    stopDrag() {
      if (!this.isDragging) return
      
      this.isDragging = false
      this.dragType = null
      
      this.$emit('drag-end', {
        min: this.localMin,
        max: this.localMax
      })
    },
    
    isMarkActive(value) {
      return value >= this.localMin && value <= this.localMax
    },
    
    isPresetActive(preset) {
      return preset.value[0] === this.localMin && preset.value[1] === this.localMax
    },
    
    applyPreset(preset) {
      if (this.disabled || this.readonly) return
      
      this.localMin = preset.value[0]
      this.localMax = preset.value[1]
      
      this.$emit('preset-applied', preset)
    },
    
    resetInterval() {
      this.localMin = this.min
      this.localMax = this.max
      this.error = ''
      
      this.$emit('reset')
    },
    
    applyInterval() {
      if (!this.isValid) return
      
      this.$emit('apply', {
        min: this.localMin,
        max: this.localMax,
        range: this.rangeValue
      })
    }
  }
}
</script>

<style scoped lang="scss">
.interval-selector {
  background: white;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  padding: 20px;
  transition: border-color 0.3s;
  
  &:hover:not(.disabled):not(.readonly) {
    border-color: #c0c4cc;
  }
  
  &.has-error {
    border-color: #f56c6c;
  }
  
  &.disabled,
  &.readonly {
    background-color: #f5f7fa;
    cursor: not-allowed;
    
    .interval-input,
    .slider-handle,
    .preset-btn {
      cursor: not-allowed;
      opacity: 0.6;
    }
  }
  
  &.dragging {
    user-select: none;
  }
  
  .interval-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 20px;
  }
  
  .interval-container {
    display: flex;
    align-items: center;
    gap: 15px;
    margin-bottom: 20px;
    
    .interval-input-group {
      flex: 1;
      
      .input-label {
        display: block;
        font-size: 14px;
        color: #606266;
        margin-bottom: 8px;
      }
      
      .input-wrapper {
        position: relative;
        
        .interval-input {
          width: 100%;
          padding: 10px 15px;
          padding-right: 40px;
          border: 1px solid #dcdfe6;
          border-radius: 4px;
          font-size: 14px;
          color: #303133;
          background: white;
          transition: border-color 0.3s;
          
          &:focus {
            outline: none;
            border-color: #409eff;
            box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
          }
          
          &:disabled {
            background-color: #f5f7fa;
            cursor: not-allowed;
          }
        }
        
        .input-unit {
          position: absolute;
          right: 15px;
          top: 50%;
          transform: translateY(-50%);
          font-size: 14px;
          color: #909399;
        }
      }
    }
    
    .interval-separator {
      padding-top: 25px;
      
      .separator-text {
        color: #909399;
        font-size: 14px;
        white-space: nowrap;
      }
    }
  }
  
  .interval-slider {
    margin: 30px 0;
    position: relative;
    
    .slider-track {
      position: relative;
      height: v-bind('sliderHeight + "px"');
      background: #ebeef5;
      border-radius: 3px;
      cursor: pointer;
      
      .slider-range {
        position: absolute;
        height: 100%;
        background: linear-gradient(to right, #409eff, #66b1ff);
        border-radius: 3px;
        pointer-events: none;
      }
      
      .slider-handle {
        position: absolute;
        top: 50%;
        transform: translate(-50%, -50%);
        width: v-bind('handleSize + "px"');
        height: v-bind('handleSize + "px"');
        background: white;
        border: 2px solid #409eff;
        border-radius: 50%;
        cursor: grab;
        box-shadow: 0 2px 8px rgba(64, 158, 255, 0.3);
        transition: all 0.2s;
        z-index: 10;
        
        &:hover {
          transform: translate(-50%, -50%) scale(1.1);
          box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
        }
        
        &:active {
          cursor: grabbing;
        }
        
        &.min-handle {
          z-index: 11;
        }
        
        .handle-tooltip {
          position: absolute;
          bottom: calc(100% + 8px);
          left: 50%;
          transform: translateX(-50%);
          padding: 4px 8px;
          background: rgba(0, 0, 0, 0.8);
          color: white;
          font-size: 12px;
          border-radius: 4px;
          white-space: nowrap;
          opacity: 0;
          transition: opacity 0.3s;
          pointer-events: none;
          
          &::after {
            content: '';
            position: absolute;
            top: 100%;
            left: 50%;
            transform: translateX(-50%);
            border: 4px solid transparent;
            border-top-color: rgba(0, 0, 0, 0.8);
          }
        }
        
        &:hover .handle-tooltip {
          opacity: 1;
        }
      }
    }
    
    .slider-marks {
      position: relative;
      height: 20px;
      margin-top: 15px;
      
      .slider-mark {
        position: absolute;
        transform: translateX(-50%);
        text-align: center;
        
        .mark-line {
          display: block;
          width: 2px;
          height: 6px;
          background: #c0c4cc;
          margin: 0 auto 4px;
        }
        
        .mark-label {
          font-size: 12px;
          color: #909399;
          white-space: nowrap;
        }
        
        &.active {
          .mark-line {
            background: #409eff;
            height: 10px;
          }
          
          .mark-label {
            color: #409eff;
            font-weight: 500;
          }
        }
      }
    }
  }
  
  .interval-presets {
    margin: 20px 0;
    
    .presets-label {
      font-size: 14px;
      color: #606266;
      margin-bottom: 10px;
    }
    
    .presets-list {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      
      .preset-btn {
        padding: 6px 12px;
        background: #f5f7fa;
        border: 1px solid #dcdfe6;
        border-radius: 4px;
        font-size: 12px;
        color: #606266;
        cursor: pointer;
        transition: all 0.3s;
        
        &:hover {
          background: #ecf5ff;
          border-color: #c6e2ff;
          color: #409eff;
        }
        
        &.active {
          background: #409eff;
          border-color: #409eff;
          color: white;
        }
      }
    }
  }
  
  .interval-display {
    margin: 20px 0;
    padding: 15px;
    background: #f8f9fa;
    border-radius: 6px;
    
    .display-label {
      font-size: 14px;
      color: #606266;
      margin-bottom: 8px;
    }
    
    .display-value {
      font-size: 18px;
      color: #303133;
      
      .value-min,
      .value-max {
        font-weight: 600;
        color: #409eff;
      }
      
      .value-separator {
        margin: 0 8px;
        color: #909399;
      }
      
      .value-range {
        margin-left: 12px;
        font-size: 14px;
        color: #67c23a;
      }
    }
  }
  
  .interval-error {
    padding: 10px 15px;
    background: #fef0f0;
    border-radius: 4px;
    border-left: 3px solid #f56c6c;
    color: #f56c6c;
    font-size: 14px;
    margin: 15px 0;
    
    .error-icon {
      margin-right: 8px;
    }
  }
  
  .interval-actions {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    margin-top: 20px;
    padding-top: 20px;
    border-top: 1px solid #ebeef5;
    
    .action-btn {
      padding: 8px 20px;
      border: none;
      border-radius: 4px;
      font-size: 14px;
      cursor: pointer;
      transition: all 0.3s;
      
      &.reset-btn {
        background: #f5f7fa;
        color: #606266;
        border: 1px solid #dcdfe6;
        
        &:hover:not(:disabled) {
          background: #ebeef5;
        }
      }
      
      &.apply-btn {
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
</style>