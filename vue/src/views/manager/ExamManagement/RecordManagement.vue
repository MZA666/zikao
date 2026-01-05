<template>
  <div class="record-management">
    <div class="toolbar">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-select v-model="searchSubjectId" placeholder="选择学科" clearable>
            <el-option
              v-for="subject in subjects"
              :key="subject.id"
              :label="subject.name"
              :value="subject.id"
            />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-input v-model="searchUsername" placeholder="搜索用户名" clearable />
        </el-col>
        <el-col :span="8">
          <el-date-picker
            v-model="searchDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="loadRecords">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-col>
      </el-row>
    </div>

    <el-table :data="records" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="userId" label="用户ID" width="100" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="paperName" label="试卷名称" show-overflow-tooltip />
      <el-table-column prop="subjectName" label="学科" width="120" />
      <el-table-column prop="score" label="得分" width="80">
        <template #default="scope">
          <span :class="getScoreClass(scope.row.score, scope.row.totalScore)">{{ scope.row.score }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="totalScore" label="总分" width="80" />
      <el-table-column prop="accuracy" label="正确率" width="100">
        <template #default="scope">
          {{ scope.row.accuracy }}%
        </template>
      </el-table-column>
      <el-table-column prop="duration" label="用时(分钟)" width="120" />
      <el-table-column prop="examTime" label="考试时间" :formatter="formatDate" width="160" />
      <el-table-column label="操作" width="150">
        <template #default="scope">
          <el-button size="small" @click="viewRecord(scope.row)">查看详情</el-button>
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

    <!-- 查看记录详情对话框 -->
    <el-dialog title="考试记录详情" v-model="detailDialogVisible" width="80%" top="5vh">
      <div v-if="selectedRecord" class="detail-content">
        <div class="summary">
          <h3>{{ selectedRecord.paperName }}</h3>
          <div class="info-row">
            <span><strong>用户：</strong>{{ selectedRecord.username }}</span>
            <span><strong>学科：</strong>{{ selectedRecord.subjectName }}</span>
            <span><strong>得分：</strong>{{ selectedRecord.score }} / {{ selectedRecord.totalScore }}</span>
            <span><strong>正确率：</strong>{{ selectedRecord.accuracy }}%</span>
            <span><strong>用时：</strong>{{ selectedRecord.duration }}分钟</span>
            <span><strong>考试时间：</strong>{{ formatDateByValue(selectedRecord.examTime) }}</span>
          </div>
        </div>
        
        <div class="answers-section">
          <h4>答题详情</h4>
          <div
            v-for="(answer, index) in selectedRecord.answers || []"
            :key="index"
            class="answer-item"
          >
            <div class="question-info">
              <span class="question-num">{{ index + 1 }}.</span>
              <span class="question-content">{{ answer.questionContent }}</span>
            </div>
            <div class="answer-detail">
              <div><strong>我的答案：</strong>{{ answer.userAnswer || '未作答' }}</div>
              <div><strong>正确答案：</strong>{{ answer.correctAnswer }}</div>
              <div><strong>得分：</strong>{{ answer.score }}分</div>
              <div v-if="answer.analysis"><strong>解析：</strong>{{ answer.analysis }}</div>
            </div>
          </div>
          
          <div v-if="!(selectedRecord.answers && selectedRecord.answers.length > 0)" class="no-answers">
            暂无答题详情
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'RecordManagement',
  data() {
    return {
      records: [],
      subjects: [],
      loading: false,
      currentPage: 1,
      pageSize: 10,
      total: 0,
      searchSubjectId: '',
      searchUsername: '',
      searchDateRange: [],
      detailDialogVisible: false,
      selectedRecord: null
    }
  },
  mounted() {
    this.loadSubjects()
    this.loadRecords()
  },
  methods: {
    // 加载学科列表
    async loadSubjects() {
      try {
        const response = await request.get('/exam/subject/list')
        this.subjects = response.data
      } catch (error) {
        console.error('加载学科列表失败:', error)
        this.$message.error('加载学科列表失败')
      }
    },
    
    // 加载考试记录列表
    async loadRecords() {
      this.loading = true
      try {
        const params = {
          subjectId: this.searchSubjectId || undefined,
          username: this.searchUsername || undefined,
          startDate: this.searchDateRange ? this.searchDateRange[0] : undefined,
          endDate: this.searchDateRange ? this.searchDateRange[1] : undefined,
          pageNum: this.currentPage,
          pageSize: this.pageSize
        }
        
        // 模拟加载考试记录，实际项目中需要后端提供接口
        // 这里先用模拟数据展示
        this.records = [
          {
            id: 1,
            userId: 1,
            username: '张三',
            paperName: '数学模拟考试一',
            subjectName: '数学',
            score: 85,
            totalScore: 100,
            accuracy: 85,
            duration: 45,
            examTime: new Date(),
            answers: [
              {
                questionContent: '1+1等于多少？',
                userAnswer: '2',
                correctAnswer: '2',
                score: 5,
                analysis: '这是一道基础数学题'
              },
              {
                questionContent: '三角形内角和是多少？',
                userAnswer: '180度',
                correctAnswer: '180度',
                score: 5,
                analysis: '三角形内角和恒为180度'
              }
            ]
          },
          {
            id: 2,
            userId: 2,
            username: '李四',
            paperName: '语文模拟考试一',
            subjectName: '语文',
            score: 78,
            totalScore: 100,
            accuracy: 78,
            duration: 52,
            examTime: new Date(Date.now() - 86400000), // 一天前
            answers: [
              {
                questionContent: '《红楼梦》的作者是谁？',
                userAnswer: '曹雪芹',
                correctAnswer: '曹雪芹',
                score: 5,
                analysis: '《红楼梦》是清代作家曹雪芹创作的章回体长篇小说'
              }
            ]
          }
        ]
        
        this.total = this.records.length
      } catch (error) {
        console.error('加载考试记录失败:', error)
        this.$message.error('加载考试记录失败')
      } finally {
        this.loading = false
      }
    },
    
    // 重置搜索
    resetSearch() {
      this.searchSubjectId = ''
      this.searchUsername = ''
      this.searchDateRange = []
      this.currentPage = 1
      this.loadRecords()
    },
    
    // 查看记录详情
    viewRecord(record) {
      this.selectedRecord = record
      this.detailDialogVisible = true
    },
    
    // 获取分数样式类
    getScoreClass(score, totalScore) {
      const percentage = (score / totalScore) * 100
      if (percentage >= 90) return 'score-excellent'
      if (percentage >= 80) return 'score-good'
      if (percentage >= 60) return 'score-pass'
      return 'score-fail'
    },
    
    // 格式化日期
    formatDate(row, column, cellValue) {
      if (!cellValue) return ''
      const date = new Date(cellValue)
      return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
    },
    
    // 根据值格式化日期
    formatDateByValue(value) {
      if (!value) return ''
      const date = new Date(value)
      return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
    },
    
    // 分页相关方法
    handleSizeChange(newSize) {
      this.pageSize = newSize
      this.loadRecords()
    },
    
    handleCurrentChange(newPage) {
      this.currentPage = newPage
      this.loadRecords()
    }
  }
}
</script>

<style scoped>
.record-management {
  padding: 20px 0;
}

.toolbar {
  margin-bottom: 20px;
}

.info-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-top: 15px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.score-excellent {
  color: #67C23A;
  font-weight: bold;
}

.score-good {
  color: #E6A23C;
  font-weight: bold;
}

.score-pass {
  color: #409EFF;
  font-weight: bold;
}

.score-fail {
  color: #F56C6C;
  font-weight: bold;
}

.detail-content {
  max-height: 70vh;
  overflow-y: auto;
}

.summary h3 {
  margin: 0 0 15px 0;
  color: #303133;
}

.answers-section h4 {
  margin: 20px 0 10px 0;
  padding-bottom: 5px;
  border-bottom: 1px solid #eee;
}

.answer-item {
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

.answer-detail div {
  margin: 5px 0;
  line-height: 1.5;
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