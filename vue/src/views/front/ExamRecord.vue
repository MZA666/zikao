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
             <!-- 显示题目选项 -->
            <div class="question-options" v-if="getQuestionOptions(answer.questionId).length > 0">
              <div 
                v-for="option in getQuestionOptions(answer.questionId)" 
                :key="option.id"
                class="option-item"
                :class="{ 
                  'user-selected': isUserSelected(answer, option.optionKey),
                  'correct-option': option.isCorrect 
                }"
              >
                <strong>{{ option.optionKey }}.</strong> {{ option.content }}
              </div>
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
        
        <div class="review-controls" v-if="selectedRecord.answers && selectedRecord.answers.length > 0">
          <el-checkbox v-model="wrongOnly" @change="handleWrongOnlyChange">只显示错题</el-checkbox>
        </div>
        <div class="review-questions" v-if="selectedRecord.answers && selectedRecord.answers.length > 0">
          <div
            v-for="(answer, index) in filteredAnswers"
            :key="index"
            class="review-question"
            :class="{ 'wrong-answer': !isCorrect(answer) }"
          >
            <h4>{{ getQuestionNumber(index) }}. {{ answer.questionContent }}</h4>
            <!-- 显示题目选项 -->
            <div class="question-options" v-if="getQuestionOptions(answer.questionId).length > 0">
              <div 
                v-for="option in getQuestionOptions(answer.questionId)" 
                :key="option.id"
                class="option-item"
                :class="{ 
                  'user-selected': isUserSelected(answer, option.optionKey),
                  'correct-option': option.isCorrect 
                }"
              >
                <strong>{{ option.optionKey }}.</strong> {{ option.content }}
              </div>
            </div>
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
      wrongOnly: false,  // 是否只显示错题
      selectedRecord: null,
      questionOptions: {} // 缓存题目选项
    }
  },
  computed: {
    // 根据wrongOnly标志过滤答案
    filteredAnswers() {
      if (!this.wrongOnly || !this.selectedRecord || !this.selectedRecord.answers) {
        return this.selectedRecord && this.selectedRecord.answers ? this.selectedRecord.answers : [];
      }
      return this.selectedRecord.answers.filter(answer => !this.isCorrect(answer));
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
        
        const response = await request.get('/exam/record/user/' + userId, { 
          params: {
            pageNum: this.currentPage,
            pageSize: this.pageSize
          }
        })
        this.examRecords = Array.isArray(response.data.list) ? response.data.list : response.data
        this.total = response.data.total || this.examRecords.length
      } catch (error) {
        console.error('加载考试记录失败:', error)
        this.$message.error('加载考试记录失败')
      }
    },
    
    // 查看记录详情
    async viewRecord(record) {
      this.selectedRecord = record
      // 为每道题加载选项
      if (record.answers) {
        for (const answer of record.answers) {
          await this.loadQuestionOptions(answer.questionId);
        }
      }
      this.detailDialogVisible = true
    },
    
    // 回顾考试
    async reviewExam(record) {
      this.selectedRecord = record
      // 为每道题加载选项
      if (record.answers) {
        for (const answer of record.answers) {
          await this.loadQuestionOptions(answer.questionId);
        }
      }
      this.reviewDialogVisible = true
    },
    
    // 加载题目选项
    async loadQuestionOptions(questionId) {
      console.log('正在加载题目选项，题目ID:', questionId);
      if (!this.questionOptions[questionId]) {
        try {
          const response = await request.get(`/exam/question/options/${questionId}`);
          console.log(`题目${questionId}的选项数据:`, response.data);
          this.questionOptions[questionId] = response.data;
        } catch (error) {
          console.error(`加载题目${questionId}的选项失败:`, error);
          this.questionOptions[questionId] = [];
        }
      } else {
        console.log(`题目${questionId}的选项已缓存`);
      }
    },
    
    // 获取题目选项
    getQuestionOptions(questionId) {
      if (!questionId) {
        console.log('题目ID为空，返回空数组');
        return [];
      }
      const options = this.questionOptions[questionId] || [];
      console.log(`获取题目${questionId}的选项，选项数量:`, options.length);
      return options;
    },
    
    // 检查用户是否选择了某个选项
    isUserSelected(answer, optionKey) {
      console.log('检查用户选择，answer:', answer, 'optionKey:', optionKey);
      if (!answer || !answer.userAnswer || !optionKey) {
        console.log('答案或选项键为空');
        return false;
      }
      
      // 如果是多选题，userAnswer可能是数组
      if (Array.isArray(answer.userAnswer)) {
        const isSelected = answer.userAnswer.includes(optionKey);
        console.log(`多选题，用户选择: ${answer.userAnswer}, 检查选项${optionKey}，结果: ${isSelected}`);
        return isSelected;
      } else {
        // 单选题或字符串形式的答案
        const isSelected = String(answer.userAnswer).includes(optionKey);
        console.log(`单选题，用户答案: ${answer.userAnswer}, 检查选项${optionKey}，结果: ${isSelected}`);
        return isSelected;
      }
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
    
    // 判断答案是否正确
    isCorrect(answer) {
      if (!answer.userAnswer || !answer.correctAnswer) {
        return false;
      }
      // 处理多选题，比较排序后的选项
      const userAnswer = Array.isArray(answer.userAnswer) ? 
        answer.userAnswer.sort().join('') : 
        String(answer.userAnswer).split('').sort().join('');
      const correctAnswer = Array.isArray(answer.correctAnswer) ? 
        answer.correctAnswer.sort().join('') : 
        String(answer.correctAnswer).split('').sort().join('');
      
      return userAnswer === correctAnswer;
    },
    
    // 获取题目编号（考虑过滤后的情况）
    getQuestionNumber(index) {
      if (!this.wrongOnly) {
        // 如果不是只显示错题，则返回原始索引+1
        return index + 1;
      } else {
        // 如果是只显示错题，则计算这是第几道错题
        const wrongAnswers = this.selectedRecord.answers.filter(ans => !this.isCorrect(ans));
        if (wrongAnswers[index]) {
          // 找到原数组中的位置
          const originalIndex = this.selectedRecord.answers.indexOf(wrongAnswers[index]);
          return originalIndex + 1;
        }
        return index + 1;
      }
    },
    
    // 处理错题筛选变化
    handleWrongOnlyChange() {
      // 当筛选条件改变时不需要额外操作，因为computed会自动更新
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

.question-options {
  margin: 10px 0;
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 4px;
}

.option-item {
  padding: 5px 0;
  display: flex;
  align-items: flex-start;
}

.option-item strong {
  margin-right: 8px;
  min-width: 20px;
}

.option-item.user-selected {
  background-color: #e6f7ff;
  border-left: 3px solid #1890ff;
  padding-left: 12px;
}

.option-item.correct-option {
  background-color: #f6ffed;
  border-left: 3px solid #52c41a;
  padding-left: 12px;
}

.option-item.user-selected.correct-option {
  background-color: #e6f7ff;
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

.review-controls {
  margin-bottom: 15px;
}

.wrong-answer {
  border: 2px solid #f56c6c;
  background-color: #fef0f0;
}

.wrong-answer h4 {
  color: #f56c6c;
}

.pagination {
  text-align: center;
  margin-top: 20px;
}

.el-checkbox {
  margin-right: 20px;
}
</style>