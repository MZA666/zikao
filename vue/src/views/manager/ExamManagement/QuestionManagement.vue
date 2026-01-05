<template>
  <div class="question-management">
    <div class="toolbar">
      <el-row :gutter="20">
        <el-col :span="4">
          <el-select v-model="searchSubjectId" placeholder="选择学科" clearable>
            <el-option
              v-for="subject in subjects"
              :key="subject.id"
              :label="subject.name"
              :value="subject.id"
            />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="searchType" placeholder="题目类型" clearable>
            <el-option label="单选题" value="SINGLE_CHOICE" />
            <el-option label="多选题" value="MULTIPLE_CHOICE" />
            <el-option label="判断题" value="TRUE_FALSE" />
            <el-option label="填空题" value="FILL_BLANK" />
            <el-option label="问答题" value="ESSAY" />
          </el-select>
        </el-col>
        <el-col :span="8">
          <el-input v-model="searchContent" placeholder="搜索题目内容" clearable />
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="loadQuestions">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="showAddQuestionDialog">添加题目</el-button>
        </el-col>
      </el-row>
    </div>

    <el-table :data="questions" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="subjectId" label="学科" :formatter="formatSubject" />
      <el-table-column prop="type" label="类型" :formatter="formatType" />
      <el-table-column prop="content" label="题目内容" show-overflow-tooltip />
      <el-table-column prop="difficulty" label="难度" width="100">
        <template #default="scope">
          <el-tag :type="getDifficultyType(scope.row.difficulty)">{{ getDifficultyText(scope.row.difficulty) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button size="small" @click="editQuestion(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteQuestion(scope.row.id)">删除</el-button>
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

    <!-- 添加/编辑题目对话框 -->
    <el-dialog :title="isEdit ? '编辑题目' : '添加题目'" v-model="dialogVisible" width="80%" top="5vh">
      <el-form :model="questionForm" :rules="questionRules" ref="questionFormRef" label-width="100px">
        <el-form-item label="学科" prop="subjectId">
          <el-select v-model="questionForm.subjectId" placeholder="请选择学科">
            <el-option
              v-for="subject in subjects"
              :key="subject.id"
              :label="subject.name"
              :value="subject.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="题目类型" prop="type">
          <el-select v-model="questionForm.type" placeholder="请选择题目类型" @change="onTypeChange">
            <el-option label="单选题" value="SINGLE_CHOICE" />
            <el-option label="多选题" value="MULTIPLE_CHOICE" />
            <el-option label="判断题" value="TRUE_FALSE" />
            <el-option label="填空题" value="FILL_BLANK" />
            <el-option label="问答题" value="ESSAY" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度" prop="difficulty">
          <el-select v-model="questionForm.difficulty" placeholder="请选择难度">
            <el-option label="简单" value="EASY" />
            <el-option label="中等" value="MEDIUM" />
            <el-option label="困难" value="HARD" />
          </el-select>
        </el-form-item>
        <el-form-item label="题目内容" prop="content">
          <el-input v-model="questionForm.content" type="textarea" :rows="4" placeholder="请输入题目内容" />
        </el-form-item>
        
        <!-- 选择题选项 -->
        <div v-if="questionForm.type === 'SINGLE_CHOICE' || questionForm.type === 'MULTIPLE_CHOICE'">
          <el-form-item label="选项">
            <div v-for="(option, index) in questionForm.options" :key="option.key" class="option-item">
              <el-input v-model="option.optionKey" style="width: 60px; margin-right: 10px;" placeholder="选项键" />
              <el-input v-model="option.content" style="flex: 1; margin-right: 10px;" placeholder="选项内容" />
              <el-checkbox v-model="option.isCorrect">正确答案</el-checkbox>
              <el-button type="danger" size="small" @click="removeOption(index)" style="margin-left: 10px;">删除</el-button>
            </div>
            <el-button @click="addOption" style="margin-top: 10px;">添加选项</el-button>
          </el-form-item>
        </div>
        
        <el-form-item label="答案" prop="answer">
          <el-input v-model="questionForm.answer" type="textarea" :rows="2" placeholder="请输入答案" />
        </el-form-item>
        <el-form-item label="解析" prop="analysis">
          <el-input v-model="questionForm.analysis" type="textarea" :rows="3" placeholder="请输入解析" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveQuestion">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'QuestionManagement',
  data() {
    return {
      questions: [],
      subjects: [],
      loading: false,
      currentPage: 1,
      pageSize: 10,
      total: 0,
      searchSubjectId: '',
      searchType: '',
      searchContent: '',
      dialogVisible: false,
      isEdit: false,
      questionForm: {
        id: null,
        subjectId: '',
        type: '',
        content: '',
        answer: '',
        analysis: '',
        difficulty: 'MEDIUM',
        options: []
      },
      questionRules: {
        subjectId: [
          { required: true, message: '请选择学科', trigger: 'change' }
        ],
        type: [
          { required: true, message: '请选择题目类型', trigger: 'change' }
        ],
        content: [
          { required: true, message: '请输入题目内容', trigger: 'blur' }
        ]
      }
    }
  },
  mounted() {
    this.loadSubjects()
    this.loadQuestions()
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
    
    // 加载题目列表
    async loadQuestions() {
      this.loading = true
      try {
        const params = {
          subjectId: this.searchSubjectId || undefined,
          type: this.searchType || undefined,
          content: this.searchContent || undefined,
          pageNum: this.currentPage,
          pageSize: this.pageSize
        }
        
        const response = await request.get('/exam/question/list', { params })
        this.questions = Array.isArray(response.data.list) ? response.data.list : response.data
        this.total = response.data.total || this.questions.length
      } catch (error) {
        console.error('加载题目列表失败:', error)
        this.$message.error('加载题目列表失败')
      } finally {
        this.loading = false
      }
    },
    
    // 重置搜索
    resetSearch() {
      this.searchSubjectId = ''
      this.searchType = ''
      this.searchContent = ''
      this.currentPage = 1
      this.loadQuestions()
    },
    
    // 显示添加题目对话框
    showAddQuestionDialog() {
      this.isEdit = false
      this.resetQuestionForm()
      this.dialogVisible = true
    },
    
    // 编辑题目
    editQuestion(question) {
      this.isEdit = true
      // 深拷贝题目信息
      this.questionForm = {
        ...question,
        options: question.options ? [...question.options] : []
      }
      this.dialogVisible = true
    },
    
    // 重置题目表单
    resetQuestionForm() {
      this.questionForm = {
        id: null,
        subjectId: '',
        type: '',
        content: '',
        answer: '',
        analysis: '',
        difficulty: 'MEDIUM',
        options: []
      }
    },
    
    // 类型改变事件
    onTypeChange(type) {
      // 根据类型初始化选项
      if (type === 'SINGLE_CHOICE' || type === 'MULTIPLE_CHOICE') {
        if (this.questionForm.options.length === 0) {
          // 初始化4个选项
          for (let i = 0; i < 4; i++) {
            this.questionForm.options.push({
              id: null,
              optionKey: String.fromCharCode(65 + i), // A, B, C, D
              content: '',
              isCorrect: false
            })
          }
        }
      }
    },
    
    // 添加选项
    addOption() {
      this.questionForm.options.push({
        id: null,
        optionKey: String.fromCharCode(65 + this.questionForm.options.length),
        content: '',
        isCorrect: false
      })
    },
    
    // 删除选项
    removeOption(index) {
      this.questionForm.options.splice(index, 1)
    },
    
    // 保存题目
    async saveQuestion() {
      try {
        await this.$refs.questionFormRef.validate()
        
        // 准备提交的数据
        const submitData = { ...this.questionForm }
        
        if (this.isEdit) {
          await request.put('/exam/question', submitData)
          this.$message.success('更新题目成功')
        } else {
          await request.post('/exam/question', submitData)
          this.$message.success('添加题目成功')
        }
        
        this.dialogVisible = false
        this.loadQuestions()
      } catch (error) {
        console.error('保存题目失败:', error)
        this.$message.error('保存题目失败')
      }
    },
    
    // 删除题目
    async deleteQuestion(id) {
      try {
        await this.$confirm('确定要删除这个题目吗？', '确认删除', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await request.delete(`/exam/question/${id}`)
        this.$message.success('删除题目成功')
        this.loadQuestions()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除题目失败:', error)
          this.$message.error('删除题目失败')
        }
      }
    },
    
    // 格式化学科名称
    formatSubject(row, column, cellValue) {
      const subject = this.subjects.find(s => s.id === cellValue)
      return subject ? subject.name : '未知学科'
    },
    
    // 格式化题目类型
    formatType(row, column, cellValue) {
      const typeMap = {
        'SINGLE_CHOICE': '单选题',
        'MULTIPLE_CHOICE': '多选题',
        'TRUE_FALSE': '判断题',
        'FILL_BLANK': '填空题',
        'ESSAY': '问答题'
      }
      return typeMap[cellValue] || cellValue
    },
    
    // 获取难度类型
    getDifficultyType(difficulty) {
      const typeMap = {
        'EASY': 'success',
        'MEDIUM': 'warning',
        'HARD': 'danger'
      }
      return typeMap[difficulty] || 'info'
    },
    
    // 获取难度文本
    getDifficultyText(difficulty) {
      const textMap = {
        'EASY': '简单',
        'MEDIUM': '中等',
        'HARD': '困难'
      }
      return textMap[difficulty] || difficulty
    },
    
    // 分页相关方法
    handleSizeChange(newSize) {
      this.pageSize = newSize
      this.loadQuestions()
    },
    
    handleCurrentChange(newPage) {
      this.currentPage = newPage
      this.loadQuestions()
    }
  }
}
</script>

<style scoped>
.question-management {
  padding: 20px 0;
}

.toolbar {
  margin-bottom: 20px;
}

.option-item {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.pagination {
  text-align: center;
  margin-top: 20px;
}
</style>