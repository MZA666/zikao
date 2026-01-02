<template>
  <div>

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
</template>

<script setup>
import { reactive } from "vue";
import request from "@/utils/request";

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
})
</script>