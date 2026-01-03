<template>
  <div class="container">
    <div class="header">
      <h2>学员首页</h2>
      <div class="user-info">
        <span>欢迎，{{ data.user.name || data.user.username }}</span>
        <button @click="logout" class="btn-logout">退出</button>
      </div>
    </div>
    
    <div class="content">
      <!-- 主选项卡 -->
      <el-tabs v-model="data.activeMainTab" class="main-tabs" @tab-change="handleMainTabChange">
        <el-tab-pane label="课程资料" name="course-materials">
          <CourseMaterials :user="data.user" />
        </el-tab-pane>
        <el-tab-pane label="考试题库" name="exam-bank">
          <ExamBank />
        </el-tab-pane>
        <el-tab-pane label="学员论坛" name="student-forum">
          <StudentForum />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted } from "vue";
import CourseMaterials from './CourseMaterials.vue';
import ExamBank from './ExamBank.vue';
import StudentForum from './StudentForum.vue';
import request from "@/utils/request";

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  activeMainTab: 'course-materials',
});

const handleMainTabChange = (tabName) => {
  // 可以根据需要添加选项卡切换逻辑
};

const logout = () => {
  localStorage.removeItem('system-user');
  // 这里需要使用router进行跳转，但setup语法中需要特殊处理
  window.location.href = '/login';
};

onMounted(() => {
  // 组件初始化逻辑
});
</script>

<style scoped>
.container {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 20px;
  background-color: white;
  border-radius: 4px;
  margin-bottom: 20px;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-info span {
  margin-right: 20px;
}

.btn-logout {
  padding: 6px 12px;
  background-color: #f56c6c;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.content {
  background-color: white;
  padding: 20px;
  border-radius: 4px;
}

.main-tabs {
  margin-top: 20px;
}
</style>