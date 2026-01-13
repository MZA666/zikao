<template>
  <div class="study-space-container">
    <div class="header">
      <h2>学习空间</h2>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="filter-section">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-select v-model="searchSubjectId" placeholder="选择学科" clearable @change="loadCollectedQuestions">
            <el-option
              v-for="subject in subjects"
              :key="subject.id"
              :label="subject.name"
              :value="subject.id"
            />
          </el-select>
        </el-col>
        <el-col :span="8">
          <el-button type="primary" @click="startPractice">开始练习</el-button>
        </el-col>
      </el-row>
    </div>

    <!-- 收藏的题目列表 -->
    <div class="question-list">
      <el-card
        v-for="item in collectedQuestionsWithDetails"
        :key="item.question.id"
        class="question-card"
        shadow="hover"
      >
        <div class="question-header">
          <div class="question-info">
            <span class="question-type">{{ getQuestionTypeText(item.question.type) }}</span>
            <span class="question-subject">{{ getSubjectName(item.question.subjectId) }}</span>
          </div>
          <div class="question-actions">
            <el-button size="small" type="danger" @click="unCollectQuestion(item.question.id)">
              取消收藏
            </el-button>
            <el-button size="small" type="primary" @click="practiceQuestion(item.question)">练习</el-button>
          </div>
        </div>
        <div class="question-content" v-html="item.question.content"></div>
        
        <!-- 选择题选项 -->
        <div v-if="item.question.type === 'SINGLE_CHOICE' || item.question.type === 'MULTIPLE_CHOICE'" class="options">
          <div
            v-for="option in item.question.options"
            :key="option.id"
            class="option-item"
          >
            <span class="option-key">{{ option.optionKey }}.</span>
            <span class="option-content">{{ option.content }}</span>
          </div>
        </div>
        
        <!-- 答案和解析 -->
        <div class="question-info">
          <div v-if="item.question.answer" class="answer">
            <strong>答案：</strong>{{ item.question.answer }}
          </div>
          <div v-if="item.question.analysis" class="analysis">
            <strong>解析：</strong>{{ item.question.analysis }}
          </div>
        </div>
      </el-card>
    </div>

    <!-- 收藏的题库列表 -->
    <div class="bank-section">
      <div class="section-header">
        <h3>收藏的题库</h3>
      </div>
      <div class="bank-list">
        <el-card
          v-for="bank in collectedBanks"
          :key="bank.id"
          class="bank-card"
          shadow="hover"
        >
          <div class="bank-header">
            <h3 class="bank-name">{{ bank.bankName }}</h3>
            <div class="bank-actions">
              <el-button size="small" type="primary" @click="startPracticeFromBank(bank)">开始练习</el-button>
              <el-button size="small" type="danger" @click="unCollectBank(bank)">取消收藏</el-button>
            </div>
          </div>
          <div class="bank-info">
            <div class="info-item">
              <span class="label">学科：</span>
              <span class="value">{{ getSubjectName(bank.subjectId) }}</span>
            </div>
            <div class="info-item" v-if="bank.uploaderId">
              <span class="label">上传者ID：</span>
              <span class="value">{{ bank.uploaderId }}</span>
            </div>
            <div class="info-item" v-if="bank.collectedTime">
              <span class="label">收藏时间：</span>
              <span class="value">{{ formatDate(bank.collectedTime) }}</span>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 练习模式弹窗 -->
    <el-dialog
      title="练习模式"
      v-model="practiceDialogVisible"
      width="80%"
      :before-close="closePracticeDialog"
    >
      <div v-if="currentPracticeQuestions.length > 0" class="practice-container">
        <div class="practice-header">
          <span>第 {{ currentQuestionIndex + 1 }} 题，共 {{ currentPracticeQuestions.length }} 题</span>
          <el-progress :percentage="getProgressPercentage" :show-text="false" />
        </div>
        
        <div class="practice-question">
          <h3>{{ currentQuestionIndex + 1 }}. {{ currentPracticeQuestion.content }}</h3>
          
          <!-- 选择题 -->
          <div v-if="currentPracticeQuestion.type === 'SINGLE_CHOICE' || currentPracticeQuestion.type === 'MULTIPLE_CHOICE'" class="practice-options">
            <el-checkbox-group
              v-if="currentPracticeQuestion.type === 'MULTIPLE_CHOICE'"
              v-model="userAnswers[currentQuestionIndex]"
            >
              <el-checkbox
                v-for="option in currentPracticeQuestion.options"
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
                v-for="option in currentPracticeQuestion.options"
                :key="option.id"
                :label="option.optionKey"
              >
                {{ option.optionKey }}. {{ option.content }}
              </el-radio>
            </el-radio-group>
          </div>
          
          <!-- 判断题 -->
          <div v-else-if="currentPracticeQuestion.type === 'TRUE_FALSE'" class="practice-options">
            <el-radio-group v-model="userAnswers[currentQuestionIndex]">
              <el-radio label="正确">正确</el-radio>
              <el-radio label="错误">错误</el-radio>
            </el-radio-group>
          </div>
          
          <!-- 填空题和问答题 -->
          <div v-else-if="currentPracticeQuestion.type === 'FILL_BLANK' || currentPracticeQuestion.type === 'ESSAY'" class="practice-options">
            <el-input
              v-model="userAnswers[currentQuestionIndex]"
              :type="currentPracticeQuestion.type === 'FILL_BLANK' ? 'text' : 'textarea'"
              :rows="currentPracticeQuestion.type === 'ESSAY' ? 4 : 1"
              placeholder="请输入答案"
            />
          </div>
        </div>
        
        <div class="practice-actions">
          <el-button @click="prevQuestion" :disabled="currentQuestionIndex === 0">上一题</el-button>
          <el-button @click="nextQuestion" :disabled="currentQuestionIndex === currentPracticeQuestions.length - 1">下一题</el-button>
          <el-button v-if="currentQuestionIndex === currentPracticeQuestions.length - 1" type="primary" @click="submitPractice">提交</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'StudySpace',
  data() {
    return {
      subjects: [],
      collectedQuestions: [], // 收藏的题目ID列表
      allQuestions: [], // 所有题目详情
      searchSubjectId: '',
      searchType: '',
      practiceDialogVisible: false,
      currentPracticeQuestions: [],
      currentQuestionIndex: 0,
      userAnswers: [],
      collectedQuestionsWithDetails: [], // 包含题目详情的收藏列表
      // 添加收藏题库的相关数据
      collectedBanks: [] // 收藏的题库列表
    }
  },
  computed: {
    currentPracticeQuestion() {
      return this.currentPracticeQuestions[this.currentQuestionIndex] || {}
    },
    getProgressPercentage() {
      return Math.round(((this.currentQuestionIndex + 1) / this.currentPracticeQuestions.length) * 100)
    }
  },
  created() {
    this.loadSubjects()
    this.loadCollectedQuestions()
    // 加载收藏的题库
    this.loadCollectedBanks()
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
    
    // 加载收藏的题目
    async loadCollectedQuestions() {
      try {
        const userId = localStorage.getItem('userId')
        if (!userId) {
          this.$message.error('请先登录')
          return
        }
        
        // 获取收藏的题目ID列表
        const collectionResponse = await request.get(`/exam/collection/${userId}`)
        // 确保返回的数据是数组格式
        const collectionData = Array.isArray(collectionResponse.data) ? collectionResponse.data : collectionResponse.data.list || []
        this.collectedQuestions = collectionData.map(item => item.questionId)
        
        if (this.collectedQuestions.length === 0) {
          this.collectedQuestionsWithDetails = []
          return
        }
        
        // 根据筛选条件获取题目详情
        let params = {}
        if (this.searchSubjectId) {
          params.subjectId = this.searchSubjectId
        }
        if (this.searchType) {
          params.type = this.searchType
        }
        
        const questionResponse = await request.get('/exam/question/list', { params })
        const allQuestions = Array.isArray(questionResponse.data.list) ? questionResponse.data.list : questionResponse.data
        
        // 过滤出收藏的题目
        this.collectedQuestionsWithDetails = allQuestions
          .filter(question => this.collectedQuestions.includes(question.id))
          .map(question => ({ question }))
      } catch (error) {
        console.error('加载收藏题目失败:', error)
      }
    },
    
    // 加载收藏的题库
    async loadCollectedBanks() {
      try {
        let userId = localStorage.getItem('userId');
        if (!userId) {
          try {
            const systemUser = JSON.parse(localStorage.getItem('system-user'));
            userId = systemUser?.userId;
          } catch (e) {
            console.error('解析用户信息失败:', e);
          }
        }
        console.log('当前用户ID:', userId);
        if (!userId) {
          return
        }
        
        const response = await request.get(`/exam/bank/user/${userId}/collections`)
        console.log('收藏题库接口返回数据:', response);
        // 确保返回的数据是数组格式
        this.collectedBanks = Array.isArray(response.data) ? response.data : response.data || []
        console.log('处理后的收藏题库数据:', this.collectedBanks);
      } catch (error) {
        console.error('加载收藏题库失败:', error)
      }
    },
    
    // 取消收藏题目
    async unCollectQuestion(questionId) {
      try {
        let userId = localStorage.getItem('userId');
        if (!userId) {
          try {
            const systemUser = JSON.parse(localStorage.getItem('system-user'));
            userId = systemUser?.userId;
          } catch (e) {
            console.error('解析用户信息失败:', e);
          }
        }
        if (!userId) {
          this.$message.error('请先登录')
          return
        }
        
        await request.delete(`/exam/collection/${userId}/${questionId}`)
        this.$message.success('已取消收藏')
        // 重新加载收藏列表
        this.loadCollectedQuestions()
      } catch (error) {
        console.error('取消收藏失败:', error)
        this.$message.error('操作失败')
      }
    },
    
    // 取消收藏题库
    async unCollectBank(bank) {
      try {
        let userId = localStorage.getItem('userId');
        if (!userId) {
          try {
            const systemUser = JSON.parse(localStorage.getItem('system-user'));
            userId = systemUser?.userId;
          } catch (e) {
            console.error('解析用户信息失败:', e);
          }
        }
        if (!userId) {
          this.$message.error('请先登录')
          return
        }
        
        // 处理虚拟ID的编码问题
        const encodedBankId = encodeURIComponent(bank.bankId || bank.id);
        const response = await request.delete(`/exam/bank/collection/${userId}/${encodedBankId}`)
        if (response.code === 200) {
          this.$message.success('已取消收藏')
          // 重新加载收藏列表
          this.loadCollectedBanks()
        } else {
          this.$message.error(response.msg || '取消收藏失败')
        }
      } catch (error) {
        console.error('取消收藏题库失败:', error)
        this.$message.error('操作失败')
      }
    },
    
    // 获取题目类型文本
    getQuestionTypeText(type) {
      const typeMap = {
        'SINGLE_CHOICE': '单选题',
        'MULTIPLE_CHOICE': '多选题',
        'TRUE_FALSE': '判断题',
        'FILL_BLANK': '填空题',
        'ESSAY': '问答题'
      }
      return typeMap[type] || type
    },
    
    // 获取学科名称
    getSubjectName(subjectId) {
      const subject = this.subjects.find(s => s.id === subjectId)
      return subject ? subject.name : ''
    },
    
    // 格式化日期
    formatDate(date) {
      if (!date) return '未知'
      const d = new Date(date)
      return `${d.getFullYear()}-${(d.getMonth() + 1).toString().padStart(2, '0')}-${d.getDate().toString().padStart(2, '0')}`
    },
    
    // 从题库开始练习
    async startPracticeFromBank(bank) {
      try {
        // 获取该题库（学科+上传者）的所有题目
        const params = { 
          subjectId: bank.subjectId 
        }
        
        // 如果有上传者ID，添加到参数中
        if (bank.uploaderId) {
          params.uploaderId = bank.uploaderId
        }
        
        const response = await request.get('/exam/question/by-uploader', { params })
        let questions = Array.isArray(response.data) ? response.data : response.data || []
        
        if (questions.length === 0) {
          this.$message.warning('该题库暂无题目')
          return
        }
        
        // 为每个题目获取选项（如果还没有获取的话）
        for (let i = 0; i < questions.length; i++) {
          if (!questions[i].options || questions[i].options.length === 0) {
            const detailResponse = await request.get(`/exam/question/${questions[i].id}`)
            questions[i].options = detailResponse.data?.options || []
          }
        }
        
        this.currentPracticeQuestions = questions
        this.currentQuestionIndex = 0
        this.userAnswers = new Array(questions.length).fill('')
        this.practiceDialogVisible = true
      } catch (error) {
        console.error('获取题目失败:', error)
        this.$message.error('获取题目失败')
      }
    },
    
    // 开始练习（随机选择收藏的题目）
    startPractice() {
      if (this.collectedQuestionsWithDetails.length === 0) {
        this.$message.warning('请先收藏一些题目')
        return
      }
      
      // 随机选择题目进行练习（最多10题）
      const shuffled = [...this.collectedQuestionsWithDetails].sort(() => 0.5 - Math.random())
      this.currentPracticeQuestions = shuffled.slice(0, Math.min(10, shuffled.length)).map(item => item.question)
      
      this.currentQuestionIndex = 0
      this.userAnswers = new Array(this.currentPracticeQuestions.length).fill('')
      this.practiceDialogVisible = true
    },
    
    // 练习单个题目
    practiceQuestion(question) {
      this.currentPracticeQuestions = [question]
      this.currentQuestionIndex = 0
      this.userAnswers = ['']
      this.practiceDialogVisible = true
    },
    
    // 上一题
    prevQuestion() {
      if (this.currentQuestionIndex > 0) {
        this.currentQuestionIndex--
      }
    },
    
    // 下一题
    nextQuestion() {
      if (this.currentQuestionIndex < this.currentPracticeQuestions.length - 1) {
        this.currentQuestionIndex++
      }
    },
    
    // 提交练习
    submitPractice() {
      // 计算正确率
      let correctCount = 0
      for (let i = 0; i < this.currentPracticeQuestions.length; i++) {
        const userAnswer = this.userAnswers[i]
        const correctAnswer = this.currentPracticeQuestions[i].answer
        
        if (userAnswer === correctAnswer) {
          correctCount++
        }
      }
      
      const accuracy = (correctCount / this.currentPracticeQuestions.length) * 100
      this.$message.success(`练习完成！正确率：${accuracy.toFixed(2)}% (${correctCount}/${this.currentPracticeQuestions.length})`)
      
      this.closePracticeDialog()
    },
    
    // 关闭练习对话框
    closePracticeDialog() {
      this.practiceDialogVisible = false
      this.currentPracticeQuestions = []
      this.currentQuestionIndex = 0
      this.userAnswers = []
    }
  }
}
</script>

<style scoped>
.study-space-container {
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

.question-card {
  margin-bottom: 20px;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.question-info {
  display: flex;
  gap: 15px;
}

.question-type {
  font-weight: bold;
  color: #409EFF;
}

.question-subject {
  color: #909399;
}

.question-actions {
  display: flex;
  gap: 10px;
}

.question-content {
  margin-bottom: 15px;
  line-height: 1.6;
}

.options {
  margin: 15px 0;
}

.option-item {
  padding: 8px 0;
  display: flex;
}

.option-key {
  font-weight: bold;
  min-width: 30px;
}

.option-content {
  flex: 1;
}

.answer, .analysis {
  margin: 5px 0;
  line-height: 1.5;
}

.answer {
  color: #67C23A;
}

.analysis {
  color: #909399;
  font-size: 14px;
}

.practice-container {
  min-height: 400px;
}

.practice-header {
  margin-bottom: 20px;
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

.empty-message {
  text-align: center;
  padding: 40px;
  color: #999;
}

.bank-section {
  margin-top: 30px;
}

.section-header {
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 2px solid #e4e7ed;
}

.section-header h3 {
  margin: 0;
  color: #303133;
  font-size: 18px;
  font-weight: bold;
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

.tab-content {
  margin-top: 20px;
}

.empty-message {
  text-align: center;
  padding: 40px;
  color: #999;
}
</style>