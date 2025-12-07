<template>
  <div class="word-put-question" :class="{ 'has-error': error }">
    <!-- 题目信息 -->
    <div class="question-header">
      <div class="question-meta">
        <span class="question-type">填空题</span>
        <span class="question-score" v-if="question.score">
          ({{ question.score }}分)
        </span>
      </div>
      
      <!-- 题目描述（含填空位置） -->
      <div class="question-content">
        <div 
          class="content-text"
          v-html="renderQuestionWithBlanks()"
        ></div>
      </div>
      
      <!-- 提示信息 -->
      <div v-if="question.hint" class="hint">
        <span class="hint-icon">💡</span>
        {{ question.hint }}
      </div>
    </div>
    
    <!-- 答案输入区域 -->
    <div class="answers-area">
      <div
        v-for="(blank, index) in question.blanks"
        :key="blank.id || index"
        class="blank-item"
        :class="{
          'has-value': answers[index],
          'correct': showAnswer && isAnswerCorrect(index),
          'wrong': showAnswer && answers[index] && !isAnswerCorrect(index)
        }"
      >
        <div class="blank-label">
          第{{ index + 1 }}空
          <span v-if="blank.score" class="blank-score">({{ blank.score }}分)</span>
        </div>
        
        <div class="input-container">
          <!-- 单行输入 -->
          <input
            v-if="!blank.multiple"
            type="text"
            v-model="answers[index]"
            :placeholder="blank.placeholder || '请输入答案'"
            :maxlength="blank.maxLength"
            class="answer-input"
            @input="handleInput(index, $event)"
          />
          
          <!-- 多行输入 -->
          <textarea
            v-else
            v-model="answers[index]"
            :placeholder="blank.placeholder || '请输入答案'"
            :rows="blank.rows || 3"
            :maxlength="blank.maxLength"
            class="answer-textarea"
            @input="handleInput(index, $event)"
          ></textarea>
          
          <!-- 输入计数器 -->
          <div v-if="blank.maxLength" class="char-counter">
            {{ answers[index]?.length || 0 }}/{{ blank.maxLength }}
          </div>
        </div>
        
        <!-- 答案提示 -->
        <div v-if="question.allowHint && blank.hint" class="blank-hint">
          <button class="hint-btn" @click="showHint(index)">
            显示提示
          </button>
          <div v-if="hintsShown[index]" class="hint-text">
            {{ blank.hint }}
          </div>
        </div>
        
        <!-- 参考答案 -->
        <div v-if="showAnswer && blank.correctAnswers" class="correct-answers">
          <div class="correct-title">参考答案：</div>
          <div class="answers-list">
            <span
              v-for="(answer, ansIndex) in blank.correctAnswers"
              :key="ansIndex"
              class="correct-answer"
            >
              {{ answer }}
              <span v-if="ansIndex < blank.correctAnswers.length - 1">、</span>
            </span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 错误提示 -->
    <div v-if="error" class="error-message">
      {{ error }}
    </div>
    
    <!-- 批量填写 -->
    <div v-if="question.batchInput" class="batch-input">
      <div class="batch-title">批量填写：</div>
      <textarea
        v-model="batchText"
        placeholder="每行一个答案，按顺序对应填空"
        :rows="question.blanks?.length || 1"
        class="batch-textarea"
        @input="handleBatchInput"
      ></textarea>
    </div>
    
    <!-- 知识点关联 -->
    <div v-if="question.knowledgePoints" class="knowledge-points">
      <h4>相关知识点：</h4>
      <ul class="points-list">
        <li
          v-for="(point, index) in question.knowledgePoints"
          :key="index"
          class="point-item"
        >
          {{ point }}
        </li>
      </ul>
    </div>
  </div>
</template>

<script>
export default {
  name: 'WordPutQuestion',
  props: {
    question: {
      type: Object,
      required: true,
      default: () => ({
        id: '',
        title: '',
        content: '', // 含{{blank}}的文本
        score: 0,
        blanks: [],
        hint: '',
        allowHint: false,
        batchInput: false,
        knowledgePoints: []
      })
    },
    value: {
      type: Array,
      default: () => []
    },
    showAnswer: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      answers: this.value || [],
      batchText: '',
      hintsShown: {},
      error: ''
    }
  },
  watch: {
    value(newVal) {
      this.answers = [...newVal]
    },
    answers: {
      handler(newVal) {
        this.$emit('input', newVal)
        this.validate()
      },
      deep: true
    }
  },
  mounted() {
    // 初始化答案数组
    if (!this.answers.length && this.question.blanks) {
      this.answers = new Array(this.question.blanks.length).fill('')
    }
    
    // 初始化提示显示状态
    if (this.question.blanks) {
      this.question.blanks.forEach((_, index) => {
        this.$set(this.hintsShown, index, false)
      })
    }
  },
  methods: {
    renderQuestionWithBlanks() {
      if (!this.question.content) return ''
      
      let content = this.question.content
      const blankCount = this.question.blanks?.length || 0
      
      // 替换{{blank}}为带下划线的空白
      for (let i = 0; i < blankCount; i++) {
        const blank = this.question.blanks[i]
        const blankHtml = `
          <span class="blank-placeholder" data-index="${i}">
            <span class="blank-number">${i + 1}</span>
            <span class="blank-line"></span>
          </span>
        `
        content = content.replace('{{blank}}', blankHtml)
      }
      
      return content
    },
    
    handleInput(index, event) {
      this.answers[index] = event.target.value
    },
    
    handleBatchInput(event) {
      const lines = event.target.value.split('\n')
      this.question.blanks?.forEach((_, index) => {
        if (lines[index] !== undefined) {
          this.answers[index] = lines[index].trim()
        }
      })
    },
    
    showHint(index) {
      this.$set(this.hintsShown, index, true)
    },
    
    isAnswerCorrect(index) {
      if (!this.answers[index] || !this.question.blanks?.[index]?.correctAnswers) {
        return false
      }
      
      const userAnswer = this.answers[index].trim().toLowerCase()
      const correctAnswers = this.question.blanks[index].correctAnswers.map(
        ans => ans.trim().toLowerCase()
      )
      
      // 支持多个正确答案，只要匹配其中一个即可
      return correctAnswers.some(correct => {
        // 如果是精确匹配模式
        if (this.question.blanks[index].exactMatch) {
          return userAnswer === correct
        }
        // 模糊匹配（包含关系）
        return userAnswer.includes(correct) || correct.includes(userAnswer)
      })
    },
    
    validate() {
      const requiredBlanks = this.question.blanks?.filter(blank => blank.required) || []
      
      for (let i = 0; i < requiredBlanks.length; i++) {
        if (!this.answers[i]?.trim()) {
          this.error = `请填写第${i + 1}空`
          return false
        }
      }
      
      // 检查长度限制
      for (let i = 0; i < this.question.blanks?.length; i++) {
        const blank = this.question.blanks[i]
        if (blank.maxLength && this.answers[i]?.length > blank.maxLength) {
          this.error = `第${i + 1}空超过字数限制`
          return false
        }
      }
      
      this.error = ''
      return true
    },
    
    // 检查答案正确性
    checkAnswer() {
      if (!this.question.blanks) {
        return { correct: false, score: 0 }
      }
      
      let totalScore = 0
      let correctCount = 0
      const blankResults = []
      
      this.question.blanks.forEach((blank, index) => {
        const isCorrect = this.isAnswerCorrect(index)
        const blankScore = isCorrect ? (blank.score || 0) : 0
        
        totalScore += blankScore
        if (isCorrect) correctCount++
        
        blankResults.push({
          index,
          userAnswer: this.answers[index] || '',
          correct: isCorrect,
          score: blankScore,
          correctAnswers: blank.correctAnswers || []
        })
      })
      
      const allCorrect = correctCount === this.question.blanks.length
      
      return {
        correct: allCorrect,
        score: totalScore,
        correctCount,
        totalCount: this.question.blanks.length,
        blankResults
      }
    },
    
    // 供父组件调用的方法
    getAnswer() {
      return this.answers
    },
    
    clear() {
      this.answers = new Array(this.question.blanks?.length || 0).fill('')
      this.batchText = ''
      this.hintsShown = {}
      this.error = ''
    },
    
    fillAllCorrect() {
      if (!this.question.blanks) return
      
      this.question.blanks.forEach((blank, index) => {
        if (blank.correctAnswers && blank.correctAnswers.length > 0) {
          this.answers[index] = blank.correctAnswers[0]
        }
      })
    }
  }
}
</script>

<style scoped lang="scss">
.word-put-question {
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
      margin-bottom: 15px;
      
      .question-type {
        background: linear-gradient(135deg, #67c23a, #5daf34);
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
    
    .question-content {
      line-height: 1.8;
      font-size: 16px;
      color: #333;
      
      .blank-placeholder {
        display: inline-block;
        vertical-align: middle;
        margin: 0 5px;
        text-align: center;
        min-width: 80px;
        
        .blank-number {
          display: block;
          font-size: 12px;
          color: #909399;
          margin-bottom: 2px;
        }
        
        .blank-line {
          display: block;
          height: 2px;
          background: #333;
          min-width: 60px;
        }
      }
    }
    
    .hint {
      margin-top: 15px;
      padding: 10px 15px;
      background: #f0f9eb;
      border-radius: 4px;
      border-left: 3px solid #67c23a;
      color: #333;
      
      .hint-icon {
        margin-right: 8px;
      }
    }
  }
  
  .answers-area {
    .blank-item {
      margin-bottom: 20px;
      padding: 15px;
      border: 1px solid #e1e5eb;
      border-radius: 6px;
      transition: all 0.3s;
      
      &:hover {
        border-color: #409eff;
      }
      
      &.has-value {
        border-color: #409eff;
        background: #f0f5ff;
      }
      
      &.correct {
        border-color: #67c23a;
        background: #f0f9eb;
      }
      
      &.wrong {
        border-color: #f56c6c;
        background: #fef0f0;
      }
      
      .blank-label {
        font-weight: 600;
        color: #333;
        margin-bottom: 10px;
        
        .blank-score {
          color: #e6a23c;
          margin-left: 5px;
          font-size: 12px;
        }
      }
      
      .input-container {
        position: relative;
        margin-bottom: 10px;
        
        .answer-input {
          width: 100%;
          padding: 10px 15px;
          border: 1px solid #dcdfe6;
          border-radius: 4px;
          font-size: 14px;
          
          &:focus {
            outline: none;
            border-color: #409eff;
          }
        }
        
        .answer-textarea {
          width: 100%;
          padding: 10px 15px;
          border: 1px solid #dcdfe6;
          border-radius: 4px;
          font-size: 14px;
          resize: vertical;
          font-family: inherit;
          
          &:focus {
            outline: none;
            border-color: #409eff;
          }
        }
        
        .char-counter {
          position: absolute;
          bottom: 5px;
          right: 10px;
          font-size: 12px;
          color: #909399;
        }
      }
      
      .blank-hint {
        margin-top: 10px;
        
        .hint-btn {
          padding: 4px 12px;
          background: transparent;
          border: 1px solid #e6a23c;
          color: #e6a23c;
          border-radius: 4px;
          font-size: 12px;
          cursor: pointer;
          
          &:hover {
            background: #e6a23c;
            color: white;
          }
        }
        
        .hint-text {
          margin-top: 8px;
          padding: 8px;
          background: #fdf6ec;
          border-radius: 4px;
          font-size: 14px;
          color: #333;
        }
      }
      
      .correct-answers {
        margin-top: 10px;
        padding: 10px;
        background: #f8f9fa;
        border-radius: 4px;
        
        .correct-title {
          font-size: 12px;
          color: #909399;
          margin-bottom: 5px;
        }
        
        .answers-list {
          font-size: 14px;
          color: #333;
          
          .correct-answer {
            color: #67c23a;
            font-weight: 600;
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
  
  .batch-input {
    margin-top: 20px;
    padding: 15px;
    background: #f8f9fa;
    border-radius: 6px;
    
    .batch-title {
      font-weight: 600;
      color: #333;
      margin-bottom: 10px;
    }
    
    .batch-textarea {
      width: 100%;
      padding: 10px;
      border: 1px solid #dcdfe6;
      border-radius: 4px;
      font-family: monospace;
      font-size: 14px;
      resize: vertical;
      
      &:focus {
        outline: none;
        border-color: #409eff;
      }
    }
  }
  
  .knowledge-points {
    margin-top: 20px;
    padding-top: 20px;
    border-top: 1px solid #ebeef5;
    
    h4 {
      margin: 0 0 10px 0;
      font-size: 14px;
      color: #909399;
    }
    
    .points-list {
      list-style: none;
      padding-left: 0;
      
      .point-item {
        padding: 5px 0;
        color: #333;
        position: relative;
        padding-left: 15px;
        
        &::before {
          content: '•';
          position: absolute;
          left: 0;
          color: #67c23a;
        }
      }
    }
  }
}
</style>