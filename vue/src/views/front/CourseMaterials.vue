<template>
  <div class="course-materials-container">
    <el-tabs v-model="data.activeCourseTab" type="card" class="sub-tabs" @tab-click="onTabChange">
      <el-tab-pane label="我的课程" name="my-course">
        <div class="tab-content">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
            <h3>我的上传文件</h3>
            <div>
              <el-autocomplete 
                v-model="data.myFileMajorQueryStr" 
                :fetch-suggestions="queryMajorSearchForQuery"
                placeholder="选择专业" 
                style="width: 120px; margin-right: 10px;"
                clearable
                @select="handleMajorQuerySelect">
                <template #default="{ item }">
                  <span>{{ item.name }}</span>
                </template>
              </el-autocomplete>
              <el-autocomplete 
                v-model="data.myFileSubjectQueryStr" 
                :fetch-suggestions="querySubjectSearchForQuery"
                placeholder="选择学科" 
                style="width: 120px; margin-right: 10px;"
                clearable
                @select="handleSubjectQuerySelect">
                <template #default="{ item }">
                  <span>{{ item.name }}</span>
                </template>
              </el-autocomplete>
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
            <el-table-column label="专业" width="120">
              <template #default="scope">
                {{ getMajorNameById(scope.row.majorId) }}
              </template>
            </el-table-column>
            <el-table-column label="学科" width="120">
              <template #default="scope">
                {{ getSubjectNameById(scope.row.disciplineId) }}
              </template>
            </el-table-column>
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
          <el-pagination
            v-model:current-page="data.myFilePagination.currentPage"
            v-model:page-size="data.myFilePagination.pageSize"
            :total="data.myFilePagination.total"
            @size-change="handleMyFileSizeChange"
            @current-change="handleMyFileCurrentChange"
            layout="total, sizes, prev, pager, next, jumper"
            :page-sizes="[10, 20, 50, 100]"
            style="margin-top: 20px; text-align: center;"
          />
        </div>
      </el-tab-pane>
      <el-tab-pane label="课程仓库" name="course-warehouse">
        <div class="tab-content">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
            <h3>共享课程资料</h3>
            <div>
              <el-autocomplete 
                v-model="data.sharedFileMajorQueryStr" 
                :fetch-suggestions="queryMajorSearchForQuery"
                placeholder="选择专业" 
                style="width: 120px; margin-right: 10px;"
                clearable
                @select="handleSharedMajorQuerySelect">
                <template #default="{ item }">
                  <span>{{ item.name }}</span>
                </template>
              </el-autocomplete>
              <el-autocomplete 
                v-model="data.sharedFileSubjectQueryStr" 
                :fetch-suggestions="querySubjectSearchForQuery"
                placeholder="选择学科" 
                style="width: 120px; margin-right: 10px;"
                clearable
                @select="handleSharedSubjectQuerySelect">
                <template #default="{ item }">
                  <span>{{ item.name }}</span>
                </template>
              </el-autocomplete>
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
            <el-table-column label="专业" width="120">
              <template #default="scope">
                {{ getMajorNameById(scope.row.majorId) }}
              </template>
            </el-table-column>
            <el-table-column label="学科" width="120">
              <template #default="scope">
                {{ getSubjectNameById(scope.row.disciplineId) }}
              </template>
            </el-table-column>
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
          <el-pagination
            v-model:current-page="data.sharedFilePagination.currentPage"
            v-model:page-size="data.sharedFilePagination.pageSize"
            :total="data.sharedFilePagination.total"
            @size-change="handleSharedFileSizeChange"
            @current-change="handleSharedFileCurrentChange"
            layout="total, sizes, prev, pager, next, jumper"
            :page-sizes="[10, 20, 50, 100]"
            style="margin-top: 20px; text-align: center;"
          />
        </div>
      </el-tab-pane>
      <el-tab-pane label="文件上传" name="file-upload">
        <div class="tab-content">
          <h3>上传文件</h3>
          <el-form :model="data.uploadForm" label-width="120px">
            <el-form-item label="专业">
              <el-autocomplete
                v-model="data.uploadForm.majorName"
                :fetch-suggestions="queryMajorSearch"
                placeholder="请输入或选择专业"
                style="width: 100%"
                @select="handleMajorSelectFromAutoComplete">
                <template #default="{ item }">
                  <span>{{ item.name }}</span>
                </template>
              </el-autocomplete>
            </el-form-item>
            <el-form-item label="学科">
              <el-autocomplete
                v-model="data.uploadForm.subjectName"
                :fetch-suggestions="querySubjectSearch"
                placeholder="请输入或选择学科"
                style="width: 100%"
                @select="handleSubjectSelectFromAutoComplete">
                <template #default="{ item }">
                  <span>{{ item.name }}</span>
                </template>
              </el-autocomplete>
            </el-form-item>
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
import { useRouter, useRoute } from 'vue-router';

const router = useRouter();
const route = useRoute();

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
    fileType: 'private', // 默认为私密文件
    majorName: '', // 专业名称
    subjectName: '' // 学科名称
  },
  user: props.user,
  myFileQuery: '', // 我的文件查询条件
  sharedFileQuery: '', // 共享文件查询条件
  myFileMajorQuery: null, // 我的文件专业查询条件
  myFileSubjectQuery: null, // 我的文件学科查询条件
  sharedFileMajorQuery: null, // 共享文件专业查询条件
  sharedFileSubjectQuery: null, // 共享文件学科查询条件
  myFileMajorQueryStr: '', // 我的文件专业查询字符串
  myFileSubjectQueryStr: '', // 我的文件学科查询字符串
  sharedFileMajorQueryStr: '', // 共享文件专业查询字符串
  sharedFileSubjectQueryStr: '', // 共享文件学科查询字符串
  majors: [], // 专业列表
  subjects: [], // 学科列表
  // 分页相关
  myFilePagination: {
    currentPage: 1,
    pageSize: 20,
    total: 0
  },
  sharedFilePagination: {
    currentPage: 1,
    pageSize: 20,
    total: 0
  }
});

// 加载专业和学科数据
const loadMajorsAndSubjects = async () => {
  try {
    const majorRes = await request({
      url: '/files/majors',
      method: 'get'
    });
    if (majorRes.code === '200') {
      data.majors = majorRes.data || [];
    }
    
    const subjectRes = await request({
      url: '/files/disciplines', // 仍然调用disciplines接口，但实际返回的是subject数据
      method: 'get'
    });
    if (subjectRes.code === '200') {
      data.subjects = subjectRes.data || [];
    }
  } catch (error) {
    console.error('加载专业和学科数据失败:', error);
  }
};

// 获取专业名称通过ID
const getMajorNameById = (id) => {
  if (!id) return '';
  const major = data.majors.find(m => m.id === id);
  return major ? major.name : '';
};

// 获取学科名称通过ID
const getSubjectNameById = (id) => {
  if (!id) return '';
  const subject = data.subjects.find(s => s.id === id);
  return subject ? subject.name : '';
};

// 专业自动完成搜索
const queryMajorSearch = (queryString, callback) => {
  const results = queryString
    ? data.majors.filter(major => major.name.toLowerCase().includes(queryString.toLowerCase()))
    : data.majors;
  callback(results);
};

// 用于查询的专业自动完成搜索
const queryMajorSearchForQuery = (queryString, callback) => {
  const results = queryString
    ? data.majors.filter(major => major.name.toLowerCase().includes(queryString.toLowerCase()))
    : data.majors;
  callback(results);
};

// 学科自动完成搜索
const querySubjectSearch = (queryString, callback) => {
  let results = [];
  if (queryString) {
    results = data.subjects.filter(subject => 
      subject.name.toLowerCase().includes(queryString.toLowerCase())
    );
  } else {
    results = data.subjects;
  }
  
  // 如果有选择专业，只显示该专业下的学科
  if (data.uploadForm.majorId) {
    results = results.filter(s => s.majorId === data.uploadForm.majorId);
  }
  
  callback(results);
};

// 用于查询的学科自动完成搜索
const querySubjectSearchForQuery = (queryString, callback) => {
  let results = [];
  if (queryString) {
    results = data.subjects.filter(subject => 
      subject.name.toLowerCase().includes(queryString.toLowerCase())
    );
  } else {
    results = data.subjects;
  }
  
  // 如果是在查询模式下，根据查询的专业ID过滤学科
  if (data.myFileMajorQuery) {
    results = results.filter(s => s.majorId === data.myFileMajorQuery);
  } else if (data.sharedFileMajorQuery) {
    results = results.filter(s => s.majorId === data.sharedFileMajorQuery);
  }
  
  callback(results);
};

// 专业选择处理
const handleMajorSelect = (item) => {
  data.uploadForm.majorId = item.id;
  // 清空已选择的学科，因为可能需要重新筛选
  data.uploadForm.subjectId = null;
  data.uploadForm.subjectName = '';
};

// 学科选择处理
const handleSubjectSelect = (item) => {
  data.uploadForm.subjectId = item.id;
  data.uploadForm.subjectName = item.name;
};

// 专业自动完成选择处理
const handleMajorSelectFromAutoComplete = (item) => {
  data.uploadForm.majorId = item.id;
  data.uploadForm.majorName = item.name;
  // 清空已选择的学科，因为可能需要重新筛选
  data.uploadForm.subjectId = null;
  data.uploadForm.subjectName = '';
};

// 学科自动完成选择处理
const handleSubjectSelectFromAutoComplete = (item) => {
  data.uploadForm.subjectId = item.id;
  data.uploadForm.subjectName = item.name;
};

// 查询专业选择处理
const handleMajorQuerySelect = (item) => {
  data.myFileMajorQuery = item.id;
  data.myFileMajorQueryStr = item.name;
  // 触发搜索
  searchMyFiles();
};

// 查询学科选择处理
const handleSubjectQuerySelect = (item) => {
  data.myFileSubjectQuery = item.id;
  data.myFileSubjectQueryStr = item.name;
  // 触发搜索
  searchMyFiles();
};

// 共享文件查询专业选择处理
const handleSharedMajorQuerySelect = (item) => {
  data.sharedFileMajorQuery = item.id;
  data.sharedFileMajorQueryStr = item.name;
  // 触发搜索
  searchSharedFiles();
};

// 共享文件查询学科选择处理
const handleSharedSubjectQuerySelect = (item) => {
  data.sharedFileSubjectQuery = item.id;
  data.sharedFileSubjectQueryStr = item.name;
  // 触发搜索
  searchSharedFiles();
};

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
  data.uploadForm.majorName = '';
  data.uploadForm.subjectName = '';
  data.uploadForm.majorId = null;
  data.uploadForm.subjectId = null;
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

const uploadFile = async () => {
  if (!data.selectedFile) {
    ElMessage.warning('请选择文件');
    return;
  }
  
  // 设置上传状态，禁用按钮
  data.isUploading = true;
  
  // 如果专业或学科不存在，则先创建
  let majorId = data.uploadForm.majorId;
  let subjectId = data.uploadForm.subjectId;
  
  if (data.uploadForm.majorName && !data.uploadForm.majorId) {
    // 尝试创建专业
    try {
      const majorRes = await request({
        url: '/major-management/major',
        method: 'post',
        data: { name: data.uploadForm.majorName }
      });
      
      if (majorRes.code === '200') {
        majorId = majorRes.data.id; // 假设后端返回创建的对象
        // 更新专业列表
        loadMajorsAndSubjects();
      } else {
        ElMessage.error(majorRes.msg || '专业创建失败');
        data.isUploading = false;
        return;
      }
    } catch (error) {
      // 如果创建失败，尝试查找是否已存在同名专业
      try {
        const majorRes = await request({
          url: '/files/majors',
          method: 'get'
        });
        if (majorRes.code === '200') {
          const existingMajor = majorRes.data.find(m => m.name === data.uploadForm.majorName);
          if (existingMajor) {
            majorId = existingMajor.id;
          }
        }
      } catch (findError) {
        console.error('查找专业失败:', findError);
      }
    }
  }
  
  if (data.uploadForm.subjectName && !data.uploadForm.subjectId) {
    // 尝试创建学科
    try {
      const subjectData = { 
        name: data.uploadForm.subjectName,
        majorId: majorId 
      };
      
      const subjectRes = await request({
        url: '/major-management/subject',
        method: 'post',
        data: subjectData
      });
      
      if (subjectRes.code === '200') {
        subjectId = subjectRes.data.id; // 假设后端返回创建的对象
        // 更新学科列表
        loadMajorsAndSubjects();
      } else {
        ElMessage.error(subjectRes.msg || '学科创建失败');
        data.isUploading = false;
        return;
      }
    } catch (error) {
      // 如果创建失败，尝试查找是否已存在同学科
      try {
        const subjectRes = await request({
          url: '/files/disciplines',
          method: 'get'
        });
        if (subjectRes.code === '200') {
          const existingSubject = subjectRes.data.find(s => s.name === data.uploadForm.subjectName);
          if (existingSubject) {
            subjectId = existingSubject.id;
          }
        }
      } catch (findError) {
        console.error('查找学科失败:', findError);
      }
    }
  }
  
  // 手动触发上传
  const formData = new FormData();
  formData.append('file', data.selectedFile);
  formData.append('uploaderId', data.user.userId);
  formData.append('uploader', data.user.username);
  formData.append('description', data.fileDescription);
  formData.append('isShared', data.uploadForm.fileType === 'shared' ? 1 : 0);
  if (majorId) {
    formData.append('majorId', majorId);
  }
  if (subjectId) {
    formData.append('disciplineId', subjectId);
  }
  // 添加专业和学科名称，以便后端自动创建
  if (data.uploadForm.majorName) {
    formData.append('majorName', data.uploadForm.majorName);
  }
  if (data.uploadForm.subjectName) {
    formData.append('disciplineName', data.uploadForm.subjectName);
  }
  
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
      data.uploadForm.majorName = '';
      data.uploadForm.subjectName = '';
      data.uploadForm.majorId = null;
      data.uploadForm.subjectId = null;
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
      url: '/files/myFiles/page',
      method: 'get',
      params: { 
        uploaderId: data.user.userId,
        pageNum: data.myFilePagination.currentPage,
        pageSize: data.myFilePagination.pageSize,
        majorId: data.myFileMajorQuery || undefined,
        disciplineId: data.myFileSubjectQuery || undefined
      }
    }).then(res => {
      if (res.code === '200') {
        data.myFiles = res.data.list || [];
        data.myFilePagination.total = res.data.total || 0;
      }
    });
  }
};

const getSharedFiles = () => {
  request({
    url: '/files/sharedFiles/page',
    method: 'get',
    params: { 
      pageNum: data.sharedFilePagination.currentPage,
      pageSize: data.sharedFilePagination.pageSize,
      majorId: data.sharedFileMajorQuery || undefined,
      disciplineId: data.sharedFileSubjectQuery || undefined
    }
  }).then(res => {
    if (res.code === '200') {
      data.sharedFiles = res.data.list || [];
      data.sharedFilePagination.total = res.data.total || 0;
    }
  });
};

// 搜索我的文件
const searchMyFiles = () => {
  // 向服务器发送带查询条件的请求，无论查询条件是否为空
  let majorId = null;
  let disciplineId = null;
  
  // 优先使用选择的ID，如果没有则尝试使用名称匹配
  if (data.myFileMajorQuery) {
    majorId = data.myFileMajorQuery;
  } else if (data.myFileMajorQueryStr) {
    // 如果输入的是专业名称，查找对应ID
    const major = data.majors.find(m => m.name === data.myFileMajorQueryStr);
    if (major) {
      majorId = major.id;
    } else {
      // 如果没找到精确匹配，尝试模糊匹配
      const fuzzyMajor = data.majors.find(m => m.name.toLowerCase().includes(data.myFileMajorQueryStr.toLowerCase()));
      if (fuzzyMajor) {
        majorId = fuzzyMajor.id;
      }
    }
  }
  
  // 优先使用选择的ID，如果没有则尝试使用名称匹配
  if (data.myFileSubjectQuery) {
    disciplineId = data.myFileSubjectQuery;
  } else if (data.myFileSubjectQueryStr) {
    // 如果输入的是学科名称，查找对应ID
    const subject = data.subjects.find(s => s.name === data.myFileSubjectQueryStr);
    if (subject) {
      disciplineId = subject.id;
    } else {
      // 如果没找到精确匹配，尝试模糊匹配
      const fuzzySubject = data.subjects.find(s => s.name.toLowerCase().includes(data.myFileSubjectQueryStr.toLowerCase()));
      if (fuzzySubject) {
        disciplineId = fuzzySubject.id;
      }
    }
  }
  
  request({
    url: '/files/myFiles/page',
    method: 'get',
    params: { 
      uploaderId: data.user.userId,
      pageNum: data.myFilePagination.currentPage,
      pageSize: data.myFilePagination.pageSize,
      fileName: data.myFileQuery.trim() === '' ? null : data.myFileQuery,  // 添加文件名查询参数，如果为空则传null
      majorId: majorId || undefined,
      disciplineId: disciplineId || undefined
    }
  }).then(res => {
    if (res.code === '200') {
      data.myFiles = res.data.list || [];
      data.myFilePagination.total = res.data.total || 0;
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
  let majorId = null;
  let disciplineId = null;
  
  // 优先使用选择的ID，如果没有则尝试使用名称匹配
  if (data.sharedFileMajorQuery) {
    majorId = data.sharedFileMajorQuery;
  } else if (data.sharedFileMajorQueryStr) {
    // 如果输入的是专业名称，查找对应ID
    const major = data.majors.find(m => m.name === data.sharedFileMajorQueryStr);
    if (major) {
      majorId = major.id;
    } else {
      // 如果没找到精确匹配，尝试模糊匹配
      const fuzzyMajor = data.majors.find(m => m.name.toLowerCase().includes(data.sharedFileMajorQueryStr.toLowerCase()));
      if (fuzzyMajor) {
        majorId = fuzzyMajor.id;
      }
    }
  }
  
  // 优先使用选择的ID，如果没有则尝试使用名称匹配
  if (data.sharedFileSubjectQuery) {
    disciplineId = data.sharedFileSubjectQuery;
  } else if (data.sharedFileSubjectQueryStr) {
    // 如果输入的是学科名称，查找对应ID
    const subject = data.subjects.find(s => s.name === data.sharedFileSubjectQueryStr);
    if (subject) {
      disciplineId = subject.id;
    } else {
      // 如果没找到精确匹配，尝试模糊匹配
      const fuzzySubject = data.subjects.find(s => s.name.toLowerCase().includes(data.sharedFileSubjectQueryStr.toLowerCase()));
      if (fuzzySubject) {
        disciplineId = fuzzySubject.id;
      }
    }
  }
  
  request({
    url: '/files/sharedFiles/page',
    method: 'get',
    params: { 
      pageNum: data.sharedFilePagination.currentPage,
      pageSize: data.sharedFilePagination.pageSize,
      fileName: data.sharedFileQuery.trim() === '' ? null : data.sharedFileQuery,  // 添加文件名查询参数，如果为空则传null
      majorId: majorId || undefined,
      disciplineId: disciplineId || undefined
    }
  }).then(res => {
    if (res.code === '200') {
      data.sharedFiles = res.data.list || [];
      data.sharedFilePagination.total = res.data.total || 0;
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
  // 跳转到文件预览页面，传递当前标签页信息
  router.push({
    path: '/front-file-preview',
    query: { 
      fileId: file.fileId,
      fromTab: data.activeCourseTab
    }
  });
};

// 监听标签页变化，当用户切换标签页时加载对应的数据
const onTabChange = (pane) => {
  const tabName = pane.paneName;
  // 更新当前激活的标签页
  data.activeCourseTab = tabName;
  // 重置分页参数
  if (tabName === 'my-course') {
    data.myFilePagination.currentPage = 1;
    getMyFiles();
  } else if (tabName === 'course-warehouse') {
    data.sharedFilePagination.currentPage = 1;
    getSharedFiles();
  }
  // 不需要为 file-upload 标签页加载特定数据
};

// 处理我的文件每页显示数量改变
const handleMyFileSizeChange = (size) => {
  data.myFilePagination.pageSize = size;
  data.myFilePagination.currentPage = 1;
  getMyFiles();
};

// 处理我的文件当前页改变
const handleMyFileCurrentChange = (currentPage) => {
  data.myFilePagination.currentPage = currentPage;
  getMyFiles();
};

// 处理共享文件每页显示数量改变
const handleSharedFileSizeChange = (size) => {
  data.sharedFilePagination.pageSize = size;
  data.sharedFilePagination.currentPage = 1;
  getSharedFiles();
};

// 处理共享文件当前页改变
const handleSharedFileCurrentChange = (currentPage) => {
  data.sharedFilePagination.currentPage = currentPage;
  getSharedFiles();
};

onMounted(async () => {
  // 加载专业和学科数据
  await loadMajorsAndSubjects();
  
  // 从路由参数获取标签页，如果存在则切换到对应标签页
  const { tab } = route.query;
  if (tab && ['my-course', 'course-warehouse', 'file-upload'].includes(tab)) {
    data.activeCourseTab = tab;
  }
  
  // 根据当前标签页加载对应的数据
  if (data.activeCourseTab === 'my-course') {
    getMyFiles();
  } else if (data.activeCourseTab === 'course-warehouse') {
    getSharedFiles();
  }
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