<template>
  <div class="evaluations-container">
    <!-- 评估头部 -->
    <div class="eval-header">
      <h2 class="eval-title">{{ title }}</h2>
      <div v-if="subtitle" class="eval-subtitle">{{ subtitle }}</div>
      
      <!-- 总体评分 -->
      <div v-if="showOverallScore" class="overall-score">
        <div class="score-value">{{ overallScore.toFixed(1) }}</div>
        <div class="score-label">综合评分</div>
        <div class="score-stars">
          <span
            v-for="n in 5"
            :key="n"
            class="star"
            :class="{ 'filled': n <= Math.round(overallScore) }"
          >
            ★
          </span>
        </div>
      </div>
    </div>
    
    <!-- 评估项目列表 -->
    <div class="eval-items">
      <div
        v-for="(item, index) in items"
        :key="item.id || index"
        class="eval-item"
      >
        <!-- 项目头部 -->
        <div class="item-header">
          <div class="item-title">
            <span class="item-index">{{ index + 1 }}.</span>
            {{ item.title }}
            <span v-if="item.required" class="required-mark">*</span>
          </div>
          
          <div v-if="item.description" class="item-description">
            {{ item.description }}
          </div>
        </div>
        
        <!-- 评分类型：星级评分 -->
        <div v-if="item.type === 'star'" class="rating-star">
          <div class="stars-container">
            <span
              v-for="n in item.maxStars || 5"
              :key="n"
              class="star"
              :class="{ 'filled': n <= (ratings[item.id] || 0) }"
              @click="setRating(item.id, n)"
              @mouseenter="hoverStar = { id: item.id, value: n }"
              @mouseleave="hoverStar = null"
            >
              ★
            </span>
          </div>
          
          <div class="star-labels" v-if="item.starLabels">
            <span
              v-for="(label, idx) in item.starLabels"
              :key="idx"
              class="star-label"
            >
              {{ label }}
            </span>
          </div>
          
          <div class="current-rating">
            当前评分: {{ ratings[item.id] || 0 }} / {{ item.maxStars || 5 }}
          </div>
        </div>
        
        <!-- 评分类型：数字评分 -->
        <div v-else-if="item.type === 'number'" class="rating-number">
          <div class="number-scale">
            <span
              v-for="n in item.maxScore"
              :key="n"
              class="number-item"
              :class="{
                'selected': ratings[item.id] === n,
                'hover': hoverNumber && hoverNumber.id === item.id && hoverNumber.value === n
              }"
              @click="setRating(item.id, n)"
              @mouseenter="hoverNumber = { id: item.id, value: n }"
              @mouseleave="hoverNumber = null"
            >
              {{ n }}
            </span>
          </div>
          
          <div class="number-labels" v-if="item.numberLabels">
            <div class="label-min">{{ item.numberLabels[0] }}</div>
            <div class="label-max">{{ item.numberLabels[1] }}</div>
          </div>
        </div>
        
        <!-- 评分类型：滑动条评分 -->
        <div v-else-if="item.type === 'slider'" class="rating-slider">
          <div class="slider-container">
            <input
              type="range"
              :min="item.min || 0"
              :max="item.max || 100"
              :step="item.step || 1"
              v-model="ratings[item.id]"
              class="slider-input"
              @input="handleSliderChange(item.id)"
            />
            
            <div class="slider-track">
              <div
                class="slider-fill"
                :style="{ width: sliderPercentage(item.id) + '%' }"
              ></div>
            </div>
            
            <div class="slider-value">
              {{ ratings[item.id] || item.min || 0 }}
              <span v-if="item.unit">{{ item.unit }}</span>
            </div>
          </div>
          
          <div class="slider-labels" v-if="item.sliderLabels">
            <div class="label-min">{{ item.sliderLabels[0] }}</div>
            <div class="label-max">{{ item.sliderLabels[1] }}</div>
          </div>
        </div>
        
        <!-- 评分类型：选项评分 -->
        <div v-else-if="item.type === 'option'" class="rating-option">
          <div class="options-container">
            <div
              v-for="option in item.options"
              :key="option.value"
              class="option-item"
              :class="{
                'selected': ratings[item.id] === option.value,
                'hover': hoverOption && hoverOption.id === item.id && hoverOption.value === option.value
              }"
              @click="setRating(item.id, option.value)"
              @mouseenter="hoverOption = { id: item.id, value: option.value }"
              @mouseleave="hoverOption = null"
            >
              <div class="option-icon">{{ option.icon }}</div>
              <div class="option-text">{{ option.text }}</div>
              <div v-if="option.score" class="option-score">
                ({{ option.score }}分)
              </div>
            </div>
          </div>
        </div>
        
        <!-- 评论区域 -->
        <div v-if="item.allowComment" class="comment-area">
          <textarea
            v-model="comments[item.id]"
            :placeholder="item.commentPlaceholder || '请输入您的评价...'"
            :rows="item.commentRows || 3"
            class="comment-input"
            @input="handleCommentChange(item.id)"
          ></textarea>
          
          <div v-if="item.maxCommentLength" class="comment-counter">
            {{ (comments[item.id] || '').length }} / {{ item.maxCommentLength }}
          </div>
        </div>
        
        <!-- 项目得分显示 -->
        <div v-if="showItemScore" class="item-score">
          <div class="score-bar">
            <div
              class="score-fill"
              :style="{ width: itemScorePercentage(item) + '%' }"
              :class="getScoreColor(item)"
            ></div>
          </div>
          <div class="score-text">
            得分: {{ calculateItemScore(item) }} / {{ item.maxScore || 5 }}
          </div>
        </div>
      </div>
    </div>
    
    <!-- 总体评论 -->
    <div v-if="allowOverallComment" class="overall-comment">
      <h3>总体评价</h3>
      <textarea
        v-model="overallComment"
        :placeholder="overallCommentPlaceholder || '请写下您的总体评价...'"
        :rows="4"
        class="overall-comment-input"
        @input="handleOverallCommentChange"
      ></textarea>
      
      <div v-if="maxOverallCommentLength" class="comment-counter">
        {{ overallComment.length }} / {{ maxOverallCommentLength }}
      </div>
    </div>
    
    <!-- 评估结果统计 -->
    <div v-if="showStatistics" class="eval-statistics">
      <h3>评估统计</h3>
      <div class="stats-grid">
        <div class="stat-item">
          <div class="stat-value">{{ completedItems }}</div>
          <div class="stat-label">已完成项目</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ totalItems }}</div>
          <div class="stat-label">总项目数</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ completionPercentage }}%</div>
          <div class="stat-label">完成度</div>
        </div>
        <div class="stat-item">
          <div class="stat-value">{{ averageScore.toFixed(1) }}</div>
          <div class="stat-label">平均分</div>
        </div>
      </div>
      
      <!-- 评分分布 -->
      <div v-if="scoreDistribution.length > 0" class="score-distribution">
        <h4>评分分布</h4>
        <div class="distribution-bars">
          <div
            v-for="(dist, index) in scoreDistribution"
            :key="index"
            class="distribution-bar"
            :title="`${dist.score}分: ${dist.count}人 (${dist.percentage}%)`"
          >
            <div
              class="bar-fill"
              :style="{ height: dist.percentage + '%' }"
            ></div>
            <div class="bar-label">{{ dist.score }}</div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 评估操作 -->
    <div class="eval-actions">
      <button
        v-if="showResetButton"
        class="action-btn reset-btn"
        @click="resetEvaluations"
      >
        重置评估
      </button>
      
      <button
        class="action-btn submit-btn"
        :class="{ 'disabled': !isValid }"
        @click="submitEvaluations"
        :disabled="!isValid"
      >
        {{ submitButtonText }}
      </button>
    </div>
    
    <!-- 评估结果展示 -->
    <div v-if="showResults && evaluationResults" class="eval-results">
      <h3>评估结果</h3>
      <div class="results-summary">
        <div class="result-item">
          <div class="result-label">综合评分</div>
          <div class="result-value">{{ evaluationResults.overallScore.toFixed(1) }}</div>
        </div>
        <div class="result-item">
          <div class="result-label">评估时间</div>
          <div class="result-value">{{ formatDate(evaluationResults.timestamp) }}</div>
        </div>
        <div class="result-item">
          <div class="result-label">评估状态</div>
          <div class="result-value">
            <span :class="evaluationResults.status === 'completed' ? 'completed' : 'pending'">
              {{ evaluationResults.status === 'completed' ? '已完成' : '进行中' }}
            </span>
          </div>
        </div>
      </div>
      
      <!-- 详细结果 -->
      <div class="detailed-results">
        <h4>详细评分</h4>
        <table class="results-table">
          <thead>
            <tr>
              <th>评估项目</th>
              <th>评分</th>
              <th>权重</th>
              <th>得分</th>
              <th v-if="showComments">评价</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="item in evaluationResults.items"
              :key="item.id"
            >
              <td>{{ item.title }}</td>
              <td>
                <div class="score-display">
                  {{ item.rating }} / {{ item.maxRating }}
                </div>
              </td>
              <td>{{ (item.weight * 100).toFixed(0) }}%</td>
              <td>{{ item.score.toFixed(1) }}</td>
              <td v-if="showComments" class="comment-cell">
                {{ item.comment || '-' }}
              </td>
            </tr>
          </tbody>
          <tfoot>
            <tr>
              <td colspan="3"><strong>总计</strong></td>
              <td><strong>{{ evaluationResults.totalScore.toFixed(1) }}</strong></td>
              <td v-if="showComments"></td>
            </tr>
          </tfoot>
        </table>
      </div>
      
      <!-- 结果建议 -->
      <div v-if="evaluationResults.suggestions" class="result-suggestions">
        <h4>改进建议</h4>
        <ul class="suggestions-list">
          <li
            v-for="(suggestion, index) in evaluationResults.suggestions"
            :key="index"
          >
            {{ suggestion }}
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Evaluations',
  props: {
    // 评估配置
    config: {
      type: Object,
      required: true,
      default: () => ({
        title: '评估表单',
        subtitle: '',
        items: [],
        showOverallScore: true,
        showItemScore: true,
        showStatistics: true,
        showResults: false,
        showComments: false,
        showResetButton: true,
        allowOverallComment: true,
        maxOverallCommentLength: 500,
        overallCommentPlaceholder: '',
        submitButtonText: '提交评估',
        autoCalculate: true,
        validateRequired: true
      })
    },
    
    // 初始数据
    initialData: {
      type: Object,
      default: () => ({
        ratings: {},
        comments: {},
        overallComment: ''
      })
    },
    
    // 评估结果
    evaluationResults: {
      type: Object,
      default: null
    },
    
    // 历史统计数据
    statistics: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      title: this.config.title,
      subtitle: this.config.subtitle,
      items: this.config.items,
      ratings: { ...this.initialData.ratings },
      comments: { ...this.initialData.comments },
      overallComment: this.initialData.overallComment || '',
      hoverStar: null,
      hoverNumber: null,
      hoverOption: null,
      isSubmitting: false
    }
  },
  computed: {
    showOverallScore() {
      return this.config.showOverallScore !== false
    },
    
    showItemScore() {
      return this.config.showItemScore !== false
    },
    
    showStatistics() {
      return this.config.showStatistics !== false
    },
    
    showResults() {
      return this.config.showResults && this.evaluationResults
    },
    
    showComments() {
      return this.config.showComments
    },
    
    showResetButton() {
      return this.config.showResetButton !== false
    },
    
    allowOverallComment() {
      return this.config.allowOverallComment !== false
    },
    
    maxOverallCommentLength() {
      return this.config.maxOverallCommentLength
    },
    
    overallCommentPlaceholder() {
      return this.config.overallCommentPlaceholder
    },
    
    submitButtonText() {
      return this.config.submitButtonText || '提交评估'
    },
    
    totalItems() {
      return this.items.length
    },
    
    completedItems() {
      return this.items.filter(item => {
        if (item.required && this.config.validateRequired) {
          return this.ratings[item.id] !== undefined && this.ratings[item.id] !== null
        }
        return true
      }).length
    },
    
    completionPercentage() {
      if (this.totalItems === 0) return 0
      return Math.round((this.completedItems / this.totalItems) * 100)
    },
    
    isValid() {
      if (!this.config.validateRequired) return true
      
      return this.items.every(item => {
        if (item.required) {
          const rating = this.ratings[item.id]
          return rating !== undefined && rating !== null && rating !== ''
        }
        return true
      })
    },
    
    overallScore() {
      if (this.items.length === 0) return 0
      
      const totalScore = this.items.reduce((sum, item) => {
        return sum + (this.calculateItemScore(item) || 0)
      }, 0)
      
      const totalWeight = this.items.reduce((sum, item) => {
        return sum + (item.weight || 1)
      }, 0)
      
      return totalWeight > 0 ? totalScore / totalWeight : 0
    },
    
    averageScore() {
      const ratedItems = this.items.filter(item => 
        this.ratings[item.id] !== undefined && this.ratings[item.id] !== null
      )
      
      if (ratedItems.length === 0) return 0
      
      const total = ratedItems.reduce((sum, item) => {
        return sum + (this.ratings[item.id] || 0)
      }, 0)
      
      return total / ratedItems.length
    },
    
    scoreDistribution() {
      if (!this.statistics || !this.statistics.scoreDistribution) {
        return []
      }
      
      return this.statistics.scoreDistribution
    }
  },
  watch: {
    ratings: {
      handler() {
        this.$emit('change', this.getEvaluationData())
        
        if (this.config.autoCalculate) {
          this.$emit('score-change', {
            overallScore: this.overallScore,
            averageScore: this.averageScore
          })
        }
      },
      deep: true
    },
    
    comments: {
      handler() {
        this.$emit('change', this.getEvaluationData())
      },
      deep: true
    },
    
    overallComment() {
      this.$emit('change', this.getEvaluationData())
    },
    
    config: {
      handler(newConfig) {
        this.title = newConfig.title
        this.subtitle = newConfig.subtitle
        this.items = newConfig.items
      },
      deep: true
    }
  },
  methods: {
    setRating(itemId, value) {
      this.$set(this.ratings, itemId, value)
      this.$emit('rating-changed', { itemId, value })
    },
    
    handleSliderChange(itemId) {
      this.$emit('rating-changed', { 
        itemId, 
        value: this.ratings[itemId] 
      })
    },
    
    handleCommentChange(itemId) {
      this.$emit('comment-changed', { 
        itemId, 
        comment: this.comments[itemId] 
      })
    },
    
    handleOverallCommentChange() {
      this.$emit('overall-comment-changed', this.overallComment)
    },
    
    calculateItemScore(item) {
      const rating = this.ratings[item.id]
      if (rating === undefined || rating === null) return 0
      
      let score = rating
      
      // 根据评分类型转换分数
      if (item.type === 'star') {
        const maxStars = item.maxStars || 5
        const maxScore = item.maxScore || 10
        score = (rating / maxStars) * maxScore
      } else if (item.type === 'option') {
        const option = item.options?.find(opt => opt.value === rating)
        score = option?.score || rating
      }
      
      // 应用权重
      const weight = item.weight || 1
      return score * weight
    },
    
    itemScorePercentage(item) {
      const maxScore = item.maxScore || 10
      const score = this.calculateItemScore(item)
      return (score / maxScore) * 100
    },
    
    sliderPercentage(itemId) {
      const item = this.items.find(i => i.id === itemId)
      if (!item) return 0
      
      const rating = this.ratings[itemId] || item.min || 0
      const min = item.min || 0
      const max = item.max || 100
      
      return ((rating - min) / (max - min)) * 100
    },
    
    getScoreColor(item) {
      const percentage = this.itemScorePercentage(item)
      
      if (percentage >= 80) return 'excellent'
      if (percentage >= 60) return 'good'
      if (percentage >= 40) return 'average'
      return 'poor'
    },
    
    resetEvaluations() {
      this.ratings = {}
      this.comments = {}
      this.overallComment = ''
      this.$emit('reset')
    },
    
    submitEvaluations() {
      if (!this.isValid) return
      
      this.isSubmitting = true
      
      const evaluationData = this.getEvaluationData()
      
      this.$emit('submit', evaluationData)
      
      // 模拟提交完成
      setTimeout(() => {
        this.isSubmitting = false
      }, 1000)
    },
    
    getEvaluationData() {
      const itemsData = this.items.map(item => ({
        id: item.id,
        title: item.title,
        type: item.type,
        rating: this.ratings[item.id] || null,
        comment: this.comments[item.id] || '',
        weight: item.weight || 1,
        score: this.calculateItemScore(item)
      }))
      
      return {
        timestamp: new Date().toISOString(),
        overallScore: this.overallScore,
        averageScore: this.averageScore,
        overallComment: this.overallComment,
        items: itemsData,
        completion: {
          total: this.totalItems,
          completed: this.completedItems,
          percentage: this.completionPercentage
        },
        isValid: this.isValid
      }
    },
    
    formatDate(dateString) {
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }
  }
}
</script>

<style scoped lang="scss">
.evaluations-container {
  background: white;
  border-radius: 12px;
  padding: 30px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  
  .eval-header {
    margin-bottom: 30px;
    text-align: center;
    
    .eval-title {
      margin: 0 0 10px 0;
      color: #303133;
      font-size: 24px;
      font-weight: 600;
    }
    
    .eval-subtitle {
      color: #909399;
      font-size: 16px;
      margin-bottom: 20px;
    }
    
    .overall-score {
      display: inline-flex;
      flex-direction: column;
      align-items: center;
      background: linear-gradient(135deg, #409eff, #66b1ff);
      color: white;
      padding: 20px 30px;
      border-radius: 12px;
      margin-top: 10px;
      
      .score-value {
        font-size: 48px;
        font-weight: 700;
        line-height: 1;
      }
      
      .score-label {
        font-size: 14px;
        margin: 8px 0;
        opacity: 0.9;
      }
      
      .score-stars {
        display: flex;
        gap: 2px;
        
        .star {
          font-size: 24px;
          color: rgba(255, 255, 255, 0.3);
          
          &.filled {
            color: #ffd700;
          }
        }
      }
    }
  }
  
  .eval-items {
    .eval-item {
      margin-bottom: 30px;
      padding: 20px;
      border: 1px solid #ebeef5;
      border-radius: 8px;
      transition: border-color 0.3s;
      
      &:hover {
        border-color: #c6e2ff;
      }
      
      .item-header {
        margin-bottom: 20px;
        
        .item-title {
          font-size: 18px;
          font-weight: 600;
          color: #303133;
          margin-bottom: 8px;
          
          .item-index {
            color: #409eff;
            margin-right: 5px;
          }
          
          .required-mark {
            color: #f56c6c;
            margin-left: 4px;
          }
        }
        
        .item-description {
          font-size: 14px;
          color: #909399;
          line-height: 1.5;
        }
      }
      
      .rating-star {
        .stars-container {
          display: flex;
          gap: 8px;
          margin-bottom: 10px;
          
          .star {
            font-size: 32px;
            color: #dcdfe6;
            cursor: pointer;
            transition: all 0.2s;
            
            &:hover,
            &.filled {
              color: #ffd700;
              transform: scale(1.1);
            }
          }
        }
        
        .star-labels {
          display: flex;
          justify-content: space-between;
          max-width: 400px;
          margin-bottom: 10px;
          
          .star-label {
            font-size: 12px;
            color: #909399;
            text-align: center;
            flex: 1;
          }
        }
        
        .current-rating {
          font-size: 14px;
          color: #409eff;
          font-weight: 500;
        }
      }
      
      .rating-number {
        .number-scale {
          display: flex;
          gap: 8px;
          margin-bottom: 10px;
          max-width: 500px;
          
          .number-item {
            flex: 1;
            height: 40px;
            display: flex;
            align-items: center;
            justify-content: center;
            background: #f5f7fa;
            border: 2px solid #dcdfe6;
            border-radius: 6px;
            font-size: 16px;
            font-weight: 600;
            color: #606266;
            cursor: pointer;
            transition: all 0.3s;
            
            &:hover,
            &.hover {
              border-color: #409eff;
              background: #ecf5ff;
              color: #409eff;
            }
            
            &.selected {
              background: #409eff;
              border-color: #409eff;
              color: white;
            }
          }
        }
        
        .number-labels {
          display: flex;
          justify-content: space-between;
          max-width: 500px;
          
          .label-min,
          .label-max {
            font-size: 12px;
            color: #909399;
          }
        }
      }
      
      .rating-slider {
        .slider-container {
          position: relative;
          margin-bottom: 10px;
          max-width: 500px;
          
          .slider-input {
            width: 100%;
            height: 40px;
            opacity: 0;
            position: absolute;
            z-index: 2;
            cursor: pointer;
          }
          
          .slider-track {
            position: absolute;
            top: 50%;
            transform: translateY(-50%);
            width: 100%;
            height: 6px;
            background: #dcdfe6;
            border-radius: 3px;
            overflow: hidden;
            
            .slider-fill {
              height: 100%;
              background: linear-gradient(to right, #67c23a, #e6a23c, #f56c6c);
              transition: width 0.3s;
            }
          }
          
          .slider-value {
            position: absolute;
            right: 0;
            top: -25px;
            font-size: 14px;
            font-weight: 600;
            color: #409eff;
            background: white;
            padding: 2px 8px;
            border-radius: 4px;
            border: 1px solid #dcdfe6;
          }
        }
        
        .slider-labels {
          display: flex;
          justify-content: space-between;
          max-width: 500px;
          
          .label-min,
          .label-max {
            font-size: 12px;
            color: #909399;
          }
        }
      }
      
      .rating-option {
        .options-container {
          display: flex;
          flex-wrap: wrap;
          gap: 10px;
          
          .option-item {
            flex: 1;
            min-width: 120px;
            padding: 15px;
            border: 2px solid #dcdfe6;
            border-radius: 8px;
            text-align: center;
            cursor: pointer;
            transition: all 0.3s;
            
            &:hover,
            &.hover {
              border-color: #409eff;
              background: #ecf5ff;
            }
            
            &.selected {
              border-color: #409eff;
              background: #ecf5ff;
              
              .option-icon {
                color: #409eff;
              }
            }
            
            .option-icon {
              font-size: 32px;
              margin-bottom: 8px;
            }
            
            .option-text {
              font-size: 14px;
              color: #303133;
              margin-bottom: 4px;
            }
            
            .option-score {
              font-size: 12px;
              color: #e6a23c;
            }
          }
        }
      }
      
      .comment-area {
        margin-top: 20px;
        
        .comment-input {
          width: 100%;
          padding: 12px;
          border: 1px solid #dcdfe6;
          border-radius: 6px;
          font-size: 14px;
          font-family: inherit;
          resize: vertical;
          min-height: 80px;
          
          &:focus {
            outline: none;
            border-color: #409eff;
          }
          
          &::placeholder {
            color: #c0c4cc;
          }
        }
        
        .comment-counter {
          text-align: right;
          font-size: 12px;
          color: #909399;
          margin-top: 5px;
        }
      }
      
      .item-score {
        margin-top: 20px;
        padding-top: 20px;
        border-top: 1px solid #ebeef5;
        
        .score-bar {
          height: 8px;
          background: #ebeef5;
          border-radius: 4px;
          overflow: hidden;
          margin-bottom: 8px;
          
          .score-fill {
            height: 100%;
            transition: width 0.5s ease-out;
            
            &.excellent {
              background: linear-gradient(to right, #67c23a, #85ce61);
            }
            
            &.good {
              background: linear-gradient(to right, #e6a23c, #ebb563);
            }
            
            &.average {
              background: linear-gradient(to right, #f56c6c, #f78989);
            }
            
            &.poor {
              background: linear-gradient(to right, #909399, #a6a9ad);
            }
          }
        }
        
        .score-text {
          font-size: 14px;
          color: #606266;
          text-align: right;
        }
      }
    }
  }
  
  .overall-comment {
    margin: 30px 0;
    padding: 20px;
    background: #f8f9fa;
    border-radius: 8px;
    
    h3 {
      margin: 0 0 15px 0;
      color: #303133;
      font-size: 18px;
    }
    
    .overall-comment-input {
      width: 100%;
      padding: 12px;
      border: 1px solid #dcdfe6;
      border-radius: 6px;
      font-size: 14px;
      font-family: inherit;
      resize: vertical;
      min-height: 100px;
      
      &:focus {
        outline: none;
        border-color: #409eff;
      }
      
      &::placeholder {
        color: #c0c4cc;
      }
    }
    
    .comment-counter {
      text-align: right;
      font-size: 12px;
      color: #909399;
      margin-top: 5px;
    }
  }
  
  .eval-statistics {
    margin: 30px 0;
    padding: 20px;
    background: #f8f9fa;
    border-radius: 8px;
    
    h3 {
      margin: 0 0 20px 0;
      color: #303133;
      font-size: 18px;
    }
    
    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
      gap: 20px;
      margin-bottom: 30px;
      
      .stat-item {
        text-align: center;
        padding: 20px;
        background: white;
        border-radius: 8px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
        
        .stat-value {
          font-size: 32px;
          font-weight: 700;
          color: #409eff;
          margin-bottom: 5px;
        }
        
        .stat-label {
          font-size: 14px;
          color: #909399;
        }
      }
    }
    
    .score-distribution {
      h4 {
        margin: 0 0 15px 0;
        color: #606266;
        font-size: 16px;
      }
      
      .distribution-bars {
        display: flex;
        align-items: flex-end;
        height: 150px;
        gap: 10px;
        
        .distribution-bar {
          flex: 1;
          display: flex;
          flex-direction: column;
          align-items: center;
          
          .bar-fill {
            width: 30px;
            background: linear-gradient(to top, #409eff, #66b1ff);
            border-radius: 4px 4px 0 0;
            transition: height 0.5s;
          }
          
          .bar-label {
            margin-top: 8px;
            font-size: 12px;
            color: #606266;
            font-weight: 600;
          }
        }
      }
    }
  }
  
  .eval-actions {
    display: flex;
    justify-content: center;
    gap: 20px;
    margin-top: 30px;
    padding-top: 30px;
    border-top: 1px solid #ebeef5;
    
    .action-btn {
      padding: 12px 32px;
      border: none;
      border-radius: 6px;
      font-size: 16px;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s;
      
      &.reset-btn {
        background: #f5f7fa;
        color: #606266;
        border: 1px solid #dcdfe6;
        
        &:hover {
          background: #ebeef5;
        }
      }
      
      &.submit-btn {
        background: linear-gradient(135deg, #409eff, #66b1ff);
        color: white;
        
        &:hover:not(.disabled) {
          background: linear-gradient(135deg, #66b1ff, #409eff);
          box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
        }
        
        &.disabled {
          opacity: 0.6;
          cursor: not-allowed;
        }
      }
    }
  }
  
  .eval-results {
    margin-top: 30px;
    padding: 20px;
    background: #f0f9eb;
    border-radius: 8px;
    border: 1px solid #e1f3d8;
    
    h3 {
      margin: 0 0 20px 0;
      color: #67c23a;
      font-size: 20px;
    }
    
    .results-summary {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 20px;
      margin-bottom: 30px;
      
      .result-item {
        text-align: center;
        padding: 15px;
        background: white;
        border-radius: 6px;
        
        .result-label {
          font-size: 14px;
          color: #909399;
          margin-bottom: 5px;
        }
        
        .result-value {
          font-size: 24px;
          font-weight: 600;
          color: #303133;
          
          .completed {
            color: #67c23a;
          }
          
          .pending {
            color: #e6a23c;
          }
        }
      }
    }
    
    .detailed-results {
      margin-bottom: 30px;
      
      h4 {
        margin: 0 0 15px 0;
        color: #606266;
        font-size: 16px;
      }
      
      .results-table {
        width: 100%;
        border-collapse: collapse;
        font-size: 14px;
        
        th {
          background: #f5f7fa;
          color: #303133;
          font-weight: 600;
          padding: 12px;
          text-align: left;
          border-bottom: 2px solid #ebeef5;
        }
        
        td {
          padding: 12px;
          border-bottom: 1px solid #ebeef5;
          color: #606266;
          
          .score-display {
            display: inline-block;
            padding: 4px 12px;
            background: #ecf5ff;
            color: #409eff;
            border-radius: 4px;
            font-weight: 600;
          }
          
          &.comment-cell {
            max-width: 300px;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
          }
        }
        
        tfoot {
          td {
            font-size: 16px;
            color: #303133;
            border-top: 2px solid #ebeef5;
          }
        }
      }
    }
    
    .result-suggestions {
      h4 {
        margin: 0 0 15px 0;
        color: #606266;
        font-size: 16px;
      }
      
      .suggestions-list {
        list-style: none;
        padding-left: 0;
        margin: 0;
        
        li {
          padding: 8px 0;
          color: #606266;
          position: relative;
          padding-left: 20px;
          
          &::before {
            content: '💡';
            position: absolute;
            left: 0;
          }
        }
      }
    }
  }
}
</style>