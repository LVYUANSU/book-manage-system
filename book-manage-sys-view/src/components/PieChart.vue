<template>
  <div class="pie-chart-container" :style="containerStyle">
    <!-- 标题 -->
    <div v-if="title" class="chart-title">{{ title }}</div>
    
    <!-- 图表容器 -->
    <div class="chart-content">
      <div class="chart-main">
        <!-- 如果没有数据，显示空状态 -->
        <div v-if="isEmpty" class="empty-chart">
          <div class="empty-icon">🥧</div>
          <div class="empty-text">暂无数据</div>
        </div>
        
        <!-- 图表主体 -->
        <div v-else class="chart-wrapper">
          <canvas
            ref="chartCanvas"
            :width="canvasSize"
            :height="canvasSize"
            class="chart-canvas"
          ></canvas>
          
          <!-- 中心文本 -->
          <div v-if="showCenterText" class="center-text" :style="centerTextStyle">
            <div v-if="centerTitle" class="center-title">{{ centerTitle }}</div>
            <div v-if="centerValue" class="center-value">{{ formatValue(centerValue) }}</div>
            <div v-if="centerSubtitle" class="center-subtitle">{{ centerSubtitle }}</div>
          </div>
        </div>
      </div>
      
      <!-- 图例 -->
      <div v-if="showLegend && legendData.length > 0" class="chart-legend">
        <div
          v-for="item in legendData"
          :key="item.name"
          class="legend-item"
          :class="{ 'hidden': hiddenSeries.has(item.name) }"
          @click="toggleLegend(item.name)"
        >
          <div class="legend-color-wrapper">
            <div
              class="legend-color"
              :style="{ backgroundColor: item.color }"
            ></div>
            <div v-if="hiddenSeries.has(item.name)" class="legend-hide-icon">✕</div>
          </div>
          
          <div class="legend-content">
            <div class="legend-name">{{ item.name }}</div>
            <div class="legend-details">
              <span class="legend-value">{{ formatValue(item.value) }}</span>
              <span class="legend-percentage">({{ item.percentage.toFixed(1) }}%)</span>
            </div>
          </div>
        </div>
        
        <!-- 总计 -->
        <div v-if="showTotal" class="legend-total">
          <div class="total-label">总计</div>
          <div class="total-value">{{ formatValue(totalValue) }}</div>
        </div>
      </div>
    </div>
    
    <!-- 工具提示 -->
    <div
      v-if="showTooltip && tooltip.visible"
      class="chart-tooltip"
      :style="tooltipStyle"
    >
      <div class="tooltip-title">{{ tooltip.title }}</div>
      <div class="tooltip-content">
        <div class="tooltip-color" :style="{ backgroundColor: tooltip.color }"></div>
        <div class="tooltip-info">
          <div class="tooltip-name">{{ tooltip.name }}</div>
          <div class="tooltip-value">{{ formatValue(tooltip.value) }}</div>
          <div class="tooltip-percentage">{{ tooltip.percentage.toFixed(1) }}%</div>
        </div>
      </div>
    </div>
    
    <!-- 图表控制 -->
    <div v-if="showControls" class="chart-controls">
      <button
        class="control-btn"
        @click="toggleChartType"
        :class="{ 'active': chartType === 'donut' }"
      >
        {{ chartType === 'pie' ? '环形图' : '饼图' }}
      </button>
      <button
        class="control-btn"
        @click="toggleExplode"
        :class="{ 'active': isExploded }"
      >
        {{ isExploded ? '合并显示' : '分离显示' }}
      </button>
      <button
        class="control-btn"
        @click="exportChart"
      >
        导出图片
      </button>
      <button
        v-if="showDataTable"
        class="control-btn"
        @click="toggleDataTable"
      >
        {{ showTable ? '隐藏数据' : '显示数据' }}
      </button>
    </div>
    
    <!-- 数据表格 -->
    <div v-if="showTable && showDataTable" class="data-table">
      <table>
        <thead>
          <tr>
            <th>名称</th>
            <th>数值</th>
            <th>百分比</th>
            <th>颜色</th>
            <th v-if="hasSubValues">子项</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="item in visibleData"
            :key="item.name"
            :class="{ 'hidden': hiddenSeries.has(item.name) }"
          >
            <td>
              <div class="item-name">
                <div
                  class="item-color"
                  :style="{ backgroundColor: item.color }"
                ></div>
                {{ item.name }}
              </div>
            </td>
            <td>{{ formatValue(item.value) }}</td>
            <td>
              <div class="percentage-bar">
                <div
                  class="bar-fill"
                  :style="{ width: item.percentage + '%' }"
                ></div>
                <span class="bar-text">{{ item.percentage.toFixed(1) }}%</span>
              </div>
            </td>
            <td>
              <div class="color-display" :style="{ backgroundColor: item.color }"></div>
            </td>
            <td v-if="hasSubValues">
              <div v-if="item.children" class="sub-items">
                <div
                  v-for="child in item.children"
                  :key="child.name"
                  class="sub-item"
                >
                  <span class="sub-name">{{ child.name }}</span>
                  <span class="sub-value">{{ formatValue(child.value) }}</span>
                </div>
              </div>
              <div v-else class="no-sub">-</div>
            </td>
          </tr>
        </tbody>
        <tfoot v-if="showTotal">
          <tr>
            <td><strong>总计</strong></td>
            <td><strong>{{ formatValue(totalValue) }}</strong></td>
            <td><strong>100%</strong></td>
            <td></td>
            <td v-if="hasSubValues"></td>
          </tr>
        </tfoot>
      </table>
    </div>
    
    <!-- 统计信息 -->
    <div v-if="showStatistics" class="chart-statistics">
      <div class="stat-row">
        <div class="stat-item">
          <div class="stat-label">数据项数</div>
          <div class="stat-value">{{ visibleData.length }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">最大值</div>
          <div class="stat-value">{{ formatValue(maxValue) }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">最小值</div>
          <div class="stat-value">{{ formatValue(minValue) }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">平均值</div>
          <div class="stat-value">{{ formatValue(averageValue) }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PieChart',
  props: {
    // 图表数据
    chartData: {
      type: Array,
      required: true,
      default: () => []
    },
    
    // 图表配置
    options: {
      type: Object,
      default: () => ({
        title: '',
        size: 300,
        chartType: 'pie', // 'pie' 或 'donut'
        innerRadius: 0.6, // 环形图内半径比例
        explode: false,
        explodeDistance: 10,
        colors: ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#9B59B6', '#1ABC9C'],
        animation: true,
        animationDuration: 1000,
        showLegend: true,
        showTooltip: true,
        showDataTable: false,
        showControls: true,
        showStatistics: false,
        showCenterText: false,
        centerTitle: '',
        centerValue: null,
        centerSubtitle: '',
        showTotal: true,
        formatValue: null,
        sort: true, // 是否按值排序
        minSliceAngle: 0.1, // 最小切片角度（弧度）
        strokeWidth: 2,
        strokeColor: '#ffffff'
      })
    }
  },
  data() {
    return {
      chartType: 'pie',
      isExploded: false,
      showTable: false,
      tooltip: {
        visible: false,
        x: 0,
        y: 0,
        title: '',
        name: '',
        value: 0,
        percentage: 0,
        color: ''
      },
      hiddenSeries: new Set(),
      canvasContext: null,
      animationFrame: null,
      animationProgress: 0,
      hoveredIndex: -1
    }
  },
  computed: {
    title() {
      return this.options.title || ''
    },
    
    containerStyle() {
      return {
        width: '100%'
      }
    },
    
    canvasSize() {
      return this.options.size || 300
    },
    
    isEmpty() {
      return !this.chartData || this.chartData.length === 0
    },
    
    visibleData() {
      return this.chartData.filter(item => !this.hiddenSeries.has(item.name))
    },
    
    legendData() {
      return this.chartData.map(item => ({
        name: item.name,
        value: item.value,
        color: item.color || this.getItemColor(item.name),
        percentage: this.calculatePercentage(item.value)
      }))
    },
    
    totalValue() {
      return this.visibleData.reduce((sum, item) => sum + item.value, 0)
    },
    
    maxValue() {
      if (this.visibleData.length === 0) return 0
      return Math.max(...this.visibleData.map(item => item.value))
    },
    
    minValue() {
      if (this.visibleData.length === 0) return 0
      return Math.min(...this.visibleData.map(item => item.value))
    },
    
    averageValue() {
      if (this.visibleData.length === 0) return 0
      return this.totalValue / this.visibleData.length
    },
    
    showLegend() {
      return this.options.showLegend !== false
    },
    
    showTooltip() {
      return this.options.showTooltip !== false
    },
    
    showDataTable() {
      return this.options.showDataTable || false
    },
    
    showControls() {
      return this.options.showControls !== false
    },
    
    showStatistics() {
      return this.options.showStatistics || false
    },
    
    showCenterText() {
      return this.options.showCenterText || false
    },
    
    centerTitle() {
      return this.options.centerTitle || ''
    },
    
    centerValue() {
      return this.options.centerValue !== null ? this.options.centerValue : this.totalValue
    },
    
    centerSubtitle() {
      return this.options.centerSubtitle || ''
    },
    
    showTotal() {
      return this.options.showTotal !== false
    },
    
    chartRadius() {
      return this.canvasSize / 2
    },
    
    innerRadius() {
      return this.chartType === 'donut' ? this.chartRadius * (this.options.innerRadius || 0.6) : 0
    },
    
    centerTextStyle() {
      const size = this.innerRadius * 1.8
      return {
        width: size + 'px',
        height: size + 'px',
        fontSize: Math.max(12, size / 10) + 'px'
      }
    },
    
    tooltipStyle() {
      return {
        left: this.tooltip.x + 'px',
        top: this.tooltip.y + 'px',
        display: this.tooltip.visible ? 'block' : 'none'
      }
    },
    
    hasSubValues() {
      return this.chartData.some(item => item.children && item.children.length > 0)
    }
  },
  watch: {
    chartData: {
      handler() {
        this.$nextTick(() => {
          this.renderChart()
        })
      },
      deep: true
    },
    
    chartType() {
      this.renderChart()
    },
    
    isExploded() {
      this.renderChart()
    },
    
    hiddenSeries: {
      handler() {
        this.renderChart()
      },
      deep: true
    }
  },
  mounted() {
    this.initChart()
    this.chartType = this.options.chartType || 'pie'
    this.isExploded = this.options.explode || false
  },
  beforeDestroy() {
    if (this.animationFrame) {
      cancelAnimationFrame(this.animationFrame)
    }
  },
  methods: {
    initChart() {
      const canvas = this.$refs.chartCanvas
      if (!canvas) return
      
      this.canvasContext = canvas.getContext('2d')
      this.renderChart()
      
      // 添加鼠标事件
      this.addChartEvents()
    },
    
    renderChart() {
      if (!this.canvasContext || this.isEmpty) return
      
      const ctx = this.canvasContext
      const size = this.canvasSize
      const radius = this.chartRadius
      
      // 清除画布
      ctx.clearRect(0, 0, size, size)
      
      // 绘制背景
      this.drawBackground(ctx, size)
      
      // 计算数据
      const sortedData = this.getSortedData()
      const totalAngle = this.calculateTotalAngle(sortedData)
      
      // 绘制饼图
      this.drawPieChart(ctx, sortedData, totalAngle)
      
      // 绘制边框
      if (this.options.strokeWidth > 0) {
        this.drawStroke(ctx, radius)
      }
      
      // 绘制中心文本
      if (this.showCenterText) {
        // 中心文本在HTML中渲染
      }
    },
    
    drawBackground(ctx, size) {
      // 绘制圆形背景
      ctx.beginPath()
      ctx.arc(size / 2, size / 2, this.chartRadius, 0, Math.PI * 2)
      ctx.fillStyle = '#f8f9fa'
      ctx.fill()
    },
    
    drawPieChart(ctx, data, totalAngle) {
      const centerX = this.canvasSize / 2
      const centerY = this.canvasSize / 2
      const outerRadius = this.chartRadius - (this.isExploded ? this.options.explodeDistance || 10 : 0)
      const innerRadius = this.innerRadius
      
      let startAngle = -Math.PI / 2 // 从12点方向开始
      
      data.forEach((item, index) => {
        const isHovered = index === this.hoveredIndex
        const isHidden = this.hiddenSeries.has(item.name)
        
        if (isHidden) return
        
        // 计算切片角度
        const sliceAngle = (item.value / this.totalValue) * totalAngle * this.animationProgress
        
        // 分离效果
        const explodeDistance = this.isExploded && isHovered ? (this.options.explodeDistance || 10) * 1.5 : 
                               this.isExploded ? (this.options.explodeDistance || 10) : 0
        
        const offsetX = Math.cos(startAngle + sliceAngle / 2) * explodeDistance
        const offsetY = Math.sin(startAngle + sliceAngle / 2) * explodeDistance
        
        // 绘制切片
        ctx.beginPath()
        
        if (this.chartType === 'donut') {
          // 环形图
          ctx.arc(centerX + offsetX, centerY + offsetY, outerRadius, startAngle, startAngle + sliceAngle)
          ctx.arc(centerX + offsetX, centerY + offsetY, innerRadius, startAngle + sliceAngle, startAngle, true)
        } else {
          // 饼图
          ctx.moveTo(centerX + offsetX, centerY + offsetY)
          ctx.arc(centerX + offsetX, centerY + offsetY, outerRadius, startAngle, startAngle + sliceAngle)
        }
        
        ctx.closePath()
        
        // 填充颜色
        const color = item.color || this.getItemColor(item.name)
        ctx.fillStyle = color
        ctx.fill()
        
        // 绘制边框
        if (this.options.strokeWidth > 0) {
          ctx.strokeStyle = this.options.strokeColor || '#ffffff'
          ctx.lineWidth = this.options.strokeWidth
          ctx.stroke()
        }
        
        // 绘制标签
        if (this.options.showLabels && sliceAngle > this.options.minSliceAngle) {
          this.drawSliceLabel(ctx, item, startAngle, sliceAngle, centerX + offsetX, centerY + offsetY, outerRadius)
        }
        
        startAngle += sliceAngle
      })
    },
    
    drawStroke(ctx, radius) {
      const centerX = this.canvasSize / 2
      const centerY = this.canvasSize / 2
      
      ctx.beginPath()
      ctx.arc(centerX, centerY, radius, 0, Math.PI * 2)
      ctx.strokeStyle = '#dcdfe6'
      ctx.lineWidth = 1
      ctx.stroke()
    },
    
    drawSliceLabel(ctx, item, startAngle, sliceAngle, centerX, centerY, radius) {
      const midAngle = startAngle + sliceAngle / 2
      const labelRadius = radius * 0.7
      
      const x = centerX + Math.cos(midAngle) * labelRadius
      const y = centerY + Math.sin(midAngle) * labelRadius
      
      ctx.fillStyle = '#ffffff'
      ctx.font = '12px Arial'
      ctx.textAlign = 'center'
      ctx.textBaseline = 'middle'
      
      const percentage = ((item.value / this.totalValue) * 100).toFixed(1)
      ctx.fillText(`${percentage}%`, x, y)
    },
    
    getSortedData() {
      const data = [...this.chartData]
      
      if (this.options.sort !== false) {
        return data.sort((a, b) => b.value - a.value)
      }
      
      return data
    },
    
    calculateTotalAngle(data) {
      const visibleData = data.filter(item => !this.hiddenSeries.has(item.name))
      if (visibleData.length === 0) return 0
      
      return Math.PI * 2
    },
    
    calculatePercentage(value) {
      if (this.totalValue === 0) return 0
      return (value / this.totalValue) * 100
    },
    
    getItemColor(itemName) {
      const colors = this.options.colors || ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#9B59B6', '#1ABC9C']
      const index = this.chartData.findIndex(item => item.name === itemName)
      return colors[index % colors.length]
    },
    
    addChartEvents() {
      const canvas = this.$refs.chartCanvas
      if (!canvas) return
      
      canvas.onmousemove = (e) => {
        this.handleMouseMove(e)
      }
      
      canvas.onmouseleave = () => {
        this.hoveredIndex = -1
        this.tooltip.visible = false
        this.renderChart()
      }
      
      canvas.onclick = (e) => {
        this.handleClick(e)
      }
    },
    
    handleMouseMove(e) {
      if (!this.showTooltip) return
      
      const rect = this.$refs.chartCanvas.getBoundingClientRect()
      const x = e.clientX - rect.left
      const y = e.clientY - rect.top
      
      const centerX = this.canvasSize / 2
      const centerY = this.canvasSize / 2
      
      // 计算鼠标位置相对于圆心的角度
      const dx = x - centerX
      const dy = y - centerY
      const distance = Math.sqrt(dx * dx + dy * dy)
      
      // 检查是否在图表范围内
      if (distance > this.chartRadius) {
        this.hoveredIndex = -1
        this.tooltip.visible = false
        this.renderChart()
        return
      }
      
      // 计算角度
      let angle = Math.atan2(dy, dx)
      if (angle < 0) angle += Math.PI * 2
      
      // 调整角度（从12点方向开始）
      angle += Math.PI / 2
      if (angle > Math.PI * 2) angle -= Math.PI * 2
      
      // 查找对应的切片
      const sortedData = this.getSortedData()
      const visibleData = sortedData.filter(item => !this.hiddenSeries.has(item.name))
      
      let accumulatedAngle = 0
      let foundIndex = -1
      
      for (let i = 0; i < visibleData.length; i++) {
        const item = visibleData[i]
        const sliceAngle = (item.value / this.totalValue) * Math.PI * 2
        
        if (angle >= accumulatedAngle && angle < accumulatedAngle + sliceAngle) {
          foundIndex = i
          break
        }
        
        accumulatedAngle += sliceAngle
      }
      
      // 更新悬停状态
      if (foundIndex !== this.hoveredIndex) {
        this.hoveredIndex = foundIndex
        this.renderChart()
      }
      
      // 更新工具提示
      if (foundIndex !== -1) {
        const item = visibleData[foundIndex]
        const percentage = this.calculatePercentage(item.value)
        
        this.tooltip = {
          visible: true,
          x: e.clientX + 15,
          y: e.clientY - 15,
          title: this.title,
          name: item.name,
          value: item.value,
          percentage: percentage,
          color: item.color || this.getItemColor(item.name)
        }
      } else {
        this.tooltip.visible = false
      }
    },
    
    handleClick(e) {
      if (this.hoveredIndex !== -1) {
        const sortedData = this.getSortedData()
        const visibleData = sortedData.filter(item => !this.hiddenSeries.has(item.name))
        const item = visibleData[this.hoveredIndex]
        
        this.$emit('slice-click', item)
      }
    },
    
    toggleLegend(seriesName) {
      if (this.hiddenSeries.has(seriesName)) {
        this.hiddenSeries.delete(seriesName)
      } else {
        this.hiddenSeries.add(seriesName)
      }
    },
    
    toggleChartType() {
      this.chartType = this.chartType === 'pie' ? 'donut' : 'pie'
    },
    
    toggleExplode() {
      this.isExploded = !this.isExploded
    },
    
    toggleDataTable() {
      this.showTable = !this.showTable
    },
    
    exportChart() {
      const canvas = this.$refs.chartCanvas
      if (!canvas) return
      
      const link = document.createElement('a')
      link.download = `pie-chart-${new Date().getTime()}.png`
      link.href = canvas.toDataURL('image/png')
      link.click()
    },
    
    formatValue(value) {
      if (this.options.formatValue && typeof this.options.formatValue === 'function') {
        return this.options.formatValue(value)
      }
      
      // 默认格式化
      if (typeof value === 'number') {
        if (value >= 1000000) {
          return (value / 1000000).toFixed(1) + 'M'
        } else if (value >= 1000) {
          return (value / 1000).toFixed(1) + 'K'
        } else if (Number.isInteger(value)) {
          return value.toString()
        } else {
          return value.toFixed(2)
        }
      }
      
      return value
    },
    
    // 动画相关方法
    animate() {
      if (!this.options.animation) {
        this.animationProgress = 1
        return
      }
      
      const startTime = Date.now()
      const duration = this.options.animationDuration || 1000
      
      const animateStep = () => {
        const elapsed = Date.now() - startTime
        this.animationProgress = Math.min(elapsed / duration, 1)
        
        if (this.animationProgress < 1) {
          this.animationFrame = requestAnimationFrame(animateStep)
        }
        
        this.renderChart()
      }
      
      this.animationProgress = 0
      animateStep()
    }
  }
}
</script>

<style scoped lang="scss">
.pie-chart-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  padding: 20px;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  
  .chart-title {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 20px;
    text-align: center;
  }
  
  .chart-content {
    display: flex;
    flex-wrap: wrap;
    gap: 30px;
    align-items: center;
    justify-content: center;
    
    .chart-main {
      position: relative;
      
      .empty-chart {
        width: v-bind('canvasSize + "px"');
        height: v-bind('canvasSize + "px"');
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        color: #909399;
        
        .empty-icon {
          font-size: 48px;
          margin-bottom: 10px;
        }
        
        .empty-text {
          font-size: 16px;
        }
      }
      
      .chart-wrapper {
        position: relative;
        
        .chart-canvas {
          display: block;
        }
        
        .center-text {
          position: absolute;
          top: 50%;
          left: 50%;
          transform: translate(-50%, -50%);
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          text-align: center;
          background: white;
          border-radius: 50%;
          box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
          
          .center-title {
            font-size: 0.8em;
            color: #909399;
            margin-bottom: 4px;
          }
          
          .center-value {
            font-size: 1.2em;
            font-weight: 700;
            color: #303133;
            line-height: 1;
          }
          
          .center-subtitle {
            font-size: 0.7em;
            color: #909399;
            margin-top: 4px;
          }
        }
      }
    }
    
    .chart-legend {
      flex: 1;
      min-width: 200px;
      max-width: 300px;
      
      .legend-item {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 10px;
        margin-bottom: 8px;
        border-radius: 6px;
        cursor: pointer;
        transition: all 0.3s;
        
        &:hover {
          background: #f5f7fa;
        }
        
        &.hidden {
          opacity: 0.5;
          
          .legend-name {
            text-decoration: line-through;
          }
        }
        
        .legend-color-wrapper {
          position: relative;
          
          .legend-color {
            width: 20px;
            height: 20px;
            border-radius: 4px;
          }
          
          .legend-hide-icon {
            position: absolute;
            top: -5px;
            right: -5px;
            width: 16px;
            height: 16px;
            background: #f56c6c;
            color: white;
            border-radius: 50%;
            font-size: 10px;
            display: flex;
            align-items: center;
            justify-content: center;
            line-height: 1;
          }
        }
        
        .legend-content {
          flex: 1;
          
          .legend-name {
            font-size: 14px;
            color: #303133;
            font-weight: 500;
            margin-bottom: 4px;
          }
          
          .legend-details {
            display: flex;
            gap: 8px;
            font-size: 12px;
            
            .legend-value {
              color: #606266;
              font-weight: 600;
            }
            
            .legend-percentage {
              color: #909399;
            }
          }
        }
      }
      
      .legend-total {
        margin-top: 20px;
        padding-top: 20px;
        border-top: 1px solid #ebeef5;
        display: flex;
        justify-content: space-between;
        align-items: center;
        
        .total-label {
          font-size: 14px;
          color: #303133;
          font-weight: 600;
        }
        
        .total-value {
          font-size: 18px;
          color: #409eff;
          font-weight: 700;
        }
      }
    }
  }
  
  .chart-tooltip {
    position: fixed;
    background: rgba(255, 255, 255, 0.95);
    border: 1px solid #dcdfe6;
    border-radius: 6px;
    padding: 12px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    z-index: 1000;
    min-width: 160px;
    
    .tooltip-title {
      font-weight: 600;
      color: #303133;
      margin-bottom: 8px;
      padding-bottom: 6px;
      border-bottom: 1px solid #ebeef5;
      font-size: 12px;
    }
    
    .tooltip-content {
      display: flex;
      align-items: center;
      gap: 10px;
      
      .tooltip-color {
        width: 16px;
        height: 16px;
        border-radius: 4px;
        flex-shrink: 0;
      }
      
      .tooltip-info {
        flex: 1;
        
        .tooltip-name {
          font-size: 14px;
          font-weight: 600;
          color: #303133;
          margin-bottom: 4px;
        }
        
        .tooltip-value {
          font-size: 12px;
          color: #606266;
          font-weight: 600;
          margin-bottom: 2px;
        }
        
        .tooltip-percentage {
          font-size: 11px;
          color: #409eff;
          font-weight: 600;
        }
      }
    }
  }
  
  .chart-controls {
    display: flex;
    justify-content: center;
    gap: 10px;
    margin-top: 20px;
    padding-top: 20px;
    border-top: 1px solid #ebeef5;
    
    .control-btn {
      padding: 6px 16px;
      background: #f5f7fa;
      color: #606266;
      border: 1px solid #dcdfe6;
      border-radius: 4px;
      cursor: pointer;
      font-size: 14px;
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
  
  .data-table {
    margin-top: 20px;
    overflow-x: auto;
    
    table {
      width: 100%;
      border-collapse: collapse;
      font-size: 14px;
      
      th {
        background: #f8f9fa;
        color: #303133;
        font-weight: 600;
        padding: 12px;
        text-align: left;
        border-bottom: 2px solid #ebeef5;
      }
      
      td {
        padding: 10px 12px;
        border-bottom: 1px solid #ebeef5;
        color: #606266;
        
        .item-name {
          display: flex;
          align-items: center;
          gap: 8px;
          
          .item-color {
            width: 12px;
            height: 12px;
            border-radius: 2px;
            flex-shrink: 0;
          }
        }
        
        .percentage-bar {
          position: relative;
          height: 20px;
          background: #ebeef5;
          border-radius: 10px;
          overflow: hidden;
          
          .bar-fill {
            height: 100%;
            background: linear-gradient(to right, #409eff, #66b1ff);
            border-radius: 10px;
            transition: width 0.5s;
          }
          
          .bar-text {
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            font-size: 11px;
            color: white;
            font-weight: 600;
            text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
          }
        }
        
        .color-display {
          width: 20px;
          height: 20px;
          border-radius: 4px;
        }
        
        .sub-items {
          .sub-item {
            display: flex;
            justify-content: space-between;
            padding: 2px 0;
            font-size: 12px;
            
            .sub-name {
              color: #606266;
            }
            
            .sub-value {
              color: #909399;
              font-weight: 600;
            }
          }
        }
        
        .no-sub {
          color: #c0c4cc;
          font-style: italic;
        }
      }
      
      tbody tr {
        &.hidden {
          opacity: 0.5;
          text-decoration: line-through;
        }
        
        &:hover {
          background: #f5f7fa;
        }
      }
      
      tfoot {
        td {
          font-weight: 600;
          color: #303133;
          border-top: 2px solid #ebeef5;
        }
      }
    }
  }
  
  .chart-statistics {
    margin-top: 20px;
    padding: 15px;
    background: #f8f9fa;
    border-radius: 6px;
    
    .stat-row {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
      gap: 15px;
      
      .stat-item {
        text-align: center;
        padding: 10px;
        background: white;
        border-radius: 4px;
        
        .stat-label {
          font-size: 12px;
          color: #909399;
          margin-bottom: 5px;
        }
        
        .stat-value {
          font-size: 16px;
          font-weight: 600;
          color: #409eff;
        }
      }
    }
  }
}
</style>