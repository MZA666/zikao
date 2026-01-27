<template>
  <div class="exam-upload-container">
    <div class="header">
      <h2>题库上传</h2>
    </div>

    <div class="upload-section">
      <el-card class="upload-card">
        <h3>上传考试题库</h3>
        <p>请上传包含考试题目的Word文档（.doc或.docx格式）或通过表单录入题目</p>
        
        <!-- 添加选项卡切换 -->
        <el-tabs v-model="activeTab" type="card" class="upload-tabs">
          <el-tab-pane label="文件上传" name="file">
            <el-form :model="uploadForm" label-width="120px" class="upload-form">
              <el-form-item label="题库名称">
                <el-input
                  v-model="uploadForm.bankName"
                  placeholder="请输入题库名称"
                  clearable
                />
              </el-form-item>
              
              <el-form-item label="选择专业">
                <el-autocomplete
                  v-model="uploadForm.majorName"
                  :fetch-suggestions="queryMajors"
                  placeholder="选择或输入专业"
                  clearable
                  @select="onMajorSelect"
                  @change="onMajorChange"
                  @clear="onMajorClear"
                  style="width: 100%;"
                >
                  <template #default="{ item }">
                    <div class="value">{{ item.name }}</div>
                  </template>
                </el-autocomplete>
              </el-form-item>
              
              <el-form-item label="选择学科">
                <el-autocomplete
                  v-model="uploadForm.subjectName"
                  :fetch-suggestions="querySubjects"
                  placeholder="选择或输入学科"
                  clearable
                  @select="onSubjectSelect"
                  @change="onSubjectChange"
                  @clear="onSubjectClear"
                  style="width: 100%;"
                >
                  <template #default="{ item }">
                    <div class="value">{{ item.name }}</div>
                  </template>
                </el-autocomplete>
              </el-form-item>
              
              <el-form-item label="上传文件">
                <el-upload
                  class="upload-demo"
                  drag
                  :action="uploadUrl"
                  :data="uploadFormData"
                  :on-success="handleUploadSuccess"
                  :on-error="handleUploadError"
                  :before-upload="beforeUpload"
                  :on-change="handleFileChange"
                  :file-list="fileList"
                  :auto-upload="false"
                  ref="upload"
                  accept=".doc,.docx"
                >
                  <i class="el-icon-upload"></i>
                  <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
                  <template #tip>
                    <div class="el-upload__tip">
                      只能上传Word文档文件，且不超过50MB
                    </div>
                  </template>
                </el-upload>
              </el-form-item>
              
              <el-form-item>
                <el-button type="primary" @click="submitUpload" :loading="uploading">开始上传</el-button>
                <el-button @click="clearFiles">清空</el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
          
          <el-tab-pane label="表单上传" name="form">
            <div class="form-upload-section">
              <el-form :model="questionForm" label-width="120px" class="question-form">
                <el-form-item label="题库名称">
                  <el-input
                    v-model="questionForm.bankName"
                    placeholder="请输入题库名称"
                    clearable
                  />
                </el-form-item>
                
                <el-form-item label="选择专业">
                  <el-autocomplete
                    v-model="questionForm.majorName"
                    :fetch-suggestions="queryMajors"
                    placeholder="选择或输入专业"
                    clearable
                    @select="onQuestionMajorSelect"
                    @change="onQuestionMajorChange"
                    @clear="onQuestionMajorClear"
                    style="width: 100%;"
                  >
                    <template #default="{ item }">
                      <div class="value">{{ item.name }}</div>
                    </template>
                  </el-autocomplete>
                </el-form-item>
                
                <el-form-item label="选择学科">
                  <el-autocomplete
                    v-model="questionForm.subjectName"
                    :fetch-suggestions="querySubjects"
                    placeholder="选择或输入学科"
                    clearable
                    @select="onQuestionSubjectSelect"
                    @change="onQuestionSubjectChange"
                    @clear="onQuestionSubjectClear"
                    style="width: 100%;"
                  >
                    <template #default="{ item }">
                      <div class="value">{{ item.name }}</div>
                    </template>
                  </el-autocomplete>
                </el-form-item>
                
                <el-form-item label="题目类型">
                  <el-select v-model="questionForm.type" placeholder="请选择题目类型" @change="handleTypeChange">
                    <el-option label="单选题" value="SINGLE_CHOICE"></el-option>
                    <el-option label="多选题" value="MULTIPLE_CHOICE"></el-option>
                    <el-option label="判断题" value="TRUE_FALSE"></el-option>
                    <el-option label="填空题" value="FILL_BLANK"></el-option>
                    <el-option label="问答题" value="ESSAY"></el-option>
                  </el-select>
                </el-form-item>
                
                <el-form-item label="题目内容">
                  <el-input 
                    v-model="questionForm.content" 
                    type="textarea" 
                    :rows="3" 
                    placeholder="请输入题目内容"
                  />
                </el-form-item>
                
                <el-form-item label="题目难度">
                  <el-select v-model="questionForm.difficulty" placeholder="请选择难度">
                    <el-option label="简单" value="EASY"></el-option>
                    <el-option label="中等" value="MEDIUM"></el-option>
                    <el-option label="困难" value="HARD"></el-option>
                  </el-select>
                </el-form-item>
                
                <el-form-item label="章节" v-if="showChapterField">
                  <el-input v-model="questionForm.chapter" placeholder="请输入章节名称（可选）" />
                </el-form-item>
                
                <!-- 单选题和多选题选项 -->
                <div v-if="questionForm.type === 'SINGLE_CHOICE' || questionForm.type === 'MULTIPLE_CHOICE'">
                  <h4>题目选项</h4>
                  <div v-for="(option, index) in questionForm.options" :key="index" class="option-item">
                    <el-row :gutter="10">
                      <el-col :span="2">
                        <el-tag type="info">{{ option.label }}</el-tag>
                      </el-col>
                      <el-col :span="16">
                        <el-input v-model="option.content" placeholder="选项内容"></el-input>
                      </el-col>
                      <el-col :span="4">
                        <el-checkbox v-model="option.isCorrect">
                          {{ questionForm.type === 'SINGLE_CHOICE' ? '正确答案' : '正确选项' }}
                        </el-checkbox>
                      </el-col>
                      <el-col :span="2">
                        <el-button size="small" @click="removeOption(index)">删除</el-button>
                      </el-col>
                    </el-row>
                  </div>
                  <el-button @click="addOption" size="small" style="margin-top: 10px;">添加选项</el-button>
                </div>
                
                <!-- 判断题选项 -->
                <div v-else-if="questionForm.type === 'TRUE_FALSE'">
                  <h4>请选择正确答案</h4>
                  <el-radio-group v-model="questionForm.answer">
                    <el-radio label="true">正确</el-radio>
                    <el-radio label="false">错误</el-radio>
                  </el-radio-group>
                </div>
                
                <!-- 填空题和问答题答案 -->
                <el-form-item label="答案" v-else>
                  <el-input 
                    v-model="questionForm.answer" 
                    type="textarea" 
                    :rows="3" 
                    placeholder="请输入答案"
                  />
                </el-form-item>
                
                <el-form-item label="解析" v-if="showAnalysisField">
                  <el-input 
                    v-model="questionForm.analysis" 
                    type="textarea" 
                    :rows="3" 
                    placeholder="请输入题目解析（可选）"
                  />
                </el-form-item>
                
                <el-form-item>
                  <el-button type="primary" @click="addQuestionToBatch">添加到批量列表</el-button>
                  <el-button @click="clearQuestionForm">清空表单</el-button>
                </el-form-item>
              </el-form>
              
              <!-- 批量上传列表 -->
              <div v-if="batchQuestions.length > 0" class="batch-questions">
                <h3>批量上传列表 ({{ batchQuestions.length }} 道题)</h3>
                <div v-for="(question, index) in batchQuestions" :key="index" class="question-item">
                  <el-card class="question-card">
                    <div class="question-header">
                      <span class="question-type">{{ getQuestionTypeLabel(question.type) }}</span>
                      <el-button type="danger" size="small" @click="removeQuestionFromBatch(index)">删除</el-button>
                    </div>
                    <div class="question-content">{{ question.content }}</div>
                    <div class="question-answer">答案: {{ formatAnswer(question) }}</div>
                  </el-card>
                </div>
                <el-button type="primary" @click="submitBatchQuestions" :loading="batchUploading" style="margin-top: 20px;">
                  批量上传
                </el-button>
                <el-button @click="clearBatchQuestions" style="margin-top: 20px;">清空列表</el-button>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
        
        <div class="upload-tips">
          <h4>上传须知：</h4>
          <ul>
            <li>支持 .doc 和 .docx 格式的Word文档</li>
            <li>文档内容需符合考试题库模板格式</li>
            <li>单个文件大小不超过50MB</li>
            <li>题目格式应按照考题模板要求编写</li>
          </ul>
          
          <h4>考题模板格式示例：</h4>
          <div class="template-example">
            <pre>{{ templateExample }}</pre>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'ExamUpload',
  data() {
    return {
      activeTab: 'file', // 新增：当前激活的选项卡
      currentUser: {}, // 当前登录用户信息
      subjects: [],
      uploadForm: {
        bankName: '', // 题库名称
        majorId: '', // 专业ID
        majorName: '', // 专业名称
        subjectId: '',
        subjectName: '' // 新增：用于autocomplete的学科名称
      },
      questionForm: { // 新增：表单上传题目表单
        bankName: '', // 题库名称
        majorId: '', // 专业ID
        majorName: '', // 专业名称
        subjectId: '',
        subjectName: '', // 新增：用于autocomplete的学科名称
        type: '',
        content: '',
        answer: '',
        analysis: '',
        chapter: '',
        difficulty: 'MEDIUM',
        options: [] // 选项列表
      },
      batchQuestions: [], // 新增：批量上传题目列表
      fileList: [],
      uploading: false,
      batchUploading: false, // 新增：批量上传状态
      uploadUrl: request.defaults.baseURL + '/exam/upload/bank',
      templateExample: `单选题
1、消防救援队伍作战训练行动，遵循（ ）的指导思想。
A.救人第一、科学施救 
B.安全第一、预防为主 
C.生命至上  
D.强攻近战、固移结合。
答案：B
解析：略

多选题
1、高层建筑火灾现场指挥员应充分利用消防控制室"两屏、三器、两柜"等设施，掌握火势发展变化和建筑消防设施动作情况，实施灾情研判和决策指挥，其中"两屏"指的是：
A.视频监控屏		
B.消防电源显示屏		
C.预警信息发布屏		
D.图形显示屏（消防设施）
答案：AD

判断题
1、绳索救援过程中一定要打绳尾结。	
答案：对
解析：XXX

填空题
1.我国古典四大名著是（）（）（）（） 
答案:红楼梦|水浒传|三国演义|西游记 

问答题
1.如何保持身体健康？ 
答案:规律饮食、坚持锻炼，早睡早起，定期体检。`
    }
  },
  computed: {
    uploadFormData() {
      // 将当前用户信息也包含在上传数据中
      const data = {
        subjectId: this.uploadForm.subjectId,
        bankName: this.uploadForm.bankName,
        majorName: this.uploadForm.majorName,
        subjectName: this.uploadForm.subjectName
      };
      
      // 添加用户信息
      if (this.currentUser && this.currentUser.userId) {
        data.userId = this.currentUser.userId;
      }
      if (this.currentUser && this.currentUser.name) {
        data.username = this.currentUser.name;
      } else if (this.currentUser && this.currentUser.username) {
        data.username = this.currentUser.username;
      }
      
      // 添加专业ID
      if (this.uploadForm.majorId) {
        data.majorId = this.uploadForm.majorId;
      }
      
      return data;
    },
    // 新增：根据题目类型显示相关字段
    showChapterField() {
      return this.questionForm.type !== '';
    },
    showAnalysisField() {
      return this.questionForm.type !== '';
    }
  },
  created() {
    this.loadCurrentUser(); // 加载当前用户信息
    this.loadMajors(); // 加载专业列表
    this.loadSubjects()
  },
  methods: {
    // 查询专业建议
    queryMajors(queryString, cb) {
      let results = queryString ? 
        this.majors.filter(item => item.name.toLowerCase().indexOf(queryString.toLowerCase()) !== -1) : 
        this.majors;
      cb(results);
    },
    
    // 专业选择事件（当选中下拉选项时触发）
    onMajorSelect(item) {
      this.uploadForm.majorId = item.id;
      this.uploadForm.majorName = item.name;
    },
    
    // 专业改变事件（当输入值改变时触发）
    onMajorChange(value) {
      // 如果输入的值匹配某个专业名称，则设置对应的ID
      const matchedMajor = this.majors.find(major => major.name === value);
      if (matchedMajor) {
        this.uploadForm.majorId = matchedMajor.id;
      } else {
        // 如果输入的是自定义值，暂时清空majorId
        this.uploadForm.majorId = '';
      }
    },
    
    // 专业清除事件
    onMajorClear() {
      this.uploadForm.majorId = '';
      this.uploadForm.majorName = '';
    },
    
    // 问题表单的专业选择事件
    onQuestionMajorSelect(item) {
      this.questionForm.majorId = item.id;
      this.questionForm.majorName = item.name;
    },
    
    // 问题表单的专业改变事件
    onQuestionMajorChange(value) {
      // 如果输入的值匹配某个专业名称，则设置对应的ID
      const matchedMajor = this.majors.find(major => major.name === value);
      if (matchedMajor) {
        this.questionForm.majorId = matchedMajor.id;
      } else {
        // 如果输入的是自定义值，暂时清空majorId
        this.questionForm.majorId = '';
      }
    },
    
    // 问题表单的专业清除事件
    onQuestionMajorClear() {
      this.questionForm.majorId = '';
      this.questionForm.majorName = '';
    },
    
    // 查询学科建议
    querySubjects(queryString, cb) {
      let results = queryString ? 
        this.subjects.filter(item => item.name.toLowerCase().indexOf(queryString.toLowerCase()) !== -1) : 
        this.subjects;
      cb(results);
    },
    
    // 学科选择事件（当选中下拉选项时触发）
    onSubjectSelect(item) {
      this.uploadForm.subjectId = item.id;
      this.uploadForm.subjectName = item.name;
    },
    
    // 学科改变事件（当输入值改变时触发）
    onSubjectChange(value) {
      // 如果输入的值匹配某个学科名称，则设置对应的ID
      const matchedSubject = this.subjects.find(subject => subject.name === value);
      if (matchedSubject) {
        this.uploadForm.subjectId = matchedSubject.id;
      } else {
        // 如果输入的是自定义值，暂时清空subjectId
        this.uploadForm.subjectId = '';
      }
    },
    
    // 学科清除事件
    onSubjectClear() {
      this.uploadForm.subjectId = '';
      this.uploadForm.subjectName = '';
    },
    
    // 问题表单的学科选择事件
    onQuestionSubjectSelect(item) {
      this.questionForm.subjectId = item.id;
      this.questionForm.subjectName = item.name;
    },
    
    // 问题表单的学科改变事件
    onQuestionSubjectChange(value) {
      // 如果输入的值匹配某个学科名称，则设置对应的ID
      const matchedSubject = this.subjects.find(subject => subject.name === value);
      if (matchedSubject) {
        this.questionForm.subjectId = matchedSubject.id;
      } else {
        // 如果输入的是自定义值，暂时清空subjectId
        this.questionForm.subjectId = '';
      }
    },
    
    // 问题表单的学科清除事件
    onQuestionSubjectClear() {
      this.questionForm.subjectId = '';
      this.questionForm.subjectName = '';
    },
    
    // 加载当前用户信息
    loadCurrentUser() {
      const userData = localStorage.getItem('system-user');
      if (userData) {
        this.currentUser = JSON.parse(userData);
      } else {
        this.$message.error('请先登录');
        this.$router.push('/login');
      }
    },
    
    // 加载专业列表
    async loadMajors() {
      try {
        const response = await request.get('/files/majors')
        this.majors = response.data
      } catch (error) {
        console.error('加载专业列表失败:', error)
        this.$message.error('加载专业列表失败')
      }
    },
    
    // 加载学科列表
    async loadSubjects() {
      try {
        const response = await request.get('/exam/subject/list')
        this.subjects = response.data
      } catch (error) {
        console.error('加载学科列表失败:', error)
        this.$message.error('加载学科列表失败')
      }
    },
    
    // 监听文件变化
    handleFileChange(file, fileList) {
      // 更新组件内部的fileList
      this.fileList = fileList
    },
    
    // 上传前检查
    beforeUpload(file) {
      const isWord = file.type === 'application/msword' || 
                     file.type === 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
      const isLt50M = file.size / 1024 / 1024 < 50

      if (!isWord) {
        this.$message.error('只能上传Word文档文件!')
      }
      if (!isLt50M) {
        this.$message.error('文件大小不能超过50MB!')
      }
      return isWord && isLt50M
    },
    
    // 开始上传
    submitUpload() {
      if (!this.uploadForm.bankName) {
        this.$message.warning('请输入题库名称')
        return
      }
      
      if (!this.uploadForm.majorName) {
        this.$message.warning('请输入专业名称')
        return
      }
      
      if (!this.uploadForm.subjectId) {
        this.$message.warning('请选择学科')
        return
      }
      
      if (this.fileList.length === 0) {
        this.$message.warning('请先选择文件')
        return
      }
      
      this.uploading = true
      this.$refs.upload.submit()
    },
    
    // 上传成功
    handleUploadSuccess(response, file, fileList) {
      this.uploading = false
      if (response.code === 200) {
        this.$message.success(response.msg || '上传成功')
        this.fileList = []
      } else {
        this.$message.error(response.msg || '上传失败')
      }
    },
    
    // 上传失败
    handleUploadError(err, file, fileList) {
      this.uploading = false
      console.error('上传失败:', err)
      this.$message.error('上传失败，请重试')
    },
    
    // 清空文件
    clearFiles() {
      this.fileList = []
      this.$refs.upload.clearFiles()
    },
    
    // 新增：处理题目类型变化
    handleTypeChange() {
      // 根据题目类型初始化选项
      if (this.questionForm.type === 'SINGLE_CHOICE' || this.questionForm.type === 'MULTIPLE_CHOICE') {
        // 清空选项
        this.questionForm.options = [
          { label: 'A', content: '', isCorrect: false },
          { label: 'B', content: '', isCorrect: false },
          { label: 'C', content: '', isCorrect: false },
          { label: 'D', content: '', isCorrect: false }
        ];
        this.questionForm.answer = '';
      } else if (this.questionForm.type === 'TRUE_FALSE') {
        this.questionForm.answer = 'true';
        this.questionForm.options = [];
      } else {
        // 填空题、问答题
        this.questionForm.answer = '';
        this.questionForm.options = [];
      }
    },
    
    // 新增：添加选项
    addOption() {
      const nextLabel = String.fromCharCode(65 + this.questionForm.options.length); // A, B, C...
      this.questionForm.options.push({
        label: nextLabel,
        content: '',
        isCorrect: false
      });
    },
    
    // 新增：删除选项
    removeOption(index) {
      this.questionForm.options.splice(index, 1);
    },
    
    // 新增：添加题目到批量列表
    addQuestionToBatch() {
      if (!this.validateQuestionForm()) {
        return;
      }
      
      // 创建题目副本
      const question = { ...this.questionForm };
      
      // 处理选项
      if (question.type === 'SINGLE_CHOICE' || question.type === 'MULTIPLE_CHOICE') {
        // 过滤掉空选项
        question.options = question.options.filter(option => option.content.trim() !== '');
        
        // 根据题目类型设置答案
        if (question.type === 'SINGLE_CHOICE') {
          const correctOption = question.options.find(option => option.isCorrect);
          question.answer = correctOption ? correctOption.label : '';
        } else if (question.type === 'MULTIPLE_CHOICE') {
          const correctOptions = question.options.filter(option => option.isCorrect);
          question.answer = correctOptions.map(option => option.label).join('');
        }
      }
      
      // 添加到批量列表
      this.batchQuestions.push(question);
      this.$message.success('题目已添加到批量列表');
      this.clearQuestionForm();
    },
    
    // 新增：验证题目表单
    validateQuestionForm() {
      if (!this.questionForm.bankName?.trim()) {
        this.$message.warning('请输入题库名称');
        return false;
      }
      
      if (!this.questionForm.subjectId) {
        this.$message.warning('请选择学科');
        return false;
      }
      
      if (!this.questionForm.type) {
        this.$message.warning('请选择题目类型');
        return false;
      }
      
      if (!this.questionForm.content.trim()) {
        this.$message.warning('请输入题目内容');
        return false;
      }
      
      if (this.questionForm.type === 'SINGLE_CHOICE' || this.questionForm.type === 'MULTIPLE_CHOICE') {
        const hasContent = this.questionForm.options.some(option => option.content.trim() !== '');
        if (!hasContent) {
          this.$message.warning('请至少添加一个选项');
          return false;
        }
        
        if (this.questionForm.type === 'SINGLE_CHOICE') {
          const correctCount = this.questionForm.options.filter(option => option.isCorrect).length;
          if (correctCount !== 1) {
            this.$message.warning('单选题必须且只能选择一个正确答案');
            return false;
          }
        } else if (this.questionForm.type === 'MULTIPLE_CHOICE') {
          const correctCount = this.questionForm.options.filter(option => option.isCorrect).length;
          if (correctCount === 0) {
            this.$message.warning('多选题至少需要选择一个正确答案');
            return false;
          }
        }
      } else if (this.questionForm.type === 'TRUE_FALSE') {
        // 判断题不需要额外验证
      } else {
        // 填空题、问答题验证答案
        if (!this.questionForm.answer.trim()) {
          this.$message.warning('请输入答案');
          return false;
        }
      }
      
      return true;
    },
    
    // 新增：清空题目表单
    clearQuestionForm() {
      this.questionForm = {
        subjectId: '',
        subjectName: '',
        type: '',
        content: '',
        answer: '',
        analysis: '',
        chapter: '',
        difficulty: 'MEDIUM',
        options: []
      };
    },
    
    // 新增：从批量列表删除题目
    removeQuestionFromBatch(index) {
      this.batchQuestions.splice(index, 1);
      this.$message.info('已从批量列表中删除该题目');
    },
    
    // 新增：清空批量列表
    clearBatchQuestions() {
      this.batchQuestions = [];
      this.$message.info('已清空批量列表');
    },
    
    // 新增：提交批量题目
    async submitBatchQuestions() {
      if (this.batchQuestions.length === 0) {
        this.$message.warning('批量列表中没有题目');
        return;
      }
      
      this.batchUploading = true;
      
      try {
        // 发送批量上传请求，包含用户信息
        const requestData = {
          questions: this.batchQuestions,
          userId: this.currentUser.userId,
          username: this.currentUser.name || this.currentUser.username,
          // 使用第一个题目的题库名称和专业信息
          bankName: this.batchQuestions[0].bankName,
          majorName: this.batchQuestions[0].majorName,
          subjectName: this.batchQuestions[0].subjectName,
          subjectId: this.batchQuestions[0].subjectId
        };
        
        const response = await request.post('/exam/upload/batch', requestData);
        
        if (response.code === 200) {
          this.$message.success(response.msg || `成功上传${this.batchQuestions.length}道题目`);
          this.batchQuestions = []; // 清空列表
        } else {
          this.$message.error(response.msg || '批量上传失败');
        }
      } catch (error) {
        console.error('批量上传失败:', error);
        this.$message.error('批量上传失败，请重试');
      } finally {
        this.batchUploading = false;
      }
    },
    
    // 新增：获取题目类型标签
    getQuestionTypeLabel(type) {
      const typeLabels = {
        'SINGLE_CHOICE': '单选题',
        'MULTIPLE_CHOICE': '多选题',
        'TRUE_FALSE': '判断题',
        'FILL_BLANK': '填空题',
        'ESSAY': '问答题'
      };
      return typeLabels[type] || type;
    },
    
    // 新增：格式化答案显示
    formatAnswer(question) {
      if (question.type === 'TRUE_FALSE') {
        return question.answer === 'true' ? '正确' : '错误';
      }
      return question.answer;
    }
  }
}
</script>

<style scoped>
.exam-upload-container {
  padding: 20px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.header {
  margin-bottom: 20px;
}

.upload-card {
  max-width: 800px;
  margin: 0 auto;
}

.upload-form {
  margin-top: 20px;
}

.upload-tabs {
  margin-bottom: 20px;
}

.upload-tabs >>> .el-tabs__item {
  font-size: 14px;
}

.form-upload-section {
  padding: 20px 0;
}

.question-form {
  margin-top: 20px;
}

.option-item {
  margin-bottom: 10px;
}

.batch-questions {
  margin-top: 30px;
}

.question-item {
  margin-bottom: 15px;
}

.question-card {
  margin-bottom: 10px;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.question-type {
  font-weight: bold;
  color: #409EFF;
}

.question-content {
  margin-bottom: 5px;
}

.question-answer {
  color: #67C23A;
  font-weight: bold;
}

.upload-demo {
  text-align: center;
}

.upload-tips {
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.upload-tips h4 {
  margin: 15px 0 10px 0;
  color: #303133;
}

.upload-tips ul {
  padding-left: 20px;
  color: #606266;
}

.template-example {
  background-color: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  border-left: 4px solid #409EFF;
  overflow-x: auto;
}

.template-example pre {
  margin: 0;
  font-family: monospace;
  font-size: 13px;
  line-height: 1.5;
  color: #303133;
}

.el-autocomplete {
  width: 100%;
}
</style>