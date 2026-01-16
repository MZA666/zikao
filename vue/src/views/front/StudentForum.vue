<template>
  <div class="student-forum-container">
    <h3>学员论坛</h3>
    
    <!-- 选项卡 -->
    <el-tabs v-model="activeTab" class="forum-tabs" @tab-click="onTabChange">
      <el-tab-pane label="交流论坛" name="discussion"></el-tab-pane>
      <el-tab-pane label="我的发帖" name="my-posts"></el-tab-pane>
    </el-tabs>
    
    <!-- 发帖按钮 -->
    <div class="post-actions" v-if="activeTab === 'discussion'">
      <el-button type="primary" @click="showPostDialog = true">新增发帖</el-button>
    </div>
    
    <!-- 交流论坛内容 -->
    <div v-if="activeTab === 'discussion'" class="discussion-content">
      <div v-for="post in discussionPosts" :key="post.id" class="post-item">
        <div class="post-header">
          <div class="post-title">
            <el-link @click="viewPostDetail(post)" type="primary" :underline="false">{{ post.title }}</el-link>
          </div>
          <div class="post-meta">
            <span class="post-author">{{ post.username }}</span>
            <span class="post-time">{{ formatDate(post.createTime) }}</span>
            <span v-if="post.isTop === 1" class="post-top">置顶</span>
          </div>
        </div>
        <div class="post-summary" v-if="post.content" v-html="formatContent(post.content)"></div>
      </div>
      
      <div v-if="discussionPosts.length === 0" class="empty-state">
        暂无帖子，快来发布第一篇吧！
      </div>
    </div>
    
    <!-- 我的发帖内容 -->
    <div v-if="activeTab === 'my-posts'" class="my-posts-content">
      <div v-for="post in myPosts" :key="post.id" class="post-item">
        <div class="post-header">
          <div class="post-title">
            <el-link @click="viewPostDetail(post)" type="primary" :underline="false">{{ post.title }}</el-link>
          </div>
          <div class="post-meta">
            <span class="post-status" :class="getStatusClass(post.status)">{{ getStatusText(post.status) }}</span>
            <span class="post-time">{{ formatDate(post.createTime) }}</span>
          </div>
        </div>
        <div class="post-summary" v-if="post.content" v-html="formatContent(post.content)"></div>
        <div v-if="post.status === 2 && post.offlineReason" class="offline-reason">
          <el-tag type="danger">下架原因：{{ post.offlineReason }}</el-tag>
        </div>
      </div>
      
      <div v-if="myPosts.length === 0" class="empty-state">
        暂无发帖记录
      </div>
    </div>
    
    <!-- 发帖弹窗 -->
    <el-dialog v-model="showPostDialog" title="发表帖子" width="80%" top="5vh">
      <el-form :model="postForm" :rules="postRules" ref="postFormRef" label-width="80px">
        <el-form-item label="帖子标题" prop="title">
          <el-input v-model="postForm.title" placeholder="请输入帖子标题" maxlength="255"></el-input>
        </el-form-item>
        <el-form-item label="帖子内容" prop="content">
          <div style="border: 1px solid #ccc; border-radius: 4px;">
            <Toolbar
              style="border-bottom: 1px solid #ccc"
              :editor="editorRef"
              :defaultConfig="toolbarConfig"
            />
            <Editor
              v-model="postForm.content"
              :defaultConfig="editorConfig"
              :style="{ height: '300px', overflowY: 'hidden' }"
              @onCreated="handleCreated"
            />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="saveDraft">保存草稿</el-button>
        <el-button type="primary" @click="publishPost">发布帖子</el-button>
        <el-button @click="cancelPost">取消</el-button>
      </template>
    </el-dialog>
    
    <!-- 帖子详情对话框 -->
    <el-dialog v-model="showPostDetailDialog" title="帖子详情" width="80%" top="5vh">
      <div class="post-detail-content">
        <h3>{{ currentPostDetail.title }}</h3>
        <div class="post-detail-meta">
          <span>作者：{{ currentPostDetail.username }}</span>
          <span>发布时间：{{ formatDate(currentPostDetail.createTime) }}</span>
          <span v-if="currentPostDetail.isTop === 1" class="post-top">置顶</span>
        </div>
        <div class="post-detail-body" v-html="currentPostDetail.content"></div>
      </div>
      <template #footer>
        <el-button @click="showPostDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, shallowRef, onMounted } from 'vue'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'
import '@wangeditor/editor/dist/css/style.css'
import request from '@/utils/request'
import { ElMessage, ElLink } from 'element-plus'

// 状态变量
const activeTab = ref('discussion')
const discussionPosts = ref([])
const myPosts = ref([])
const showPostDialog = ref(false)
const showPostDetailDialog = ref(false)
const currentPostDetail = ref({})
const editorRef = shallowRef()

// 发帖表单
const postForm = reactive({
  id: null,
  title: '',
  content: '',
  userId: null,
  username: '',
  status: 0 // 0-已保存，1-已发布，2-下架
})

// 表单验证规则
const postRules = {
  title: [
    { required: true, message: '请输入帖子标题', trigger: 'blur' },
    { min: 1, max: 100, message: '标题长度在1到100个字符之间', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入帖子内容', trigger: 'blur' }
  ]
}

// 编辑器配置
const toolbarConfig = {}
const editorConfig = {
  placeholder: '请输入帖子内容...',
  MENU_CONF: {}
}

// 获取当前用户信息
const getCurrentUserInfo = () => {
  console.log('开始获取用户信息...');
  console.log('当前localStorage中的所有项:');
  for (let i = 0; i < localStorage.length; i++) {
    const key = localStorage.key(i);
    const value = localStorage.getItem(key);
    console.log(`  ${key}: ${value}`);
  }
  
  // 检查所有可能的存储键
  const userKey = localStorage.getItem('user');
  console.log('localStorage user key:', userKey);
  
  const systemUserKey = localStorage.getItem('system-user');
  console.log('localStorage system-user key:', systemUserKey);
  
  const userIdKey = localStorage.getItem('userId');
  console.log('localStorage userId key:', userIdKey);
  
  try {
    // 尝试从多种可能的存储键中获取用户信息
    let userId = localStorage.getItem('userId');
    console.log('尝试从userId获取:', userId);
    
    if (!userId) {
      const systemUserStr = localStorage.getItem('system-user');
      console.log('尝试从system-user获取:', systemUserStr);
      if (systemUserStr) {
        const systemUser = JSON.parse(systemUserStr);
        console.log('解析system-user对象:', systemUser);
        userId = systemUser.userId || systemUser.user_id;
        console.log('从system-user中获取的userId:', userId);
      }
    }
    
    if (userId) {
      const systemUserStr = localStorage.getItem('system-user');
      const systemUser = systemUserStr ? JSON.parse(systemUserStr) : {};
      
      console.log('最终获取的用户信息:', {
        user_id: userId,
        name: systemUser.name || systemUser.username,
        username: systemUser.username || systemUser.name,
        roleId: systemUser.roleId
      });
      
      // 确保返回统一格式的用户信息
      return {
        user_id: userId,
        name: systemUser.name || systemUser.username,
        username: systemUser.username || systemUser.name,
        roleId: systemUser.roleId
      };
    }
    console.log('未能获取用户信息，返回null');
    return null;
  } catch (error) {
    console.error('获取用户信息失败:', error);
    return null;
  }
}

// 组件创建完成回调
const handleCreated = (editor) => {
  editorRef.value = editor
}

// 格式化内容，截取前100个字符作为摘要
const formatContent = (content) => {
  if (!content) return ''
  
  // 移除HTML标签并截取内容
  const div = document.createElement('div')
  div.innerHTML = content
  const plainText = div.textContent || div.innerText || ''
  
  // 如果内容超过100个字符，则截取前100个字符并添加省略号
  if (plainText.length > 100) {
    return plainText.substring(0, 100) + '...'
  }
  return plainText
}

// 验证内容是否为空
const validateContent = (content) => {
  if (!content) return false
  
  // 移除HTML标签并检查纯文本内容
  const div = document.createElement('div')
  div.innerHTML = content
  const plainText = div.textContent || div.innerText || ''
  
  // 去除空格和换行符后检查是否为空
  return plainText.trim().length > 0
}

// 查看帖子详情
const viewPostDetail = (post) => {
  currentPostDetail.value = post
  showPostDetailDialog.value = true
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN') + ' ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 0: return '已保存'
    case 1: return '已发布'
    case 2: return '已下架'
    default: return '未知'
  }
}

// 获取状态样式类
const getStatusClass = (status) => {
  switch (status) {
    case 0: return 'status-draft'
    case 1: return 'status-published'
    case 2: return 'status-offline'
    default: return ''
  }
}

// 加载交流论坛帖子
const loadDiscussionPosts = async () => {
  try {
    const response = await request.get('/post/published')
    discussionPosts.value = response.data || []
  } catch (error) {
    console.error('加载交流论坛帖子失败:', error)
  }
}

// 加载我的帖子
const loadMyPosts = async () => {
  try {
    const userInfo = getCurrentUserInfo()
    if (!userInfo || !userInfo.user_id) {
      console.error('未找到用户信息')
      return
    }
    
    const response = await request.get('/post/my-posts', {
      params: { userId: userInfo.user_id }
    })
    myPosts.value = response.data || []
  } catch (error) {
    console.error('加载我的帖子失败:', error)
  }
}

// 保存草稿
const saveDraft = async () => {
  try {
    const userInfo = getCurrentUserInfo()
    if (!userInfo) {
      ElMessage.error('请先登录')
      return
    }
    
    // 验证表单
    if (!postForm.title.trim()) {
      ElMessage.error('请输入帖子标题')
      return
    }
    
    if (!validateContent(postForm.content)) {
      ElMessage.error('请输入帖子内容')
      return
    }
    
    // 设置用户信息
    postForm.userId = userInfo.user_id
    postForm.username = userInfo.name || userInfo.username
    
    // 设置为已保存状态
    postForm.status = 0
    
    if (postForm.id) {
      // 更新现有帖子
      await request.put('/post', postForm)
    } else {
      // 创建新帖子
      await request.post('/post', postForm)
    }
    
    ElMessage.success('草稿保存成功')
    showPostDialog.value = false
    resetPostForm()
    
    // 重新加载我的帖子
    if (activeTab.value === 'my-posts') {
      await loadMyPosts()
    }
  } catch (error) {
    console.error('保存草稿失败:', error)
    ElMessage.error('保存草稿失败')
  }
}

// 发布帖子
const publishPost = async () => {
  try {
    const userInfo = getCurrentUserInfo()
    if (!userInfo) {
      ElMessage.error('请先登录')
      return
    }
    
    // 验证表单
    if (!postForm.title.trim()) {
      ElMessage.error('请输入帖子标题')
      return
    }
    
    if (!postForm.content.trim()) {
      ElMessage.error('请输入帖子内容')
      return
    }
    
    // 设置用户信息
    postForm.userId = userInfo.user_id
    postForm.username = userInfo.name || userInfo.username
    
    // 设置为已发布状态
    postForm.status = 1
    
    if (postForm.id) {
      // 更新现有帖子
      await request.put('/post', postForm)
    } else {
      // 创建新帖子
      await request.post('/post', postForm)
    }
    
    ElMessage.success('帖子发布成功')
    showPostDialog.value = false
    resetPostForm()
    
    // 重新加载帖子
    await loadDiscussionPosts()
    if (activeTab.value === 'my-posts') {
      await loadMyPosts()
    }
  } catch (error) {
    console.error('发布帖子失败:', error)
    ElMessage.error('发布帖子失败')
  }
}

// 取消发帖
const cancelPost = () => {
  showPostDialog.value = false
  resetPostForm()
}

// 重置发帖表单
const resetPostForm = () => {
  postForm.id = null
  postForm.title = ''
  postForm.content = ''
  postForm.userId = null
  postForm.username = ''
  postForm.status = 0
}

// 监听选项卡切换
const onTabChange = async () => {
  if (activeTab.value === 'discussion') {
    await loadDiscussionPosts()
  } else if (activeTab.value === 'my-posts') {
    await loadMyPosts()
  }
}

// 初始化
onMounted(async () => {
  console.log('StudentForum组件挂载，开始初始化');
  // 加载初始数据
  await loadDiscussionPosts()
  const userInfo = getCurrentUserInfo()
  console.log('初始化时获取到的用户信息:', userInfo);
  
  if (userInfo && userInfo.user_id) {
    console.log('用户已登录，加载我的帖子');
    await loadMyPosts()
  } else {
    console.log('用户未登录，跳过加载我的帖子');
  }
})
</script>

<style scoped>
.student-forum-container {
  padding: 20px 0;
  max-width: 1000px;
  margin: 0 auto;
}

.forum-tabs {
  margin-bottom: 20px;
}

.post-actions {
  margin-bottom: 20px;
  text-align: right;
}

.post-item {
  background: white;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 15px;
  margin-bottom: 15px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  cursor: pointer;
}

.post-item:hover {
  box-shadow: 0 4px 8px rgba(0,0,0,0.15);
}

.post-header {
  margin-bottom: 10px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.post-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
  cursor: pointer;
}

.post-title:hover {
  color: #409EFF;
}

.post-meta {
  display: flex;
  align-items: center;
  gap: 15px;
  font-size: 12px;
  color: #909399;
}

.post-author {
  color: #606266;
}

.post-time {
  color: #909399;
}

.post-top {
  color: #e6a23c;
  background: #fdf6ec;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 12px;
}

.post-content {
  color: #606266;
  line-height: 1.6;
  cursor: pointer;
}

.post-content:hover {
  opacity: 0.9;
}

.post-summary {
  color: #606266;
  line-height: 1.6;
  font-size: 14px;
  cursor: pointer;
}

.post-summary:hover {
  opacity: 0.9;
}

.offline-reason {
  margin-top: 10px;
}

.status-draft {
  color: #909399;
}

.status-published {
  color: #67c23a;
}

.status-offline {
  color: #f56c6c;
}

.empty-state {
  text-align: center;
  padding: 40px;
  color: #909399;
  font-size: 14px;
}

.post-detail-content h3 {
  margin: 0 0 15px 0;
  color: #303133;
}

.post-detail-meta {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  font-size: 14px;
}

.post-detail-body {
  padding: 15px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  min-height: 200px;
  line-height: 1.6;
}

:deep(.w-e-toolbar) {
  border: none !important;
  border-bottom: 1px solid #ccc !important;
}

:deep(.w-e-text-container) {
  border: none !important;
  border-top: 1px solid #ccc !important;
}
</style>