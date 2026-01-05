<template>
  <div>
    <div style="height: 60px; background-color: #2e3143; display: flex; align-items: center; border-bottom: 1px solid #ddd">
      <div style="flex: 1">
        <div style="padding-left: 20px; display: flex; align-items: center">
          <img src="@/assets/imgs/logo.png" alt="" style="width: 40px">
          <div style="font-weight: bold; font-size: 24px; margin-left: 5px; color: #fff">北京自考学习平台</div>
        </div>
      </div>
      <div style="width: fit-content; padding-right: 10px; display: flex; align-items: center;">
        <img style="width: 40px; height: 40px; border-radius: 50%" :src="data.user.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" alt="">
        <span style="color: #fff; margin-left: 5px">{{ data.user.name }}</span>
      </div>
    </div>

    <div style="display: flex">
      <div style="width: 200px; border-right: 1px solid #ddd; min-height: calc(100vh - 60px)">
        <el-menu
            router
            style="border: none"
            :default-active="router.currentRoute.value.path"
            :default-openeds="data.isManager ? ['user', 'audit', 'exam'] : ['upload']"
        >
          <el-menu-item index="/manager/home">
            <el-icon><HomeFilled /></el-icon>
            <span>系统首页</span>
          </el-menu-item>
          <el-sub-menu index="upload" v-if="!data.isManager">
            <template #title>
              <el-icon><Upload /></el-icon>
              <span>资料管理</span>
            </template>
            <el-menu-item index="/front/home">
              <el-icon><Folder /></el-icon>
              <span>我的上传资料</span>
            </el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="audit" v-if="data.isManager">
            <template #title>
              <el-icon><Document /></el-icon>
              <span>审核管理</span>
            </template>
            <el-menu-item index="/manager/file-audit">
              <el-icon><FolderOpened /></el-icon>
              <span>课程资料审核</span>
            </el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="exam" v-if="data.isManager">
            <template #title>
              <el-icon><Edit /></el-icon>
              <span>考试管理</span>
            </template>
            <el-menu-item index="/manager/exam-management">
              <el-icon><List /></el-icon>
              <span>考试管理</span>
            </el-menu-item>
          </el-sub-menu>
          <el-sub-menu index="user">
            <template #title>
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </template>
            <el-menu-item index="/manager/admin">
              <el-icon><User /></el-icon>
              <span>用户信息</span>
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item @click="logout">
            <el-icon><SwitchButton /></el-icon>
            <span>退出系统</span>
          </el-menu-item>
        </el-menu>
      </div>
      <div style="flex: 1; width: 0; background-color: #f8f8ff; padding: 10px">
        <router-view @updateUser="updateUser" />
      </div>
    </div>

  </div>
</template>

<script setup>
import { reactive, onMounted } from "vue";
import router from "@/router";
import {ElMessage} from "element-plus";
import {HomeFilled, User, SwitchButton, Document, FolderOpened, Upload, Folder, Edit, List} from '@element-plus/icons-vue';
import request from "@/utils/request";

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  isManager: false
})

onMounted(() => {
  checkUserRole();
})

const checkUserRole = async () => {
  // 检查用户角色，如果是管理员则显示审核菜单
  const user = JSON.parse(localStorage.getItem('system-user') || '{}');
  // 假设角色为 "管理员" 或 "学员"，具体根据您的业务逻辑调整
  if (user.roleId === '1' || user.roleId === 1 || user.roleId === '管理员') {
    data.isManager = true;
  } else {
    data.isManager = false;
  }
}

if (!data.user?.userId) {
  ElMessage.error('请登录！')
  router.push('/login')
}

const updateUser = () => {
  data.user = JSON.parse(localStorage.getItem('system-user') || '{}')
  checkUserRole();
}

const logout = () => {
  router.push('/login')
  ElMessage.success('退出成功')
  localStorage.removeItem('system-user')
}
</script>

<style scoped>
.el-menu-item.is-active {
  background-color: #d7d7e6 !important;
}
.el-menu-item:hover {
  color: #000;
}
</style>