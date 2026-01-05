<template>
  <div class="exam-upload-container">
    <div class="header">
      <h2>题库上传</h2>
    </div>

    <div class="upload-section">
      <el-card class="upload-card">
        <h3>上传考试题库</h3>
        <p>请上传包含考试题目的Word文档（.doc或.docx格式）</p>
        
        <el-form :model="uploadForm" label-width="120px" class="upload-form">
          <el-form-item label="选择学科">
            <el-select v-model="uploadForm.subjectId" placeholder="请选择学科">
              <el-option
                v-for="subject in subjects"
                :key="subject.id"
                :label="subject.name"
                :value="subject.id"
              />
            </el-select>
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
      subjects: [],
      uploadForm: {
        subjectId: ''
      },
      fileList: [],
      uploading: false,
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
      return {
        subjectId: this.uploadForm.subjectId
      }
    }
  },
  created() {
    this.loadSubjects()
  },
  methods: {
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
</style>