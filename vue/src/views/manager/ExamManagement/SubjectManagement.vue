<template>
  <div class="subject-management">
    <div class="toolbar">
      <el-button type="primary" @click="showAddSubjectDialog">添加学科</el-button>
    </div>

    <el-table :data="subjects" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="学科名称" />
      <el-table-column prop="description" label="描述" />
      <el-table-column prop="createdTime" label="创建时间" :formatter="formatDate" />
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button size="small" @click="editSubject(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="deleteSubject(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加/编辑学科对话框 -->
    <el-dialog :title="isEdit ? '编辑学科' : '添加学科'" v-model="dialogVisible" width="500px">
      <el-form :model="subjectForm" :rules="subjectRules" ref="subjectFormRef" label-width="100px">
        <el-form-item label="学科名称" prop="name">
          <el-input v-model="subjectForm.name" placeholder="请输入学科名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="subjectForm.description" type="textarea" placeholder="请输入学科描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveSubject">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'SubjectManagement',
  data() {
    return {
      subjects: [],
      loading: false,
      dialogVisible: false,
      isEdit: false,
      subjectForm: {
        id: null,
        name: '',
        description: ''
      },
      subjectRules: {
        name: [
          { required: true, message: '请输入学科名称', trigger: 'blur' }
        ]
      }
    }
  },
  mounted() {
    this.loadSubjects()
  },
  methods: {
    // 加载学科列表
    async loadSubjects() {
      this.loading = true
      try {
        const response = await request.get('/exam/subject/list')
        this.subjects = response.data
      } catch (error) {
        console.error('加载学科列表失败:', error)
        this.$message.error('加载学科列表失败')
      } finally {
        this.loading = false
      }
    },
    
    // 显示添加学科对话框
    showAddSubjectDialog() {
      this.isEdit = false
      this.subjectForm = {
        id: null,
        name: '',
        description: ''
      }
      this.dialogVisible = true
    },
    
    // 编辑学科
    editSubject(subject) {
      this.isEdit = true
      this.subjectForm = { ...subject }
      this.dialogVisible = true
    },
    
    // 保存学科
    async saveSubject() {
      try {
        await this.$refs.subjectFormRef.validate()
        
        if (this.isEdit) {
          await request.put('/exam/subject', this.subjectForm)
          this.$message.success('更新学科成功')
        } else {
          await request.post('/exam/subject', this.subjectForm)
          this.$message.success('添加学科成功')
        }
        
        this.dialogVisible = false
        this.loadSubjects()
      } catch (error) {
        console.error('保存学科失败:', error)
        this.$message.error('保存学科失败')
      }
    },
    
    // 删除学科
    async deleteSubject(id) {
      try {
        await this.$confirm('确定要删除这个学科吗？', '确认删除', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await request.delete(`/exam/subject/${id}`)
        this.$message.success('删除学科成功')
        this.loadSubjects()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除学科失败:', error)
          this.$message.error('删除学科失败')
        }
      }
    },
    
    // 格式化日期
    formatDate(row, column, cellValue) {
      if (!cellValue) return ''
      const date = new Date(cellValue)
      return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`
    }
  }
}
</script>

<style scoped>
.subject-management {
  padding: 20px 0;
}

.toolbar {
  margin-bottom: 20px;
}
</style>