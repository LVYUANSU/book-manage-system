<template>
  <div class="line-chart-container" :style="containerStyle">
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
        <div class="empty-icon">📈</div>
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
        :style="{ width: (100 / xAxisLabels.length) + '%' }"
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
        <span v-if="item.change" class="tooltip-change" :class="getChangeClass(item.change)">
          {{ item.change > 0 ? '+' : '' }}{{ item.change.toFixed(1) }}%
        </span>
      </div>
      <div v-if="tooltip.total" class="tooltip-total">
        总计: {{ formatValue(tooltip.total) }}
      </div>
    </div>
    
    <!-- 图表控制 -->
    <div v-if="showControls" class="chart-controls">
      <button
        class="control-btn"
        @click="toggleSmooth"
        :class="{ 'active': isSmooth }"
      >
        {{ isSmooth ? '平滑曲线' : '折线' }}
      </button>
      <button
        class="control-btn"
        @click="toggleArea"
        :class="{ 'active': showArea }"
      >
        {{ showArea ? '面积图' : '折线图' }}
      </button>
      <button
        class="control-btn"
        @click="toggleStacked"
        v-if="canStacked"
        :class="{ 'active': isStacked }"
      >
        {{ isStacked ? '堆叠显示' : '普通显示' }}
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
            <th>时间/分类</th>
            <th v-for="series in chartData.series" :key="series.name">
              {{ series.name }}
            </th>
            <th v-if="isStacked">总计</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(label, index) in xAxisLabels" :key="index">
            <td>{{ label }}</td>
            <td v-for="series in chartData.series" :key="series.name">
              {{ formatValue(series.data[index]) }}
            </td>
            <td v-if="isStacked">
              {{ formatValue(getCategoryTotal(index)) }}
            </td>
          </tr>
        </tbody>
        <tfoot>
          <tr>
            <td>平均值</td>
            <td v-for="series in chartData.series" :key="series.name">
              {{ formatValue(calculateAverage(series.data)) }}
            </td>
            <td v-if="isStacked">
              {{ formatValue(calculateTotalAverage()) }}
            </td>
          </tr>
          <tr>
            <td>增长率</td>
            <td v-for="series in chartData.series" :key="series.name">
              <span :class="getChangeClass(calculateGrowthRate(series.data))">
                {{ calculateGrowthRate(series.data).toFixed(1) }}%
              </span>
            </td>
            <td v-if="isStacked">
              <span :class="getChangeClass(calculateTotalGrowthRate())">
                {{ calculateTotalGrowthRate().toFixed(1) }}%
              </span>
            </td>
          </tr>
        </tfoot>
      </table>
    </div>
    
    <!-- 预测区域 -->
    <div v-if="showForecast && forecastData" class="forecast-section">
      <h4>趋势预测</h4>
      <div class="forecast-chart">
        <!-- 这里可以添加预测图 -->
        <div class="forecast-text">
          基于历史数据预测未来 {{ forecastPeriod }} 个周期
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'LineChart',
  props: {
    // 图表数据
    chartData: {
      type: Object,
      required: true,
      default: () => ({
        categories: [], // X轴分类（时间或分类）
        series: []      // 数据系列 [{ name: '', data: [], color: '' }]
      })
    },
    
    // 预测数据
    forecastData: {
      type: Object,
      default: null
    },
    
    // 图表配置
    options: {
      type: Object,
      default: () => ({
        title: '',
        height: 400,
        width: '100%',
        colors: ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399'],
        lineWidth: 2,
        pointRadius: 4,
        smooth: true,
        showArea: false,
        stacked: false,
        animation: true,
        animationDuration: 1000,
        showLegend: true,
        showXAxis: true,
        showYAxis: true,
        showGrid: true,
        showTooltip: true,
        showDataTable: false,
        showControls: true,
        showForecast: false,
        forecastPeriod: 5,
        yAxisMin: null,
        yAxisMax: null,
        formatValue: null,
        showTrendLine: false
      })
    }
  },
  data() {
    return {
      isSmooth: true,
      showArea: false,
      isStacked: false,
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
        value: this.calculateTotal(series.data)
      }))
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
    
    showForecast() {
      return this.options.showForecast && this.forecastData
    },
    
    forecastPeriod() {
      return this.options.forecastPeriod || 5
    },
    
    canStacked() {
      return this.options.stacked !== false && this.chartData.series.length > 1
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
    
    isSmooth() {
      this.renderChart()
    },
    
    showArea() {
      this.renderChart()
    },
    
    isStacked() {
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
    
    // 初始化配置
    this.isSmooth = this.options.smooth !== false
    this.showArea = this.options.showArea || false
    this.isStacked = this.options.stacked || false
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
      const xScale = chartWidth / Math.max(1, this.xAxisLabels.length - 1)
      const yScale = chartHeight / (maxValue - minValue)
      
      // 绘制Y轴
      if (this.showYAxis) {
        this.drawYAxis(ctx, margin, chartHeight, minValue, maxValue)
      }
      
      // 绘制网格
      if (this.showGrid) {
        this.drawGrid(ctx, margin, chartWidth, chartHeight, minValue, maxValue)
      }
      
      // 绘制折线图
      const visibleSeries = this.chartData.series.filter(
        series => !this.hiddenSeries.has(series.name)
      )
      
      if (this.isStacked) {
        this.drawStackedLines(ctx, visibleSeries, margin, chartWidth, chartHeight, xScale, yScale, minValue)
      } else {
        this.drawLines(ctx, visibleSeries, margin, chartWidth, chartHeight, xScale, yScale, minValue)
      }
      
      // 绘制趋势线
      if (this.options.showTrendLine) {
        this.drawTrendLines(ctx, visibleSeries, margin, chartWidth, chartHeight, xScale, yScale, minValue)
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
      for (let i = 0; i < categoryCount; i++) {
        const x = margin.left + (i / (categoryCount - 1)) * chartWidth
        
        ctx.beginPath()
        ctx.moveTo(x, margin.top)
        ctx.lineTo(x, margin.top + chartHeight)
        ctx.stroke()
      }
    },
    
    drawLines(ctx, series, margin, chartWidth, chartHeight, xScale, yScale, minValue) {
      series.forEach(seriesItem => {
        const color = seriesItem.color || this.getSeriesColor(seriesItem.name)
        
        // 绘制数据点
        ctx.fillStyle = color
        seriesItem.data.forEach((value, index) => {
          if (value === null || value === undefined) return
          
          const x = margin.left + index * xScale
          const y = margin.top + chartHeight - (value - minValue) * yScale * this.animationProgress
          
          ctx.beginPath()
          ctx.arc(x, y, this.options.pointRadius || 4, 0, Math.PI * 2)
          ctx.fill()
          
          // 显示数据点值
          if (this.options.showPointValues && value !== 0) {
            ctx.fillStyle = '#303133'
            ctx.font = '10px Arial'
            ctx.textAlign = 'center'
            ctx.textBaseline = 'bottom'
            ctx.fillText(this.formatValue(value), x, y - 10)
          }
        })
        
        // 绘制折线
        ctx.beginPath()
        ctx.strokeStyle = color
        ctx.lineWidth = this.options.lineWidth || 2
        ctx.lineJoin = 'round'
        ctx.lineCap = 'round'
        
        let firstPoint = true
        seriesItem.data.forEach((value, index) => {
          if (value === null || value === undefined) return
          
          const x = margin.left + index * xScale
          const y = margin.top + chartHeight - (value - minValue) * yScale * this.animationProgress
          
          if (firstPoint) {
            ctx.moveTo(x, y)
            firstPoint = false
          } else {
            if (this.isSmooth && index > 0) {
              // 贝塞尔曲线
              const prevValue = seriesItem.data[index - 1]
              if (prevValue !== null && prevValue !== undefined) {
                const prevX = margin.left + (index - 1) * xScale
                const prevY = margin.top + chartHeight - (prevValue - minValue) * yScale * this.animationProgress
                
                const cp1x = prevX + xScale * 0.3
                const cp1y = prevY
                const cp2x = x - xScale * 0.3
                const cp2y = y
                
                ctx.bezierCurveTo(cp1x, cp1y, cp2x, cp2y, x, y)
              }
            } else {
              ctx.lineTo(x, y)
            }
          }
        })
        
        ctx.stroke()
        
        // 绘制面积
        if (this.showArea) {
          ctx.fillStyle = color + '40' // 添加透明度
          ctx.lineTo(margin.left + (seriesItem.data.length - 1) * xScale, margin.top + chartHeight)
          ctx.lineTo(margin.left, margin.top + chartHeight)
          ctx.closePath()
          ctx.fill()
        }
      })
    },
    
    drawStackedLines(ctx, series, margin, chartWidth, chartHeight, xScale, yScale, minValue) {
      // 计算每个数据点的累计值
      const stackedData = []
      const categoryCount = this.xAxisLabels.length
      
      for (let i = 0; i < categoryCount; i++) {
        stackedData[i] = []
        let sum = 0
        
        series.forEach(seriesItem => {
          const value = seriesItem.data[i] || 0
          sum += value
          stackedData[i].push({
            bottom: sum - value,
            top: sum,
            series: seriesItem
          })
        })
      }
      
      // 从下往上绘制
      series.forEach((seriesItem, seriesIndex) => {
        const color = seriesItem.color || this.getSeriesColor(seriesItem.name)
        
        // 绘制面积
        if (this.showArea) {
          ctx.beginPath()
          ctx.fillStyle = color + '80'
          
          // 上边界
          for (let i = 0; i < categoryCount; i++) {
            const data = stackedData[i][seriesIndex]
            const x = margin.left + i * xScale
            const y = margin.top + chartHeight - (data.top - minValue) * yScale * this.animationProgress
            
            if (i === 0) {
              ctx.moveTo(x, y)
            } else {
              if (this.isSmooth) {
                const prevData = stackedData[i - 1][seriesIndex]
                const prevX = margin.left + (i - 1) * xScale
                const prevY = margin.top + chartHeight - (prevData.top - minValue) * yScale * this.animationProgress
                
                const cp1x = prevX + xScale * 0.3
                const cp1y = prevY
                const cp2x = x - xScale * 0.3
                const cp2y = y
                
                ctx.bezierCurveTo(cp1x, cp1y, cp2x, cp2y, x, y)
              } else {
                ctx.lineTo(x, y)
              }
            }
          }
          
          // 下边界（倒序绘制）
          for (let i = categoryCount - 1; i >= 0; i--) {
            const data = stackedData[i][seriesIndex]
            const x = margin.left + i * xScale
            const y = margin.top + chartHeight - (data.bottom - minValue) * yScale * this.animationProgress
            
            if (this.isSmooth && i < categoryCount - 1) {
              const nextData = stackedData[i + 1][seriesIndex]
              const nextX = margin.left + (i + 1) * xScale
              const nextY = margin.top + chartHeight - (nextData.bottom - minValue) * yScale * this.animationProgress
              
              const cp1x = nextX - xScale * 0.3
              const cp1y = nextY
              const cp2x = x + xScale * 0.3
              const cp2y = y
              
              ctx.bezierCurveTo(cp1x, cp1y, cp2x, cp2y, x, y)
            } else {
              ctx.lineTo(x, y)
            }
          }
          
          ctx.closePath()
          ctx.fill()
        }
        
        // 绘制上边界线
        ctx.beginPath()
        ctx.strokeStyle = color
        ctx.lineWidth = this.options.lineWidth || 2
        ctx.lineJoin = 'round'
        ctx.lineCap = 'round'
        
        for (let i = 0; i < categoryCount; i++) {
          const data = stackedData[i][seriesIndex]
          const x = margin.left + i * xScale
          const y = margin.top + chartHeight - (data.top - minValue) * yScale * this.animationProgress
          
          if (i === 0) {
            ctx.moveTo(x, y)
          } else {
            if (this.isSmooth) {
              const prevData = stackedData[i - 1][seriesIndex]
              const prevX = margin.left + (i - 1) * xScale
              const prevY = margin.top + chartHeight - (prevData.top - minValue) * yScale * this.animationProgress
              
              const cp1x = prevX + xScale * 0.3
              const cp1y = prevY
              const cp2x = x - xScale * 0.3
              const cp2y = y
              
              ctx.bezierCurveTo(cp1x, cp1y, cp2x, cp2y, x, y)
            } else {
              ctx.lineTo(x, y)
            }
          }
        }
        
        ctx.stroke()
        
        // 绘制数据点
        ctx.fillStyle = color
        for (let i = 0; i < categoryCount; i++) {
          const data = stackedData[i][seriesIndex]
          const x = margin.left + i * xScale
          const y = margin.top + chartHeight - (data.top - minValue) * yScale * this.animationProgress
          
          ctx.beginPath()
          ctx.arc(x, y, this.options.pointRadius || 4, 0, Math.PI * 2)
          ctx.fill()
        }
      })
    },
    
    drawTrendLines(ctx, series, margin, chartWidth, chartHeight, xScale, yScale, minValue) {
      series.forEach(seriesItem => {
        const data = seriesItem.data.filter(val => val !== null && val !== undefined)
        if (data.length < 2) return
        
        // 计算线性回归
        const n = data.length
        let sumX = 0
        let sumY = 0
        let sumXY = 0
        let sumX2 = 0
        
        data.forEach((value, index) => {
          sumX += index
          sumY += value
          sumXY += index * value
          sumX2 += index * index
        })
        
        const slope = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX)
        const intercept = (sumY - slope * sumX) / n
        
        // 绘制趋势线
        ctx.beginPath()
        ctx.strokeStyle = this.getSeriesColor(seriesItem.name) + '80'
        ctx.lineWidth = 1
        ctx.setLineDash([5, 5])
        
        const startY = margin.top + chartHeight - (intercept - minValue) * yScale
        const endY = margin.top + chartHeight - (intercept + slope * (n - 1) - minValue) * yScale
        
        ctx.moveTo(margin.left, startY)
        ctx.lineTo(margin.left + (n - 1) * xScale, endY)
        ctx.stroke()
        
        ctx.setLineDash([])
      })
    },
    
    calculateDataRange() {
      let max = 0
      let min = 0
      
      if (this.isStacked) {
        // 堆叠模式下计算累计最大值
        const categoryCount = this.xAxisLabels.length
        for (let i = 0; i < categoryCount; i++) {
          let sum = 0
          this.chartData.series.forEach(series => {
            if (!this.hiddenSeries.has(series.name)) {
              sum += series.data[i] || 0
            }
          })
          max = Math.max(max, sum)
          min = Math.min(min, sum)
        }
      } else {
        // 普通模式下计算各系列最大值
        this.chartData.series.forEach(series => {
          if (this.hiddenSeries.has(series.name)) return
          
          if (!series.data) return
          
          series.data.forEach(value => {
            if (value > max) max = value
            if (value < min) min = value
          })
        })
      }
      
      // 如果有自定义范围，使用自定义范围
      if (this.options.yAxisMax !== null) max = this.options.yAxisMax
      if (this.options.yAxisMin !== null) min = this.options.yAxisMin
      
      // 添加一些边距
      const range = max - min
      max = max + range * 0.1
      min = min - range * 0.1
      
      // 确保最小值为0（对于某些图表类型）
      if (this.options.minFromZero && min > 0) min = 0
      
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
      const xScale = chartWidth / Math.max(1, this.xAxisLabels.length - 1)
      
      // 计算鼠标所在的索引
      const index = Math.round((x - margin.left) / xScale)
      
      if (index >= 0 && index < this.xAxisLabels.length) {
        const categoryName = this.xAxisLabels[index]
        
        // 收集该索引下的所有数据
        const items = []
        let total = 0
        let prevValues = {}
        
        this.chartData.series.forEach(series => {
          if (this.hiddenSeries.has(series.name)) return
            
          const value = series.data[index]
          if (value !== null && value !== undefined) {
            // 计算变化率
            let change = 0
            if (index > 0) {
              const prevValue = series.data[index - 1]
              if (prevValue && prevValue !== 0) {
                change = ((value - prevValue) / prevValue) * 100
              }
            }
            
            items.push({
              name: series.name,
              value: value,
              change: change,
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
          total: this.isStacked ? total : null
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
    
    toggleSmooth() {
      this.isSmooth = !this.isSmooth
    },
    
    toggleArea() {
      this.showArea = !this.showArea
    },
    
    toggleStacked() {
      this.isStacked = !this.isStacked
    },
    
    toggleDataTable() {
      this.showTable = !this.showTable
    },
    
    exportChart() {
      const canvas = this.$refs.chartCanvas
      if (!canvas) return
      
      const link = document.createElement('a')
      link.download = `line-chart-${new Date().getTime()}.png`
      link.href = canvas.toDataURL('image/png')
      link.click()
    },
    
    getCategoryTotal(index) {
      return this.chartData.series.reduce((sum, series) => {
        return sum + (series.data[index] || 0)
      }, 0)
    },
    
    calculateAverage(data) {
      const validData = data.filter(val => val !== null && val !== undefined)
      if (validData.length === 0) return 0
      
      const sum = validData.reduce((a, b) => a + b, 0)
      return sum / validData.length
    },
    
    calculateTotalAverage() {
      const totals = this.xAxisLabels.map((_, index) => this.getCategoryTotal(index))
      return this.calculateAverage(totals)
    },
    
    calculateGrowthRate(data) {
      const validData = data.filter(val => val !== null && val !== undefined)
      if (validData.length < 2) return 0
      
      const first = validData[0]
      const last = validData[validData.length - 1]
      
      if (first === 0) return 0
      
      return ((last - first) / first) * 100
    },
    
    calculateTotalGrowthRate() {
      const totals = this.xAxisLabels.map((_, index) => this.getCategoryTotal(index))
      return this.calculateGrowthRate(totals)
    },
    
    calculateTotal(data) {
      return data.reduce((sum, val) => sum + (val || 0), 0)
    },
    
    getChangeClass(change) {
      if (change > 0) return 'positive'
      if (change < 0) return 'negative'
      return 'neutral'
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
.line-chart-container {
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
    justify-content: space-between;
    margin-top: 10px;
    padding: 0 40px;
    
    .x-axis-label {
      text-align: center;
      font-size: 12px;
      color: #606266;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      transform: rotate(-45deg);
      transform-origin: left top;
      height: 40px;
      width: 40px;
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
    min-width: 200px;
    
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
        flex: 1;
      }
      
      .tooltip-value {
        font-size: 12px;
        font-weight: 600;
        color: #303133;
      }
      
      .tooltip-change {
        font-size: 11px;
        font-weight: 600;
        padding: 1px 4px;
        border-radius: 2px;
        
        &.positive {
          background: #f0f9eb;
          color: #67c23a;
        }
        
        &.negative {
          background: #fef0f0;
          color: #f56c6c;
        }
        
        &.neutral {
          background: #f4f4f5;
          color: #909399;
        }
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
        
        .positive {
          color: #67c23a;
        }
        
        .negative {
          color: #f56c6c;
        }
        
        .neutral {
          color: #909399;
        }
      }
      
      tbody tr:hover {
        background: #f5f7fa;
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
  
  .forecast-section {
    margin-top: 30px;
    padding: 20px;
    background: #f8f9fa;
    border-radius: 8px;
    border: 1px solid #e1f3d8;
    
    h4 {
      margin: 0 0 15px 0;
      color: #67c23a;
      font-size: 16px;
    }
    
    .forecast-chart {
      height: 100px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: white;
      border-radius: 6px;
      border: 1px solid #ebeef5;
      
      .forecast-text {
        color: #909399;
        font-size: 14px;
      }
    }
  }
}
</style>