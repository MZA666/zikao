<template>
  <div class="post-management-container">
    <div class="header">
      <h2>帖子管理</h2>
    </div>

    <!-- 查询区域 -->
    <div class="query-section">
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="帖子标题">
          <el-input 
            v-model="queryForm.title" 
            placeholder="请输入帖子标题" 
            clearable
            @keyup.enter="loadPosts"
          ></el-input>
        </el-form-item>
        <el-form-item label="发帖时间">
          <el-date-picker
            v-model="queryForm.timeRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          ></el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadPosts">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 帖子列表 -->
    <div class="post-list">
      <el-table :data="posts" style="width: 100%" v-loading="loading">
        <el-table-column prop="title" label="帖子标题" min-width="200" show-overflow-tooltip></el-table-column>
        <el-table-column prop="username" label="发帖人" width="120"></el-table-column>
        <el-table-column prop="createTime" label="发帖时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="置顶" width="80">
          <template #default="scope">
            <el-tag v-if="scope.row.isTop === 1" type="warning">是</el-tag>
            <el-tag v-else type="info">否</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="previewPost(scope.row)">预览</el-button>
            <el-button 
              size="small" 
              :type="scope.row.isTop === 1 ? 'info' : 'warning'" 
              @click="toggleTop(scope.row)"
            >
              {{ scope.row.isTop === 1 ? '取消置顶' : '置顶' }}
            </el-button>
            <el-button 
              size="small" 
              type="danger" 
              @click="offlinePost(scope.row)"
            >
              下架
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
        />
      </div>
    </div>

    <!-- 预览帖子对话框 -->
    <el-dialog title="帖子预览" v-model="previewDialogVisible" width="80%" top="5vh">
      <div class="preview-content">
        <h3>{{ previewPostData.title }}</h3>
        <div class="preview-meta">
          <span>发帖人：{{ previewPostData.username }}</span>
          <span>发帖时间：{{ formatDate(previewPostData.createTime) }}</span>
          <span>状态：<el-tag :type="getStatusType(previewPostData.status)">{{ getStatusText(previewPostData.status) }}</el-tag></span>
          <span>置顶：<el-tag v-if="previewPostData.isTop === 1" type="warning">是</el-tag><el-tag v-else type="info">否</el-tag></span>
        </div>
        <div class="preview-body" v-html="previewPostData.content"></div>
      </div>
      <template #footer>
        <el-button @click="previewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 下架确认对话框 -->
    <el-dialog title="下架帖子" v-model="offlineDialogVisible" width="500px">
      <el-form :model="offlineForm" :rules="offlineRules" ref="offlineFormRef">
        <el-form-item label="下架原因" prop="reason">
          <el-input 
            v-model="offlineForm.reason" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入下架原因"
            maxlength="500"
            show-word-limit
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="offlineDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmOffline">确认下架</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

export default {
  name: 'PostManagement',
  data() {
    return {
      loading: false,
      posts: [],
      total: 0,
      currentPage: 1,
      pageSize: 10,
      queryForm: {
        title: '',
        timeRange: []
      },
      previewDialogVisible: false,
      previewPostData: {},
      offlineDialogVisible: false,
      offlineForm: {
        postId: null,
        reason: ''
      },
      offlineRules: {
        reason: [
          { required: true, message: '请输入下架原因', trigger: 'blur' },
          { min: 1, max: 500, message: '下架原因长度在1到500个字符之间', trigger: 'blur' }
        ]
      }
    }
  },
  mounted() {
    this.loadPosts()
  },
  methods: {
    // 加载帖子列表
    async loadPosts() {
      this.loading = true
      try {
        const params = {
          pageNum: this.currentPage,
          pageSize: this.pageSize,
          title: this.queryForm.title || undefined,
          startTime: this.queryForm.timeRange && this.queryForm.timeRange[0] ? this.queryForm.timeRange[0] : undefined,
          endTime: this.queryForm.timeRange && this.queryForm.timeRange[1] ? this.queryForm.timeRange[1] : undefined
        }
        
        const response = await request.get('/post/manage/page', { params })
        this.posts = response.data.list || []
        this.total = response.data.total || 0
      } catch (error) {
        console.error('加载帖子列表失败:', error)
        ElMessage.error('加载帖子列表失败')
      } finally {
        this.loading = false
      }
    },

    // 重置查询
    resetQuery() {
      this.queryForm = {
        title: '',
        timeRange: []
      }
      this.currentPage = 1
      this.loadPosts()
    },

    // 分页大小改变
    handleSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
      this.loadPosts()
    },

    // 当前页改变
    handleCurrentChange(page) {
      this.currentPage = page
      this.loadPosts()
    },

    // 格式化日期
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN') + ' ' + date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    },

    // 获取状态文本
    getStatusText(status) {
      switch (status) {
        case 0: return '已保存'
        case 1: return '已发布'
        case 2: return '已下架'
        default: return '未知'
      }
    },

    // 获取状态类型
    getStatusType(status) {
      switch (status) {
        case 0: return 'info'
        case 1: return 'success'
        case 2: return 'danger'
        default: return 'info'
      }
    },

    // 预览帖子
    previewPost(post) {
      this.previewPostData = { ...post }
      this.previewDialogVisible = true
    },

    // 切换置顶状态
    async toggleTop(post) {
      try {
        const isTop = post.isTop === 1 ? 0 : 1
        await request.put(`/post/top/${post.id}?isTop=${isTop}`)
        ElMessage.success(post.isTop === 1 ? '取消置顶成功' : '置顶成功')
        // 更新本地数据
        post.isTop = isTop
      } catch (error) {
        console.error('操作失败:', error)
        ElMessage.error(post.isTop === 1 ? '取消置顶失败' : '置顶失败')
      }
    },

    // 下架帖子
    offlinePost(post) {
      this.offlineForm.postId = post.id
      this.offlineForm.reason = ''
      this.offlineDialogVisible = true
    },

    // 确认下架
    async confirmOffline() {
      try {
        await this.$refs.offlineFormRef.validate()
        
        await request.put(`/post/offline/${this.offlineForm.postId}?offlineReason=${encodeURIComponent(this.offlineForm.reason)}`)
        
        ElMessage.success('帖子下架成功')
        this.offlineDialogVisible = false
        this.loadPosts()
      } catch (error) {
        console.error('下架帖子失败:', error)
        ElMessage.error('下架帖子失败')
      }
    }
  }
}
</script>

<style scoped>
.post-management-container {
  padding: 20px;
}

.header {
  margin-bottom: 20px;
}

.query-section {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.query-form {
  margin-bottom: 0;
}

.post-list {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.pagination {
  margin-top: 20px;
  text-align: center;
}

.preview-meta {
  display: flex;
  gap: 20px;
  margin: 15px 0;
  flex-wrap: wrap;
}

.preview-body {
  margin-top: 20px;
  padding: 15px;
  border: 1px solid #eee;
  border-radius: 4px;
  min-height: 200px;
}
</style>