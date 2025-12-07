<template>
  <div class="judgment-question" :class="{ 'has-error': error }">
    <!-- 题目信息 -->
    <div class="question-header">
      <div class="question-meta">
        <span class="question-type">判断题</span>
        <span class="question-score" v-if="question.score">
          ({{ question.score }}分)
        </span>
      </div>
      
      <div class="question-title" v-html="question.title"></div>
    </div>
    
    <!-- 答题区域 -->
    <div class="judgment-options">
      <div
        class="option"
        :class="{ 
          'selected': answer === true, 
          'correct': showAnswer && question.correctAnswer === true,
          'wrong': showAnswer && answer === true && question.correctAnswer === false
        }"
        @click="selectOption(true)"
      >
        <div class="option-icon">✓</div>
        <div class="option-text">正确</div>
      </div>
      
      <div
        class="option"
        :class="{ 
          'selected': answer === false, 
          'correct': showAnswer && question.correctAnswer === false,
          'wrong': showAnswer && answer === false && question.correctAnswer === true
        }"
        @click="selectOption(false)"
      >
        <div class="option-icon">✗</div>
        <div class="option-text">错误</div>
      </div>
      
      <!-- 不确定选项 -->
      <div
        v-if="question.allowUndecided"
        class="option"
        :class="{ 'selected': answer === null }"
        @click="selectOption(null)"
      >
        <div class="option-icon">?</div>
        <div class="option-text">不确定</div>
      </div>
    </div>
    
    <!-- 错误提示 -->
    <div v-if="error" class="error-message">
      {{ error }}
    </div>
    
    <!-- 答案解析 -->
    <div v-if="showAnswer && question.explanation" class="explanation">
      <h4>答案解析：</h4>
      <div class="explanation-content" v-html="question.explanation"></div>
      
      <div class="correct-answer">
        <strong>正确答案：</strong>
        <span :class="question.correctAnswer === true ? 'correct-text' : 'wrong-text'">
          {{ question.correctAnswer === true ? '正确' : '错误' }}
        </span>
      </div>
      
      <div v-if="question.reference" class="reference">
        <strong>参考：</strong>{{ question.reference }}
      </div>
    </div>
    
    <!-- 知识点标签 -->
    <div v-if="question.tags && question.tags.length" class="knowledge-tags">
      <h4>知识点：</h4>
      <div class="tags">
        <span
          v-for="tag in question.tags"
          :key="tag"
          class="tag"
        >
          {{ tag }}
        </span>
      </div>
    </div>
    
    <!-- 正确率统计 -->
    <div v-if="showStatistics && question.statistics" class="statistics">
      <div class="stat-row">
        <div class="stat-item">
          <div class="stat-label">正确率</div>
          <div class="stat-value">{{ question.statistics.correctRate }}%</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">选择正确</div>
          <div class="stat-value">{{ question.statistics.selectTrue || 0 }}人</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">选择错误</div>
          <div class="stat-value">{{ question.statistics.selectFalse || 0 }}人</div>
        </div>
      </div>
      
      <div class="stat-chart">
        <div class="chart-bar true-bar" :style="{ width: question.statistics.trueRate + '%' }">
          正确 {{ question.statistics.trueRate }}%
        </div>
        <div class="chart-bar false-bar" :style="{ width: question.statistics.falseRate + '%' }">
          错误 {{ question.statistics.falseRate }}%
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'JudgmentQuestion',
  props: {
    question: {
      type: Object,
      required: true,
      default: () => ({
        id: '',
        title: '',
        score: 0,
        correctAnswer: null, // true/false
        allowUndecided: false,
        explanation: '',
        reference: '',
        tags: [],
        statistics: null
      })
    },
    value: {
      type: Boolean,
      default: null
    },
    showAnswer: {
      type: Boolean,
      default: false
    },
    showStatistics: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      answer: this.value,
      error: ''
    }
  },
  watch: {
    value(newVal) {
      this.answer = newVal
    },
    answer(newVal) {
      this.$emit('input', newVal)
      this.validate()
    }
  },
  methods: {
    selectOption(value) {
      this.answer = value
    },
    
    validate() {
      if (this.question.required && this.answer === null) {
        this.error = '请选择答案'
        return false
      }
      
      this.error = ''
      return true
    },
    
    // 检查答案是否正确
    checkAnswer() {
      if (this.answer === null) {
        return { correct: false, score: 0 }
      }
      
      const isCorrect = this.answer === this.question.correctAnswer
      const score = isCorrect ? (this.question.score || 0) : 0
      
      return {
        correct: isCorrect,
        score: score,
        userAnswer: this.answer,
        correctAnswer: this.question.correctAnswer
      }
    },
    
    // 供父组件调用的方法
    getAnswer() {
      return this.answer
    },
    
    clear() {
      this.answer = null
      this.error = ''
    },
    
    selectCorrect() {
      this.answer = this.question.correctAnswer
    }
  }
}
</script>

<style scoped lang="scss">
.judgment-question {
  border: 1px solid #e1e5eb;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  background: white;
  
  &.has-error {
    border-color: #f56c6c;
  }
  
  .question-header {
    margin-bottom: 25px;
    
    .question-meta {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 10px;
      
      .question-type {
        background: linear-gradient(135deg, #e6a23c, #d48813);
        color: white;
        padding: 4px 12px;
        border-radius: 20px;
        font-size: 12px;
        font-weight: 600;
      }
      
      .question-score {
        color: #e6a23c;
        font-weight: 600;
      }
    }
    
    .question-title {
      font-size: 16px;
      line-height: 1.6;
      color: #333;
    }
  }
  
  .judgment-options {
    display: flex;
    gap: 20px;
    margin-bottom: 20px;
    
    .option {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 25px 20px;
      border: 2px solid #e1e5eb;
      border-radius: 10px;
      cursor: pointer;
      transition: all 0.3s;
      
      &:hover {
        border-color: #409eff;
        background: #f0f5ff;
      }
      
      &.selected {
        border-color: #409eff;
        background: #f0f5ff;
        
        .option-icon {
          background: #409eff;
          color: white;
        }
      }
      
      &.correct {
        border-color: #67c23a;
        background: #f0f9eb;
        
        .option-icon {
          background: #67c23a;
          color: white;
        }
      }
      
      &.wrong {
        border-color: #f56c6c;
        background: #fef0f0;
        
        .option-icon {
          background: #f56c6c;
          color: white;
        }
      }
      
      .option-icon {
        width: 60px;
        height: 60px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 30px;
        font-weight: bold;
        background: #f5f7fa;
        border-radius: 50%;
        margin-bottom: 15px;
        transition: all 0.3s;
      }
      
      .option-text {
        font-size: 18px;
        font-weight: 600;
        color: #333;
      }
    }
  }
  
  .error-message {
    color: #f56c6c;
    font-size: 12px;
    margin-bottom: 8px;
    padding: 8px;
    background: #fef0f0;
    border-radius: 4px;
  }
  
  .explanation {
    margin-top: 20px;
    padding: 15px;
    background: #f8f9fa;
    border-radius: 6px;
    
    h4 {
      margin: 0 0 10px 0;
      font-size: 14px;
      color: #909399;
    }
    
    .explanation-content {
      line-height: 1.6;
      color: #333;
      margin-bottom: 10px;
    }
    
    .correct-answer {
      margin: 10px 0;
      padding: 8px 12px;
      background: white;
      border-radius: 4px;
      
      .correct-text {
        color: #67c23a;
        font-weight: 600;
      }
      
      .wrong-text {
        color: #f56c6c;
        font-weight: 600;
      }
    }
    
    .reference {
      font-size: 12px;
      color: #909399;
      padding: 8px;
      background: white;
      border-radius: 4px;
    }
  }
  
  .knowledge-tags {
    margin-top: 20px;
    padding-top: 20px;
    border-top: 1px solid #ebeef5;
    
    h4 {
      margin: 0 0 10px 0;
      font-size: 14px;
      color: #909399;
    }
    
    .tags {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      
      .tag {
        padding: 4px 12px;
        background: #ecf5ff;
        color: #409eff;
        border-radius: 20px;
        font-size: 12px;
        
        &:hover {
          background: #409eff;
          color: white;
        }
      }
    }
  }
  
  .statistics {
    margin-top: 20px;
    padding: 15px;
    background: #f8f9fa;
    border-radius: 6px;
    
    .stat-row {
      display: flex;
      justify-content: space-around;
      margin-bottom: 15px;
      
      .stat-item {
        text-align: center;
        
        .stat-label {
          font-size: 12px;
          color: #909399;
          margin-bottom: 4px;
        }
        
        .stat-value {
          font-size: 20px;
          font-weight: 600;
          color: #409eff;
        }
      }
    }
    
    .stat-chart {
      height: 30px;
      display: flex;
      border-radius: 15px;
      overflow: hidden;
      
      .chart-bar {
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
        font-size: 12px;
        font-weight: 600;
        transition: width 0.5s;
        
        &.true-bar {
          background: #67c23a;
        }
        
        &.false-bar {
          background: #f56c6c;
        }
      }
    }
  }
}
</style>