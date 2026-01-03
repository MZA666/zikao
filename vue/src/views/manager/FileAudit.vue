<template>
  <div class="container">
    <div class="header">
      <h2>文件审核</h2>
    </div>
    
    <div class="content">
      <div class="audit-section">
        <el-tabs v-model="data.activeTab" @tab-click="handleTabClick">
          <el-tab-pane label="待审核文件" name="pending">
            <el-table :data="data.pendingFiles" style="width: 100%">
              <el-table-column prop="originalName" label="文件名" width="200"></el-table-column>
              <el-table-column prop="uploader" label="上传者" width="120"></el-table-column>
              <el-table-column prop="fileSize" label="文件大小" width="120">
                <template #default="scope">
                  {{ formatFileSize(scope.row.fileSize) }}
                </template>
              </el-table-column>
              <el-table-column prop="fileType" label="文件类型" width="100"></el-table-column>
              <el-table-column prop="isShared" label="文件类型" width="100">
                <template #default="scope">
                  <el-tag :type="scope.row.isShared === 1 ? 'success' : 'info'">
                    {{ scope.row.isShared === 1 ? '共享' : '私密' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="uploadTime" label="上传时间" width="180">
                <template #default="scope">
                  {{ scope.row.uploadTime ? new Date(scope.row.uploadTime).toLocaleString() : '' }}
                </template>
              </el-table-column>
              <el-table-column prop="description" label="文件描述" width="200"></el-table-column>
              <el-table-column label="操作" width="200">
                <template #default="scope">
                  <el-button size="small" @click="previewFile(scope.row)">预览</el-button>
                  <el-button size="small" type="success" @click="approveFile(scope.row)">通过</el-button>
                  <el-button size="small" type="danger" @click="rejectFile(scope.row)">拒绝</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
          
          <el-tab-pane label="已通过文件" name="approved">
            <el-table :data="data.approvedFiles" style="width: 100%">
              <el-table-column prop="originalName" label="文件名" width="200"></el-table-column>
              <el-table-column prop="uploader" label="上传者" width="120"></el-table-column>
              <el-table-column prop="fileSize" label="文件大小" width="120">
                <template #default="scope">
                  {{ formatFileSize(scope.row.fileSize) }}
                </template>
              </el-table-column>
              <el-table-column prop="fileType" label="文件类型" width="100"></el-table-column>
              <el-table-column prop="isShared" label="文件类型" width="100">
                <template #default="scope">
                  <el-tag :type="scope.row.isShared === 1 ? 'success' : 'info'">
                    {{ scope.row.isShared === 1 ? '共享' : '私密' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="uploadTime" label="上传时间" width="180">
                <template #default="scope">
                  {{ scope.row.uploadTime ? new Date(scope.row.uploadTime).toLocaleString() : '' }}
                </template>
              </el-table-column>
              <el-table-column prop="auditTime" label="审核时间" width="180">
                <template #default="scope">
                  {{ scope.row.auditTime ? new Date(scope.row.auditTime).toLocaleString() : '' }}
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>
          
          <el-tab-pane label="已拒绝文件" name="rejected">
            <el-table :data="data.rejectedFiles" style="width: 100%">
              <el-table-column prop="originalName" label="文件名" width="200"></el-table-column>
              <el-table-column prop="uploader" label="上传者" width="120"></el-table-column>
              <el-table-column prop="fileSize" label="文件大小" width="120">
                <template #default="scope">
                  {{ formatFileSize(scope.row.fileSize) }}
                </template>
              </el-table-column>
              <el-table-column prop="fileType" label="文件类型" width="100"></el-table-column>
              <el-table-column prop="isShared" label="文件类型" width="100">
                <template #default="scope">
                  <el-tag :type="scope.row.isShared === 1 ? 'success' : 'info'">
                    {{ scope.row.isShared === 1 ? '共享' : '私密' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="uploadTime" label="上传时间" width="180">
                <template #default="scope">
                  {{ scope.row.uploadTime ? new Date(scope.row.uploadTime).toLocaleString() : '' }}
                </template>
              </el-table-column>
              <el-table-column prop="reason" label="拒绝原因" width="200"></el-table-column>
            </el-table>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
    
    <!-- 审核拒绝对话框 -->
    <el-dialog title="拒绝文件" v-model="data.rejectDialogVisible" width="40%">
      <el-input
        type="textarea"
        :rows="4"
        placeholder="请输入拒绝原因"
        v-model="data.rejectReason">
      </el-input>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.rejectDialogVisible = false">取 消</el-button>
          <el-button type="primary" @click="confirmReject">确 定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, onMounted } from "vue";
import { ElMessage, ElMessageBox } from 'element-plus';
import request from "@/utils/request";
import { useRouter } from 'vue-router';

const router = useRouter();

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  activeTab: 'pending',
  pendingFiles: [],
  approvedFiles: [],
  rejectedFiles: [],
  rejectDialogVisible: false,
  rejectReason: '',
  currentFile: null
});

const loadFiles = () => {
  loadPendingFiles();
  loadApprovedFiles();
  loadRejectedFiles();
};

const loadPendingFiles = () => {
  request({
    url: '/files/pendingFiles',
    method: 'get'
  }).then(res => {
    if (res.code === '200') {
      data.pendingFiles = res.data || [];
    }
  });
};

const loadApprovedFiles = () => {
  request({
    url: '/files/filesByStatus',
    method: 'get',
    params: { status: 1 }
  }).then(res => {
    if (res.code === '200') {
      data.approvedFiles = res.data || [];
    }
  });
};

const loadRejectedFiles = () => {
  request({
    url: '/files/filesByStatus',
    method: 'get',
    params: { status: 2 }
  }).then(res => {
    if (res.code === '200') {
      data.rejectedFiles = res.data || [];
    }
  });
};

const handleTabClick = (tab) => {
  // 根据标签页加载对应数据
  if (tab.paneName === 'pending') {
    loadPendingFiles();
  } else if (tab.paneName === 'approved') {
    loadApprovedFiles();
  } else if (tab.paneName === 'rejected') {
    loadRejectedFiles();
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

const previewFile = (file) => {
  // 跳转到文件预览页面
  router.push({
    name: 'FilePreview',
    query: { fileId: file.fileId }
  });
};

const approveFile = (file) => {
  ElMessageBox.confirm('确定要通过此文件审核吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    request({
      url: '/files/audit',
      method: 'put',
      params: {
        fileId: file.fileId,
        auditorId: data.user.userId,
        status: 1
      }
    }).then(res => {
      if (res.code === '200') {
        ElMessage.success('审核通过');
        loadFiles(); // 重新加载数据
      } else {
        ElMessage.error(res.msg || '操作失败');
      }
    });
  });
};

const rejectFile = (file) => {
  data.currentFile = file;
  data.rejectReason = '';
  data.rejectDialogVisible = true;
};

const confirmReject = () => {
  if (!data.rejectReason.trim()) {
    ElMessage.warning('请输入拒绝原因');
    return;
  }
  
  request({
    url: '/files/audit',
    method: 'put',
    params: {
      fileId: data.currentFile.fileId,
      auditorId: data.user.userId,
      status: 2,
      reason: data.rejectReason
    }
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success('文件已拒绝');
      data.rejectDialogVisible = false;
      loadFiles(); // 重新加载数据
    } else {
      ElMessage.error(res.msg || '操作失败');
    }
  });
};

onMounted(() => {
  loadFiles();
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

.content {
  background-color: white;
  padding: 20px;
  border-radius: 4px;
}

.audit-section {
  padding: 20px 0;
}
</style>