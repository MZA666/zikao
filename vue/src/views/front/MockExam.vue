<template>
  <div class="mock-exam-container">
    <div class="header">
      <h2>模拟考试</h2>
    </div>

    <!-- 考试配置区域 -->
    <div v-if="!isInExam" class="exam-config">
      <el-card class="config-card">
        <h3>考试配置</h3>
        <el-form :model="examConfig" label-width="120px" class="config-form">
          <el-form-item label="选择学科">
            <el-select v-model="examConfig.subjectId" placeholder="请选择学科">
              <el-option
                v-for="subject in subjects"
                :key="subject.id"
                :label="subject.name"
                :value="subject.id"
              />
            </el-select>
          </el-form-item>
          
          <el-form-item label="题目数量">
            <el-input-number v-model="examConfig.questionCount" :min="1" :max="100" />
          </el-form-item>
          
          <el-form-item label="题目类型">
            <el-checkbox-group v-model="examConfig.questionTypes">
              <el-checkbox label="SINGLE_CHOICE">单选题</el-checkbox>
              <el-checkbox label="MULTIPLE_CHOICE">多选题</el-checkbox>
              <el-checkbox label="TRUE_FALSE">判断题</el-checkbox>
              <el-checkbox label="FILL_BLANK">填空题</el-checkbox>
              <el-checkbox label="ESSAY">问答题</el-checkbox>
            </el-checkbox-group>
          </el-form-item>
          
          <el-form-item label="题目难度">
            <el-select v-model="examConfig.difficulty" placeholder="请选择难度">
              <el-option label="全部难度" value=""></el-option>
              <el-option label="简单" value="EASY"></el-option>
              <el-option label="中等" value="MEDIUM"></el-option>
              <el-option label="困难" value="HARD"></el-option>
            </el-select>
          </el-form-item>
          
          <el-form-item label="考试时长(分钟)">
            <el-input-number v-model="examConfig.duration" :min="10" :max="180" />
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="startExam" :disabled="!canStartExam">开始考试</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>

    <!-- 考试进行中 -->
    <div v-else class="exam-content">
      <div class="exam-header">
        <div class="exam-info">
          <h3>{{ currentExamPaper.name || '模拟考试' }}</h3>
          <div class="time-info">
            <span>剩余时间: {{ formatTime(remainingTime) }}</span>
            <span>进度: {{ currentQuestionIndex + 1 }} / {{ examQuestions.length }}</span>
          </div>
        </div>
        <el-button type="warning" @click="submitExam">提交试卷</el-button>
      </div>

      <div class="exam-body">
        <div class="question-list">
          <div
            v-for="(question, index) in examQuestions"
            :key="question.id"
            :class="['question-nav-item', { 'current': index === currentQuestionIndex, 'answered': userAnswers[index] !== '' }]"
            @click="goToQuestion(index)"
          >
            {{ index + 1 }}
          </div>
        </div>

        <div class="question-area">
          <div class="question-content">
            <h4>{{ currentQuestionIndex + 1 }}. {{ currentExamQuestion.content }}</h4>
            
            <!-- 选择题 -->
            <div v-if="currentExamQuestion.type === 'SINGLE_CHOICE' || currentExamQuestion.type === 'MULTIPLE_CHOICE'" class="question-options">
              <el-checkbox-group
                v-if="currentExamQuestion.type === 'MULTIPLE_CHOICE'"
                v-model="userAnswers[currentQuestionIndex]"
                @change="onAnswerChange"
              >
                <el-checkbox
                  v-for="option in currentExamQuestion.options"
                  :key="option.id"
                  :label="option.optionKey"
                >
                  {{ option.optionKey }}. {{ option.content }}
                </el-checkbox>
              </el-checkbox-group>
              
              <el-radio-group
                v-else
                v-model="userAnswers[currentQuestionIndex]"
                @change="onAnswerChange"
              >
                <el-radio
                  v-for="option in currentExamQuestion.options"
                  :key="option.id"
                  :label="option.optionKey"
                >
                  {{ option.optionKey }}. {{ option.content }}
                </el-radio>
              </el-radio-group>
            </div>
            
            <!-- 判断题 -->
            <div v-else-if="currentExamQuestion.type === 'TRUE_FALSE'" class="question-options">
              <el-radio-group
                v-model="userAnswers[currentQuestionIndex]"
                @change="onAnswerChange"
              >
                <el-radio label="正确">正确</el-radio>
                <el-radio label="错误">错误</el-radio>
              </el-radio-group>
            </div>
            
            <!-- 填空题和问答题 -->
            <div v-else-if="currentExamQuestion.type === 'FILL_BLANK' || currentExamQuestion.type === 'ESSAY'" class="question-options">
              <el-input
                v-model="userAnswers[currentQuestionIndex]"
                :type="currentExamQuestion.type === 'FILL_BLANK' ? 'text' : 'textarea'"
                :rows="currentExamQuestion.type === 'ESSAY' ? 6 : 2"
                placeholder="请输入答案"
                @input="onAnswerChange"
              />
            </div>
          </div>
          
          <div class="question-actions">
            <el-button @click="prevQuestion" :disabled="currentQuestionIndex === 0">上一题</el-button>
            <el-button @click="nextQuestion" :disabled="currentQuestionIndex === examQuestions.length - 1">下一题</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 考试结果 -->
    <el-dialog title="考试结果" v-model="showResult" width="50%">
      <div class="result-content">
        <h3>考试完成！</h3>
        <p>得分：{{ score }} / {{ totalScore }}</p>
        <p>正确率：{{ accuracy }}%</p>
        <p>用时：{{ usedTime }}分钟</p>
      </div>
      <template #footer>
        <el-button @click="finishExam">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'MockExam',
  data() {
    return {
      isInExam: false,
      subjects: [],
      examConfig: {
        subjectId: '',
        questionCount: 20,
        questionTypes: ['SINGLE_CHOICE', 'TRUE_FALSE'],
        difficulty: '', // 添加难度筛选
        duration: 60
      },
      examQuestions: [],
      currentQuestionIndex: 0,
      userAnswers: [],
      remainingTime: 0,
      timer: null,
      showResult: false,
      score: 0,
      totalScore: 0,
      usedTime: 0,
      currentExamPaper: {}
    }
  },
  computed: {
    currentExamQuestion() {
      return this.examQuestions[this.currentQuestionIndex] || {}
    },
    canStartExam() {
      return this.examConfig.subjectId && this.examConfig.questionTypes.length > 0
    },
    accuracy() {
      if (this.totalScore === 0) return 0
      return parseFloat(((this.score / this.totalScore) * 100).toFixed(2))
    }
  },
  created() {
    this.loadSubjects()
  },
  beforeUnmount() {
    if (this.timer) {
      clearInterval(this.timer)
    }
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
    
    // 开始考试
    async startExam() {
      if (!this.canStartExam) {
        this.$message.warning('请完成考试配置')
        return
      }
      
      try {
        // 根据配置生成考试题目
        const params = {
          subjectId: this.examConfig.subjectId,
          questionCount: this.examConfig.questionCount,
          questionTypes: this.examConfig.questionTypes.join(','),
          difficulty: this.examConfig.difficulty // 添加难度参数
        }
        
        const response = await request.post('/exam/paper/generate', {}, { params })
        this.examQuestions = response.data
        
        if (this.examQuestions.length === 0) {
          this.$message.warning('该学科下没有找到符合条件的题目')
          return
        }
        
        // 初始化用户答案
        this.userAnswers = new Array(this.examQuestions.length).fill('')
        
        // 设置考试时长
        this.remainingTime = this.examConfig.duration * 60 // 转换为秒
        
        // 开始计时
        this.startTimer()
        
        // 设置考试状态
        this.isInExam = true
        
        // 设置考试信息
        const subject = this.subjects.find(s => s.id === this.examConfig.subjectId)
        this.currentExamPaper = {
          name: subject.name + '模拟考试',
          subjectId: this.examConfig.subjectId,
          duration: this.examConfig.duration,
          totalQuestions: this.examQuestions.length
        }
      } catch (error) {
        console.error('开始考试失败:', error)
        this.$message.error('开始考试失败')
      }
    },
    
    // 开始计时
    startTimer() {
      if (this.timer) {
        clearInterval(this.timer)
      }
      
      this.timer = setInterval(() => {
        this.remainingTime--
        if (this.remainingTime <= 0) {
          this.timeUp()
        }
      }, 1000)
    },
    
    // 时间到
    timeUp() {
      clearInterval(this.timer)
      this.submitExam()
    },
    
    // 格式化时间
    formatTime(seconds) {
      const mins = Math.floor(seconds / 60)
      const secs = seconds % 60
      return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
    },
    
    // 跳转到指定题目
    goToQuestion(index) {
      this.currentQuestionIndex = index
    },
    
    // 上一题
    prevQuestion() {
      if (this.currentQuestionIndex > 0) {
        this.currentQuestionIndex--
      }
    },
    
    // 下一题
    nextQuestion() {
      if (this.currentQuestionIndex < this.examQuestions.length - 1) {
        this.currentQuestionIndex++
      }
    },
    
    // 答案变更
    onAnswerChange() {
      // 这里可以添加保存答案的逻辑
    },
    
    // 提交考试
    async submitExam() {
      clearInterval(this.timer)
      
      // 计算得分
      this.calculateScore()
      
      // 显示结果
      this.showResult = true
      
      // 保存考试记录
      await this.saveExamRecord()
    },
    
    // 计算得分
    calculateScore() {
      this.totalScore = this.examQuestions.length * 5 // 每题5分
      this.score = 0
      
      for (let i = 0; i < this.examQuestions.length; i++) {
        const userAnswer = Array.isArray(this.userAnswers[i]) ? this.userAnswers[i].join('') : this.userAnswers[i]
        const correctAnswer = this.examQuestions[i].answer
        
        if (userAnswer === correctAnswer) {
          this.score += 5
        }
      }
      
      this.usedTime = this.examConfig.duration - Math.ceil((this.remainingTime < 0 ? 0 : this.remainingTime) / 60)
    },
    
    // 保存考试记录
    async saveExamRecord() {
      try {
        const userId = localStorage.getItem('userId') || JSON.parse(localStorage.getItem('system-user')).userId
        if (!userId) return
        
        const examRecord = {
          userId: parseInt(userId),
          username: JSON.parse(localStorage.getItem('system-user')).name,
          paperId: 0, // 暂时没有实际试卷ID
          paperName: this.currentExamPaper.name,
          subjectId: this.currentExamPaper.subjectId,
          subjectName: this.subjects.find(s => s.id === this.currentExamPaper.subjectId)?.name,
          score: this.score,
          totalScore: this.totalScore,
          accuracy: Math.round((this.score / this.totalScore) * 100),
          duration: this.usedTime,
          examTime: new Date()
        }
        
        await request.post('/exam/record', examRecord)
      } catch (error) {
        console.error('保存考试记录失败:', error)
      }
    },
    
    // 完成考试
    finishExam() {
      this.showResult = false
      this.isInExam = false
      this.examQuestions = []
      this.userAnswers = []
      this.currentQuestionIndex = 0
      this.remainingTime = 0
      
      if (this.timer) {
        clearInterval(this.timer)
        this.timer = null
      }
    }
  }
}
</script>

<style scoped>
.mock-exam-container {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.header {
  margin-bottom: 20px;
}

.config-card {
  max-width: 600px;
  margin: 0 auto;
}

.config-form {
  margin-top: 20px;
}

.exam-content {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.exam-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background-color: #409EFF;
  color: white;
}

.time-info {
  display: flex;
  gap: 20px;
}

.exam-body {
  display: flex;
  min-height: 500px;
}

.question-list {
  width: 150px;
  background-color: #f5f5f5;
  padding: 20px 10px;
  overflow-y: auto;
}

.question-nav-item {
  width: 30px;
  height: 30px;
  line-height: 30px;
  text-align: center;
  border-radius: 50%;
  background-color: #dcdfe6;
  color: #606266;
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 0.3s;
}

.question-nav-item:hover {
  background-color: #a0cfff;
  color: white;
}

.question-nav-item.current {
  background-color: #409EFF;
  color: white;
}

.question-nav-item.answered {
  background-color: #67c23a;
  color: white;
}

.question-area {
  flex: 1;
  padding: 20px;
}

.question-content {
  margin-bottom: 30px;
}

.question-options {
  margin-top: 15px;
}

.question-actions {
  text-align: center;
  margin-top: 30px;
}

.result-content {
  text-align: center;
  padding: 20px 0;
}

.result-content h3 {
  margin-bottom: 15px;
}

.result-content p {
  margin: 10px 0;
  font-size: 16px;
}
</style>