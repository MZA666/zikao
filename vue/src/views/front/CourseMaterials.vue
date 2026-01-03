<template>
  <div class="course-materials-container">
    <el-tabs v-model="data.activeCourseTab" type="card" class="sub-tabs">
      <el-tab-pane label="我的课程" name="my-course">
        <div class="tab-content">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
            <h3>我的上传文件</h3>
            <div>
              <el-input 
                v-model="data.myFileQuery" 
                placeholder="请输入文件名" 
                style="width: 200px; margin-right: 10px"
                @keyup.enter="searchMyFiles">
                <template #append>
                  <el-button @click="searchMyFiles">
                    <el-icon><Search /></el-icon>
                  </el-button>
                </template>
              </el-input>
              <el-button @click="getMyFiles" type="primary" size="small">刷新</el-button>
            </div>
          </div>
          <el-table :data="data.myFiles" style="width: 100%">
            <el-table-column prop="originalName" label="文件名" width="200"></el-table-column>
            <el-table-column prop="fileSize" label="文件大小" width="120">
              <template #default="scope">
                {{ formatFileSize(scope.row.fileSize) }}
              </template>
            </el-table-column>
            <el-table-column prop="fileType" label="文件类型" width="100"></el-table-column>
            <el-table-column prop="uploadTime" label="上传时间" width="180">
              <template #default="scope">
                {{ scope.row.uploadTime ? new Date(scope.row.uploadTime).toLocaleString() : '' }}
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)">
                  {{ getFileStatus(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="reason" label="审核意见" width="200"></el-table-column>
            <el-table-column label="操作" width="250">
              <template #default="scope">
                <el-button 
                  size="small" 
                  @click="downloadFile(scope.row)">下载</el-button>
                <el-button 
                  size="small" 
                  @click="previewFile(scope.row)">预览</el-button>
                <el-button 
                  size="small" 
                  type="danger"
                  @click="deleteFile(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
      <el-tab-pane label="课程仓库" name="course-warehouse">
        <div class="tab-content">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
            <h3>共享课程资料</h3>
            <div>
              <el-input 
                v-model="data.sharedFileQuery" 
                placeholder="请输入文件名" 
                style="width: 200px; margin-right: 10px"
                @keyup.enter="searchSharedFiles">
                <template #append>
                  <el-button @click="searchSharedFiles">
                    <el-icon><Search /></el-icon>
                  </el-button>
                </template>
              </el-input>
              <el-button @click="getSharedFiles" type="primary" size="small">刷新</el-button>
            </div>
          </div>
          <el-table :data="data.sharedFiles" style="width: 100%">
            <el-table-column prop="originalName" label="文件名" width="200"></el-table-column>
            <el-table-column prop="fileSize" label="文件大小" width="120">
              <template #default="scope">
                {{ formatFileSize(scope.row.fileSize) }}
              </template>
            </el-table-column>
            <el-table-column prop="fileType" label="文件类型" width="100"></el-table-column>
            <el-table-column prop="uploader" label="上传者" width="120"></el-table-column>
            <el-table-column prop="uploadTime" label="上传时间" width="180">
              <template #default="scope">
                {{ scope.row.uploadTime ? new Date(scope.row.uploadTime).toLocaleString() : '' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200">
              <template #default="scope">
                <el-button size="small" @click="downloadFile(scope.row)">下载</el-button>
                <el-button size="small" @click="previewFile(scope.row)">预览</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
      <el-tab-pane label="文件上传" name="file-upload">
        <div class="tab-content">
          <h3>上传文件</h3>
          <el-form :model="data.uploadForm" label-width="120px">
            <el-form-item label="文件类型">
              <el-radio-group v-model="data.uploadForm.fileType">
                <el-radio label="private">私密文件</el-radio>
                <el-radio label="shared">共享文件</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="选择文件">
              <el-upload
                class="upload-demo"
                :action="data.uploadUrl"
                :on-success="handleUploadSuccess"
                :on-error="handleUploadError"
                :before-upload="beforeUpload"
                :file-list="data.fileList"
                :auto-upload="false"
                :on-change="handleFileChange"
                :on-remove="handleFileRemove"
                ref="uploadRef">
                <template #trigger>
                  <el-button type="primary">选择文件</el-button>
                </template>
                <template #tip>
                  <div class="el-upload__tip">只能上传PDF, DOC, DOCX, PPT, PPTX, XLS, XLSX, TXT文件，且不超过50MB</div>
                </template>
              </el-upload>
            </el-form-item>
            <el-form-item label="文件描述">
              <el-input
                v-model="data.fileDescription"
                type="textarea"
                :rows="3"
                placeholder="请输入文件描述（可选）">
              </el-input>
            </el-form-item>
            <el-form-item>
              <el-button 
                type="primary" 
                @click="uploadFile" 
                :disabled="data.isUploading || !data.selectedFile">
                {{ data.isUploading ? '上传中...' : '上传文件' }}
              </el-button>
              <el-button 
                @click="clearSelectedFile"
                :disabled="data.isUploading">
                清空选择
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { reactive, onMounted, ref, defineProps } from "vue";
import { ElMessage, ElMessageBox } from 'element-plus';
import { Search } from '@element-plus/icons-vue';
import request from "@/utils/request";
import { useRouter } from 'vue-router';

const router = useRouter();

// 接收父组件传递的用户信息
const props = defineProps({
  user: {
    type: Object,
    required: true
  }
});

const uploadRef = ref(null); // 添加对上传组件的引用

const data = reactive({
  activeCourseTab: 'my-course',
  uploadUrl: 'http://localhost:9090/files/upload',
  fileList: [],
  selectedFile: null,
  fileDescription: '',
  isUploading: false,
  myFiles: [],
  sharedFiles: [],
  uploadForm: {
    fileType: 'private' // 默认为私密文件
  },
  user: props.user,
  myFileQuery: '', // 我的文件查询条件
  sharedFileQuery: '' // 共享文件查询条件
});

// 监听文件选择变化
const handleFileChange = (file, fileList) => {
  // 更新文件列表
  data.fileList = [...fileList]; // 使用展开运算符创建新数组，确保响应性
  
  // 从fileList中查找最后一个有raw属性的文件（用户最近添加的文件）
  let foundFile = null;
  for (let i = fileList.length - 1; i >= 0; i--) {
    if (fileList[i].raw) {
      foundFile = fileList[i].raw;
      break;
    }
  }
  
  // 更新选择的文件
  data.selectedFile = foundFile;
};

// 监听文件移除事件
const handleFileRemove = (file, fileList) => {
  // 更新文件列表
  data.fileList = [...fileList];
  
  // 重新确定当前选中的文件
  let foundFile = null;
  for (let i = fileList.length - 1; i >= 0; i--) {
    if (fileList[i].raw) {
      foundFile = fileList[i].raw;
      break;
    }
  }
  
  // 更新选择的文件
  data.selectedFile = foundFile;
};

const clearSelectedFile = () => {
  data.selectedFile = null;
  data.fileList = [];
  if (uploadRef.value) {
    uploadRef.value.clearFiles();
  }
};

const handleUploadSuccess = (response, file, fileList) => {
  ElMessage.success('文件上传成功');
  // 上传成功后，清空当前文件选择，以便用户可以上传新文件
  data.selectedFile = null;
  data.fileList = [];
  data.fileDescription = '';
  // 根据文件类型刷新相应列表
  if (data.uploadForm.fileType === 'private') {
    getMyFiles();
  } else {
    getSharedFiles();
  }
  // 清空上传组件的文件列表
  if (uploadRef.value) {
    uploadRef.value.clearFiles();
  }
  // 重置上传状态
  data.isUploading = false;
};

const handleUploadError = (err, file, fileList) => {
  ElMessage.error('文件上传失败：' + err.message);
  // 上传失败后也要重置状态
  data.selectedFile = null;
  data.fileList = [];
  if (uploadRef.value) {
    uploadRef.value.clearFiles();
  }
  // 重置上传状态
  data.isUploading = false;
};

const beforeUpload = (file) => {
  // 验证文件类型和大小
  const allowedTypes = ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 
                       'application/vnd.ms-powerpoint', 'application/vnd.openxmlformats-officedocument.presentationml.presentation',
                       'application/vnd.ms-excel', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
                       'text/plain', 'image/jpeg', 'image/png'];
  const isValidType = allowedTypes.includes(file.type);
  const isLt50M = file.size / 1024 / 1024 < 50;

  if (!isValidType) {
    ElMessage.error('只能上传PDF, DOC, DOCX, PPT, PPTX, XLS, XLSX, TXT文件!');
  }
  if (!isLt50M) {
    ElMessage.error('文件大小不能超过50MB!');
  }
  
  return isValidType && isLt50M;
};

const uploadFile = () => {
  if (!data.selectedFile) {
    ElMessage.warning('请选择文件');
    return;
  }
  
  // 设置上传状态，禁用按钮
  data.isUploading = true;
  
  // 手动触发上传
  const formData = new FormData();
  formData.append('file', data.selectedFile);
  formData.append('uploaderId', data.user.userId);
  formData.append('uploader', data.user.username);
  formData.append('description', data.fileDescription);
  formData.append('isShared', data.uploadForm.fileType === 'shared' ? 1 : 0);
  
  request({
    url: '/files/upload',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' }
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('文件上传成功');
      // 上传成功后，清空当前文件选择，以便用户可以上传新文件
      data.selectedFile = null;
      data.fileList = [];
      data.fileDescription = '';
      // 根据文件类型刷新相应列表
      if (data.uploadForm.fileType === 'private') {
        getMyFiles();
      } else {
        getSharedFiles();
      }
      // 清空上传组件的文件列表
      if (uploadRef.value) {
        uploadRef.value.clearFiles();
      }
    } else {
      ElMessage.error(res.msg || '上传失败');
    }
  }).catch(err => {
    ElMessage.error(err.message || '上传失败');
  }).finally(() => {
    // 无论成功或失败，都要重置上传状态
    data.isUploading = false;
  });
};

const getMyFiles = () => {
  if (data.user.userId) {
    request({
      url: '/files/myFiles',
      method: 'get',
      params: { uploaderId: data.user.userId }
    }).then(res => {
      if (res.code === '200') {
        data.myFiles = res.data || [];
      }
    });
  }
};

const getSharedFiles = () => {
  request({
    url: '/files/sharedFiles',
    method: 'get',
    params: { status: 1 } // 只获取已审核通过的共享文件
  }).then(res => {
    if (res.code === '200') {
      data.sharedFiles = res.data || [];
    }
  });
};

// 搜索我的文件
const searchMyFiles = () => {
  // 向服务器发送带查询条件的请求，无论查询条件是否为空
  request({
    url: '/files/myFiles',
    method: 'get',
    params: { 
      uploaderId: data.user.userId,
      fileName: data.myFileQuery.trim() === '' ? null : data.myFileQuery  // 添加文件名查询参数，如果为空则传null
    }
  }).then(res => {
    if (res.code === '200') {
      data.myFiles = res.data || [];
    } else {
      data.myFiles = [];
      ElMessage.error(res.msg || '查询失败');
    }
  }).catch(error => {
    data.myFiles = [];
    ElMessage.error('查询失败: ' + error.message);
  });
};

// 搜索共享文件
const searchSharedFiles = () => {
  // 向服务器发送带查询条件的请求，无论查询条件是否为空
  request({
    url: '/files/sharedFiles',
    method: 'get',
    params: { 
      status: 1,  // 只获取已审核通过的共享文件
      fileName: data.sharedFileQuery.trim() === '' ? null : data.sharedFileQuery  // 添加文件名查询参数，如果为空则传null
    }
  }).then(res => {
    if (res.code === '200') {
      data.sharedFiles = res.data || [];
    } else {
      data.sharedFiles = [];
      ElMessage.error(res.msg || '查询失败');
    }
  }).catch(error => {
    data.sharedFiles = [];
    ElMessage.error('查询失败: ' + error.message);
  });
};

// 删除文件
const deleteFile = async (file) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除此文件吗？此操作不可恢复',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );
    
    // 调用删除文件的API
    const res = await request({
      url: `/files/${file.fileId}`,
      method: 'delete',
      params: {
        uploaderId: data.user.userId
      }
    });
    
    if (res.code === '200') {
      ElMessage.success('文件删除成功');
      // 重新加载我的文件列表
      getMyFiles();
    } else {
      ElMessage.error(res.msg || '文件删除失败');
    }
  } catch (error) {
    // 用户取消删除操作
    if (error !== 'cancel') {
      ElMessage.error('文件删除失败');
    }
  }
};

const formatFileSize = (size) => {
  if (!size) return '0B';
  const units = ['B', 'KB', 'MB', 'GB'];
  let index = 0;
  let fileSize = size;
  while (fileSize >= 1024 && index < units.length - 1) {
    fileSize /= 1024;
    index++;
  }
  return fileSize.toFixed(2) + units[index];
};

const getStatusType = (status) => {
  if (status === 0) return 'warning'; // 待审核
  if (status === 1) return 'success'; // 已通过
  if (status === 2) return 'danger'; // 已拒绝
  return 'info';
};

const getFileStatus = (status) => {
  if (status === 0) return '待审核';
  if (status === 1) return '已通过';
  if (status === 2) return '已拒绝';
  return '未知';
};

const downloadFile = (file) => {
  // 构建下载链接
  const downloadUrl = `http://localhost:9090/files/download/${file.fileName}`;
  window.open(downloadUrl, '_blank');
};

const previewFile = (file) => {
  // 跳转到文件预览页面
  router.push({
    path: '/front-file-preview',
    query: { fileId: file.fileId }
  });
};

onMounted(() => {
  // 默认加载我的文件
  getMyFiles();
  getSharedFiles();
});
</script>

<style scoped>
.course-materials-container {
  padding: 20px 0;
}

.sub-tabs {
  margin-top: 20px;
}

.tab-content {
  padding: 20px 0;
}

.el-upload__tip {
  font-size: 12px;
  color: #909399;
  margin-top: 7px;
}
</style>