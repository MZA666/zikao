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
          <el-select v-model="searchType" placeholder="题目类型" clearable @change="loadCollectedQuestions">
            <el-option label="单选题" value="SINGLE_CHOICE" />
            <el-option label="多选题" value="MULTIPLE_CHOICE" />
            <el-option label="判断题" value="TRUE_FALSE" />
            <el-option label="填空题" value="FILL_BLANK" />
            <el-option label="问答题" value="ESSAY" />
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
        this.collectedQuestions = collectionResponse.data.map(item => item.questionId)
        
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
        const userId = localStorage.getItem('userId')
        if (!userId) {
          return
        }
        
        const response = await request.get(`/exam/bank/user/${userId}/collections`)
        this.collectedBanks = response.data || []
      } catch (error) {
        console.error('加载收藏题库失败:', error)
      }
    },
    
    // 取消收藏题目
    async unCollectQuestion(questionId) {
      try {
        const userId = localStorage.getItem('userId')
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
</style>