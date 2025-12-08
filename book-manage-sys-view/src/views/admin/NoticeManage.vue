<template>
  <div class="notice-management">
    <div class="page-header">
      <h2>📢 通知管理</h2>
      <div class="header-actions">
        <button @click="createNotice" class="btn-primary">+ 创建通知</button>
      </div>
    </div>

    <!-- 通知列表 -->
    <div class="notice-list">
      <div v-for="notice in notices" :key="notice.id" class="notice-item">
        <div class="notice-header">
          <h3>{{ notice.title }}</h3>
          <span class="notice-type" :class="notice.type">
            {{ notice.type }}
          </span>
        </div>
        
        <p class="notice-content">{{ notice.content }}</p>
        
        <div class="notice-footer">
          <span class="notice-author">发布者：{{ notice.author }}</span>
          <span class="notice-time">{{ notice.time }}</span>
          <div class="notice-actions">
            <button @click="editNotice(notice)" class="btn-edit">编辑</button>
            <button @click="deleteNotice(notice)" class="btn-delete">删除</button>
            <button 
              @click="toggleStatus(notice)" 
              :class="notice.status === 'published' ? 'btn-unpublish' : 'btn-publish'"
            >
              {{ notice.status === 'published' ? '取消发布' : '发布' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 创建/编辑通知模态框 -->
    <div v-if="showModal" class="modal-overlay" @click="closeModal">
      <div class="modal-content" @click.stop>
        <h3>{{ editingNotice ? '编辑通知' : '创建通知' }}</h3>
        
        <form @submit.prevent="saveNotice">
          <div class="form-group">
            <label>标题</label>
            <input type="text" v-model="formData.title" required>
          </div>
          
          <div class="form-group">
            <label>内容</label>
            <textarea v-model="formData.content" rows="5" required></textarea>
          </div>
          
          <div class="form-group">
            <label>类型</label>
            <select v-model="formData.type">
              <option value="normal">普通通知</option>
              <option value="important">重要通知</option>
              <option value="urgent">紧急通知</option>
            </select>
          </div>
          
          <div class="form-group">
            <label>状态</label>
            <select v-model="formData.status">
              <option value="draft">草稿</option>
              <option value="published">已发布</option>
            </select>
          </div>
          
          <div class="form-actions">
            <button type="button" @click="closeModal" class="btn-cancel">取消</button>
            <button type="submit" class="btn-save">保存</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

// 数据
const notices = ref([])
const showModal = ref(false)
const editingNotice = ref(null)

// 表单数据
const formData = ref({
  id: '',
  title: '',
  content: '',
  type: 'normal',
  status: 'draft'
})

// 方法
const loadNotices = () => {
  // 模拟数据
  notices.value = [
    { 
      id: 1, 
      title: '系统维护通知', 
      content: '将于今晚24:00进行系统维护，预计持续2小时。', 
      type: 'important', 
      author: '管理员', 
      time: '2024-01-15 10:30',
      status: 'published'
    },
    { 
      id: 2, 
      title: '新功能上线', 
      content: '系统新增用户管理功能，欢迎使用。', 
      type: 'normal', 
      author: '技术部', 
      time: '2024-01-14 14:20',
      status: 'draft'
    }
  ]
}

const createNotice = () => {
  resetForm()
  showModal.value = true
}

const editNotice = (notice) => {
  editingNotice.value = notice
  formData.value = { ...notice }
  showModal.value = true
}

const deleteNotice = (notice) => {
  if (confirm(`确定要删除通知 "${notice.title}" 吗？`)) {
    notices.value = notices.value.filter(n => n.id !== notice.id)
  }
}

const toggleStatus = (notice) => {
  notice.status = notice.status === 'published' ? 'draft' : 'published'
}

const saveNotice = () => {
  if (editingNotice.value) {
    // 更新通知
    const index = notices.value.findIndex(n => n.id === editingNotice.value.id)
    notices.value[index] = { 
      ...formData.value,
      author: '管理员',
      time: new Date().toLocaleDateString()
    }
  } else {
    // 创建通知
    const newNotice = {
      ...formData.value,
      id: Date.now(),
      author: '管理员',
      time: new Date().toLocaleDateString()
    }
    notices.value.push(newNotice)
  }
  
  closeModal()
}

const resetForm = () => {
  formData.value = {
    id: '',
    title: '',
    content: '',
    type: 'normal',
    status: 'draft'
  }
  editingNotice.value = null
}

const closeModal = () => {
  showModal.value = false
  resetForm()
}

// 生命周期
onMounted(() => {
  loadNotices()
})
</script>

<style scoped>
.notice-management {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.notice-item {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}

.notice-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.notice-header h3 {
  margin: 0;
  color: #333;
}

.notice-type {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
}

.notice-type.normal {
  background: #e3f2fd;
  color: #1976d2;
}

.notice-type.important {
  background: #fff3e0;
  color: #f57c00;
}

.notice-type.urgent {
  background: #ffebee;
  color: #d32f2f;
}

.notice-content {
  color: #666;
  margin: 10px 0;
  line-height: 1.6;
}

.notice-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #eee;
  color: #999;
  font-size: 14px;
}

.notice-actions {
  display: flex;
  gap: 8px;
}

.btn-edit, .btn-delete, .btn-publish, .btn-unpublish {
  padding: 4px 8px;
  border: none;
  border-radius: 3px;
  cursor: pointer;
  font-size: 12px;
}

.btn-edit {
  background: #2196f3;
  color: white;
}

.btn-delete {
  background: #f44336;
  color: white;
}

.btn-publish {
  background: #4caf50;
  color: white;
}

.btn-unpublish {
  background: #ff9800;
  color: white;
}

/* 模态框样式（复用之前的样式，可适当调整） */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 30px;
  border-radius: 10px;
  min-width: 500px;
  max-width: 600px;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  color: #333;
  font-weight: bold;
}

.form-group input,
.form-group textarea,
.form-group select {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.form-group textarea {
  resize: vertical;
  min-height: 100px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.btn-cancel, .btn-save {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-cancel {
  background: #f5f5f5;
  color: #333;
}

.btn-cancel:hover {
  background: #e0e0e0;
}

.btn-save {
  background: #4caf50;
  color: white;
}

.btn-save:hover {
  background: #45a049;
}
</style>