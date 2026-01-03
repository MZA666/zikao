<template>
  <div class="container">
    <div class="header">
      <h2>管理员首页</h2>
      <div class="user-info">
        <span>欢迎，{{ data.user.name || data.user.username }}</span>
        <button @click="logout" class="btn-logout">退出</button>
      </div>
    </div>
    
    <div class="content">
      <div class="card" style="line-height: 30px; margin-bottom: 10px">
        <div style="margin-bottom: 20px; font-size: 24px">北京自考科目资源整合及管理系统</div>
        <div>欢迎登录自考学习平台，同学们可以在此平台分享和学习自考相关课程</div>
        <div>祝大家早日毕业！</div>
      </div>

      <div class="card" style="line-height: 30px; margin-bottom: 10px">
        <div style="margin-bottom: 20px; font-size: 24px">北京自考官方网站</div>
        <div><a href="https://www.bjeea.cn/html/selfstudy/index.html">北京教育考试院</a></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from "vue";
import request from "@/utils/request";

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
})

const logout = () => {
  localStorage.removeItem('system-user');
  // 这里需要使用router进行跳转，但setup语法中需要特殊处理
  window.location.href = '/login';
};
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
</style>