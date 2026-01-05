<template>
  <div class="exam-management-container">
    <div class="header">
      <h2>考试管理</h2>
    </div>

    <!-- 选项卡 -->
    <el-tabs v-model="activeTab" class="management-tabs" type="card">
      <el-tab-pane label="学科管理" name="subjects">
        <SubjectManagement />
      </el-tab-pane>
      <el-tab-pane label="题库管理" name="questions">
        <QuestionManagement />
      </el-tab-pane>
      <el-tab-pane label="试卷管理" name="papers">
        <PaperManagement />
      </el-tab-pane>
      <el-tab-pane label="考试记录" name="records">
        <RecordManagement />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import SubjectManagement from './ExamManagement/SubjectManagement.vue'
import QuestionManagement from './ExamManagement/QuestionManagement.vue'
import PaperManagement from './ExamManagement/PaperManagement.vue'
import RecordManagement from './ExamManagement/RecordManagement.vue'

export default {
  name: 'ExamManagement',
  components: {
    SubjectManagement,
    QuestionManagement,
    PaperManagement,
    RecordManagement
  },
  data() {
    return {
      activeTab: 'subjects'
    }
  }
}
</script>

<style scoped>
.exam-management-container {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.header {
  margin-bottom: 20px;
}

.management-tabs {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}
</style>