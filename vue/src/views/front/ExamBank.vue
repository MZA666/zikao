<template>
  <div class="exam-bank-container">
    <div class="header">
      <h2>题库</h2>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="filter-section">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-select v-model="searchSubjectId" placeholder="选择学科" clearable @change="loadExamBanks">
            <el-option
              v-for="subject in subjects"
              :key="subject.id"
              :label="subject.name"
              :value="subject.id"
            />
          </el-select>
        </el-col>
        <el-col :span="12">
          <el-input
            v-model="searchBankName"
            placeholder="搜索题库名称"
            clearable
            @keyup.enter="loadExamBanks"
          />
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="loadExamBanks">搜索</el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 题库列表 -->
    <div class="bank-list">
      <el-card
        v-for="bank in examBanks"
        :key="bank.bankId"
        class="bank-card"
        shadow="hover"
      >
        <div class="bank-header">
          <h3 class="bank-name">{{ bank.bankName || getSubjectName(bank.subjectId) + '题库' }}</h3>
          <div class="bank-actions">
            <el-button size="small" type="primary" @click="startPractice(bank)">练习</el-button>
            <el-button size="small" type="warning" @click="collectBank(bank)" :loading="isCollecting[bank.bankId]">
              {{ isCollected[bank.bankId] ? '已收藏' : '收藏' }}
            </el-button>
          </div>
        </div>
        <div class="bank-info">
          <div class="info-item">
            <span class="label">学科：</span>
            <span class="value">{{ getSubjectName(bank.subjectId) }}</span>
          </div>
          <div class="info-item">
            <span class="label">题目数量：</span>
            <span class="value">{{ bank.questionCount || '未知' }}</span>
          </div>
          <div class="info-item" v-if="bank.uploadTime">
            <span class="label">上传时间：</span>
            <span class="value">{{ formatDate(bank.uploadTime) }}</span>
          </div>
          <div class="info-item" v-if="bank.uploader">
            <span class="label">上传者：</span>
            <span class="value">{{ bank.uploader }}</span>
          </div>
        </div>
        <div class="bank-description" v-if="bank.description">
          <span class="label">描述：</span>
          <span class="value">{{ bank.description }}</span>
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

    <!-- 题目练习对话框 -->
    <el-dialog
      title="题目练习"
      v-model="practiceDialogVisible"
      width="80%"
      :before-close="closePracticeDialog"
    >
      <div v-if="currentBankQuestions.length > 0" class="practice-container">
        <div class="practice-header">
          <span>{{ currentBank.bankName }} - 第 {{ currentQuestionIndex + 1 }} 题，共 {{ currentBankQuestions.length }} 题</span>
          <el-progress :percentage="getProgressPercentage" :show-text="false" />
        </div>
        
        <div class="practice-question">
          <h3>{{ currentQuestionIndex + 1 }}. {{ currentQuestion.content }}</h3>
          
          <!-- 选择题 -->
          <div v-if="currentQuestion.type === 'SINGLE_CHOICE' || currentQuestion.type === 'MULTIPLE_CHOICE'" class="practice-options">
            <el-checkbox-group
              v-if="currentQuestion.type === 'MULTIPLE_CHOICE'"
              v-model="userAnswers[currentQuestionIndex]"
            >
              <el-checkbox
                v-for="option in currentQuestion.options"
                :key="option.id"
                :label="option.optionKey"
              >
                {{ option.optionKey }}. {{ option.content }}
              </el-checkbox>
            </el-checkbox-group>
            
            <el-radio-group
              v-else
              v-model="userAnswers[currentQuestionIndex]"
            >
              <el-radio
                v-for="option in currentQuestion.options"
                :key="option.id"
                :label="option.optionKey"
              >
                {{ option.optionKey }}. {{ option.content }}
              </el-radio>
            </el-radio-group>
          </div>
          
          <!-- 判断题 -->
          <div v-else-if="currentQuestion.type === 'TRUE_FALSE'" class="practice-options">
            <el-radio-group v-model="userAnswers[currentQuestionIndex]">
              <el-radio label="正确">正确</el-radio>
              <el-radio label="错误">错误</el-radio>
            </el-radio-group>
          </div>
          
          <!-- 填空题和问答题 -->
          <div v-else-if="currentQuestion.type === 'FILL_BLANK' || currentQuestion.type === 'ESSAY'" class="practice-options">
            <el-input
              v-model="userAnswers[currentQuestionIndex]"
              :type="currentQuestion.type === 'FILL_BLANK' ? 'text' : 'textarea'"
              :rows="currentQuestion.type === 'ESSAY' ? 4 : 1"
              placeholder="请输入答案"
            />
          </div>
        </div>
        
        <div class="practice-actions">
          <el-button @click="prevQuestion" :disabled="currentQuestionIndex === 0">上一题</el-button>
          <el-button @click="nextQuestion" :disabled="currentQuestionIndex === currentBankQuestions.length - 1">下一题</el-button>
          <el-button v-if="currentQuestionIndex === currentBankQuestions.length - 1" type="primary" @click="submitPractice">提交</el-button>
        </div>
      </div>
      <div v-else class="no-questions">
        此题库暂无题目
      </div>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'ExamBank',
  data() {
    return {
      examBanks: [],
      subjects: [],
      searchSubjectId: '',
      searchBankName: '',
      currentPage: 1,
      pageSize: 10,
      total: 0,
      practiceDialogVisible: false,
      currentBank: null,
      currentBankQuestions: [],
      currentQuestionIndex: 0,
      userAnswers: [],
      currentQuestion: {},
      // 新增收藏相关的数据
      isCollected: {}, // 记录题库是否已被收藏
      isCollecting: {} // 记录正在收藏的题库
    }
  },
  computed: {
    getProgressPercentage() {
      if (this.currentBankQuestions.length === 0) return 0
      return Math.round(((this.currentQuestionIndex + 1) / this.currentBankQuestions.length) * 100)
    }
  },
  created() {
    this.loadSubjects()
    this.loadExamBanks()
    // 检查收藏状态
    this.checkCollectionStatus()
  },
  methods: {
    // 加载学科列表
    async loadSubjects() {
      try {
        const response = await request.get('/exam/subject/list')
        this.subjects = response.data
      } catch (error) {
        console.error('加载学科列表失败:', error)
      }
    },
    
    // 加载题库列表
    async loadExamBanks() {
      try {
        // 首先尝试从题库统计接口获取数据
        const params = {}
        if (this.searchSubjectId) {
          params.subjectId = this.searchSubjectId
        }
        
        const response = await request.get('/exam/bank/stats', { params })
        let examBanks = Array.isArray(response.data) ? response.data : response.data || []
        
        // 如果通过统计接口没有获取到数据，尝试从bank/list接口获取
        if (!examBanks || examBanks.length === 0) {
          const bankParams = {
            pageNum: this.currentPage,
            pageSize: this.pageSize
          }
          
          // 如果选择了学科，通过学科名称查询
          if (this.searchSubjectId) {
            const selectedSubject = this.subjects.find(s => s.id == this.searchSubjectId);
            if (selectedSubject) {
              bankParams.subject = selectedSubject.name
            }
          }
          
          if (this.searchBankName) {
            bankParams.bankName = this.searchBankName
          }
          
          const bankResponse = await request.get('/exam/bank/list', { bankParams })
          examBanks = Array.isArray(bankResponse.data.list) ? bankResponse.data.list : bankResponse.data || []
        }
        
        // 如果仍然没有数据，通过题目统计构建虚拟题库
        if (!examBanks || examBanks.length === 0) {
          examBanks = await this.loadFromQuestionStats()
        }
        
        this.examBanks = examBanks
        this.total = this.examBanks.length
        
        // 检查收藏状态
        this.checkCollectionStatus()
      } catch (error) {
        console.error('加载题库列表失败:', error)
        // 如果所有方法都失败，尝试从题目统计中获取数据
        this.examBanks = await this.loadFromQuestionStats()
        this.total = this.examBanks.length
      }
    },
    
    // 从题目统计中加载数据（作为备选方案）
    async loadFromQuestionStats() {
      try {
        // 获取所有题目并按学科分组统计
        const params = {
          pageNum: 1,
          pageSize: 1000 // 获取所有题目进行统计
        }
        
        if (this.searchSubjectId) {
          params.subjectId = this.searchSubjectId
        }
        
        const response = await request.get('/exam/question/list', { params })
        const questions = Array.isArray(response.data.list) ? response.data.list : response.data || []
        
        // 按学科ID分组统计题目数量
        const subjectStats = {}
        questions.forEach(question => {
          const subjectId = question.subjectId
          if (!subjectStats[subjectId]) {
            subjectStats[subjectId] = {
              subjectId: subjectId,
              questionCount: 0,
              latestTime: question.createdTime || question.updatedTime || question.uploadTime,
              uploader: '系统',
              description: '自动生成的题库'
            }
          }
          subjectStats[subjectId].questionCount++
          
          // 更新最新时间
          const questionTime = question.createdTime || question.updatedTime || question.uploadTime
          if (questionTime && (!subjectStats[subjectId].latestTime || new Date(questionTime) > new Date(subjectStats[subjectId].latestTime))) {
            subjectStats[subjectId].latestTime = questionTime
          }
        })
        
        // 转换为题库列表
        const examBanks = Object.keys(subjectStats).map(subjectId => {
          const subject = this.subjects.find(s => s.id == subjectId)
          return {
            bankId: subjectId, // 使用学科ID作为bankId
            subjectId: parseInt(subjectId),
            bankName: subject ? subject.name + '题库' : '未知题库',
            questionCount: subjectStats[subjectId].questionCount,
            uploadTime: subjectStats[subjectId].latestTime,
            uploader: subjectStats[subjectId].uploader,
            description: subjectStats[subjectId].description
          }
        })
        
        // 应用搜索条件
        if (this.searchBankName) {
          return examBanks.filter(bank => 
            bank.bankName.toLowerCase().includes(this.searchBankName.toLowerCase())
          )
        }
        
        return examBanks
      } catch (error) {
        console.error('从题目统计加载题库失败:', error)
        this.$message.error('加载题库列表失败')
        return []
      }
    },
    
    // 检查收藏状态
    async checkCollectionStatus() {
      try {
        const userId = localStorage.getItem('userId') || JSON.parse(localStorage.getItem('system-user')).userId
        if (!userId) return

        // 获取用户收藏的题库ID列表
        const response = await request.get(`/exam/bank/user/${userId}/collections`)
        const collectedBankIds = response.data || []

        // 更新收藏状态
        const newIsCollected = {}
        this.examBanks.forEach(bank => {
          newIsCollected[bank.bankId] = collectedBankIds.includes(bank.bankId)
        })
        this.isCollected = newIsCollected
      } catch (error) {
        console.error('检查收藏状态失败:', error)
      }
    },
    
    // 收藏题库
    async collectBank(bank) {
      try {
        const userId = localStorage.getItem('userId') || JSON.parse(localStorage.getItem('system-user')).userId
        if (!userId) {
          this.$message.error('请先登录')
          return
        }

        // 设置加载状态
        this.$set(this.isCollecting, bank.bankId, true)

        if (this.isCollected[bank.bankId]) {
          // 取消收藏
          const response = await request.delete(`/exam/bank/collection/${userId}/${bank.bankId}`)
          if (response.code === 200) {
            this.$message.success('已取消收藏')
            this.$set(this.isCollected, bank.bankId, false)
          } else {
            this.$message.error(response.msg || '取消收藏失败')
          }
        } else {
          // 添加收藏
          const collectionData = {
            userId: parseInt(userId),
            bankId: bank.bankId,
            bankName: bank.bankName,
            subjectId: bank.subjectId
          }
          const response = await request.post('/exam/bank/collection', collectionData)
          if (response.code === 200) {
            this.$message.success('收藏成功')
            this.$set(this.isCollected, bank.bankId, true)
          } else {
            this.$message.error(response.msg || '收藏失败')
          }
        }
      } catch (error) {
        console.error('收藏操作失败:', error)
        this.$message.error('收藏操作失败')
      } finally {
        // 清除加载状态
        this.$set(this.isCollecting, bank.bankId, false)
      }
    },
    
    // 获取学科名称
    getSubjectName(subjectId) {
      const subject = this.subjects.find(s => s.id === subjectId)
      return subject ? subject.name : '未知学科'
    },
    
    // 格式化日期
    formatDate(date) {
      if (!date) return '未知'
      const d = new Date(date)
      return `${d.getFullYear()}-${(d.getMonth() + 1).toString().padStart(2, '0')}-${d.getDate().toString().padStart(2, '0')}`
    },
    
    // 开始练习
    async startPractice(bank) {
      try {
        // 获取该题库（学科）的所有题目
        const params = { 
          subjectId: bank.subjectId, 
          pageNum: 1, 
          pageSize: 1000 // 获取所有题目
        }
        const response = await request.get('/exam/question/list', { params })
        let questions = Array.isArray(response.data.list) ? response.data.list : response.data || []
        
        if (questions.length === 0) {
          this.$message.warning('该题库暂无题目')
          return
        }
        
        // 为每个题目获取选项
        for (let i = 0; i < questions.length; i++) {
          if (!questions[i].options || questions[i].options.length === 0) {
            const detailResponse = await request.get(`/exam/question/${questions[i].id}`)
            questions[i].options = detailResponse.data?.options || []
          }
        }
        
        this.currentBank = bank
        this.currentBankQuestions = questions
        this.currentQuestionIndex = 0
        this.userAnswers = new Array(questions.length).fill('')
        this.currentQuestion = questions[0]
        this.practiceDialogVisible = true
      } catch (error) {
        console.error('获取题目失败:', error)
        this.$message.error('获取题目失败')
      }
    },
    
    // 上一题
    prevQuestion() {
      if (this.currentQuestionIndex > 0) {
        this.currentQuestionIndex--
        this.currentQuestion = this.currentBankQuestions[this.currentQuestionIndex]
      }
    },
    
    // 下一题
    nextQuestion() {
      if (this.currentQuestionIndex < this.currentBankQuestions.length - 1) {
        this.currentQuestionIndex++
        this.currentQuestion = this.currentBankQuestions[this.currentQuestionIndex]
      }
    },
    
    // 提交练习
    submitPractice() {
      // 计算正确率
      let correctCount = 0
      for (let i = 0; i < this.currentBankQuestions.length; i++) {
        const userAnswer = this.userAnswers[i]
        const correctAnswer = this.currentBankQuestions[i].answer
        
        if (Array.isArray(userAnswer) && Array.isArray(correctAnswer)) {
          if (userAnswer.sort().join('') === correctAnswer.sort().join('')) {
            correctCount++
          }
        } else if (userAnswer === correctAnswer) {
          correctCount++
        }
      }
      
      const accuracy = (correctCount / this.currentBankQuestions.length) * 100
      this.$message.success(`练习完成！正确率：${accuracy.toFixed(2)}% (${correctCount}/${this.currentBankQuestions.length})`)
      
      this.closePracticeDialog()
    },
    
    // 关闭练习对话框
    closePracticeDialog() {
      this.practiceDialogVisible = false
      this.currentBank = null
      this.currentBankQuestions = []
      this.currentQuestionIndex = 0
      this.userAnswers = []
      this.currentQuestion = {}
    },
    
    // 分页相关方法
    handleSizeChange(newSize) {
      this.pageSize = newSize
      this.loadExamBanks()
    },
    
    handleCurrentChange(newPage) {
      this.currentPage = newPage
      this.loadExamBanks()
    }
  }
}
</script>

<style scoped>
.exam-bank-container {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.header {
  margin-bottom: 20px;
}

.filter-section {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.bank-card {
  margin-bottom: 20px;
}

.bank-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.bank-name {
  margin: 0;
  color: #303133;
  font-size: 18px;
}

.bank-actions {
  display: flex;
  gap: 10px;
}

.bank-info {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 15px;
  margin-bottom: 10px;
}

.bank-description {
  display: flex;
  color: #909399;
  font-size: 14px;
}

.info-item {
  display: flex;
}

.label {
  font-weight: bold;
  color: #606266;
  min-width: 80px;
}

.value {
  flex: 1;
  color: #303133;
}

.practice-container {
  min-height: 400px;
}

.practice-header {
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.practice-question {
  margin-bottom: 30px;
}

.practice-options {
  margin-top: 15px;
}

.practice-actions {
  text-align: center;
  margin-top: 30px;
}

.practice-actions .el-button {
  margin: 0 10px;
}

.no-questions {
  text-align: center;
  padding: 40px;
  color: #999;
}

.pagination {
  text-align: center;
  margin-top: 20px;
}
</style>