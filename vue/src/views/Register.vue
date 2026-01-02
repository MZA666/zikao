<template>
  <div class="login-container">
    <div class="login-box">
      <div style="">欢 迎 注 册</div>
      <el-form ref="formRef" :model="data.form">
        <el-form-item prop="username">
          <el-input :prefix-icon="User" size="large" v-model="data.form.username" placeholder="请输入账号"></el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input show-password :prefix-icon="Lock" size="large" v-model="data.form.password" placeholder="请输入密码"></el-input>
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input show-password :prefix-icon="Lock" size="large" v-model="data.form.confirmPassword" placeholder="请确认密码"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button size="large" type="primary" style="" @click="register">注 册</el-button>
        </el-form-item>
        <div style="">
          已有账号？请 <a href="/login">登录</a>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { User, Lock } from "@element-plus/icons-vue";
import request from "@/utils/request";
import {ElMessage} from "element-plus";
import router from "@/router";


const validatePass = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请确认密码'))
  } else {
    if (value !== data.form.password) {
      callback(new Error("确认密码跟原密码不一致!"))
    }
    callback()
  }
}

const data = reactive({
  form: {},
  rules: {
    username: [
      { required: true, message: '请输入账号', trigger: 'blur' }
    ],
    password: [
      { required: true, message: '请输入密码', trigger: 'blur' }
    ],
    confirmPassword: [
      { validator: validatePass, trigger: 'blur' }
    ]
  }
})
const formRef = ref()

const register = () => {
  formRef.value.validate(valid => {
    if (valid) { // 表示表单校验通过
      request.post('/register',data.form ).then(res => {
        if (res.code === '200') {
          ElMessage.success({ options: '注册成功' })
          router.push('/login')
        } else {
          ElMessage.error(res.msg)
        }
      })
    }
  })
}
</script>

<style scoped>
/* 页面整体容器，用于实现背景图和居中效果 */
.login-container {
  width: 100%;
  height: 100vh; /* 占满整个视口高度 */
  background: url('@/assets/imgs/denglubeijingtu.png') no-repeat center center;
  background-size: cover; /* 背景图自适应覆盖 */
  display: flex;
  justify-content: center; /* 水平居中 */
  align-items: center; /* 垂直居中 */
  position: relative;
}

/* 背景遮罩层，用于让前景内容更清晰 */
.login-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.3); /* 黑色半透明遮罩 */
}

/* 登录/注册卡片框 */
.login-box {
  width: 400px;
  padding: 30px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  text-align: center;
  position: relative; /* 使其在遮罩层之上 */
  z-index: 10;
}

/* 欢迎标题样式 */
.login-box > div:first-child {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 30px;
  letter-spacing: 5px; /* 增加字间距，更美观 */
}

/* Element Plus 表单项的间距 */
.login-box .el-form-item {
  margin-bottom: 20px;
}

/* 注册按钮的宽度 */
.login-box .el-button {
  width: 100%;
}

/* 底部 "已有账号？" 文字 */
.login-box > div:last-child {
  margin-top: 20px;
  font-size: 14px;
  color: #666;
}

/* 底部链接样式 */
.login-box > div:last-child a {
  color: #409EFF; /* Element Plus 主题色 */
  text-decoration: none;
}

.login-box > div:last-child a:hover {
  text-decoration: underline;
}
</style>