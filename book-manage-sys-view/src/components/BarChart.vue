<template>
  <div class="bar-chart-container" :style="containerStyle">
    <!-- 标题 -->
    <div v-if="title" class="chart-title">{{ title }}</div>
    
    <!-- 图例 -->
    <div v-if="showLegend && legendData.length > 0" class="chart-legend">
      <div
        v-for="item in legendData"
        :key="item.name"
        class="legend-item"
        @click="toggleLegend(item.name)"
      >
        <div
          class="legend-color"
          :style="{ backgroundColor: item.color }"
        ></div>
        <span class="legend-text">{{ item.name }}</span>
        <span v-if="showLegendValue" class="legend-value">
          {{ formatValue(item.value) }}
        </span>
      </div>
    </div>
    
    <!-- 图表容器 -->
    <div
      ref="chartContainer"
      class="chart-wrapper"
      :style="{ height: chartHeight + 'px' }"
    >
      <!-- 如果没有数据，显示空状态 -->
      <div v-if="isEmpty" class="empty-chart">
        <div class="empty-icon">📊</div>
        <div class="empty-text">暂无数据</div>
      </div>
      
      <!-- 图表主体 -->
      <canvas
        v-else
        ref="chartCanvas"
        :width="canvasWidth"
        :height="canvasHeight"
      ></canvas>
    </div>
    
    <!-- X轴标签 -->
    <div v-if="showXAxis && xAxisLabels.length > 0" class="x-axis">
      <div
        v-for="(label, index) in xAxisLabels"
        :key="index"
        class="x-axis-label"
        :style="{ width: barWidth + 'px', marginLeft: barGap + 'px' }"
      >
        {{ label }}
      </div>
    </div>
    
    <!-- 工具提示 -->
    <div
      v-if="showTooltip && tooltip.visible"
      class="chart-tooltip"
      :style="tooltipStyle"
    >
      <div class="tooltip-title">{{ tooltip.title }}</div>
      <div v-for="item in tooltip.items" :key="item.name" class="tooltip-item">
        <div class="tooltip-color" :style="{ backgroundColor: item.color }"></div>
        <span class="tooltip-name">{{ item.name }}:</span>
        <span class="tooltip-value">{{ formatValue(item.value) }}</span>
      </div>
      <div v-if="tooltip.total" class="tooltip-total">
        总计: {{ formatValue(tooltip.total) }}
      </div>
    </div>
    
    <!-- 图表控制 -->
    <div v-if="showControls" class="chart-controls">
      <button
        v-if="chartType === 'stacked'"
        class="control-btn"
        @click="toggleChartType('grouped')"
      >
        分组显示
      </button>
      <button
        v-if="chartType === 'grouped'"
        class="control-btn"
        @click="toggleChartType('stacked')"
      >
        堆叠显示
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
            <th>类别</th>
            <th v-for="series in chartData.series" :key="series.name">
              {{ series.name }}
            </th>
            <th v-if="chartType === 'stacked'">总计</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(label, index) in xAxisLabels" :key="index">
            <td>{{ label }}</td>
            <td v-for="series in chartData.series" :key="series.name">
              {{ formatValue(series.data[index]) }}
            </td>
            <td v-if="chartType === 'stacked'">
              {{ formatValue(getCategoryTotal(index)) }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script>
export default {
  name: 'BarChart',
  props: {
    // 图表数据
    chartData: {
      type: Object,
      required: true,
      default: () => ({
        categories: [], // X轴分类
        series: []      // 数据系列 [{ name: '', data: [], color: '' }]
      })
    },
    
    // 图表配置
    options: {
      type: Object,
      default: () => ({
        title: '',
        height: 400,
        width: '100%',
        colors: ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399'],
        barWidth: 40,
        barGap: 20,
        animation: true,
        animationDuration: 800,
        showLegend: true,
        showXAxis: true,
        showYAxis: true,
        showGrid: true,
        showTooltip: true,
        showDataTable: false,
        showControls: true,
        yAxisMin: null,
        yAxisMax: null,
        formatValue: null,
        chartType: 'grouped' // 'grouped' 或 'stacked'
      })
    }
  },
  data() {
    return {
      chartType: 'grouped',
      showTable: false,
      tooltip: {
        visible: false,
        x: 0,
        y: 0,
        title: '',
        items: [],
        total: null
      },
      hiddenSeries: new Set(),
      canvasContext: null,
      animationFrame: null,
      animationProgress: 0,
      resizeObserver: null
    }
  },
  computed: {
    title() {
      return this.options.title || ''
    },
    
    chartHeight() {
      return this.options.height || 400
    },
    
    containerStyle() {
      return {
        width: this.options.width || '100%'
      }
    },
    
    canvasWidth() {
      const container = this.$refs.chartContainer
      return container ? container.clientWidth : 800
    },
    
    canvasHeight() {
      return this.chartHeight - 60 // 减去标题和边距
    },
    
    isEmpty() {
      return !this.chartData.series || this.chartData.series.length === 0 ||
             !this.chartData.categories || this.chartData.categories.length === 0
    },
    
    xAxisLabels() {
      return this.chartData.categories || []
    },
    
    legendData() {
      return this.chartData.series.map(series => ({
        name: series.name,
        color: series.color || this.getSeriesColor(series.name),
        value: series.data.reduce((sum, val) => sum + (val || 0), 0)
      }))
    },
    
    barWidth() {
      return this.options.barWidth || 40
    },
    
    barGap() {
      return this.options.barGap || 20
    },
    
    showLegend() {
      return this.options.showLegend !== false
    },
    
    showLegendValue() {
      return this.options.showLegendValue || false
    },
    
    showXAxis() {
      return this.options.showXAxis !== false
    },
    
    showYAxis() {
      return this.options.showYAxis !== false
    },
    
    showGrid() {
      return this.options.showGrid !== false
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
    
    tooltipStyle() {
      return {
        left: this.tooltip.x + 'px',
        top: this.tooltip.y + 'px',
        display: this.tooltip.visible ? 'block' : 'none'
      }
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
    
    hiddenSeries: {
      handler() {
        this.renderChart()
      },
      deep: true
    }
  },
  mounted() {
    this.initChart()
    
    // 监听容器大小变化
    this.resizeObserver = new ResizeObserver(() => {
      this.renderChart()
    })
    
    if (this.$refs.chartContainer) {
      this.resizeObserver.observe(this.$refs.chartContainer)
    }
  },
  beforeDestroy() {
    if (this.resizeObserver) {
      this.resizeObserver.disconnect()
    }
    
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
    },
    
    renderChart() {
      if (!this.canvasContext || this.isEmpty) return
      
      const ctx = this.canvasContext
      const width = this.canvasWidth
      const height = this.canvasHeight
      
      // 清除画布
      ctx.clearRect(0, 0, width, height)
      
      // 计算边距
      const margin = {
        top: 40,
        right: 40,
        bottom: this.showXAxis ? 60 : 40,
        left: this.showYAxis ? 80 : 40
      }
      
      const chartWidth = width - margin.left - margin.right
      const chartHeight = height - margin.top - margin.bottom
      
      // 绘制背景
      this.drawBackground(ctx, width, height, margin)
      
      // 计算数据范围
      const dataRange = this.calculateDataRange()
      const maxValue = dataRange.max
      const minValue = dataRange.min
      
      // 计算比例尺
      const xScale = chartWidth / (this.xAxisLabels.length * (this.barWidth + this.barGap) - this.barGap)
      const yScale = chartHeight / (maxValue - minValue)
      
      // 绘制Y轴
      if (this.showYAxis) {
        this.drawYAxis(ctx, margin, chartHeight, minValue, maxValue)
      }
      
      // 绘制网格
      if (this.showGrid) {
        this.drawGrid(ctx, margin, chartWidth, chartHeight, minValue, maxValue)
      }
      
      // 绘制柱状图
      const visibleSeries = this.chartData.series.filter(
        series => !this.hiddenSeries.has(series.name)
      )
      
      if (this.chartType === 'stacked') {
        this.drawStackedBars(ctx, visibleSeries, margin, chartWidth, chartHeight, xScale, yScale, minValue)
      } else {
        this.drawGroupedBars(ctx, visibleSeries, margin, chartWidth, chartHeight, xScale, yScale, minValue)
      }
      
      // 添加鼠标事件
      this.addChartEvents()
    },
    
    drawBackground(ctx, width, height, margin) {
      // 绘制背景
      ctx.fillStyle = '#f8f9fa'
      ctx.fillRect(margin.left, margin.top, 
                   width - margin.left - margin.right, 
                   height - margin.top - margin.bottom)
      
      // 绘制边框
      ctx.strokeStyle = '#dcdfe6'
      ctx.lineWidth = 1
      ctx.strokeRect(margin.left, margin.top, 
                     width - margin.left - margin.right, 
                     height - margin.top - margin.bottom)
    },
    
    drawYAxis(ctx, margin, chartHeight, minValue, maxValue) {
      ctx.strokeStyle = '#606266'
      ctx.lineWidth = 1
      ctx.beginPath()
      
      // Y轴线
      ctx.moveTo(margin.left, margin.top)
      ctx.lineTo(margin.left, margin.top + chartHeight)
      ctx.stroke()
      
      // Y轴刻度
      const tickCount = 6
      for (let i = 0; i <= tickCount; i++) {
        const y = margin.top + chartHeight - (i / tickCount) * chartHeight
        const value = minValue + (i / tickCount) * (maxValue - minValue)
        
        // 刻度线
        ctx.beginPath()
        ctx.moveTo(margin.left - 5, y)
        ctx.lineTo(margin.left, y)
        ctx.stroke()
        
        // 刻度标签
        ctx.fillStyle = '#606266'
        ctx.font = '12px Arial'
        ctx.textAlign = 'right'
        ctx.textBaseline = 'middle'
        ctx.fillText(this.formatValue(value), margin.left - 10, y)
      }
      
      // Y轴标题
      ctx.save()
      ctx.translate(20, margin.top + chartHeight / 2)
      ctx.rotate(-Math.PI / 2)
      ctx.textAlign = 'center'
      ctx.fillStyle = '#303133'
      ctx.font = '14px Arial'
      ctx.fillText('数值', 0, 0)
      ctx.restore()
    },
    
    drawGrid(ctx, margin, chartWidth, chartHeight, minValue, maxValue) {
      ctx.strokeStyle = '#ebeef5'
      ctx.lineWidth = 0.5
      
      // 水平网格线
      const tickCount = 6
      for (let i = 0; i <= tickCount; i++) {
        const y = margin.top + chartHeight - (i / tickCount) * chartHeight
        
        ctx.beginPath()
        ctx.moveTo(margin.left, y)
        ctx.lineTo(margin.left + chartWidth, y)
        ctx.stroke()
      }
      
      // 垂直网格线
      const categoryCount = this.xAxisLabels.length
      for (let i = 0; i <= categoryCount; i++) {
        const x = margin.left + (i / categoryCount) * chartWidth
        
        ctx.beginPath()
        ctx.moveTo(x, margin.top)
        ctx.lineTo(x, margin.top + chartHeight)
        ctx.stroke()
      }
    },
    
    drawGroupedBars(ctx, series, margin, chartWidth, chartHeight, xScale, yScale, minValue) {
      const categoryCount = this.xAxisLabels.length
      const seriesCount = series.length
      const groupWidth = this.barWidth + this.barGap
      const barWidth = this.barWidth / seriesCount
      
      series.forEach((seriesItem, seriesIndex) => {
        const color = seriesItem.color || this.getSeriesColor(seriesItem.name)
        
        seriesItem.data.forEach((value, categoryIndex) => {
          if (value === null || value === undefined) return
          
          const x = margin.left + categoryIndex * groupWidth * xScale + seriesIndex * barWidth * xScale
          const barHeight = (value - minValue) * yScale * this.animationProgress
          const y = margin.top + chartHeight - barHeight
          
          // 绘制柱状
          ctx.fillStyle = color
          ctx.fillRect(x, y, barWidth * xScale, barHeight)
          
          // 绘制边框
          ctx.strokeStyle = '#fff'
          ctx.lineWidth = 1
          ctx.strokeRect(x, y, barWidth * xScale, barHeight)
          
          // 绘制数值标签
          if (value > 0) {
            ctx.fillStyle = '#303133'
            ctx.font = '12px Arial'
            ctx.textAlign = 'center'
            ctx.textBaseline = 'bottom'
            ctx.fillText(
              this.formatValue(value),
              x + (barWidth * xScale) / 2,
              y - 5
            )
          }
        })
      })
    },
    
    drawStackedBars(ctx, series, margin, chartWidth, chartHeight, xScale, yScale, minValue) {
      const categoryCount = this.xAxisLabels.length
      const groupWidth = this.barWidth + this.barGap
      
      // 计算每个类别的累计值
      const categorySums = new Array(categoryCount).fill(0)
      series.forEach(seriesItem => {
        seriesItem.data.forEach((value, i) => {
          if (value) categorySums[i] += value
        })
      })
      
      series.forEach((seriesItem, seriesIndex) => {
        const color = seriesItem.color || this.getSeriesColor(seriesItem.name)
        
        seriesItem.data.forEach((value, categoryIndex) => {
          if (!value) return
          
          // 计算堆叠位置
          let prevHeight = 0
          for (let i = 0; i < seriesIndex; i++) {
            const prevValue = series[i].data[categoryIndex] || 0
            prevHeight += (prevValue - minValue) * yScale
          }
          
          const x = margin.left + categoryIndex * groupWidth * xScale
          const barHeight = (value - minValue) * yScale * this.animationProgress
          const y = margin.top + chartHeight - prevHeight - barHeight
          
          // 绘制柱状
          ctx.fillStyle = color
          ctx.fillRect(x, y, this.barWidth * xScale, barHeight)
          
          // 绘制边框
          ctx.strokeStyle = '#fff'
          ctx.lineWidth = 1
          ctx.strokeRect(x, y, this.barWidth * xScale, barHeight)
          
          // 绘制数值标签（只在最上面的柱状显示）
          if (seriesIndex === series.length - 1 && value > 0) {
            const totalHeight = (categorySums[categoryIndex] - minValue) * yScale
            ctx.fillStyle = '#303133'
            ctx.font = '12px Arial'
            ctx.textAlign = 'center'
            ctx.textBaseline = 'top'
            ctx.fillText(
              this.formatValue(categorySums[categoryIndex]),
              x + (this.barWidth * xScale) / 2,
              margin.top + chartHeight - totalHeight - 20
            )
          }
        })
      })
    },
    
    calculateDataRange() {
      let max = 0
      let min = 0
      
      this.chartData.series.forEach(series => {
        if (!series.data) return
        
        series.data.forEach(value => {
          if (value > max) max = value
          if (value < min) min = value
        })
      })
      
      // 如果有自定义范围，使用自定义范围
      if (this.options.yAxisMax !== null) max = this.options.yAxisMax
      if (this.options.yAxisMin !== null) min = this.options.yAxisMin
      
      // 确保最小值为0（对于柱状图通常从0开始）
      if (min > 0) min = 0
      
      // 添加一些边距
      max = max * 1.1
      
      return { max, min }
    },
    
    getSeriesColor(seriesName) {
      const colors = this.options.colors || ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399']
      const seriesIndex = this.chartData.series.findIndex(s => s.name === seriesName)
      return colors[seriesIndex % colors.length]
    },
    
    addChartEvents() {
      const canvas = this.$refs.chartCanvas
      if (!canvas) return
      
      canvas.onmousemove = (e) => {
        this.handleMouseMove(e)
      }
      
      canvas.onmouseleave = () => {
        this.tooltip.visible = false
      }
    },
    
    handleMouseMove(e) {
      if (!this.showTooltip) return
      
      const rect = this.$refs.chartCanvas.getBoundingClientRect()
      const x = e.clientX - rect.left
      const y = e.clientY - rect.top
      
      // 计算边距
      const margin = {
        left: this.showYAxis ? 80 : 40,
        top: 40,
        right: 40,
        bottom: this.showXAxis ? 60 : 40
      }
      
      const chartWidth = this.canvasWidth - margin.left - margin.right
      const groupWidth = this.barWidth + this.barGap
      const categoryCount = this.xAxisLabels.length
      
      // 计算鼠标所在的类别
      const categoryIndex = Math.floor((x - margin.left) / (chartWidth / categoryCount))
      
      if (categoryIndex >= 0 && categoryIndex < categoryCount) {
        const categoryName = this.xAxisLabels[categoryIndex]
        
        // 收集该类别下的所有数据
        const items = []
        let total = 0
        
        this.chartData.series.forEach(series => {
          if (this.hiddenSeries.has(series.name)) return
            
          const value = series.data[categoryIndex]
          if (value !== null && value !== undefined) {
            items.push({
              name: series.name,
              value: value,
              color: series.color || this.getSeriesColor(series.name)
            })
            total += value
          }
        })
        
        this.tooltip = {
          visible: true,
          x: e.clientX - rect.left + 15,
          y: e.clientY - rect.top - 15,
          title: categoryName,
          items: items,
          total: this.chartType === 'stacked' ? total : null
        }
      } else {
        this.tooltip.visible = false
      }
    },
    
    toggleLegend(seriesName) {
      if (this.hiddenSeries.has(seriesName)) {
        this.hiddenSeries.delete(seriesName)
      } else {
        this.hiddenSeries.add(seriesName)
      }
    },
    
    toggleChartType(type) {
      this.chartType = type
    },
    
    toggleDataTable() {
      this.showTable = !this.showTable
    },
    
    exportChart() {
      const canvas = this.$refs.chartCanvas
      if (!canvas) return
      
      const link = document.createElement('a')
      link.download = `bar-chart-${new Date().getTime()}.png`
      link.href = canvas.toDataURL('image/png')
      link.click()
    },
    
    getCategoryTotal(categoryIndex) {
      return this.chartData.series.reduce((sum, series) => {
        return sum + (series.data[categoryIndex] || 0)
      }, 0)
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
      const duration = this.options.animationDuration || 800
      
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
.bar-chart-container {
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
  
  .chart-legend {
    display: flex;
    flex-wrap: wrap;
    gap: 15px;
    margin-bottom: 20px;
    padding: 10px;
    background: #f8f9fa;
    border-radius: 6px;
    
    .legend-item {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      padding: 4px 8px;
      border-radius: 4px;
      transition: background-color 0.3s;
      
      &:hover {
        background: rgba(0, 0, 0, 0.05);
      }
      
      .legend-color {
        width: 12px;
        height: 12px;
        border-radius: 2px;
      }
      
      .legend-text {
        font-size: 14px;
        color: #606266;
      }
      
      .legend-value {
        font-size: 12px;
        color: #909399;
        margin-left: 4px;
      }
    }
  }
  
  .chart-wrapper {
    position: relative;
    
    .empty-chart {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 100%;
      color: #909399;
      
      .empty-icon {
        font-size: 48px;
        margin-bottom: 10px;
      }
      
      .empty-text {
        font-size: 16px;
      }
    }
    
    canvas {
      display: block;
      max-width: 100%;
    }
  }
  
  .x-axis {
    display: flex;
    justify-content: center;
    margin-top: 10px;
    padding: 0 40px;
    
    .x-axis-label {
      text-align: center;
      font-size: 12px;
      color: #606266;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
  
  .chart-tooltip {
    position: absolute;
    background: rgba(255, 255, 255, 0.95);
    border: 1px solid #dcdfe6;
    border-radius: 6px;
    padding: 12px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    z-index: 1000;
    min-width: 180px;
    
    .tooltip-title {
      font-weight: 600;
      color: #303133;
      margin-bottom: 8px;
      padding-bottom: 6px;
      border-bottom: 1px solid #ebeef5;
    }
    
    .tooltip-item {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 4px;
      
      .tooltip-color {
        width: 10px;
        height: 10px;
        border-radius: 2px;
      }
      
      .tooltip-name {
        font-size: 12px;
        color: #606266;
      }
      
      .tooltip-value {
        font-size: 12px;
        font-weight: 600;
        color: #303133;
        margin-left: auto;
      }
    }
    
    .tooltip-total {
      margin-top: 8px;
      padding-top: 6px;
      border-top: 1px solid #ebeef5;
      font-weight: 600;
      color: #409eff;
      font-size: 13px;
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
      background: #409eff;
      color: white;
      border: none;
      border-radius: 4px;
      cursor: pointer;
      font-size: 14px;
      transition: background-color 0.3s;
      
      &:hover {
        background: #66b1ff;
      }
      
      &:active {
        background: #3a8ee6;
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
      }
      
      tbody tr:hover {
        background: #f5f7fa;
      }
    }
  }
}
</style>