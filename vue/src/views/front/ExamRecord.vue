<template>
  <div class="exam-record-container">
    <div class="header">
      <h2>考试记录</h2>
    </div>

    <!-- 记录列表 -->
    <div class="record-list">
      <el-card
        v-for="record in examRecords"
        :key="record.id"
        class="record-card"
        shadow="hover"
      >
        <div class="record-header">
          <h3>{{ record.paperName || '模拟考试' }}</h3>
          <span class="record-date">{{ formatDate(record.examTime) }}</span>
        </div>
        <div class="record-details">
          <div class="detail-item">
            <span class="label">学科：</span>
            <span class="value">{{ record.subjectName }}</span>
          </div>
          <div class="detail-item">
            <span class="label">得分：</span>
            <span class="value score">{{ record.score }} / {{ record.totalScore }}</span>
          </div>
          <div class="detail-item">
            <span class="label">正确率：</span>
            <span class="value accuracy">{{ record.accuracy }}%</span>
          </div>
          <div class="detail-item">
            <span class="label">用时：</span>
            <span class="value">{{ record.duration }}分钟</span>
          </div>
        </div>
        <div class="record-actions">
          <el-button size="small" @click="viewRecord(record)">查看详情</el-button>
          <el-button size="small" type="primary" @click="reviewExam(record)">回顾考试</el-button>
        </div>
      </el-card>
    </div>

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

    <!-- 查看详情对话框 -->
    <el-dialog
      title="考试详情"
      v-model="detailDialogVisible"
      width="80%"
      :before-close="closeDetailDialog"
    >
      <div v-if="selectedRecord" class="detail-content">
        <h3>{{ selectedRecord.paperName }}</h3>
        <div class="summary">
          <p><strong>考试时间：</strong>{{ formatDate(selectedRecord.examTime) }}</p>
          <p><strong>学科：</strong>{{ selectedRecord.subjectName }}</p>
          <p><strong>得分：</strong>{{ selectedRecord.score }} / {{ selectedRecord.totalScore }}</p>
          <p><strong>正确率：</strong>{{ selectedRecord.accuracy }}%</p>
          <p><strong>用时：</strong>{{ selectedRecord.duration }}分钟</p>
        </div>
        
        <div class="answers-section" v-if="selectedRecord.answers && selectedRecord.answers.length > 0">
          <h4>答题详情</h4>
          <div
            v-for="(answer, index) in selectedRecord.answers"
            :key="index"
            class="answer-item"
          >
            <div class="question-info">
              <span class="question-num">{{ index + 1 }}.</span>
              <span class="question-content">{{ answer.questionContent }}</span>
            </div>
            <div class="answer-detail">
              <p><strong>我的答案：</strong>{{ answer.userAnswer || '未作答' }}</p>
              <p><strong>正确答案：</strong>{{ answer.correctAnswer }}</p>
              <p><strong>得分：</strong>{{ answer.score }}分</p>
              <p v-if="answer.analysis"><strong>解析：</strong>{{ answer.analysis }}</p>
            </div>
          </div>
        </div>
        <div v-else class="no-answers">
          暂无答题详情
        </div>
      </div>
    </el-dialog>

    <!-- 回顾考试对话框 -->
    <el-dialog
      title="回顾考试"
      v-model="reviewDialogVisible"
      width="80%"
      :before-close="closeReviewDialog"
    >
      <div v-if="selectedRecord" class="review-content">
        <div class="review-header">
          <h3>{{ selectedRecord.paperName }}</h3>
          <p>得分：{{ selectedRecord.score }} / {{ selectedRecord.totalScore }} ({{ selectedRecord.accuracy }}%)</p>
        </div>
        
        <div class="review-questions" v-if="selectedRecord.answers && selectedRecord.answers.length > 0">
          <div
            v-for="(answer, index) in selectedRecord.answers"
            :key="index"
            class="review-question"
          >
            <h4>{{ index + 1 }}. {{ answer.questionContent }}</h4>
            <div class="review-answer">
              <p><strong>我的答案：</strong>{{ answer.userAnswer || '未作答' }}</p>
              <p><strong>正确答案：</strong>{{ answer.correctAnswer }}</p>
              <p v-if="answer.analysis"><strong>解析：</strong>{{ answer.analysis }}</p>
            </div>
          </div>
        </div>
        <div v-else class="no-answers">
          暂无答题详情
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'ExamRecord',
  data() {
    return {
      examRecords: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      detailDialogVisible: false,
      reviewDialogVisible: false,
      selectedRecord: null
    }
  },
  created() {
    this.loadExamRecords()
  },
  methods: {
    // 加载考试记录
    async loadExamRecords() {
      try {
        const userId = localStorage.getItem('userId') || JSON.parse(localStorage.getItem('system-user')).userId
        if (!userId) {
          this.$message.error('请先登录')
          return
        }
        
        const params = {
          userId: userId,
          pageNum: this.currentPage,
          pageSize: this.pageSize
        }
        
        const response = await request.get('/exam/record/user/' + userId, { params })
        this.examRecords = Array.isArray(response.data.list) ? response.data.list : response.data
        this.total = response.data.total || this.examRecords.length
      } catch (error) {
        console.error('加载考试记录失败:', error)
        this.$message.error('加载考试记录失败')
      }
    },
    
    // 查看记录详情
    viewRecord(record) {
      this.selectedRecord = record
      this.detailDialogVisible = true
    },
    
    // 回顾考试
    reviewExam(record) {
      this.selectedRecord = record
      this.reviewDialogVisible = true
    },
    
    // 关闭详情对话框
    closeDetailDialog() {
      this.detailDialogVisible = false
      this.selectedRecord = null
    },
    
    // 关闭回顾对话框
    closeReviewDialog() {
      this.reviewDialogVisible = false
      this.selectedRecord = null
    },
    
    // 格式化日期
    formatDate(date) {
      if (!date) return ''
      const d = new Date(date)
      return `${d.getFullYear()}-${(d.getMonth() + 1).toString().padStart(2, '0')}-${d.getDate().toString().padStart(2, '0')} ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
    },
    
    // 分页相关方法
    handleSizeChange(newSize) {
      this.pageSize = newSize
      this.loadExamRecords()
    },
    
    handleCurrentChange(newPage) {
      this.currentPage = newPage
      this.loadExamRecords()
    }
  }
}
</script>

<style scoped>
.exam-record-container {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.header {
  margin-bottom: 20px;
}

.record-card {
  margin-bottom: 20px;
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.record-date {
  color: #909399;
  font-size: 14px;
}

.record-details {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
  margin-bottom: 15px;
}

.detail-item {
  display: flex;
}

.label {
  font-weight: bold;
  color: #606266;
  min-width: 60px;
}

.value {
  flex: 1;
  color: #303133;
}

.score {
  color: #67C23A;
  font-weight: bold;
}

.accuracy {
  color: #409EFF;
  font-weight: bold;
}

.record-actions {
  text-align: right;
}

.detail-content, .review-content {
  max-height: 60vh;
  overflow-y: auto;
}

.summary {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.summary p {
  margin: 8px 0;
}

.answers-section h4, .review-questions h4 {
  margin: 20px 0 10px 0;
  padding-bottom: 5px;
  border-bottom: 1px solid #eee;
}

.answer-item, .review-question {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 15px;
  margin-bottom: 10px;
  background-color: white;
}

.question-info {
  margin-bottom: 10px;
}

.question-num {
  font-weight: bold;
  color: #409EFF;
  margin-right: 10px;
}

.question-content {
  color: #303133;
}

.answer-detail p, .review-answer p {
  margin: 5px 0;
  line-height: 1.5;
}

.review-header {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.review-header h3 {
  margin: 0 0 10px 0;
}

.review-header p {
  margin: 0;
  color: #67C23A;
  font-weight: bold;
}

.no-answers {
  text-align: center;
  padding: 40px;
  color: #999;
}

.pagination {
  text-align: center;
  margin-top: 20px;
}
</style>