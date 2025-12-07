<template>
  <div class="ag-line-chart-container" :class="containerClasses" :style="containerStyle">
    <!-- 图表头部 -->
    <div class="chart-header">
      <div class="header-left">
        <h3 v-if="title" class="chart-title">{{ title }}</h3>
        <div v-if="subtitle" class="chart-subtitle">{{ subtitle }}</div>
      </div>
      
      <div class="header-right">
        <!-- 时间范围选择 -->
        <div v-if="showTimeRange" class="time-range-selector">
          <button
            v-for="range in timeRanges"
            :key="range.value"
            class="range-btn"
            :class="{ 'active': activeRange === range.value }"
            @click="handleTimeRangeChange(range)"
          >
            {{ range.label }}
          </button>
        </div>
        
        <!-- 比较模式 -->
        <div v-if="showCompare" class="compare-selector">
          <label class="compare-label">
            <input
              type="checkbox"
              v-model="compareMode"
              @change="handleCompareModeChange"
            />
            <span class="compare-text">同比比较</span>
          </label>
        </div>
      </div>
    </div>
    
    <!-- 图表主体 -->
    <div class="chart-main">
      <div class="chart-container" ref="chartContainer">
        <!-- 图例 -->
        <div v-if="showLegend" class="chart-legend">
          <div
            v-for="series in visibleSeries"
            :key="series.name"
            class="legend-item"
            :class="{ 'hidden': hiddenSeries.has(series.name) }"
            @click="toggleSeries(series.name)"
            @mouseenter="highlightSeries(series.name)"
            @mouseleave="clearHighlight"
          >
            <div class="legend-marker" :style="getSeriesStyle(series)"></div>
            <div class="legend-content">
              <div class="legend-name">{{ series.name }}</div>
              <div class="legend-value">{{ formatValue(getSeriesCurrentValue(series)) }}</div>
              <div
                v-if="showTrend && series.trend"
                class="legend-trend"
                :class="getTrendClass(series.trend)"
              >
                {{ series.trend > 0 ? '↑' : '↓' }} {{ Math.abs(series.trend).toFixed(1) }}%
              </div>
            </div>
          </div>
        </div>
        
        <!-- 图表区域 -->
        <div class="chart-area">
          <!-- Y轴标签 -->
          <div v-if="showYAxis" class="y-axis">
            <div
              v-for="tick in yAxisTicks"
              :key="tick.value"
              class="y-tick"
              :style="{ bottom: tick.position + '%' }"
            >
              <span class="tick-line"></span>
              <span class="tick-label">{{ formatValue(tick.value) }}</span>
            </div>
          </div>
          
          <!-- 图表画布 -->
          <div class="chart-canvas-wrapper">
            <!-- 如果没有数据，显示空状态 -->
            <div v-if="isEmpty" class="empty-chart">
              <div class="empty-icon">📊</div>
              <div class="empty-text">暂无数据</div>
            </div>
            
            <!-- 图表画布 -->
            <canvas
              v-else
              ref="chartCanvas"
              :width="canvasWidth"
              :height="canvasHeight"
              class="chart-canvas"
            ></canvas>
            
            <!-- X轴标签 -->
            <div v-if="showXAxis" class="x-axis">
              <div
                v-for="(label, index) in xAxisLabels"
                :key="index"
                class="x-tick"
                :style="{ left: getXPosition(index) + '%' }"
              >
                {{ label }}
              </div>
            </div>
          </div>
        </div>
        
        <!-- 右侧信息面板 -->
        <div v-if="showInfoPanel" class="info-panel">
          <div class="panel-section">
            <h4 class="panel-title">当前值</h4>
            <div class="current-values">
              <div
                v-for="series in visibleSeries"
                :key="series.name"
                class="current-value-item"
                :class="{ 'highlighted': highlightedSeries === series.name }"
              >
                <div class="value-color" :style="getSeriesStyle(series)"></div>
                <div class="value-content">
                  <div class="value-name">{{ series.name }}</div>
                  <div class="value-number">{{ formatValue(getSeriesCurrentValue(series)) }}</div>
                </div>
              </div>
            </div>
          </div>
          
          <div v-if="showStats" class="panel-section">
            <h4 class="panel-title">统计信息</h4>
            <div class="stats-grid">
              <div class="stat-item">
                <div class="stat-label">峰值</div>
                <div class="stat-value">{{ formatValue(stats.max) }}</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">谷值</div>
                <div class="stat-value">{{ formatValue(stats.min) }}</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">平均值</div>
                <div class="stat-value">{{ formatValue(stats.average) }}</div>
              </div>
              <div class="stat-item">
                <div class="stat-label">总计</div>
                <div class="stat-value">{{ formatValue(stats.total) }}</div>
              </div>
            </div>
          </div>
          
          <div v-if="showPredictions" class="panel-section">
            <h4 class="panel-title">趋势预测</h4>
            <div class="prediction-list">
              <div
                v-for="prediction in predictions"
                :key="prediction.series"
                class="prediction-item"
              >
                <div class="prediction-name">{{ prediction.series }}</div>
                <div class="prediction-value">
                  <span class="prediction-number">{{ formatValue(prediction.value) }}</span>
                  <span
                    class="prediction-change"
                    :class="getTrendClass(prediction.change)"
                  >
                    {{ prediction.change > 0 ? '+' : '' }}{{ prediction.change.toFixed(1) }}%
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 工具提示 -->
    <div
      v-if="showTooltip && tooltip.visible"
      class="chart-tooltip"
      :style="tooltipStyle"
    >
      <div class="tooltip-header">
        <div class="tooltip-title">{{ tooltip.title }}</div>
        <div class="tooltip-time">{{ tooltip.time }}</div>
      </div>
      <div class="tooltip-content">
        <div
          v-for="item in tooltip.items"
          :key="item.name"
          class="tooltip-item"
          :class="{ 'highlighted': item.highlighted }"
        >
          <div class="tooltip-marker" :style="{ backgroundColor: item.color }"></div>
          <div class="tooltip-info">
            <div class="tooltip-name">{{ item.name }}</div>
            <div class="tooltip-value">{{ formatValue(item.value) }}</div>
          </div>
          <div
            v-if="item.change !== undefined"
            class="tooltip-change"
            :class="getTrendClass(item.change)"
          >
            {{ item.change > 0 ? '+' : '' }}{{ item.change.toFixed(1) }}%
          </div>
        </div>
      </div>
      <div v-if="tooltip.total" class="tooltip-total">
        总计: {{ formatValue(tooltip.total) }}
      </div>
    </div>
    
    <!-- 图表控制 -->
    <div v-if="showControls" class="chart-controls">
      <div class="controls-left">
        <button
          class="control-btn"
          @click="toggleChartType"
          :class="{ 'active': chartType === 'area' }"
        >
          {{ chartType === 'line' ? '面积图' : '折线图' }}
        </button>
        <button
          class="control-btn"
          @click="toggleStacked"
          :class="{ 'active': isStacked }"
        >
          {{ isStacked ? '普通显示' : '堆叠显示' }}
        </button>
        <button
          class="control-btn"
          @click="toggleSmooth"
          :class="{ 'active': isSmooth }"
        >
          {{ isSmooth ? '折线' : '平滑' }}
        </button>
      </div>
      
      <div class="controls-right">
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
        <button
          class="control-btn"
          @click="refreshData"
        >
          刷新数据
        </button>
      </div>
    </div>
    
    <!-- 数据表格 -->
    <div v-if="showTable && showDataTable" class="data-table-container">
      <div class="table-header">
        <h4>详细数据</h4>
        <button class="table-export" @click="exportData">导出CSV</button>
      </div>
      <div class="table-wrapper">
        <table class="data-table">
          <thead>
            <tr>
              <th>时间</th>
              <th v-for="series in chartData.series" :key="series.name">
                {{ series.name }}
              </th>
              <th v-if="isStacked">总计</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(time, index) in chartData.times" :key="index">
              <td>{{ formatTime(time) }}</td>
              <td v-for="series in chartData.series" :key="series.name">
                {{ formatValue(series.data[index]) }}
              </td>
              <td v-if="isStacked">
                {{ formatValue(getTimeTotal(index)) }}
              </td>
            </tr>
          </tbody>
          <tfoot>
            <tr>
              <td><strong>平均值</strong></td>
              <td v-for="series in chartData.series" :key="series.name">
                <strong>{{ formatValue(calculateAverage(series.data)) }}</strong>
              </td>
              <td v-if="isStacked">
                <strong>{{ formatValue(calculateTotalAverage()) }}</strong>
              </td>
            </tr>
          </tfoot>
        </table>
      </div>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="chart-loading">
      <div class="loading-spinner"></div>
      <div class="loading-text">数据加载中...</div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AgLineChart',
  props: {
    // 图表数据
    chartData: {
      type: Object,
      required: true,
      default: () => ({
        times: [], // 时间序列
        series: [] // 数据系列 [{ name: '', data: [], color: '', type: 'line'/'area' }]
      })
    },
    
    // 预测数据
    predictions: {
      type: Array,
      default: () => []
    },
    
    // 图表配置
    options: {
      type: Object,
      default: () => ({
        title: '',
        subtitle: '',
        height: 500,
        showLegend: true,
        showXAxis: true,
        showYAxis: true,
        showTooltip: true,
        showInfoPanel: true,
        showStats: true,
        showPredictions: false,
        showControls: true,
        showDataTable: false,
        showTimeRange: false,
        showCompare: false,
        showTrend: true,
        chartType: 'line', // 'line' 或 'area'
        stacked: false,
        smooth: true,
        animation: true,
        colors: ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399'],
        timeFormat: 'MM-DD',
        formatValue: null,
        theme: 'light' // 'light' 或 'dark'
      })
    },
    
    // 时间范围选项
    timeRanges: {
      type: Array,
      default: () => [
        { label: '1天', value: '1d' },
        { label: '1周', value: '7d' },
        { label: '1月', value: '30d' },
        { label: '3月', value: '90d' },
        { label: '1年', value: '365d' }
      ]
    },
    
    // 加载状态
    loading: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      activeRange: '7d',
      compareMode: false,
      chartType: 'line',
      isStacked: false,
      isSmooth: true,
      showTable: false,
      hiddenSeries: new Set(),
      highlightedSeries: null,
      tooltip: {
        visible: false,
        x: 0,
        y: 0,
        title: '',
        time: '',
        items: [],
        total: null
      },
      canvasContext: null,
      canvasWidth: 800,
      canvasHeight: 400,
      resizeObserver: null,
      hoverIndex: -1
    }
  },
  computed: {
    containerClasses() {
      return {
        [this.options.theme]: true,
        'loading': this.loading,
        'has-predictions': this.predictions.length > 0
      }
    },
    
    containerStyle() {
      return {
        height: this.options.height + 'px'
      }
    },
    
    isEmpty() {
      return !this.chartData.series || this.chartData.series.length === 0 ||
             !this.chartData.times || this.chartData.times.length === 0
    },
    
    visibleSeries() {
      return this.chartData.series.filter(series => !this.hiddenSeries.has(series.name))
    },
    
    showLegend() {
      return this.options.showLegend !== false && this.visibleSeries.length > 0
    },
    
    showXAxis() {
      return this.options.showXAxis !== false
    },
    
    showYAxis() {
      return this.options.showYAxis !== false
    },
    
    showTooltip() {
      return this.options.showTooltip !== false
    },
    
    showInfoPanel() {
      return this.options.showInfoPanel !== false
    },
    
    showStats() {
      return this.options.showStats !== false
    },
    
    showPredictions() {
      return this.options.showPredictions && this.predictions.length > 0
    },
    
    showControls() {
      return this.options.showControls !== false
    },
    
    showDataTable() {
      return this.options.showDataTable || false
    },
    
    showTimeRange() {
      return this.options.showTimeRange && this.timeRanges.length > 0
    },
    
    showCompare() {
      return this.options.showCompare
    },
    
    showTrend() {
      return this.options.showTrend
    },
    
    xAxisLabels() {
      if (!this.chartData.times) return []
      
      // 根据时间格式格式化标签
      return this.chartData.times.map(time => this.formatTime(time))
    },
    
    yAxisTicks() {
      const dataRange = this.calculateDataRange()
      const maxValue = dataRange.max
      const minValue = dataRange.min
      const range = maxValue - minValue
      
      // 生成5个刻度
      const ticks = []
      const tickCount = 5
      
      for (let i = 0; i <= tickCount; i++) {
        const value = minValue + (range * i) / tickCount
        const position = (i / tickCount) * 100
        
        ticks.push({
          value: value,
          position: position
        })
      }
      
      return ticks
    },
    
    stats() {
      if (this.isEmpty) {
        return { max: 0, min: 0, average: 0, total: 0 }
      }
      
      const allValues = this.visibleSeries.flatMap(series => series.data)
      const max = Math.max(...allValues)
      const min = Math.min(...allValues)
      const total = allValues.reduce((sum, val) => sum + val, 0)
      const average = total / allValues.length
      
      return { max, min, average, total }
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
          this.initChartSize()
          this.renderChart()
        })
      },
      deep: true
    },
    
    chartType() {
      this.renderChart()
    },
    
    isStacked() {
      this.renderChart()
    },
    
    isSmooth() {
      this.renderChart()
    },
    
    hiddenSeries: {
      handler() {
        this.renderChart()
      },
      deep: true
    },
    
    highlightedSeries() {
      this.renderChart()
    }
  },
  mounted() {
    this.initChart()
    this.initChartSize()
    
    // 监听窗口大小变化
    window.addEventListener('resize', this.handleResize)
    
    // 初始化配置
    this.chartType = this.options.chartType || 'line'
    this.isStacked = this.options.stacked || false
    this.isSmooth = this.options.smooth !== false
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    
    if (this.resizeObserver) {
      this.resizeObserver.disconnect()
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
    
    initChartSize() {
      const container = this.$refs.chartContainer
      if (!container) return
      
      const rect = container.getBoundingClientRect()
      this.canvasWidth = rect.width - 200 // 减去图例和信息面板的宽度
      this.canvasHeight = rect.height - 60 // 减去X轴的高度
    },
    
    handleResize() {
      this.initChartSize()
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
        top: 20,
        right: 20,
        bottom: 0, // X轴在HTML中渲染
        left: this.showYAxis ? 60 : 20
      }
      
      const chartWidth = width - margin.left - margin.right
      const chartHeight = height - margin.top - margin.bottom
      
      // 绘制网格
      this.drawGrid(ctx, margin, chartWidth, chartHeight)
      
      // 计算数据范围
      const dataRange = this.calculateDataRange()
      const maxValue = dataRange.max
      const minValue = dataRange.min
      const yScale = chartHeight / (maxValue - minValue)
      
      // 绘制图表
      if (this.isStacked) {
        this.drawStackedChart(ctx, margin, chartWidth, chartHeight, yScale, minValue)
      } else {
        this.drawLineChart(ctx, margin, chartWidth, chartHeight, yScale, minValue)
      }
      
      // 绘制悬停线
      if (this.hoverIndex >= 0) {
        this.drawHoverLine(ctx, margin, chartWidth, chartHeight, this.hoverIndex)
      }
    },
    
    drawGrid(ctx, margin, chartWidth, chartHeight) {
      ctx.strokeStyle = this.options.theme === 'dark' ? '#444' : '#ebeef5'
      ctx.lineWidth = 0.5
      
      // 水平网格线
      const horizontalLines = 5
      for (let i = 0; i <= horizontalLines; i++) {
        const y = margin.top + (chartHeight * i) / horizontalLines
        
        ctx.beginPath()
        ctx.moveTo(margin.left, y)
        ctx.lineTo(margin.left + chartWidth, y)
        ctx.stroke()
      }
      
      // 垂直网格线
      const timeCount = this.chartData.times.length
      if (timeCount > 1) {
        for (let i = 0; i < timeCount; i++) {
          const x = margin.left + (chartWidth * i) / (timeCount - 1)
          
          ctx.beginPath()
          ctx.moveTo(x, margin.top)
          ctx.lineTo(x, margin.top + chartHeight)
          ctx.stroke()
        }
      }
    },
    
    drawLineChart(ctx, margin, chartWidth, chartHeight, yScale, minValue) {
      const timeCount = this.chartData.times.length
      const xScale = timeCount > 1 ? chartWidth / (timeCount - 1) : 0
      
      this.visibleSeries.forEach(series => {
        const isHighlighted = this.highlightedSeries === series.name
        const color = series.color || this.getSeriesColor(series.name)
        const opacity = isHighlighted ? 1 : (this.highlightedSeries ? 0.3 : 1)
        
        ctx.strokeStyle = color
        ctx.fillStyle = color
        ctx.globalAlpha = opacity
        
        if (this.chartType === 'area') {
          // 绘制面积图
          ctx.beginPath()
          
          // 移动到第一个点
          const firstValue = series.data[0] || 0
          const firstX = margin.left
          const firstY = margin.top + chartHeight - (firstValue - minValue) * yScale
          ctx.moveTo(firstX, firstY)
          
          // 绘制曲线
          for (let i = 1; i < timeCount; i++) {
            const value = series.data[i] || 0
            const x = margin.left + i * xScale
            const y = margin.top + chartHeight - (value - minValue) * yScale
            
            if (this.isSmooth && i > 0) {
              const prevValue = series.data[i - 1] || 0
              const prevX = margin.left + (i - 1) * xScale
              const prevY = margin.top + chartHeight - (prevValue - minValue) * yScale
              
              const cp1x = prevX + xScale * 0.3
              const cp1y = prevY
              const cp2x = x - xScale * 0.3
              const cp2y = y
              
              ctx.bezierCurveTo(cp1x, cp1y, cp2x, cp2y, x, y)
            } else {
              ctx.lineTo(x, y)
            }
          }
          
          // 闭合路径形成面积
          ctx.lineTo(margin.left + chartWidth, margin.top + chartHeight)
          ctx.lineTo(margin.left, margin.top + chartHeight)
          ctx.closePath()
          
          // 填充面积
          ctx.fillStyle = color + '40'
          ctx.fill()
          
          // 绘制边框
          ctx.strokeStyle = color
          ctx.stroke()
        } else {
          // 绘制折线图
          ctx.beginPath()
          ctx.lineWidth = isHighlighted ? 3 : 2
          ctx.lineJoin = 'round'
          ctx.lineCap = 'round'
          
          for (let i = 0; i < timeCount; i++) {
            const value = series.data[i] || 0
            const x = margin.left + i * xScale
            const y = margin.top + chartHeight - (value - minValue) * yScale
            
            if (i === 0) {
              ctx.moveTo(x, y)
            } else {
              if (this.isSmooth) {
                const prevValue = series.data[i - 1] || 0
                const prevX = margin.left + (i - 1) * xScale
                const prevY = margin.top + chartHeight - (prevValue - minValue) * yScale
                
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
          if (timeCount <= 20) { // 数据点过多时不显示
            for (let i = 0; i < timeCount; i++) {
              const value = series.data[i] || 0
              const x = margin.left + i * xScale
              const y = margin.top + chartHeight - (value - minValue) * yScale
              
              ctx.beginPath()
              ctx.arc(x, y, 4, 0, Math.PI * 2)
              ctx.fillStyle = color
              ctx.fill()
              ctx.strokeStyle = 'white'
              ctx.lineWidth = 2
              ctx.stroke()
            }
          }
        }
        
        ctx.globalAlpha = 1
      })
    },
    
    drawStackedChart(ctx, margin, chartWidth, chartHeight, yScale, minValue) {
      const timeCount = this.chartData.times.length
      const xScale = timeCount > 1 ? chartWidth / (timeCount - 1) : 0
      
      // 计算堆叠数据
      const stackedData = []
      for (let i = 0; i < timeCount; i++) {
        stackedData[i] = []
        let sum = 0
        
        this.visibleSeries.forEach(series => {
          const value = series.data[i] || 0
          stackedData[i].push({
            bottom: sum,
            top: sum + value,
            series: series
          })
          sum += value
        })
      }
      
      // 从下往上绘制
      this.visibleSeries.forEach((series, seriesIndex) => {
        const isHighlighted = this.highlightedSeries === series.name
        const color = series.color || this.getSeriesColor(series.name)
        const opacity = isHighlighted ? 1 : (this.highlightedSeries ? 0.3 : 1)
        
        ctx.strokeStyle = color
        ctx.fillStyle = color
        ctx.globalAlpha = opacity
        
        // 绘制堆叠区域
        ctx.beginPath()
        
        // 上边界线
        for (let i = 0; i < timeCount; i++) {
          const data = stackedData[i][seriesIndex]
          const x = margin.left + i * xScale
          const y = margin.top + chartHeight - (data.top - minValue) * yScale
          
          if (i === 0) {
            ctx.moveTo(x, y)
          } else {
            if (this.isSmooth) {
              const prevData = stackedData[i - 1][seriesIndex]
              const prevX = margin.left + (i - 1) * xScale
              const prevY = margin.top + chartHeight - (prevData.top - minValue) * yScale
              
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
        
        // 下边界线（倒序）
        for (let i = timeCount - 1; i >= 0; i--) {
          const data = stackedData[i][seriesIndex]
          const x = margin.left + i * xScale
          const y = margin.top + chartHeight - (data.bottom - minValue) * yScale
          
          if (this.isSmooth && i < timeCount - 1) {
            const nextData = stackedData[i + 1][seriesIndex]
            const nextX = margin.left + (i + 1) * xScale
            const nextY = margin.top + chartHeight - (nextData.bottom - minValue) * yScale
            
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
        
        // 填充区域
        if (this.chartType === 'area') {
          ctx.fillStyle = color + '80'
          ctx.fill()
        }
        
        // 绘制边框
        ctx.strokeStyle = color
        ctx.lineWidth = isHighlighted ? 3 : 2
        ctx.stroke()
        
        ctx.globalAlpha = 1
      })
    },
    
    drawHoverLine(ctx, margin, chartWidth, chartHeight, index) {
      const timeCount = this.chartData.times.length
      const xScale = timeCount > 1 ? chartWidth / (timeCount - 1) : 0
      const x = margin.left + index * xScale
      
      ctx.strokeStyle = '#409eff'
      ctx.lineWidth = 1
      ctx.setLineDash([5, 5])
      
      ctx.beginPath()
      ctx.moveTo(x, margin.top)
      ctx.lineTo(x, margin.top + chartHeight)
      ctx.stroke()
      
      ctx.setLineDash([])
      
      // 绘制悬停点
      this.visibleSeries.forEach(series => {
        const value = series.data[index] || 0
        const dataRange = this.calculateDataRange()
        const yScale = chartHeight / (dataRange.max - dataRange.min)
        const y = margin.top + chartHeight - (value - dataRange.min) * yScale
        
        ctx.beginPath()
        ctx.arc(x, y, 6, 0, Math.PI * 2)
        ctx.fillStyle = series.color || this.getSeriesColor(series.name)
        ctx.fill()
        ctx.strokeStyle = 'white'
        ctx.lineWidth = 2
        ctx.stroke()
      })
    },
    
    calculateDataRange() {
      if (this.isEmpty) return { max: 100, min: 0 }
      
      let max = 0
      let min = 0
      
      if (this.isStacked) {
        // 堆叠模式下计算累计最大值
        const timeCount = this.chartData.times.length
        for (let i = 0; i < timeCount; i++) {
          let sum = 0
          this.visibleSeries.forEach(series => {
            sum += series.data[i] || 0
          })
          max = Math.max(max, sum)
          min = Math.min(min, sum)
        }
      } else {
        // 普通模式下计算各系列最大值
        this.visibleSeries.forEach(series => {
          series.data.forEach(value => {
            if (value > max) max = value
            if (value < min) min = value
          })
        })
      }
      
      // 添加边距
      const range = max - min
      max = max + range * 0.1
      min = min - range * 0.1
      
      // 确保最小值不为负数（对于某些图表类型）
      if (this.options.minFromZero && min < 0) min = 0
      
      return { max, min }
    },
    
    getSeriesColor(seriesName) {
      const colors = this.options.colors || ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399']
      const index = this.chartData.series.findIndex(s => s.name === seriesName)
      return colors[index % colors.length]
    },
    
    getSeriesStyle(series) {
      const color = series.color || this.getSeriesColor(series.name)
      
      if (this.chartType === 'area') {
        return {
          backgroundColor: color + '40',
          borderColor: color
        }
      } else {
        return {
          backgroundColor: color,
          borderColor: color
        }
      }
    },
    
    getSeriesCurrentValue(series) {
      if (!series.data || series.data.length === 0) return 0
      return series.data[series.data.length - 1]
    },
    
    getTrendClass(trend) {
      if (trend > 0) return 'positive'
      if (trend < 0) return 'negative'
      return 'neutral'
    },
    
    getXPosition(index) {
      const timeCount = this.chartData.times.length
      return timeCount > 1 ? (index / (timeCount - 1)) * 100 : 50
    },
    
    addChartEvents() {
      const canvas = this.$refs.chartCanvas
      if (!canvas) return
      
      canvas.onmousemove = (e) => {
        this.handleMouseMove(e)
      }
      
      canvas.onmouseleave = () => {
        this.hoverIndex = -1
        this.tooltip.visible = false
        this.renderChart()
      }
    },
    
    handleMouseMove(e) {
      if (!this.showTooltip) return
      
      const rect = this.$refs.chartCanvas.getBoundingClientRect()
      const x = e.clientX - rect.left
      const y = e.clientY - rect.top
      
      // 计算边距
      const margin = {
        left: this.showYAxis ? 60 : 20,
        top: 20,
        right: 20,
        bottom: 0
      }
      
      const chartWidth = this.canvasWidth - margin.left - margin.right
      const timeCount = this.chartData.times.length
      const xScale = timeCount > 1 ? chartWidth / (timeCount - 1) : 0
      
      // 计算悬停索引
      const hoverIndex = Math.round((x - margin.left) / xScale)
      
      if (hoverIndex >= 0 && hoverIndex < timeCount) {
        this.hoverIndex = hoverIndex
        
        // 更新工具提示
        const time = this.chartData.times[hoverIndex]
        const items = []
        let total = 0
        
        this.visibleSeries.forEach(series => {
          const value = series.data[hoverIndex] || 0
          const isHighlighted = this.highlightedSeries === series.name
          
          // 计算变化率（与前一个点比较）
          let change = 0
          if (hoverIndex > 0) {
            const prevValue = series.data[hoverIndex - 1] || 0
            if (prevValue !== 0) {
              change = ((value - prevValue) / prevValue) * 100
            }
          }
          
          items.push({
            name: series.name,
            value: value,
            change: change,
            color: series.color || this.getSeriesColor(series.name),
            highlighted: isHighlighted
          })
          
          total += value
        })
        
        this.tooltip = {
          visible: true,
          x: e.clientX + 15,
          y: e.clientY - 15,
          title: this.options.title || '',
          time: this.formatTime(time),
          items: items,
          total: this.isStacked ? total : null
        }
        
        this.renderChart()
      } else {
        this.hoverIndex = -1
        this.tooltip.visible = false
        this.renderChart()
      }
    },
    
    toggleSeries(seriesName) {
      if (this.hiddenSeries.has(seriesName)) {
        this.hiddenSeries.delete(seriesName)
      } else {
        this.hiddenSeries.add(seriesName)
      }
    },
    
    highlightSeries(seriesName) {
      this.highlightedSeries = seriesName
    },
    
    clearHighlight() {
      this.highlightedSeries = null
    },
    
    handleTimeRangeChange(range) {
      this.activeRange = range.value
      this.$emit('time-range-change', range)
    },
    
    handleCompareModeChange() {
      this.$emit('compare-mode-change', this.compareMode)
    },
    
    toggleChartType() {
      this.chartType = this.chartType === 'line' ? 'area' : 'line'
    },
    
    toggleStacked() {
      this.isStacked = !this.isStacked
    },
    
    toggleSmooth() {
      this.isSmooth = !this.isSmooth
    },
    
    toggleDataTable() {
      this.showTable = !this.showTable
    },
    
    exportChart() {
      const canvas = this.$refs.chartCanvas
      if (!canvas) return
      
      const link = document.createElement('a')
      link.download = `ag-line-chart-${new Date().getTime()}.png`
      link.href = canvas.toDataURL('image/png')
      link.click()
    },
    
    exportData() {
      const headers = ['时间', ...this.chartData.series.map(s => s.name)]
      if (this.isStacked) headers.push('总计')
      
      const rows = this.chartData.times.map((time, index) => {
        const row = [this.formatTime(time)]
        
        this.chartData.series.forEach(series => {
          row.push(this.formatValue(series.data[index] || 0))
        })
        
        if (this.isStacked) {
          const total = this.chartData.series.reduce((sum, series) => sum + (series.data[index] || 0), 0)
          row.push(this.formatValue(total))
        }
        
        return row
      })
      
      const csvContent = [headers, ...rows]
          .map(row => row.join(','))
          .join('\n')
      
      const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' })
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = `chart-data-${new Date().getTime()}.csv`
      link.click()
    },
    
    refreshData() {
      this.$emit('refresh-data')
    },
    
    getTimeTotal(index) {
      return this.chartData.series.reduce((sum, series) => sum + (series.data[index] || 0), 0)
    },
    
    calculateAverage(data) {
      const validData = data.filter(val => val !== null && val !== undefined)
      if (validData.length === 0) return 0
      
      const sum = validData.reduce((a, b) => a + b, 0)
      return sum / validData.length
    },
    
    calculateTotalAverage() {
      const totals = this.chartData.times.map((_, index) => this.getTimeTotal(index))
      return this.calculateAverage(totals)
    },
    
    formatTime(time) {
      if (typeof time === 'string') return time
      
      if (typeof time === 'number') {
        const date = new Date(time)
        const format = this.options.timeFormat || 'MM-DD'
        
        switch (format) {
          case 'MM-DD':
            return `${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
          case 'YYYY-MM-DD':
            return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`
          case 'HH:mm':
            return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
          default:
            return date.toLocaleDateString()
        }
      }
      
      return String(time)
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
    }
  }
}
</script>

<style scoped lang="scss">
.ag-line-chart-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  padding: 24px;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  transition: all 0.3s;
  
  &.dark {
    background: #2c3e50;
    color: white;
    
    .chart-header,
    .chart-legend,
    .info-panel,
    .data-table-container {
      background: #34495e;
      color: white;
    }
    
    .y-axis .tick-label,
    .x-axis .x-tick {
      color: #bdc3c7;
    }
  }
  
  &.loading {
    opacity: 0.7;
    pointer-events: none;
  }
  
  .chart-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 24px;
    
    .header-left {
      .chart-title {
        margin: 0 0 8px 0;
        font-size: 24px;
        font-weight: 600;
        color: #303133;
      }
      
      .chart-subtitle {
        font-size: 14px;
        color: #909399;
      }
    }
    
    .header-right {
      display: flex;
      gap: 20px;
      align-items: center;
      
      .time-range-selector {
        display: flex;
        gap: 8px;
        background: #f5f7fa;
        padding: 4px;
        border-radius: 6px;
        
        .range-btn {
          padding: 6px 12px;
          background: transparent;
          border: none;
          border-radius: 4px;
          font-size: 14px;
          color: #606266;
          cursor: pointer;
          transition: all 0.3s;
          
          &:hover {
            background: white;
          }
          
          &.active {
            background: white;
            color: #409eff;
            font-weight: 600;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
          }
        }
      }
      
      .compare-selector {
        .compare-label {
          display: flex;
          align-items: center;
          gap: 8px;
          cursor: pointer;
          font-size: 14px;
          color: #606266;
          
          input[type="checkbox"] {
            width: 16px;
            height: 16px;
            cursor: pointer;
          }
        }
      }
    }
  }
  
  .chart-main {
    .chart-container {
      display: flex;
      gap: 24px;
      height: 400px;
      
      .chart-legend {
        width: 200px;
        flex-shrink: 0;
        overflow-y: auto;
        padding: 16px;
        background: #f8f9fa;
        border-radius: 8px;
        
        .legend-item {
          display: flex;
          align-items: center;
          gap: 12px;
          padding: 12px;
          margin-bottom: 8px;
          border-radius: 6px;
          cursor: pointer;
          transition: all 0.3s;
          
          &:hover {
            background: white;
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
          }
          
          &.hidden {
            opacity: 0.5;
            
            .legend-name {
              text-decoration: line-through;
            }
          }
          
          .legend-marker {
            width: 16px;
            height: 16px;
            border-radius: 4px;
            border-width: 2px;
            border-style: solid;
            flex-shrink: 0;
          }
          
          .legend-content {
            flex: 1;
            
            .legend-name {
              font-size: 14px;
              font-weight: 500;
              color: #303133;
              margin-bottom: 4px;
            }
            
            .legend-value {
              font-size: 16px;
              font-weight: 600;
              color: #409eff;
              margin-bottom: 4px;
            }
            
            .legend-trend {
              font-size: 12px;
              font-weight: 600;
              padding: 2px 6px;
              border-radius: 10px;
              display: inline-block;
              
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
        }
      }
      
      .chart-area {
        flex: 1;
        display: flex;
        position: relative;
        
        .y-axis {
          width: 60px;
          position: relative;
          flex-shrink: 0;
          
          .y-tick {
            position: absolute;
            left: 0;
            width: 100%;
            display: flex;
            align-items: center;
            
            .tick-line {
              width: 10px;
              height: 1px;
              background: #dcdfe6;
              margin-right: 8px;
            }
            
            .tick-label {
              font-size: 12px;
              color: #909399;
              white-space: nowrap;
            }
          }
        }
        
        .chart-canvas-wrapper {
          flex: 1;
          position: relative;
          
          .empty-chart {
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            color: #909399;
            
            .empty-icon {
              font-size: 48px;
              margin-bottom: 16px;
            }
            
            .empty-text {
              font-size: 16px;
            }
          }
          
          .chart-canvas {
            display: block;
            width: 100%;
            height: 100%;
          }
          
          .x-axis {
            position: absolute;
            bottom: -40px;
            left: 0;
            right: 0;
            display: flex;
            justify-content: space-between;
            
            .x-tick {
              position: absolute;
              transform: translateX(-50%);
              font-size: 12px;
              color: #909399;
              white-space: nowrap;
            }
          }
        }
      }
      
      .info-panel {
        width: 250px;
        flex-shrink: 0;
        padding: 16px;
        background: #f8f9fa;
        border-radius: 8px;
        overflow-y: auto;
        
        .panel-section {
          margin-bottom: 24px;
          
          &:last-child {
            margin-bottom: 0;
          }
          
          .panel-title {
            margin: 0 0 16px 0;
            font-size: 16px;
            font-weight: 600;
            color: #303133;
          }
          
          .current-values {
            .current-value-item {
              display: flex;
              align-items: center;
              gap: 12px;
              padding: 12px;
              margin-bottom: 8px;
              border-radius: 6px;
              transition: all 0.3s;
              
              &.highlighted {
                background: white;
                box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
              }
              
              .value-color {
                width: 12px;
                height: 12px;
                border-radius: 4px;
                flex-shrink: 0;
              }
              
              .value-content {
                flex: 1;
                
                .value-name {
                  font-size: 14px;
                  color: #606266;
                  margin-bottom: 4px;
                }
                
                .value-number {
                  font-size: 18px;
                  font-weight: 600;
                  color: #409eff;
                }
              }
            }
          }
          
          .stats-grid {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            gap: 12px;
            
            .stat-item {
              padding: 12px;
              background: white;
              border-radius: 6px;
              text-align: center;
              
              .stat-label {
                font-size: 12px;
                color: #909399;
                margin-bottom: 4px;
              }
              
              .stat-value {
                font-size: 16px;
                font-weight: 600;
                color: #303133;
              }
            }
          }
          
          .prediction-list {
            .prediction-item {
              display: flex;
              justify-content: space-between;
              align-items: center;
              padding: 10px 0;
              border-bottom: 1px solid #ebeef5;
              
              &:last-child {
                border-bottom: none;
              }
              
              .prediction-name {
                font-size: 14px;
                color: #606266;
              }
              
              .prediction-value {
                display: flex;
                flex-direction: column;
                align-items: flex-end;
                
                .prediction-number {
                  font-size: 16px;
                  font-weight: 600;
                  color: #303133;
                  margin-bottom: 4px;
                }
                
                .prediction-change {
                  font-size: 12px;
                  font-weight: 600;
                  padding: 2px 6px;
                  border-radius: 10px;
                  
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
            }
          }
        }
      }
    }
  }
  
  .chart-tooltip {
    position: fixed;
    background: rgba(255, 255, 255, 0.95);
    border: 1px solid #dcdfe6;
    border-radius: 8px;
    padding: 16px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
    z-index: 1000;
    min-width: 240px;
    backdrop-filter: blur(10px);
    
    .tooltip-header {
      margin-bottom: 12px;
      padding-bottom: 12px;
      border-bottom: 1px solid #ebeef5;
      
      .tooltip-title {
        font-size: 14px;
        font-weight: 600;
        color: #303133;
        margin-bottom: 4px;
      }
      
      .tooltip-time {
        font-size: 12px;
        color: #909399;
      }
    }
    
    .tooltip-content {
      .tooltip-item {
        display: flex;
        align-items: center;
        gap: 12px;
        padding: 8px;
        margin-bottom: 4px;
        border-radius: 4px;
        
        &.highlighted {
          background: #f5f7fa;
        }
        
        .tooltip-marker {
          width: 12px;
          height: 12px;
          border-radius: 4px;
          flex-shrink: 0;
        }
        
        .tooltip-info {
          flex: 1;
          
          .tooltip-name {
            font-size: 13px;
            color: #606266;
            margin-bottom: 2px;
          }
          
          .tooltip-value {
            font-size: 14px;
            font-weight: 600;
            color: #303133;
          }
        }
        
        .tooltip-change {
          font-size: 12px;
          font-weight: 600;
          padding: 2px 6px;
          border-radius: 10px;
          
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
    }
    
    .tooltip-total {
      margin-top: 12px;
      padding-top: 12px;
      border-top: 1px solid #ebeef5;
      font-weight: 600;
      color: #409eff;
      font-size: 14px;
    }
  }
  
  .chart-controls {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 24px;
    padding-top: 24px;
    border-top: 1px solid #ebeef5;
    
    .controls-left,
    .controls-right {
      display: flex;
      gap: 8px;
    }
    
    .control-btn {
      padding: 8px 16px;
      background: #f5f7fa;
      color: #606266;
      border: 1px solid #dcdfe6;
      border-radius: 6px;
      font-size: 14px;
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
  
  .data-table-container {
    margin-top: 24px;
    padding: 24px;
    background: #f8f9fa;
    border-radius: 8px;
    
    .table-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      
      h4 {
        margin: 0;
        font-size: 18px;
        font-weight: 600;
        color: #303133;
      }
      
      .table-export {
        padding: 6px 12px;
        background: #409eff;
        color: white;
        border: none;
        border-radius: 4px;
        font-size: 14px;
        cursor: pointer;
        
        &:hover {
          background: #66b1ff;
        }
      }
    }
    
    .table-wrapper {
      overflow-x: auto;
      
      .data-table {
        width: 100%;
        border-collapse: collapse;
        font-size: 14px;
        
        th {
          background: white;
          color: #303133;
          font-weight: 600;
          padding: 12px;
          text-align: left;
          border-bottom: 2px solid #ebeef5;
          white-space: nowrap;
        }
        
        td {
          padding: 10px 12px;
          border-bottom: 1px solid #ebeef5;
          color: #606266;
          white-space: nowrap;
        }
        
        tbody tr:hover {
          background: white;
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
  }
  
  .chart-loading {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(255, 255, 255, 0.8);
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    border-radius: 12px;
    z-index: 100;
    
    .loading-spinner {
      width: 40px;
      height: 40px;
      border: 3px solid #f3f3f3;
      border-top: 3px solid #409eff;
      border-radius: 50%;
      animation: spin 1s linear infinite;
      margin-bottom: 16px;
    }
    
    .loading-text {
      font-size: 14px;
      color: #606266;
    }
  }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>