<template>
  <div class="rich-text-editor" :class="editorClasses">
    <!-- 工具栏 -->
    <div v-if="showToolbar" class="editor-toolbar">
      <!-- 字体样式 -->
      <div class="toolbar-group">
        <select v-model="fontFamily" class="toolbar-select" @change="formatText('fontFamily', $event.target.value)">
          <option value="">默认字体</option>
          <option value="Arial, sans-serif">Arial</option>
          <option value="'Microsoft YaHei', sans-serif">微软雅黑</option>
          <option value="'SimSun', serif">宋体</option>
          <option value="'SimHei', sans-serif">黑体</option>
          <option value="'KaiTi', serif">楷体</option>
          <option value="Consolas, monospace">Consolas</option>
        </select>
        
        <select v-model="fontSize" class="toolbar-select" @change="formatText('fontSize', $event.target.value)">
          <option value="">字号</option>
          <option v-for="size in fontSizes" :key="size" :value="size">{{ size }}</option>
        </select>
        
        <button 
          type="button" 
          class="toolbar-btn" 
          :class="{ active: formats.bold }"
          @click="formatText('bold')"
          title="加粗"
        >
          <strong>B</strong>
        </button>
        <button 
          type="button" 
          class="toolbar-btn" 
          :class="{ active: formats.italic }"
          @click="formatText('italic')"
          title="斜体"
        >
          <em>I</em>
        </button>
        <button 
          type="button" 
          class="toolbar-btn" 
          :class="{ active: formats.underline }"
          @click="formatText('underline')"
          title="下划线"
        >
          <u>U</u>
        </button>
        <button 
          type="button" 
          class="toolbar-btn" 
          :class="{ active: formats.strikethrough }"
          @click="formatText('strikethrough')"
          title="删除线"
        >
          <s>S</s>
        </button>
      </div>
      
      <!-- 对齐方式 -->
      <div class="toolbar-group">
        <button 
          type="button" 
          class="toolbar-btn" 
          :class="{ active: formats.align === 'left' }"
          @click="formatText('justifyLeft')"
          title="左对齐"
        >
          ≡
        </button>
        <button 
          type="button" 
          class="toolbar-btn" 
          :class="{ active: formats.align === 'center' }"
          @click="formatText('justifyCenter')"
          title="居中对齐"
        >
          ≡
        </button>
        <button 
          type="button" 
          class="toolbar-btn" 
          :class="{ active: formats.align === 'right' }"
          @click="formatText('justifyRight')"
          title="右对齐"
        >
          ≡
        </button>
        <button 
          type="button" 
          class="toolbar-btn" 
          :class="{ active: formats.align === 'justify' }"
          @click="formatText('justifyFull')"
          title="两端对齐"
        >
          ≡
        </button>
      </div>
      
      <!-- 列表 -->
      <div class="toolbar-group">
        <button 
          type="button" 
          class="toolbar-btn" 
          @click="formatText('insertUnorderedList')"
          title="无序列表"
        >
          • 列表
        </button>
        <button 
          type="button" 
          class="toolbar-btn" 
          @click="formatText('insertOrderedList')"
          title="有序列表"
        >
          1. 列表
        </button>
        <button 
          type="button" 
          class="toolbar-btn" 
          @click="formatText('outdent')"
          title="减少缩进"
        >
          ←
        </button>
        <button 
          type="button" 
          class="toolbar-btn" 
          @click="formatText('indent')"
          title="增加缩进"
        >
          →
        </button>
      </div>
      
      <!-- 颜色 -->
      <div class="toolbar-group">
        <input 
          type="color" 
          v-model="textColor" 
          @change="formatText('foreColor', $event.target.value)"
          class="color-picker"
          title="文字颜色"
        >
        <input 
          type="color" 
          v-model="backgroundColor" 
          @change="formatText('hiliteColor', $event.target.value)"
          class="color-picker"
          title="背景颜色"
        >
        <button 
          type="button" 
          class="toolbar-btn" 
          @click="formatText('removeFormat')"
          title="清除格式"
        >
          🧹
        </button>
      </div>
      
      <!-- 链接和图片 -->
      <div class="toolbar-group">
        <button 
          type="button" 
          class="toolbar-btn" 
          @click="insertLink"
          title="插入链接"
        >
          🔗
        </button>
        <button 
          type="button" 
          class="toolbar-btn" 
          @click="insertImage"
          title="插入图片"
        >
          🖼️
        </button>
        <button 
          type="button" 
          class="toolbar-btn" 
          @click="insertTable"
          title="插入表格"
        >
          ⬜
        </button>
        <button 
          type="button" 
          class="toolbar-btn" 
          @click="insertCode"
          title="插入代码"
        >
          &lt;/&gt;
        </button>
      </div>
      
      <!-- 其他功能 -->
      <div class="toolbar-group">
        <button 
          type="button" 
          class="toolbar-btn" 
          @click="undo"
          title="撤销"
          :disabled="!canUndo"
        >
          ↩️
        </button>
        <button 
          type="button" 
          class="toolbar-btn" 
          @click="redo"
          title="重做"
          :disabled="!canRedo"
        >
          ↪️
        </button>
        <button 
          type="button" 
          class="toolbar-btn" 
          @click="showSourceCode = !showSourceCode"
          :class="{ active: showSourceCode }"
          title="查看源代码"
        >
          &lt;&gt;
        </button>
        <button 
          type="button" 
          class="toolbar-btn" 
          @click="fullscreen = !fullscreen"
          :class="{ active: fullscreen }"
          title="全屏编辑"
        >
          ⛶
        </button>
      </div>
    </div>
    
    <!-- 编辑区域 -->
    <div class="editor-container">
      <!-- 源代码模式 -->
      <textarea
        v-if="showSourceCode"
        v-model="htmlSource"
        class="source-editor"
        :placeholder="placeholder"
        @input="updateFromSource"
      ></textarea>
      
      <!-- 可视化编辑模式 -->
      <div
        v-else
        ref="editor"
        class="editor-content"
        contenteditable="true"
        :placeholder="placeholder"
        @input="handleInput"
        @focus="handleFocus"
        @blur="handleBlur"
        @keydown="handleKeydown"
        @paste="handlePaste"
        @mouseup="updateFormats"
      ></div>
      
      <!-- 字数统计 -->
      <div v-if="showWordCount" class="word-count">
        字数: {{ wordCount }} / {{ maxLength || '∞' }}
        <div class="word-progress" v-if="maxLength">
          <div 
            class="progress-bar" 
            :style="{ width: progressPercentage + '%' }"
            :class="{
              'warning': wordCount > maxLength * 0.8,
              'danger': wordCount > maxLength
            }"
          ></div>
        </div>
      </div>
      
      <!-- 插入模态框 -->
      <div v-if="showLinkModal" class="editor-modal">
        <div class="modal-content">
          <h3>插入链接</h3>
          <div class="form-group">
            <label>链接地址：</label>
            <input 
              type="text" 
              v-model="linkUrl" 
              placeholder="https://example.com"
              @keyup.enter="insertLinkConfirm"
            >
          </div>
          <div class="form-group">
            <label>显示文本：</label>
            <input 
              type="text" 
              v-model="linkText" 
              placeholder="链接文本"
              @keyup.enter="insertLinkConfirm"
            >
          </div>
          <div class="modal-actions">
            <button @click="insertLinkCancel">取消</button>
            <button @click="insertLinkConfirm" class="primary">确定</button>
          </div>
        </div>
      </div>
      
      <div v-if="showImageModal" class="editor-modal">
        <div class="modal-content">
          <h3>插入图片</h3>
          <div class="image-upload-options">
            <div class="upload-option" @click="selectLocalImage">
              <div class="option-icon">📁</div>
              <div class="option-text">本地上传</div>
              <input 
                type="file" 
                ref="imageInput" 
                accept="image/*" 
                @change="handleImageUpload"
                style="display: none"
              >
            </div>
            <div class="upload-option" @click="insertByUrl = true">
              <div class="option-icon">🌐</div>
              <div class="option-text">网络图片</div>
            </div>
          </div>
          
          <div v-if="insertByUrl" class="image-url-form">
            <div class="form-group">
              <label>图片地址：</label>
              <input 
                type="text" 
                v-model="imageUrl" 
                placeholder="https://example.com/image.jpg"
                @keyup.enter="insertImageByUrl"
              >
            </div>
            <div class="form-group">
              <label>图片描述：</label>
              <input 
                type="text" 
                v-model="imageAlt" 
                placeholder="图片描述"
                @keyup.enter="insertImageByUrl"
              >
            </div>
            <div class="form-group">
              <label>图片宽度：</label>
              <input 
                type="number" 
                v-model="imageWidth" 
                placeholder="自动"
              >
            </div>
            <div class="modal-actions">
              <button @click="insertImageCancel">取消</button>
              <button @click="insertImageByUrl" class="primary">插入</button>
            </div>
          </div>
        </div>
      </div>
      
      <div v-if="showTableModal" class="editor-modal">
        <div class="modal-content">
          <h3>插入表格</h3>
          <div class="table-size-selector">
            <div class="size-label">选择表格大小：</div>
            <div class="table-grid">
              <div 
                v-for="row in 10" 
                :key="'row-' + row"
                class="table-grid-row"
              >
                <div 
                  v-for="col in 10" 
                  :key="'cell-' + row + '-' + col"
                  class="table-grid-cell"
                  :class="{ 
                    'selected': row <= tableRows && col <= tableColumns,
                    'hover': row <= hoverRows && col <= hoverColumns
                  }"
                  @mouseenter="hoverRows = row; hoverColumns = col"
                  @click="tableRows = row; tableColumns = col"
                ></div>
              </div>
            </div>
            <div class="size-display">
              {{ tableRows }} × {{ tableColumns }} 表格
            </div>
          </div>
          <div class="modal-actions">
            <button @click="showTableModal = false">取消</button>
            <button @click="insertTableConfirm" class="primary">插入</button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 全屏遮罩 -->
    <div v-if="fullscreen" class="fullscreen-overlay">
      <div class="fullscreen-header">
        <span>全屏编辑</span>
        <button @click="fullscreen = false" class="close-fullscreen">退出全屏</button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'RichTextEditor',
  props: {
    value: {
      type: String,
      default: ''
    },
    placeholder: {
      type: String,
      default: '请输入内容...'
    },
    showToolbar: {
      type: Boolean,
      default: true
    },
    showWordCount: {
      type: Boolean,
      default: true
    },
    maxLength: {
      type: Number,
      default: null
    },
    readonly: {
      type: Boolean,
      default: false
    },
    disabled: {
      type: Boolean,
      default: false
    },
    height: {
      type: [String, Number],
      default: '300px'
    },
    toolbarConfig: {
      type: Object,
      default: () => ({
        font: true,
        size: true,
        bold: true,
        italic: true,
        underline: true,
        strikethrough: true,
        align: true,
        list: true,
        indent: true,
        color: true,
        link: true,
        image: true,
        table: true,
        code: true,
        undo: true,
        source: true,
        fullscreen: true
      })
    }
  },
  data() {
    return {
      htmlSource: this.value,
      showSourceCode: false,
      fullscreen: false,
      wordCount: 0,
      formats: {
        bold: false,
        italic: false,
        underline: false,
        strikethrough: false,
        align: 'left'
      },
      fontFamily: '',
      fontSize: '',
      textColor: '#000000',
      backgroundColor: '#ffffff',
      fontSizes: ['12px', '14px', '16px', '18px', '24px', '32px', '48px'],
      canUndo: false,
      canRedo: false,
      
      // 链接模态框
      showLinkModal: false,
      linkUrl: '',
      linkText: '',
      
      // 图片模态框
      showImageModal: false,
      insertByUrl: false,
      imageUrl: '',
      imageAlt: '',
      imageWidth: '',
      
      // 表格模态框
      showTableModal: false,
      tableRows: 3,
      tableColumns: 3,
      hoverRows: 3,
      hoverColumns: 3,
      
      // 历史记录
      history: [],
      historyIndex: -1,
      maxHistoryLength: 100
    }
  },
  computed: {
    editorClasses() {
      return {
        'readonly': this.readonly,
        'disabled': this.disabled,
        'fullscreen': this.fullscreen,
        'has-error': this.maxLength && this.wordCount > this.maxLength
      }
    },
    
    progressPercentage() {
      if (!this.maxLength) return 0
      return Math.min((this.wordCount / this.maxLength) * 100, 100)
    }
  },
  watch: {
    value(newVal) {
      if (newVal !== this.htmlSource) {
        this.htmlSource = newVal
        if (!this.showSourceCode && this.$refs.editor) {
          this.$refs.editor.innerHTML = newVal
          this.updateWordCount()
          this.updateFormats()
        }
      }
    },
    
    htmlSource(newVal) {
      this.$emit('input', newVal)
      this.updateWordCount()
    }
  },
  mounted() {
    this.initEditor()
    
    // 初始化编辑器内容
    if (this.value && this.$refs.editor) {
      this.$refs.editor.innerHTML = this.value
    }
    
    this.updateWordCount()
    this.saveToHistory()
    
    // 监听键盘事件
    document.addEventListener('keydown', this.handleGlobalKeydown)
  },
  beforeDestroy() {
    document.removeEventListener('keydown', this.handleGlobalKeydown)
  },
  methods: {
    initEditor() {
      if (!this.$refs.editor) return
      
      // 设置编辑器样式
      const editor = this.$refs.editor
      editor.style.minHeight = typeof this.height === 'number' 
        ? this.height + 'px' 
        : this.height
      
      // 监听输入事件
      editor.addEventListener('input', this.handleInput)
      
      // 初始化格式
      this.updateFormats()
    },
    
    handleInput(event) {
      this.htmlSource = this.$refs.editor.innerHTML
      this.updateWordCount()
      this.updateFormats()
      this.saveToHistory()
      
      this.$emit('change', this.htmlSource)
    },
    
    handleFocus() {
      this.$emit('focus')
    },
    
    handleBlur() {
      this.$emit('blur')
    },
    
    handleKeydown(event) {
      // 处理Tab键缩进
      if (event.key === 'Tab') {
        event.preventDefault()
        document.execCommand('indent')
        this.handleInput(event)
      }
      
      // 处理Ctrl+Z/Y撤销重做
      if (event.ctrlKey || event.metaKey) {
        if (event.key === 'z' && !event.shiftKey) {
          event.preventDefault()
          this.undo()
        } else if ((event.key === 'y') || (event.key === 'z' && event.shiftKey)) {
          event.preventDefault()
          this.redo()
        }
      }
    },
    
    handleGlobalKeydown(event) {
      // 全屏模式下按ESC退出
      if (this.fullscreen && event.key === 'Escape') {
        this.fullscreen = false
      }
    },
    
    handlePaste(event) {
      event.preventDefault()
      
      // 获取粘贴的文本
      const text = event.clipboardData.getData('text/plain')
      
      // 如果是纯文本模式
      if (this.toolbarConfig.plainPaste) {
        document.execCommand('insertText', false, text)
      } else {
        // 尝试获取HTML内容
        let html = event.clipboardData.getData('text/html')
        
        if (html) {
          // 清理HTML（移除不需要的样式和属性）
          html = this.cleanPastedHTML(html)
          document.execCommand('insertHTML', false, html)
        } else {
          document.execCommand('insertText', false, text)
        }
      }
      
      this.handleInput(event)
    },
    
    cleanPastedHTML(html) {
      // 简单的HTML清理，移除style和class属性
      const div = document.createElement('div')
      div.innerHTML = html
      
      // 移除所有元素的style和class属性
      const elements = div.querySelectorAll('*')
      elements.forEach(el => {
        el.removeAttribute('style')
        el.removeAttribute('class')
      })
      
      return div.innerHTML
    },
    
    updateFromSource() {
      if (this.$refs.editor) {
        this.$refs.editor.innerHTML = this.htmlSource
        this.updateWordCount()
        this.updateFormats()
        this.saveToHistory()
      }
    },
    
    updateWordCount() {
      if (this.showSourceCode) {
        // 源代码模式下，统计纯文本字数
        const text = this.htmlSource.replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ').trim()
        this.wordCount = text.length
      } else if (this.$refs.editor) {
        // 可视化模式下，统计编辑器中的纯文本字数
        const text = this.$refs.editor.innerText || ''
        this.wordCount = text.length
      }
    },
    
    updateFormats() {
      if (!this.$refs.editor) return
      
      const selection = window.getSelection()
      if (!selection.rangeCount) return
      
      const range = selection.getRangeAt(0)
      const parentElement = range.commonAncestorContainer.parentElement
      
      // 获取当前格式
      this.formats.bold = document.queryCommandState('bold')
      this.formats.italic = document.queryCommandState('italic')
      this.formats.underline = document.queryCommandState('underline')
      this.formats.strikethrough = document.queryCommandState('strikethrough')
      
      // 获取对齐方式
      if (document.queryCommandState('justifyLeft')) {
        this.formats.align = 'left'
      } else if (document.queryCommandState('justifyCenter')) {
        this.formats.align = 'center'
      } else if (document.queryCommandState('justifyRight')) {
        this.formats.align = 'right'
      } else if (document.queryCommandState('justifyFull')) {
        this.formats.align = 'justify'
      } else {
        this.formats.align = 'left'
      }
      
      // 获取字体和字号
      if (parentElement) {
        this.fontFamily = parentElement.style.fontFamily || ''
        this.fontSize = parentElement.style.fontSize || ''
      }
    },
    
    formatText(command, value = null) {
      if (!this.$refs.editor) return
      
      this.$refs.editor.focus()
      
      if (value !== null) {
        document.execCommand(command, false, value)
      } else {
        document.execCommand(command, false, null)
      }
      
      this.handleInput()
      this.updateFormats()
    },
    
    insertLink() {
      const selection = window.getSelection()
      this.linkText = selection.toString()
      this.linkUrl = ''
      this.showLinkModal = true
    },
    
    insertLinkConfirm() {
      if (!this.linkUrl) return
      
      const html = `<a href="${this.linkUrl}" target="_blank">${this.linkText || this.linkUrl}</a>`
      document.execCommand('insertHTML', false, html)
      this.handleInput()
      this.insertLinkCancel()
    },
    
    insertLinkCancel() {
      this.showLinkModal = false
      this.linkUrl = ''
      this.linkText = ''
    },
    
    selectLocalImage() {
      this.$refs.imageInput.click()
    },
    
    handleImageUpload(event) {
      const file = event.target.files[0]
      if (!file) return
      
      const reader = new FileReader()
      reader.onload = (e) => {
        const img = new Image()
        img.onload = () => {
          const html = `<img src="${e.target.result}" alt="${file.name}" style="max-width: 100%; height: auto;">`
          document.execCommand('insertHTML', false, html)
          this.handleInput()
          this.showImageModal = false
        }
        img.src = e.target.result
      }
      reader.readAsDataURL(file)
      
      // 重置input
      event.target.value = ''
    },
    
    insertImage() {
      this.showImageModal = true
      this.insertByUrl = false
      this.imageUrl = ''
      this.imageAlt = ''
      this.imageWidth = ''
    },
    
    insertImageByUrl() {
      if (!this.imageUrl) return
      
      let style = 'max-width: 100%; height: auto;'
      if (this.imageWidth) {
        style = `width: ${this.imageWidth}px; height: auto;`
      }
      
      const html = `<img src="${this.imageUrl}" alt="${this.imageAlt || '图片'}" style="${style}">`
      document.execCommand('insertHTML', false, html)
      this.handleInput()
      this.showImageModal = false
    },
    
    insertImageCancel() {
      this.showImageModal = false
      this.insertByUrl = false
      this.imageUrl = ''
      this.imageAlt = ''
      this.imageWidth = ''
    },
    
    insertTable() {
      this.showTableModal = true
      this.tableRows = 3
      this.tableColumns = 3
      this.hoverRows = 3
      this.hoverColumns = 3
    },
    
    insertTableConfirm() {
      let html = '<table style="border-collapse: collapse; width: 100%;">'
      
      for (let i = 0; i < this.tableRows; i++) {
        html += '<tr>'
        for (let j = 0; j < this.tableColumns; j++) {
          html += `<td style="border: 1px solid #dcdfe6; padding: 8px;">&nbsp;</td>`
        }
        html += '</tr>'
      }
      
      html += '</table>'
      
      document.execCommand('insertHTML', false, html)
      this.handleInput()
      this.showTableModal = false
    },
    
    insertCode() {
      const html = `<pre><code>// 在这里输入代码</code></pre>`
      document.execCommand('insertHTML', false, html)
      this.handleInput()
    },
    
    undo() {
      if (this.historyIndex > 0) {
        this.historyIndex--
        const content = this.history[this.historyIndex]
        this.htmlSource = content
        if (this.$refs.editor) {
          this.$refs.editor.innerHTML = content
        }
        this.updateWordCount()
        this.updateHistoryButtons()
      }
    },
    
    redo() {
      if (this.historyIndex < this.history.length - 1) {
        this.historyIndex++
        const content = this.history[this.historyIndex]
        this.htmlSource = content
        if (this.$refs.editor) {
          this.$refs.editor.innerHTML = content
        }
        this.updateWordCount()
        this.updateHistoryButtons()
      }
    },
    
    saveToHistory() {
      const content = this.htmlSource
      
      // 如果内容没有变化，不保存历史记录
      if (this.history[this.historyIndex] === content) return
      
      // 移除当前索引之后的历史记录
      this.history = this.history.slice(0, this.historyIndex + 1)
      
      // 添加新记录
      this.history.push(content)
      this.historyIndex++
      
      // 限制历史记录长度
      if (this.history.length > this.maxHistoryLength) {
        this.history.shift()
        this.historyIndex--
      }
      
      this.updateHistoryButtons()
    },
    
    updateHistoryButtons() {
      this.canUndo = this.historyIndex > 0
      this.canRedo = this.historyIndex < this.history.length - 1
    },
    
    // 获取纯文本内容
    getPlainText() {
      if (this.$refs.editor) {
        return this.$refs.editor.innerText || ''
      }
      return this.htmlSource.replace(/<[^>]+>/g, ' ')
    },
    
    // 获取HTML内容
    getHTML() {
      return this.htmlSource
    },
    
    // 设置内容
    setContent(content) {
      this.htmlSource = content
      if (this.$refs.editor) {
        this.$refs.editor.innerHTML = content
      }
      this.updateWordCount()
      this.saveToHistory()
    },
    
    // 清空内容
    clear() {
      this.setContent('')
    },
    
    // 聚焦编辑器
    focus() {
      if (this.$refs.editor) {
        this.$refs.editor.focus()
      }
    }
  }
}
</script>

<style scoped lang="scss">
.rich-text-editor {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: white;
  transition: border-color 0.3s;
  
  &:hover {
    border-color: #c0c4cc;
  }
  
  &.has-error {
    border-color: #f56c6c;
  }
  
  &.readonly .editor-content,
  &.disabled .editor-content {
    background-color: #f5f7fa;
    cursor: not-allowed;
    color: #c0c4cc;
  }
  
  &.fullscreen {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 9999;
    border: none;
    border-radius: 0;
    
    .editor-content {
      height: calc(100vh - 120px) !important;
    }
  }
  
  .editor-toolbar {
    border-bottom: 1px solid #dcdfe6;
    background: #f8f9fa;
    padding: 10px;
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    align-items: center;
    
    .toolbar-group {
      display: flex;
      gap: 5px;
      align-items: center;
      padding-right: 10px;
      border-right: 1px solid #ebeef5;
      
      &:last-child {
        border-right: none;
      }
    }
    
    .toolbar-select {
      padding: 4px 8px;
      border: 1px solid #dcdfe6;
      border-radius: 4px;
      background: white;
      font-size: 14px;
      cursor: pointer;
      
      &:focus {
        outline: none;
        border-color: #409eff;
      }
    }
    
    .toolbar-btn {
      padding: 6px 10px;
      border: 1px solid #dcdfe6;
      border-radius: 4px;
      background: white;
      cursor: pointer;
      font-size: 14px;
      transition: all 0.3s;
      min-width: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
      
      &:hover {
        background: #ecf5ff;
        border-color: #c6e2ff;
      }
      
      &.active {
        background: #409eff;
        color: white;
        border-color: #409eff;
      }
      
      &:disabled {
        opacity: 0.5;
        cursor: not-allowed;
        
        &:hover {
          background: white;
          border-color: #dcdfe6;
        }
      }
    }
    
    .color-picker {
      width: 32px;
      height: 32px;
      border: 1px solid #dcdfe6;
      border-radius: 4px;
      cursor: pointer;
      padding: 2px;
      
      &::-webkit-color-swatch-wrapper {
        padding: 0;
      }
      
      &::-webkit-color-swatch {
        border: none;
        border-radius: 2px;
      }
    }
  }
  
  .editor-container {
    position: relative;
    
    .source-editor {
      width: 100%;
      height: 300px;
      padding: 15px;
      border: none;
      resize: vertical;
      font-family: 'Consolas', 'Monaco', monospace;
      font-size: 14px;
      line-height: 1.6;
      background: #f8f9fa;
      
      &:focus {
        outline: none;
      }
    }
    
    .editor-content {
      min-height: 300px;
      padding: 15px;
      outline: none;
      overflow-y: auto;
      line-height: 1.6;
      
      &:empty::before {
        content: attr(placeholder);
        color: #c0c4cc;
      }
      
      &::after {
        content: '';
        display: block;
        height: 1px;
      }
      
      // 编辑区域内容样式
      h1, h2, h3, h4, h5, h6 {
        margin-top: 1em;
        margin-bottom: 0.5em;
        font-weight: bold;
      }
      
      h1 { font-size: 2em; }
      h2 { font-size: 1.5em; }
      h3 { font-size: 1.17em; }
      h4 { font-size: 1em; }
      h5 { font-size: 0.83em; }
      h6 { font-size: 0.67em; }
      
      p {
        margin: 0 0 10px;
      }
      
      ul, ol {
        margin: 10px 0;
        padding-left: 30px;
      }
      
      blockquote {
        border-left: 4px solid #ebeef5;
        margin: 10px 0;
        padding-left: 16px;
        color: #606266;
      }
      
      code {
        background-color: #f5f7fa;
        padding: 2px 4px;
        border-radius: 3px;
        font-family: 'Consolas', 'Monaco', monospace;
        font-size: 0.9em;
      }
      
      pre {
        background-color: #f5f7fa;
        padding: 10px;
        border-radius: 4px;
        overflow-x: auto;
        
        code {
          background-color: transparent;
          padding: 0;
        }
      }
      
      a {
        color: #409eff;
        text-decoration: none;
        
        &:hover {
          text-decoration: underline;
        }
      }
      
      img {
        max-width: 100%;
        height: auto;
      }
      
      table {
        width: 100%;
        border-collapse: collapse;
        margin: 10px 0;
        
        th, td {
          border: 1px solid #dcdfe6;
          padding: 8px;
          text-align: left;
        }
        
        th {
          background-color: #f5f7fa;
          font-weight: bold;
        }
      }
    }
    
    .word-count {
      position: absolute;
      bottom: 10px;
      right: 15px;
      font-size: 12px;
      color: #909399;
      
      .word-progress {
        width: 100px;
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
  
  .editor-modal {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1000;
    
    .modal-content {
      background: white;
      border-radius: 8px;
      padding: 20px;
      min-width: 400px;
      max-width: 600px;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
      
      h3 {
        margin: 0 0 20px 0;
        color: #303133;
        font-size: 18px;
      }
      
      .form-group {
        margin-bottom: 15px;
        
        label {
          display: block;
          margin-bottom: 5px;
          color: #606266;
          font-size: 14px;
        }
        
        input {
          width: 100%;
          padding: 8px 12px;
          border: 1px solid #dcdfe6;
          border-radius: 4px;
          font-size: 14px;
          
          &:focus {
            outline: none;
            border-color: #409eff;
          }
        }
      }
      
      .image-upload-options {
        display: flex;
        gap: 20px;
        margin: 20px 0;
        
        .upload-option {
          flex: 1;
          padding: 20px;
          border: 2px dashed #dcdfe6;
          border-radius: 8px;
          text-align: center;
          cursor: pointer;
          transition: all 0.3s;
          
          &:hover {
            border-color: #409eff;
            background: #ecf5ff;
          }
          
          .option-icon {
            font-size: 32px;
            margin-bottom: 10px;
          }
          
          .option-text {
            font-size: 14px;
            color: #606266;
          }
        }
      }
      
      .table-size-selector {
        .size-label {
          margin-bottom: 10px;
          color: #606266;
        }
        
        .table-grid {
          display: inline-block;
          border: 1px solid #dcdfe6;
          border-radius: 4px;
          overflow: hidden;
          
          .table-grid-row {
            display: flex;
            
            .table-grid-cell {
              width: 20px;
              height: 20px;
              border: 1px solid #f0f0f0;
              background: white;
              cursor: pointer;
              
              &.selected {
                background: #409eff;
                border-color: #409eff;
              }
              
              &.hover {
                background: #ecf5ff;
              }
            }
          }
        }
        
        .size-display {
          margin-top: 10px;
          font-size: 14px;
          color: #303133;
        }
      }
      
      .modal-actions {
        display: flex;
        justify-content: flex-end;
        gap: 10px;
        margin-top: 20px;
        
        button {
          padding: 8px 16px;
          border: 1px solid #dcdfe6;
          border-radius: 4px;
          background: white;
          cursor: pointer;
          font-size: 14px;
          transition: all 0.3s;
          
          &:hover {
            background: #f5f7fa;
          }
          
          &.primary {
            background: #409eff;
            color: white;
            border-color: #409eff;
            
            &:hover {
              background: #66b1ff;
            }
          }
        }
      }
    }
  }
  
  .fullscreen-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    height: 40px;
    background: #303133;
    color: white;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 20px;
    z-index: 10000;
    
    .close-fullscreen {
      background: #f56c6c;
      color: white;
      border: none;
      padding: 6px 16px;
      border-radius: 4px;
      cursor: pointer;
      font-size: 14px;
      
      &:hover {
        background: #e65c5c;
      }
    }
  }
}
</style>