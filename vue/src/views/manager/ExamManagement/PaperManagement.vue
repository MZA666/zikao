<template>
  <div class="paper-management">
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
        <el-col :span="10">
          <el-input v-model="searchName" placeholder="搜索试卷名称" clearable />
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="loadPapers">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="showAddPaperDialog">添加试卷</el-button>
        </el-col>
      </el-row>
    </div>

    <el-table :data="papers" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="试卷名称" show-overflow-tooltip />
      <el-table-column prop="subjectId" label="学科" :formatter="formatSubject" />
      <el-table-column prop="totalQuestions" label="题目数" width="100" />
      <el-table-column prop="totalScore" label="总分" width="100" />
      <el-table-column prop="duration" label="时长(分钟)" width="120" />
      <el-table-column prop="createdTime" label="创建时间" :formatter="formatDate" width="160" />
      <el-table-column label="操作" width="250">
        <template #default="scope">
          <el-button size="small" @click="editPaper(scope.row)">编辑</el-button>
          <el-button size="small" @click="viewQuestions(scope.row)">查看题目</el-button>
          <el-button size="small" type="danger" @click="deletePaper(scope.row.id)">删除</el-button>
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

    <!-- 添加/编辑试卷对话框 -->
    <el-dialog :title="isEdit ? '编辑试卷' : '添加试卷'" v-model="dialogVisible" width="600px">
      <el-form :model="paperForm" :rules="paperRules" ref="paperFormRef" label-width="100px">
        <el-form-item label="试卷名称" prop="name">
          <el-input v-model="paperForm.name" placeholder="请输入试卷名称" />
        </el-form-item>
        <el-form-item label="学科" prop="subjectId">
          <el-select v-model="paperForm.subjectId" placeholder="请选择学科">
            <el-option
              v-for="subject in subjects"
              :key="subject.id"
              :label="subject.name"
              :value="subject.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="题目数量">
          <el-input-number v-model="paperForm.totalQuestions" :min="1" :max="100" />
        </el-form-item>
        <el-form-item label="总分">
          <el-input-number v-model="paperForm.totalScore" :min="1" :max="1000" />
        </el-form-item>
        <el-form-item label="考试时长(分钟)">
          <el-input-number v-model="paperForm.duration" :min="10" :max="300" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="savePaper">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看试卷题目对话框 -->
    <el-dialog title="试卷题目" v-model="questionsDialogVisible" width="80%">
      <div v-if="currentPaperQuestions.length > 0">
        <div
          v-for="(question, index) in currentPaperQuestions"
          :key="question.id"
          class="question-item"
        >
          <h4>{{ index + 1 }}. {{ question.content }}</h4>
          <div class="question-info">
            <el-tag size="small" type="info">{{ getQuestionTypeText(question.type) }}</el-tag>
            <el-tag size="small" :type="getDifficultyType(question.difficulty)">{{ getDifficultyText(question.difficulty) }}</el-tag>
          </div>
        </div>
      </div>
      <div v-else class="no-questions">暂无题目</div>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'PaperManagement',
  data() {
    return {
      papers: [],
      subjects: [],
      loading: false,
      currentPage: 1,
      pageSize: 10,
      total: 0,
      searchSubjectId: '',
      searchName: '',
      dialogVisible: false,
      isEdit: false,
      questionsDialogVisible: false,
      currentPaperQuestions: [],
      paperForm: {
        id: null,
        name: '',
        subjectId: '',
        totalQuestions: 20,
        totalScore: 100,
        duration: 60
      },
      paperRules: {
        name: [
          { required: true, message: '请输入试卷名称', trigger: 'blur' }
        ],
        subjectId: [
          { required: true, message: '请选择学科', trigger: 'change' }
        ]
      }
    }
  },
  mounted() {
    this.loadSubjects()
    this.loadPapers()
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
    
    // 加载试卷列表
    async loadPapers() {
      this.loading = true
      try {
        const params = {
          subjectId: this.searchSubjectId || undefined,
          name: this.searchName || undefined,
          pageNum: this.currentPage,
          pageSize: this.pageSize
        }
        
        const response = await request.get('/exam/paper/list', { params })
        this.papers = Array.isArray(response.data.list) ? response.data.list : response.data
        this.total = response.data.total || this.papers.length
      } catch (error) {
        console.error('加载试卷列表失败:', error)
        this.$message.error('加载试卷列表失败')
      } finally {
        this.loading = false
      }
    },
    
    // 重置搜索
    resetSearch() {
      this.searchSubjectId = ''
      this.searchName = ''
      this.currentPage = 1
      this.loadPapers()
    },
    
    // 显示添加试卷对话框
    showAddPaperDialog() {
      this.isEdit = false
      this.paperForm = {
        id: null,
        name: '',
        subjectId: '',
        totalQuestions: 20,
        totalScore: 100,
        duration: 60
      }
      this.dialogVisible = true
    },
    
    // 编辑试卷
    editPaper(paper) {
      this.isEdit = true
      this.paperForm = { ...paper }
      this.dialogVisible = true
    },
    
    // 保存试卷
    async savePaper() {
      try {
        await this.$refs.paperFormRef.validate()
        
        if (this.isEdit) {
          await request.put('/exam/paper', this.paperForm)
          this.$message.success('更新试卷成功')
        } else {
          await request.post('/exam/paper', this.paperForm)
          this.$message.success('添加试卷成功')
        }
        
        this.dialogVisible = false
        this.loadPapers()
      } catch (error) {
        console.error('保存试卷失败:', error)
        this.$message.error('保存试卷失败')
      }
    },
    
    // 删除试卷
    async deletePaper(id) {
      try {
        await this.$confirm('确定要删除这个试卷吗？删除后无法恢复', '确认删除', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await request.delete(`/exam/paper/${id}`)
        this.$message.success('删除试卷成功')
        this.loadPapers()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除试卷失败:', error)
          this.$message.error('删除试卷失败')
        }
      }
    },
    
    // 查看试卷题目
    async viewQuestions(paper) {
      try {
        const response = await request.get(`/exam/paper/${paper.id}`)
        // 这里需要获取试卷的题目列表，但目前后端接口可能不支持
        // 先显示一个提示信息
        this.currentPaper = paper
        this.currentPaperQuestions = [] // 暂时为空，需要后端支持
        this.questionsDialogVisible = true
        this.$message.info('此功能需要后端接口支持获取试卷题目列表')
      } catch (error) {
        console.error('获取试卷题目失败:', error)
        this.$message.error('获取试卷题目失败')
      }
    },
    
    // 格式化学科名称
    formatSubject(row, column, cellValue) {
      const subject = this.subjects.find(s => s.id === cellValue)
      return subject ? subject.name : '未知学科'
    },
    
    // 格式化日期
    formatDate(row, column, cellValue) {
      if (!cellValue) return ''
      const date = new Date(cellValue)
      return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
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
      this.loadPapers()
    },
    
    handleCurrentChange(newPage) {
      this.currentPage = newPage
      this.loadPapers()
    }
  }
}
</script>

<style scoped>
.paper-management {
  padding: 20px 0;
}

.toolbar {
  margin-bottom: 20px;
}

.question-item {
  border-bottom: 1px solid #eee;
  padding: 15px 0;
}

.question-item:last-child {
  border-bottom: none;
}

.question-info {
  margin-top: 10px;
}

.question-info .el-tag {
  margin-right: 10px;
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