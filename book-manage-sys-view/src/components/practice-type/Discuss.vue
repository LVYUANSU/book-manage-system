<template>
  <div class="discuss-question" :class="{ 'has-error': error }">
    <!-- 题目信息 -->
    <div class="question-header">
      <div class="question-meta">
        <span class="question-type">论述题</span>
        <span class="question-score" v-if="question.score">
          ({{ question.score }}分)
        </span>
      </div>
      
      <div class="question-title" v-html="question.title"></div>
      
      <!-- 字数统计 -->
      <div class="word-count" v-if="question.maxLength">
        字数限制：{{ wordCount }}/{{ question.maxLength }}
        <div class="word-progress">
          <div 
            class="progress-bar" 
            :style="{ width: progressWidth }"
            :class="{
              'warning': wordCount > question.maxLength * 0.8,
              'danger': wordCount > question.maxLength
            }"
          ></div>
        </div>
      </div>
    </div>
    
    <!-- 答题区域 -->
    <div class="answer-area">
      <!-- 富文本编辑器模式 -->
      <div v-if="question.richText" class="rich-editor">
        <div class="editor-toolbar">
          <button type="button" @click="formatText('bold')" title="加粗">
            <strong>B</strong>
          </button>
          <button type="button" @click="formatText('italic')" title="斜体">
            <em>I</em>
          </button>
          <button type="button" @click="formatText('underline')" title="下划线">
            <u>U</u>
          </button>
          <button type="button" @click="formatText('list')" title="列表">
            • 列表
          </button>
        </div>
        
        <div 
          class="editor-content"
          contenteditable="true"
          ref="editor"
          @input="handleInput"
          @paste="handlePaste"
          :placeholder="question.placeholder || '请输入您的答案...'"
        ></div>
      </div>
      
      <!-- 普通文本区域 -->
      <textarea
        v-else
        v-model="answer"
        :placeholder="question.placeholder || '请输入您的答案...'"
        :rows="question.rows || 8"
        :maxlength="question.maxLength"
        class="text-area"
        @input="handleTextareaInput"
      ></textarea>
      
      <!-- 图片上传 -->
      <div v-if="question.allowImage" class="image-upload">
        <div class="upload-area" @click="triggerFileInput">
          <span class="upload-icon">📷</span>
          <p>点击上传图片</p>
          <input
            type="file"
            ref="fileInput"
            accept="image/*"
            @change="handleImageUpload"
            style="display: none"
          >
        </div>
        
        <div v-if="uploadedImages.length > 0" class="uploaded-images">
          <div v-for="(image, index) in uploadedImages" :key="index" class="image-item">
            <img :src="image.url" alt="上传的图片">
            <button class="remove-btn" @click="removeImage(index)">×</button>
          </div>
        </div>
      </div>
      
      <!-- 错误提示 -->
      <div v-if="error" class="error-message">
        {{ error }}
      </div>
    </div>
    
    <!-- 参考答案（仅管理员可见） -->
    <div v-if="showReferenceAnswer && question.referenceAnswer" class="reference-answer">
      <h4>参考答案：</h4>
      <div class="answer-content" v-html="question.referenceAnswer"></div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'DiscussQuestion',
  props: {
    question: {
      type: Object,
      required: true,
      default: () => ({
        id: '',
        title: '',
        score: 0,
        maxLength: 1000,
        richText: false,
        allowImage: false,
        rows: 8,
        placeholder: '',
        referenceAnswer: ''
      })
    },
    value: {
      type: [String, Object],
      default: ''
    },
    showReferenceAnswer: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      answer: this.value || '',
      wordCount: 0,
      uploadedImages: [],
      error: ''
    }
  },
  computed: {
    progressWidth() {
      if (!this.question.maxLength) return '0%'
      const percentage = (this.wordCount / this.question.maxLength) * 100
      return Math.min(percentage, 100) + '%'
    }
  },
  watch: {
    value(newVal) {
      if (newVal !== this.answer) {
        this.answer = newVal
        this.calculateWordCount()
      }
    },
    answer(newVal) {
      this.$emit('input', newVal)
      this.validate()
    }
  },
  mounted() {
    this.calculateWordCount()
  },
  methods: {
    handleInput(event) {
      this.answer = event.target.innerHTML
      this.calculateWordCount()
    },
    
    handleTextareaInput(event) {
      this.answer = event.target.value
      this.calculateWordCount()
    },
    
    calculateWordCount() {
      if (this.question.richText) {
        // 去除HTML标签后的纯文本长度
        const text = this.$refs.editor?.innerText || ''
        this.wordCount = text.length
      } else {
        this.wordCount = this.answer.length
      }
    },
    
    formatText(type) {
      if (!this.$refs.editor) return
      
      document.execCommand('defaultParagraphSeparator', false, 'p')
      
      switch (type) {
        case 'bold':
          document.execCommand('bold')
          break
        case 'italic':
          document.execCommand('italic')
          break
        case 'underline':
          document.execCommand('underline')
          break
        case 'list':
          document.execCommand('insertUnorderedList')
          break
      }
      
      this.$refs.editor.focus()
    },
    
    handlePaste(event) {
      event.preventDefault()
      const text = event.clipboardData.getData('text/plain')
      document.execCommand('insertText', false, text)
    },
    
    triggerFileInput() {
      this.$refs.fileInput.click()
    },
    
    handleImageUpload(event) {
      const file = event.target.files[0]
      if (!file) return
      
      if (!file.type.startsWith('image/')) {
        this.error = '请上传图片文件'
        return
      }
      
      if (file.size > 5 * 1024 * 1024) {
        this.error = '图片大小不能超过5MB'
        return
      }
      
      const reader = new FileReader()
      reader.onload = (e) => {
        this.uploadedImages.push({
          name: file.name,
          url: e.target.result,
          file: file
        })
        this.error = ''
      }
      reader.readAsDataURL(file)
      
      // 重置input
      event.target.value = ''
    },
    
    removeImage(index) {
      this.uploadedImages.splice(index, 1)
    },
    
    validate() {
      if (this.question.required && !this.answer.trim()) {
        this.error = '请填写答案'
        return false
      }
      
      if (this.question.maxLength && this.wordCount > this.question.maxLength) {
        this.error = `字数超过限制，最多${this.question.maxLength}字`
        return false
      }
      
      this.error = ''
      return true
    },
    
    // 供父组件调用的方法
    getAnswer() {
      return {
        text: this.answer,
        images: this.uploadedImages,
        wordCount: this.wordCount
      }
    },
    
    clear() {
      this.answer = ''
      this.uploadedImages = []
      this.wordCount = 0
      this.error = ''
      
      if (this.$refs.editor) {
        this.$refs.editor.innerHTML = ''
      }
    }
  }
}
</script>

<style scoped lang="scss">
.discuss-question {
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
      margin-bottom: 15px;
    }
    
    .word-count {
      font-size: 12px;
      color: #909399;
      
      .word-progress {
        width: 200px;
        height: 4px;
        background: #ebeef5;
        border-radius: 2px;
        margin-top: 4px;
        overflow: hidden;
        
        .progress-bar {
          height: 100%;
          background: #67c23a;
          transition: width 0.3s;
          
          &.warning {
            background: #e6a23c;
          }
          
          &.danger {
            background: #f56c6c;
          }
        }
      }
    }
  }
  
  .answer-area {
    .rich-editor {
      border: 1px solid #dcdfe6;
      border-radius: 4px;
      overflow: hidden;
      
      .editor-toolbar {
        background: #f5f7fa;
        padding: 8px;
        border-bottom: 1px solid #dcdfe6;
        display: flex;
        gap: 8px;
        
        button {
          background: white;
          border: 1px solid #dcdfe6;
          border-radius: 4px;
          padding: 4px 12px;
          cursor: pointer;
          
          &:hover {
            background: #ecf5ff;
            border-color: #c6e2ff;
          }
        }
      }
      
      .editor-content {
        min-height: 200px;
        padding: 12px;
        outline: none;
        
        &:empty::before {
          content: attr(placeholder);
          color: #c0c4cc;
        }
      }
    }
    
    .text-area {
      width: 100%;
      padding: 12px;
      border: 1px solid #dcdfe6;
      border-radius: 4px;
      font-family: inherit;
      font-size: 14px;
      line-height: 1.5;
      resize: vertical;
      
      &:focus {
        outline: none;
        border-color: #409eff;
      }
      
      &::placeholder {
        color: #c0c4cc;
      }
    }
    
    .image-upload {
      margin-top: 15px;
      
      .upload-area {
        width: 120px;
        height: 120px;
        border: 2px dashed #dcdfe6;
        border-radius: 6px;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        transition: all 0.3s;
        
        &:hover {
          border-color: #409eff;
          background: #ecf5ff;
        }
        
        .upload-icon {
          font-size: 32px;
          margin-bottom: 8px;
        }
        
        p {
          margin: 0;
          font-size: 12px;
          color: #909399;
        }
      }
      
      .uploaded-images {
        display: flex;
        flex-wrap: wrap;
        gap: 10px;
        margin-top: 15px;
        
        .image-item {
          position: relative;
          width: 100px;
          height: 100px;
          
          img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            border-radius: 4px;
          }
          
          .remove-btn {
            position: absolute;
            top: -8px;
            right: -8px;
            width: 20px;
            height: 20px;
            background: #f56c6c;
            color: white;
            border: none;
            border-radius: 50%;
            cursor: pointer;
            font-size: 14px;
            line-height: 1;
            
            &:hover {
              background: #e65c5c;
            }
          }
        }
      }
    }
    
    .error-message {
      color: #f56c6c;
      font-size: 12px;
      margin-top: 8px;
    }
  }
  
  .reference-answer {
    margin-top: 20px;
    padding-top: 20px;
    border-top: 1px solid #ebeef5;
    
    h4 {
      margin: 0 0 10px 0;
      font-size: 14px;
      color: #909399;
    }
    
    .answer-content {
      background: #f8f9fa;
      padding: 15px;
      border-radius: 4px;
      border-left: 3px solid #67c23a;
    }
  }
}
</style>