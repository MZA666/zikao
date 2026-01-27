<template>
  <div class="exam-bank-container">
    <div class="header">
      <h2>题库</h2>
    </div>

    <!-- 搜索和筛选区域 -->
    <div class="filter-section">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-autocomplete
            v-model="searchSubjectName"
            :fetch-suggestions="querySubjects"
            placeholder="选择或输入学科"
            clearable
            @select="onSubjectSelect"
            @change="onSubjectChange"
            @clear="onSubjectClear"
            style="width: 100%;"
          >
            <template #default="{ item }">
              <div class="value">{{ item.name }}</div>
            </template>
          </el-autocomplete>
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
          <h3 class="bank-name">{{ bank.bankName || (bank.subject ? bank.subject + '题库' : getSubjectName(bank.subjectId) + '题库') }}</h3>
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
            <span class="value">{{ bank.subject || getSubjectName(bank.subjectId) }}</span>
          </div>
          <div class="info-item">
            <span class="label">专业：</span>
            <span class="value">{{ bank.majorName || getMajorName(bank.majorId) }}</span>
          </div>
          <div class="info-item">
            <span class="label">题目数量：</span>
            <span class="value">{{ bank.questionCount || '未知' }}</span>
          </div>
          <div class="info-item" v-if="bank.uploader">
            <span class="label">上传者：</span>
            <span class="value">{{ bank.uploader }}</span>
          </div>
          <div class="info-item" v-if="bank.uploadTime">
            <span class="label">上传时间：</span>
            <span class="value">{{ formatDate(bank.uploadTime) }}</span>
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
              @change="onSingleChoiceAnswerChange"
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
          
          <!-- 显示答案和解析 -->
          <div v-if="showAnswer" class="answer-section">
            <div class="correct-answer">
              <strong>正确答案：</strong>
              <span v-if="currentQuestion.type === 'SINGLE_CHOICE' || currentQuestion.type === 'MULTIPLE_CHOICE'">
                {{ formatAnswer(currentQuestion.answer) }}
              </span>
              <span v-else-if="currentQuestion.type === 'TRUE_FALSE'">
                {{ currentQuestion.answer }}
              </span>
              <span v-else>
                {{ currentQuestion.answer }}
              </span>
            </div>
            <div v-if="currentQuestion.analysis" class="analysis">
              <strong>解析：</strong>{{ currentQuestion.analysis }}
            </div>
            <div class="check-answer-status">
              <el-tag :type="checkAnswerCorrect() ? 'success' : 'danger'">
                {{ checkAnswerCorrect() ? '回答正确' : '回答错误' }}
              </el-tag>
            </div>
          </div>
        </div>
        
        <div class="practice-actions">
          <el-button @click="prevQuestion" :disabled="currentQuestionIndex === 0">上一题</el-button>
          <el-button @click="nextQuestion" :disabled="currentQuestionIndex === currentBankQuestions.length - 1">下一题</el-button>
          <el-button v-if="!showAnswer" type="primary" @click="showCorrectAnswer">显示答案</el-button>
          <el-button v-else type="primary" @click="hideAnswer">隐藏答案</el-button>
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
      searchSubjectName: '', // 用于autocomplete的学科名称
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
      isCollecting: {}, // 记录正在收藏的题库
      showAnswer: false // 是否显示答案
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
    this.loadMajors() // 加载专业列表
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

    // 加载专业列表
    async loadMajors() {
      try {
        // 使用正确的API端点来获取专业列表
        const response = await request.get('/major-management/majors')
        this.majors = response.data || []
      } catch (error) {
        console.error('加载专业列表失败:', error)
        // 如果API不存在或出错，初始化为空数组
        this.majors = []
      }
    },
    
    // 查询学科建议
    querySubjects(queryString, cb) {
      let results = queryString ? 
        this.subjects.filter(item => item.name.toLowerCase().indexOf(queryString.toLowerCase()) !== -1) : 
        this.subjects;
      cb(results);
    },
    
    // 学科选择事件（当选中下拉选项时触发）
    onSubjectSelect(item) {
      this.searchSubjectId = item.id;
      this.searchSubjectName = item.name;
      this.loadExamBanks();
    },
    
    // 学科改变事件（当输入值改变时触发）
    onSubjectChange(value) {
      // 如果输入的值匹配某个学科名称，则设置对应的ID
      const matchedSubject = this.subjects.find(subject => subject.name === value);
      if (matchedSubject) {
        this.searchSubjectId = matchedSubject.id;
      } else {
        // 如果输入的是自定义值，暂时清空subjectId
        this.searchSubjectId = '';
      }
      // 触发搜索
      this.loadExamBanks();
    },
    
    // 学科清除事件
    onSubjectClear() {
      this.searchSubjectId = '';
      this.searchSubjectName = '';
      this.loadExamBanks();
    },
    
    // 加载题库列表
    async loadExamBanks() {
      try {
        // 从exam_bank表查询相关记录
        const params = {
          pageNum: this.currentPage,
          pageSize: this.pageSize
        }
        
        // 添加搜索条件
        if (this.searchSubjectId) {
          params.subjectId = this.searchSubjectId
        } else if (this.searchSubjectName) {
          // 如果没有找到匹配的学科ID但有学科名称，尝试用名称搜索
          const matchedSubject = this.subjects.find(subject => subject.name === this.searchSubjectName);
          if (matchedSubject) {
            params.subjectId = matchedSubject.id;
          }
        }
        
        if (this.searchBankName) {
          params.bankName = this.searchBankName
        }
        
        // 优先从exam_bank表获取数据
        const response = await request.get('/exam/bank/list', { params })
        
        // 检查响应是否包含分页信息
        let examBanks = []
        let totalCount = 0
        
        if (response.data && response.data.list !== undefined) {
          // 有分页信息
          examBanks = response.data.list || []
          totalCount = response.data.total || 0
          this.currentPage = response.data.pageNum || 1
          this.pageSize = response.data.pageSize || 10
        } else {
          // 没有分页信息，直接是数组
          examBanks = Array.isArray(response.data) ? response.data : response.data || []
          totalCount = examBanks.length
        }
        
        // 如果没有获取到数据，尝试使用统计接口
        if (!examBanks || examBanks.length === 0) {
          const statsParams = {}
          if (this.searchSubjectId) {
            statsParams.subjectId = this.searchSubjectId
          } else if (this.searchSubjectName) {
            const matchedSubject = this.subjects.find(subject => subject.name === this.searchSubjectName);
            if (matchedSubject) {
              statsParams.subjectId = matchedSubject.id;
            }
          }
          
          const statsResponse = await request.get('/exam/bank/stats', { params: statsParams })
          examBanks = Array.isArray(statsResponse.data) ? statsResponse.data : statsResponse.data || []
          totalCount = examBanks.length
        }
        
        // 如果仍然没有数据，通过题目统计构建虚拟题库
        if (!examBanks || examBanks.length === 0) {
          examBanks = await this.loadFromQuestionStats()
          totalCount = examBanks.length
        }
        
        this.examBanks = examBanks
        this.total = totalCount
        
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
        // 使用新的统计接口
        const params = {}
        if (this.searchSubjectId) {
          params.subjectId = this.searchSubjectId
        } else if (this.searchSubjectName) {
          // 如果没有找到匹配的学科ID但有学科名称，尝试查找
          const matchedSubject = this.subjects.find(subject => subject.name === this.searchSubjectName);
          if (matchedSubject) {
            params.subjectId = matchedSubject.id;
          }
        }
        
        const response = await request.get('/exam/question/stats-by-uploader', { params })
        const stats = Array.isArray(response.data) ? response.data : response.data || []
        
        // 从exam_bank表查询实际存在的题库
        const allBanksResponse = await request.get('/exam/bank/list', { 
          params: { 
            pageNum: 1, 
            pageSize: 1000 // 获取所有题库
          } 
        });
        
        const allBanks = allBanksResponse.data?.list || allBanksResponse.data || [];
        
        // 将统计信息与实际题库匹配
        const examBanks = stats.map(stat => {
          // 尝试找到匹配的实际题库
          const matchingBank = allBanks.find(bank => 
            bank.subjectId === stat.subjectId && 
            (bank.uploaderId === stat.uploaderId || 
             (bank.uploader && stat.uploader && bank.uploader === stat.uploader))
          );
          
          if (matchingBank) {
            // 如果找到匹配的实际题库，使用它的信息
            return matchingBank;
          } else {
            // 如果没找到匹配的实际题库，跳过此项
            return null;
          }
        }).filter(Boolean); // 过滤掉null值
        
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

        // 获取用户收藏的题库信息
        const response = await request.get(`/exam/bank/user/${userId}/collections`)
        const collectedBanks = response.data || []

        // 更新收藏状态 - 根据返回的详细信息处理
        const newIsCollected = {}
        this.examBanks.forEach(bank => {
          // 检查是否在收藏列表中
          const isCollected = collectedBanks.some(collectedBank => {
            return collectedBank.bankId === bank.bankId
          })
          // 为虚拟题库或实际题库设置收藏状态
          if (bank.bankId) {
            newIsCollected[bank.bankId] = isCollected
          }
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
        this.isCollecting[bank.bankId] = true

        if (this.isCollected[bank.bankId]) {
          // 取消收藏
          const response = await request.delete(`/exam/bank/collection/${userId}/${bank.bankId}`)
          if (response.code === 200) {
            this.$message.success('已取消收藏')
            this.isCollected[bank.bankId] = false
          } else {
            this.$message.error(response.msg || '取消收藏失败')
          }
        } else {
          // 添加收藏
          const collectionData = {
            userId: parseInt(userId),
            bankId: bank.bankId,
            bankName: bank.bankName,
            subjectId: bank.subjectId,
            uploaderId: bank.uploaderId || null
          }
          
          // 如果是虚拟题库ID，解析学科ID和上传者ID
          if (bank.bankId.startsWith && bank.bankId.startsWith('virtual_')) {
            const parts = bank.bankId.split('_')
            if (parts.length >= 3) {
              try {
                collectionData.subjectId = parseInt(parts[1])
                collectionData.uploaderId = parts[2] !== 'unknown' ? parseInt(parts[2]) : null
              } catch (error) {
                console.error('解析虚拟题库ID失败:', error)
                this.$message.error('虚拟题库ID格式错误')
                return
              }
            }
          } else {
            // 对于实际的题库ID，确保学科ID和上传者ID已设置
            collectionData.subjectId = bank.subjectId
            collectionData.uploaderId = bank.uploaderId || null
          }
          
          const response = await request.post('/exam/bank/collection', collectionData)
          if (response.code === 200) {
            this.$message.success('收藏成功')
            this.isCollected[bank.bankId] = true
          } else {
            this.$message.error(response.msg || '收藏失败')
          }
        }
        
        // 收藏状态变更后，重新检查收藏状态以确保界面立即更新
        await this.$nextTick(); // 等待DOM更新完成
        this.checkCollectionStatus();
      } catch (error) {
        console.error('收藏操作失败:', error)
        this.$message.error('收藏操作失败')
      } finally {
        // 清除加载状态
        this.isCollecting[bank.bankId] = false
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
    
    // 重新加载题库列表及收藏状态
    async refreshExamBanks() {
      await this.loadExamBanks();
      await this.$nextTick(); // 等待DOM更新完成
      this.checkCollectionStatus();
    },
    
    // 开始练习
    async startPractice(bank) {
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
        this.showAnswer = false // 切换题目时隐藏答案
      }
    },
    
    // 下一题
    nextQuestion() {
      if (this.currentQuestionIndex < this.currentBankQuestions.length - 1) {
        this.currentQuestionIndex++
        this.currentQuestion = this.currentBankQuestions[this.currentQuestionIndex]
        this.showAnswer = false // 切换题目时隐藏答案
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
      this.showAnswer = false
    },
    
    // 当用户选择或输入答案时触发（用于非单选题）
    onAnswerChange() {
      // 此方法不再自动显示答案，仅保留供其他用途
    },
    
    // 当用户选择单选题答案时触发
    onSingleChoiceAnswerChange() {
      // 单选题用户作答后自动显示答案
      if (this.userAnswers[this.currentQuestionIndex] !== '') {
        this.showAnswer = true
      }
    },
    
    // 显示正确答案
    showCorrectAnswer() {
      this.showAnswer = true
    },
    
    // 隐藏答案
    hideAnswer() {
      this.showAnswer = false
    },
    
    // 格式化答案显示
    formatAnswer(answer) {
      if (Array.isArray(answer)) {
        return answer.join(', ')
      }
      return answer
    },
    
    // 检查答案是否正确
    checkAnswerCorrect() {
      const userAnswer = this.userAnswers[this.currentQuestionIndex]
      const correctAnswer = this.currentQuestion.answer
      
      if (Array.isArray(userAnswer) && Array.isArray(correctAnswer)) {
        return userAnswer.sort().join(',') === correctAnswer.sort().join(',')
      } else {
        return userAnswer === correctAnswer
      }
    },
    
    // 分页相关方法
    handleSizeChange(newSize) {
      this.pageSize = newSize
      this.refreshExamBanks()
    },
    
    handleCurrentChange(newPage) {
      this.currentPage = newPage
      this.refreshExamBanks()
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

.answer-section {
  margin-top: 20px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 8px;
  border-left: 4px solid #409eff;
}

.correct-answer {
  margin-bottom: 10px;
}

.analysis {
  margin-bottom: 10px;
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
}

.check-answer-status {
  text-align: right;
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

.el-autocomplete {
  width: 100%;
}
</style>