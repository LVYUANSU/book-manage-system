<template>
  <div class="single-select-question" :class="{ 'has-error': error }">
    <!-- 题目信息 -->
    <div class="question-header">
      <div class="question-meta">
        <span class="question-type">单选题</span>
        <span class="question-score" v-if="question.score">
          ({{ question.score }}分)
        </span>
      </div>
      
      <div class="question-title" v-html="question.title"></div>
      
      <!-- 题目附件 -->
      <div v-if="question.attachment" class="question-attachment">
        <a :href="question.attachment.url" target="_blank" class="attachment-link">
          <span class="attachment-icon">📎</span>
          {{ question.attachment.name }}
        </a>
      </div>
    </div>
    
    <!-- 选项区域 -->
    <div class="options-area">
      <div
        v-for="(option, index) in question.options"
        :key="option.id || index"
        class="option-item"
        :class="{
          'selected': selectedOption && selectedOption.id === option.id,
          'correct': showAnswer && option.correct,
          'wrong': showAnswer && selectedOption && selectedOption.id === option.id && !option.correct
        }"
        @click="selectOption(option)"
      >
        <!-- 选项字母 -->
        <div class="option-letter">
          {{ String.fromCharCode(65 + index) }}
        </div>
        
        <!-- 选项内容 -->
        <div class="option-content">
          <div class="option-text" v-html="option.text"></div>
          
          <!-- 选项图片 -->
          <div v-if="option.image" class="option-image">
            <img :src="option.image" :alt="option.text">
          </div>
        </div>
        
        <!-- 单选按钮 -->
        <div class="radio-button">
          <div class="radio-inner" :class="{ 'checked': selectedOption && selectedOption.id === option.id }"></div>
        </div>
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
        <span class="answer-option">
          {{ getCorrectAnswerLetter() }}
        </span>
      </div>
      
      <div v-if="question.reference" class="reference">
        <strong>参考：</strong>{{ question.reference }}
      </div>
    </div>
    
    <!-- 难度提示 -->
    <div v-if="question.difficulty" class="difficulty-indicator">
      <span class="difficulty-label">难度：</span>
      <div class="difficulty-stars">
        <span
          v-for="n in 5"
          :key="n"
          class="star"
          :class="{ 'filled': n <= question.difficulty }"
        >
          ★
        </span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'OneSelectQuestion',
  props: {
    question: {
      type: Object,
      required: true,
      default: () => ({
        id: '',
        title: '',
        score: 0,
        options: [],
        attachment: null,
        explanation: '',
        reference: '',
        difficulty: 0
      })
    },
    value: {
      type: Object,
      default: null
    },
    showAnswer: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      selectedOption: this.value,
      error: ''
    }
  },
  watch: {
    value(newVal) {
      this.selectedOption = newVal
    },
    selectedOption(newVal) {
      this.$emit('input', newVal)
      this.validate()
    }
  },
  methods: {
    selectOption(option) {
      this.selectedOption = option
    },
    
    getCorrectAnswerLetter() {
      if (!this.question.options) return ''
      
      const correctOption = this.question.options.find(opt => opt.correct)
      if (!correctOption) return ''
      
      const index = this.question.options.indexOf(correctOption)
      return String.fromCharCode(65 + index)
    },
    
    validate() {
      if (this.question.required && !this.selectedOption) {
        this.error = '请选择一个选项'
        return false
      }
      
      this.error = ''
      return true
    },
    
    // 检查答案是否正确
    checkAnswer() {
      if (!this.selectedOption) {
        return { correct: false, score: 0 }
      }
      
      const isCorrect = this.selectedOption.correct || false
      const score = isCorrect ? (this.question.score || 0) : 0
      
      return {
        correct: isCorrect,
        score: score,
        userAnswer: this.selectedOption,
        correctAnswer: this.question.options?.find(opt => opt.correct) || null
      }
    },
    
    // 供父组件调用的方法
    getAnswer() {
      return this.selectedOption
    },
    
    clear() {
      this.selectedOption = null
      this.error = ''
    },
    
    selectCorrect() {
      const correctOption = this.question.options?.find(opt => opt.correct)
      if (correctOption) {
        this.selectedOption = correctOption
      }
    }
  }
}
</script>

<style scoped lang="scss">
.single-select-question {
  border: 1px solid #e1e5eb;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  background: white;
  
  &.has-error {
    border-color: #f56c6c;
  }
  
  .question-header {
    margin-bottom: 20px;
    
    .question-meta {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 10px;
      
      .question-type {
        background: linear-gradient(135deg, #3498db, #2980b9);
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
      margin-bottom: 10px;
    }
    
    .question-attachment {
      .attachment-link {
        display: inline-flex;
        align-items: center;
        gap: 8px;
        padding: 8px 12px;
        background: #f5f7fa;
        border-radius: 4px;
        color: #409eff;
        text-decoration: none;
        
        &:hover {
          background: #ecf5ff;
        }
        
        .attachment-icon {
          font-size: 14px;
        }
      }
    }
  }
  
  .options-area {
    .option-item {
      display: flex;
      align-items: flex-start;
      padding: 15px;
      margin-bottom: 10px;
      border: 2px solid #e1e5eb;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.3s;
      
      &:hover {
        border-color: #409eff;
        background: #f0f5ff;
      }
      
      &.selected {
        border-color: #409eff;
        background: #f0f5ff;
        
        .option-letter {
          background: #409eff;
          color: white;
        }
      }
      
      &.correct {
        border-color: #67c23a;
        background: #f0f9eb;
        
        .option-letter {
          background: #67c23a;
          color: white;
        }
      }
      
      &.wrong {
        border-color: #f56c6c;
        background: #fef0f0;
        
        .option-letter {
          background: #f56c6c;
          color: white;
        }
      }
      
      .option-letter {
        width: 28px;
        height: 28px;
        min-width: 28px;
        background: #f5f7fa;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-weight: 600;
        margin-right: 15px;
        transition: all 0.3s;
      }
      
      .option-content {
        flex: 1;
        
        .option-text {
          line-height: 1.6;
          color: #333;
        }
        
        .option-image {
          margin-top: 10px;
          
          img {
            max-width: 200px;
            max-height: 150px;
            border-radius: 4px;
          }
        }
      }
      
      .radio-button {
        margin-left: 10px;
        
        .radio-inner {
          width: 20px;
          height: 20px;
          border: 2px solid #dcdfe6;
          border-radius: 50%;
          position: relative;
          transition: all 0.3s;
          
          &.checked {
            border-color: #409eff;
            
            &::after {
              content: '';
              position: absolute;
              top: 50%;
              left: 50%;
              transform: translate(-50%, -50%);
              width: 10px;
              height: 10px;
              background: #409eff;
              border-radius: 50%;
            }
          }
        }
      }
    }
  }
  
  .error-message {
    color: #f56c6c;
    font-size: 12px;
    margin-top: 8px;
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
      
      .answer-option {
        display: inline-block;
        width: 24px;
        height: 24px;
        line-height: 24px;
        text-align: center;
        background: #67c23a;
        color: white;
        border-radius: 50%;
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
  
  .difficulty-indicator {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-top: 20px;
    padding-top: 20px;
    border-top: 1px solid #ebeef5;
    
    .difficulty-label {
      font-size: 14px;
      color: #909399;
    }
    
    .difficulty-stars {
      display: flex;
      gap: 2px;
      
      .star {
        color: #dcdfe6;
        font-size: 16px;
        
        &.filled {
          color: #e6a23c;
        }
      }
    }
  }
}
</style>