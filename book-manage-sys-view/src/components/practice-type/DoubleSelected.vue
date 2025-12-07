<template>
  <div class="double-select-question" :class="{ 'has-error': error }">
    <!-- 题目信息 -->
    <div class="question-header">
      <div class="question-meta">
        <span class="question-type">双选题</span>
        <span class="question-score" v-if="question.score">
          ({{ question.score }}分)
        </span>
      </div>
      
      <div class="question-title" v-html="question.title"></div>
      
      <!-- 选择提示 -->
      <div class="hint" v-if="question.maxSelect">
        请选择{{ question.minSelect || 1 }}-{{ question.maxSelect }}个选项
        <span v-if="selectedCount > 0">（已选{{ selectedCount }}个）</span>
      </div>
    </div>
    
    <!-- 选项区域 -->
    <div class="options-area">
      <div
        v-for="(option, index) in question.options"
        :key="option.id || index"
        class="option-item"
        :class="{
          'selected': isSelected(option),
          'correct': showAnswer && option.correct,
          'wrong': showAnswer && isSelected(option) && !option.correct
        }"
        @click="toggleOption(option)"
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
        
        <!-- 选中标记 -->
        <div class="option-checkbox">
          <div class="checkbox-inner" :class="{ 'checked': isSelected(option) }">
            <span v-if="isSelected(option)">✓</span>
          </div>
        </div>
      </div>
      
      <!-- 用户自定义选项 -->
      <div v-if="question.allowCustom" class="custom-option">
        <div class="custom-input">
          <input
            type="text"
            v-model="customText"
            placeholder="输入其他选项..."
            @keyup.enter="addCustomOption"
          >
          <button @click="addCustomOption">添加</button>
        </div>
        
        <div v-if="customOptions.length > 0" class="custom-list">
          <div
            v-for="(option, index) in customOptions"
            :key="'custom-' + index"
            class="custom-item"
            :class="{ 'selected': isSelected(option) }"
            @click="toggleOption(option)"
          >
            <span class="custom-text">{{ option.text }}</span>
            <button class="remove-btn" @click.stop="removeCustomOption(index)">
              ×
            </button>
          </div>
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
      
      <div v-if="question.reference" class="reference">
        <strong>参考：</strong>{{ question.reference }}
      </div>
    </div>
    
    <!-- 正确率统计 -->
    <div v-if="showStatistics && question.statistics" class="statistics">
      <div class="stat-item">
        <span class="stat-label">正确率：</span>
        <span class="stat-value">{{ question.statistics.correctRate }}%</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">选择分布：</span>
        <div class="stat-bars">
          <div
            v-for="(stat, index) in question.options"
            :key="index"
            class="stat-bar"
            :title="`${String.fromCharCode(65 + index)}: ${stat.rate || 0}%`"
          >
            <div class="bar-fill" :style="{ height: (stat.rate || 0) + '%' }"></div>
            <span class="bar-label">{{ String.fromCharCode(65 + index) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'DoubleSelectQuestion',
  props: {
    question: {
      type: Object,
      required: true,
      default: () => ({
        id: '',
        title: '',
        score: 0,
        options: [],
        minSelect: 2,
        maxSelect: 2,
        allowCustom: false,
        explanation: '',
        reference: '',
        statistics: null
      })
    },
    value: {
      type: Array,
      default: () => []
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
      selectedOptions: this.value || [],
      customOptions: [],
      customText: '',
      error: ''
    }
  },
  computed: {
    selectedCount() {
      return this.selectedOptions.length
    }
  },
  watch: {
    value(newVal) {
      this.selectedOptions = newVal || []
    },
    selectedOptions: {
      handler(newVal) {
        this.$emit('input', newVal)
        this.validate()
      },
      deep: true
    }
  },
  methods: {
    isSelected(option) {
      return this.selectedOptions.some(selected => 
        selected.id ? selected.id === option.id : selected.text === option.text
      )
    },
    
    toggleOption(option) {
      const isSelected = this.isSelected(option)
      
      if (isSelected) {
        // 取消选中
        this.selectedOptions = this.selectedOptions.filter(selected =>
          selected.id ? selected.id !== option.id : selected.text !== option.text
        )
      } else {
        // 检查是否超过最大选择数
        if (this.question.maxSelect && this.selectedCount >= this.question.maxSelect) {
          this.error = `最多只能选择${this.question.maxSelect}个选项`
          return
        }
        
        this.selectedOptions.push(option)
        this.error = ''
      }
    },
    
    addCustomOption() {
      if (!this.customText.trim()) return
      
      if (this.customOptions.length >= 3) {
        this.error = '最多只能添加3个自定义选项'
        return
      }
      
      const customOption = {
        id: 'custom-' + Date.now(),
        text: this.customText.trim(),
        custom: true
      }
      
      this.customOptions.push(customOption)
      this.customText = ''
      this.error = ''
    },
    
    removeCustomOption(index) {
      // 如果自定义选项已被选中，先从选中列表中移除
      const option = this.customOptions[index]
      if (this.isSelected(option)) {
        this.selectedOptions = this.selectedOptions.filter(selected => 
          selected.text !== option.text
        )
      }
      
      this.customOptions.splice(index, 1)
    },
    
    validate() {
      if (this.question.required && this.selectedCount === 0) {
        this.error = '请至少选择一个选项'
        return false
      }
      
      if (this.question.minSelect && this.selectedCount < this.question.minSelect) {
        this.error = `请至少选择${this.question.minSelect}个选项`
        return false
      }
      
      if (this.question.maxSelect && this.selectedCount > this.question.maxSelect) {
        this.error = `最多只能选择${this.question.maxSelect}个选项`
        return false
      }
      
      this.error = ''
      return true
    },
    
    // 检查答案是否正确
    checkAnswer() {
      if (!this.question.options || this.selectedCount === 0) {
        return { correct: false, score: 0 }
      }
      
      // 获取所有正确选项
      const correctOptions = this.question.options.filter(opt => opt.correct)
      const correctIds = correctOptions.map(opt => opt.id)
      
      // 用户选择的选项
      const selectedIds = this.selectedOptions.map(opt => opt.id)
      
      // 判断是否完全正确
      const isCorrect = 
        selectedIds.length === correctIds.length &&
        selectedIds.every(id => correctIds.includes(id)) &&
        correctIds.every(id => selectedIds.includes(id))
      
      // 计算分数（部分正确的情况）
      let score = 0
      if (isCorrect) {
        score = this.question.score || 0
      } else {
        // 选对一部分的情况（根据规则计算部分分数）
        const correctSelected = selectedIds.filter(id => correctIds.includes(id)).length
        const totalCorrect = correctIds.length
        score = Math.round((correctSelected / totalCorrect) * (this.question.score || 0))
      }
      
      return {
        correct: isCorrect,
        score: score,
        correctOptions: correctOptions,
        userOptions: this.selectedOptions
      }
    },
    
    // 供父组件调用的方法
    getAnswer() {
      return this.selectedOptions
    },
    
    clear() {
      this.selectedOptions = []
      this.customOptions = []
      this.customText = ''
      this.error = ''
    },
    
    selectAllCorrect() {
      if (!this.question.options) return
      
      const correctOptions = this.question.options.filter(opt => opt.correct)
      this.selectedOptions = [...correctOptions]
    }
  }
}
</script>

<style scoped lang="scss">
.double-select-question {
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
        background: linear-gradient(135deg, #9b59b6, #8e44ad);
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
    
    .hint {
      font-size: 14px;
      color: #909399;
      padding: 8px 12px;
      background: #f0f5ff;
      border-radius: 4px;
      border-left: 3px solid #409eff;
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
      
      .option-checkbox {
        margin-left: 10px;
        
        .checkbox-inner {
          width: 20px;
          height: 20px;
          border: 2px solid #dcdfe6;
          border-radius: 4px;
          display: flex;
          align-items: center;
          justify-content: center;
          transition: all 0.3s;
          
          &.checked {
            background: #409eff;
            border-color: #409eff;
            color: white;
          }
        }
      }
    }
    
    .custom-option {
      margin-top: 20px;
      padding-top: 20px;
      border-top: 1px solid #ebeef5;
      
      .custom-input {
        display: flex;
        gap: 10px;
        margin-bottom: 15px;
        
        input {
          flex: 1;
          padding: 8px 12px;
          border: 1px solid #dcdfe6;
          border-radius: 4px;
          
          &:focus {
            outline: none;
            border-color: #409eff;
          }
        }
        
        button {
          padding: 8px 20px;
          background: #409eff;
          color: white;
          border: none;
          border-radius: 4px;
          cursor: pointer;
          
          &:hover {
            background: #66b1ff;
          }
        }
      }
      
      .custom-list {
        display: flex;
        flex-wrap: wrap;
        gap: 10px;
        
        .custom-item {
          display: flex;
          align-items: center;
          gap: 8px;
          padding: 6px 12px;
          background: #f5f7fa;
          border-radius: 4px;
          border: 1px solid transparent;
          cursor: pointer;
          
          &.selected {
            background: #f0f5ff;
            border-color: #409eff;
          }
          
          .custom-text {
            font-size: 14px;
          }
          
          .remove-btn {
            width: 18px;
            height: 18px;
            background: #f56c6c;
            color: white;
            border: none;
            border-radius: 50%;
            cursor: pointer;
            font-size: 12px;
            line-height: 1;
            
            &:hover {
              background: #e65c5c;
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
    }
    
    .reference {
      margin-top: 10px;
      padding: 8px;
      background: white;
      border-radius: 4px;
      font-size: 12px;
      color: #909399;
    }
  }
  
  .statistics {
    margin-top: 20px;
    padding: 15px;
    background: #f8f9fa;
    border-radius: 6px;
    
    .stat-item {
      margin-bottom: 15px;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .stat-label {
        font-weight: 600;
        color: #333;
        margin-right: 10px;
      }
      
      .stat-value {
        color: #409eff;
        font-weight: 600;
      }
      
      .stat-bars {
        display: flex;
        align-items: flex-end;
        height: 100px;
        gap: 20px;
        margin-top: 10px;
        
        .stat-bar {
          flex: 1;
          display: flex;
          flex-direction: column;
          align-items: center;
          
          .bar-fill {
            width: 30px;
            background: #409eff;
            border-radius: 4px 4px 0 0;
            transition: height 0.5s;
          }
          
          .bar-label {
            margin-top: 5px;
            font-weight: 600;
            color: #333;
          }
        }
      }
    }
  }
}
</style>