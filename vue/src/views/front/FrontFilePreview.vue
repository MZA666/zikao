<template>
  <div class="file-preview-container">
    <div class="preview-header">
      <h3>{{ data.fileName }}</h3>
      <div class="header-actions">
        <el-button @click="downloadFile" type="primary" size="small">下载文件</el-button>
        <el-button @click="closePreview" size="small">关闭</el-button>
      </div>
    </div>
    
    <div class="preview-content">
      <!-- PDF 预览 -->
      <div v-if="data.fileType === 'pdf'" class="pdf-preview">
        <iframe 
          :src="data.previewUrl" 
          width="100%" 
          height="800px"
          frameborder="0"
          style="border: none;">
          您的浏览器不支持在线预览PDF文件
        </iframe>
      </div>
      
      <!-- Word 文档预览 (转换为HTML) -->
      <div v-else-if="data.fileType === 'doc' || data.fileType === 'docx'" class="doc-preview">
        <div v-if="data.loading" class="loading">
          <el-icon class="is-loading"><Loading /></el-icon>
          <span>正在转换文档...</span>
        </div>
        <div v-else-if="data.htmlContent" class="html-content" v-html="data.htmlContent"></div>
        <div v-else-if="data.error" class="error">{{ data.error }}</div>
        <div v-else class="error">文档转换失败</div>
      </div>
      
      <!-- TXT 文本预览 -->
      <div v-else-if="data.fileType === 'txt'" class="txt-preview">
        <pre class="txt-content">{{ data.fileContent }}</pre>
      </div>
      
      <!-- 不支持的文件类型 -->
      <div v-else class="unsupported-preview">
        <div class="unsupported-message">
          <el-icon><Document /></el-icon>
          <p>暂不支持在线预览此文件类型</p>
          <p>文件类型: {{ data.fileType }}</p>
          <el-button @click="downloadFile" type="primary" size="small" style="margin-top: 20px;">下载文件</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted, nextTick } from "vue";
import { ElMessage } from 'element-plus';
import { Document, Loading } from '@element-plus/icons-vue';
import request from "@/utils/request";
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();

const data = reactive({
  fileName: '',
  fileType: '',
  filePath: '',
  previewUrl: '',
  fileContent: '',
  htmlContent: '',
  fileId: null,
  loading: false,
  error: null
});

// 初始化预览
const initPreview = async () => {
  const { fileId } = route.query;
  if (!fileId) {
    ElMessage.error('文件ID不能为空');
    router.back();
    return;
  }
  
  data.fileId = fileId;
  
  // 获取文件信息
  try {
    const res = await request({
      url: `/files/${fileId}`,
      method: 'get'
    });
    
    if (res.code === '200') {
      const file = res.data;
      data.fileName = file.originalName;
      data.filePath = file.fileName;
      
      // 确定文件类型
      const originalName = file.originalName.toLowerCase();
      if (originalName.endsWith('.pdf')) {
        data.fileType = 'pdf';
        // 使用服务器的PDF预览URL
        data.previewUrl = `http://localhost:9090/files/preview/${file.fileName}`;
      } else if (originalName.endsWith('.doc') || originalName.endsWith('.docx')) {
        data.fileType = originalName.split('.').pop();
        // 使用POI转换Word文档为HTML
        await loadWordContent(fileId);
      } else if (originalName.endsWith('.txt')) {
        data.fileType = 'txt';
        // 加载TXT文件内容
        loadTxtContent(file.fileName);
      } else {
        data.fileType = originalName.split('.').pop();
      }
    } else {
      ElMessage.error(res.msg || '获取文件信息失败');
      router.back();
    }
  } catch (error) {
    console.error('获取文件信息失败:', error);
    ElMessage.error('获取文件信息失败');
    router.back();
  }
};

// 加载Word文档内容
const loadWordContent = async (fileId) => {
  try {
    data.loading = true;
    data.error = null;
    const res = await request({
      url: `/files/word-to-html/${fileId}`,
      method: 'get'
    });
    
    if (res.code === '200') {
      data.htmlContent = res.data;
      // 等待DOM更新后处理HTML内容
      await nextTick();
    } else {
      console.error('转换Word文档失败:', res.msg);
      data.error = res.msg || '文档转换失败';
      data.htmlContent = null;
    }
  } catch (error) {
    console.error('加载Word文档失败:', error);
    data.error = error.message || '文档加载失败';
    data.htmlContent = null;
  } finally {
    data.loading = false;
  }
};

// 加载TXT文件内容
const loadTxtContent = async (fileName) => {
  try {
    const response = await fetch(`http://localhost:9090/files/download/${fileName}`);
    if (response.ok) {
      const text = await response.text();
      data.fileContent = text;
    } else {
      throw new Error('获取TXT文件内容失败');
    }
  } catch (error) {
    console.error('加载TXT文件失败:', error);
    data.fileContent = '加载文件内容失败';
  }
};

// 下载文件
const downloadFile = () => {
  const downloadUrl = `http://localhost:9090/files/download/${data.filePath}`;
  window.open(downloadUrl, '_blank');
};

// 关闭预览
const closePreview = () => {
  const { fromTab } = route.query;
  
  if (fromTab === 'course-warehouse') {
    // 如果是从课程仓库打开的，返回课程仓库页面
    router.push({
      path: '/front/course-materials',
      query: { tab: 'course-warehouse' }
    });
  } else {
    // 否则返回上一页（默认行为）
    router.go(-1);
  }
};

onMounted(() => {
  initPreview();
});
</script>

<style scoped>
.file-preview-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  background-color: #f5f5f5;
  border-bottom: 1px solid #ddd;
}

.preview-header h3 {
  margin: 0;
  font-size: 16px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 70%;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.preview-content {
  flex: 1;
  overflow: auto;
  padding: 20px;
  background-color: #fff;
}

.pdf-preview {
  width: 100%;
  height: 800px;
  border: 1px solid #ddd;
  border-radius: 4px;
  overflow: hidden;
}

.doc-preview {
  width: 100%;
  min-height: 800px;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 20px;
  overflow: auto;
  background-color: #f8f9fa;
}

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  font-size: 16px;
}

.loading .el-icon {
  font-size: 32px;
  margin-bottom: 10px;
}

.html-content {
  width: 100%;
}

.error {
  color: #f56c6c;
  text-align: center;
  padding: 40px;
  font-size: 16px;
}

.txt-preview {
  width: 100%;
  height: 800px;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 20px;
  overflow: auto;
  background-color: #f8f9fa;
}

.txt-content {
  font-family: 'Courier New', monospace;
  white-space: pre-wrap;
  word-wrap: break-word;
  line-height: 1.5;
  margin: 0;
  font-size: 14px;
}

.unsupported-preview {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 800px;
}

.unsupported-message {
  text-align: center;
  padding: 40px;
  border: 1px dashed #ddd;
  border-radius: 8px;
  background-color: #fafafa;
}

.unsupported-message .el-icon {
  font-size: 48px;
  color: #909090;
  margin-bottom: 15px;
}

.unsupported-message p {
  margin: 10px 0;
  color: #666;
}
</style>