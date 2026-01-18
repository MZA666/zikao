<template>
  <div class="major-management-container">
    <el-card class="page-header">
      <template #header>
        <div class="card-header">
          <span>专业与学科管理</span>
        </div>
      </template>
      <div class="header-content">
        <p>在此页面您可以管理所有的专业和学科信息。</p>
      </div>
    </el-card>

    <el-row :gutter="20" class="content-row">
      <!-- 专业管理 -->
      <el-col :span="12">
        <el-card class="management-card">
          <template #header>
            <div class="card-header">
              <span>专业管理</span>
              <el-button class="button" type="primary" @click="showAddMajorDialog">新增专业</el-button>
            </div>
          </template>
          
          <el-table :data="data.majors" style="width: 100%" v-loading="data.loading.majors">
            <el-table-column prop="name" label="专业名称" width="180"></el-table-column>
            <el-table-column prop="description" label="描述" show-overflow-tooltip></el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="scope">
                <el-button size="small" @click="editMajor(scope.row)">编辑</el-button>
                <el-button size="small" type="danger" @click="deleteMajor(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 学科管理 -->
      <el-col :span="12">
        <el-card class="management-card">
          <template #header>
            <div class="card-header">
              <span>学科管理</span>
              <el-button class="button" type="primary" @click="showAddSubjectDialog">新增学科</el-button>
            </div>
          </template>
          
          <el-table :data="data.subjects" style="width: 100%" v-loading="data.loading.subjects">
            <el-table-column prop="name" label="学科名称" width="180"></el-table-column>
            <el-table-column label="所属专业" width="120">
              <template #default="scope">
                {{ getMajorNameById(scope.row.majorId) }}
              </template>
            </el-table-column>
            <el-table-column prop="description" label="描述" show-overflow-tooltip></el-table-column>
            <el-table-column label="操作" width="150">
              <template #default="scope">
                <el-button size="small" @click="editSubject(scope.row)">编辑</el-button>
                <el-button size="small" type="danger" @click="deleteSubject(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 新增/编辑专业的对话框 -->
    <el-dialog
      v-model="data.majorDialog.visible"
      :title="data.majorDialog.isEdit ? '编辑专业' : '新增专业'"
      width="500px"
    >
      <el-form :model="data.majorDialog.form" :rules="data.majorDialog.rules" ref="majorFormRef" label-width="100px">
        <el-form-item label="专业名称" prop="name">
          <el-input v-model="data.majorDialog.form.name" placeholder="请输入专业名称"></el-input>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="data.majorDialog.form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入专业描述">
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.majorDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="saveMajor">确认</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 新增/编辑学科的对话框 -->
    <el-dialog
      v-model="data.subjectDialog.visible"
      :title="data.subjectDialog.isEdit ? '编辑学科' : '新增学科'"
      width="500px"
    >
      <el-form :model="data.subjectDialog.form" :rules="data.subjectDialog.rules" ref="subjectFormRef" label-width="100px">
        <el-form-item label="学科名称" prop="name">
          <el-input v-model="data.subjectDialog.form.name" placeholder="请输入学科名称"></el-input>
        </el-form-item>
        <el-form-item label="所属专业" prop="majorId">
          <el-select v-model="data.subjectDialog.form.majorId" placeholder="请选择所属专业" style="width: 100%">
            <el-option
              v-for="major in data.majors"
              :key="major.id"
              :label="major.name"
              :value="major.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="data.subjectDialog.form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入学科描述">
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="data.subjectDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="saveSubject">确认</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import request from '@/utils/request';

const majorFormRef = ref();
const subjectFormRef = ref();

const data = reactive({
  majors: [],
  subjects: [],
  loading: {
    majors: false,
    subjects: false
  },
  majorDialog: {
    visible: false,
    isEdit: false,
    form: {
      id: null,
      name: '',
      description: ''
    },
    rules: {
      name: [
        { required: true, message: '请输入专业名称', trigger: 'blur' }
      ]
    }
  },
  subjectDialog: {
    visible: false,
    isEdit: false,
    form: {
      id: null,
      name: '',
      majorId: null,
      description: ''
    },
    rules: {
      name: [
        { required: true, message: '请输入学科名称', trigger: 'blur' }
      ],
      majorId: [
        { required: true, message: '请选择所属专业', trigger: 'change' }
      ]
    }
  }
});

// 加载专业列表
const loadMajors = async () => {
  data.loading.majors = true;
  try {
    const res = await request({
      url: '/major-management/majors',
      method: 'get'
    });
    if (res.code === '200') {
      data.majors = res.data || [];
    } else {
      ElMessage.error(res.msg || '获取专业列表失败');
    }
  } catch (error) {
    console.error('获取专业列表失败:', error);
    ElMessage.error('获取专业列表失败');
  } finally {
    data.loading.majors = false;
  }
};

// 加载学科列表
const loadSubjects = async () => {
  data.loading.subjects = true;
  try {
    const res = await request({
      url: '/major-management/subjects',
      method: 'get'
    });
    if (res.code === '200') {
      data.subjects = res.data || [];
    } else {
      ElMessage.error(res.msg || '获取学科列表失败');
    }
  } catch (error) {
    console.error('获取学科列表失败:', error);
    ElMessage.error('获取学科列表失败');
  } finally {
    data.loading.subjects = false;
  }
};

// 获取专业名称通过ID
const getMajorNameById = (id) => {
  if (!id) return '';
  const major = data.majors.find(m => m.id === id);
  return major ? major.name : '';
};

// 显示新增专业对话框
const showAddMajorDialog = () => {
  data.majorDialog.isEdit = false;
  data.majorDialog.form = {
    id: null,
    name: '',
    description: ''
  };
  data.majorDialog.visible = true;
};

// 显示编辑专业对话框
const editMajor = (row) => {
  data.majorDialog.isEdit = true;
  data.majorDialog.form = { ...row };
  data.majorDialog.visible = true;
};

// 保存专业
const saveMajor = async () => {
  try {
    await majorFormRef.value.validate();
    
    let res;
    if (data.majorDialog.isEdit) {
      res = await request({
        url: '/major-management/major',
        method: 'put',
        data: data.majorDialog.form
      });
    } else {
      res = await request({
        url: '/major-management/major',
        method: 'post',
        data: data.majorDialog.form
      });
    }
    
    if (res.code === '200') {
      ElMessage.success(data.majorDialog.isEdit ? '专业更新成功' : '专业创建成功');
      data.majorDialog.visible = false;
      loadMajors(); // 重新加载专业列表
    } else {
      ElMessage.error(res.msg || (data.majorDialog.isEdit ? '专业更新失败' : '专业创建失败'));
    }
  } catch (error) {
    console.error(data.majorDialog.isEdit ? '更新专业失败' : '创建专业失败:', error);
    ElMessage.error(data.majorDialog.isEdit ? '专业更新失败' : '专业创建失败');
  }
};

// 删除专业
const deleteMajor = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除专业 "${row.name}" 吗？此操作不可恢复`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );
    
    const res = await request({
      url: `/major-management/major/${row.id}`,
      method: 'delete'
    });
    
    if (res.code === '200') {
      ElMessage.success('专业删除成功');
      loadMajors(); // 重新加载专业列表
      loadSubjects(); // 重新加载学科列表，因为可能有级联影响
    } else {
      ElMessage.error(res.msg || '专业删除失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除专业失败:', error);
      ElMessage.error('专业删除失败');
    }
  }
};

// 显示新增学科对话框
const showAddSubjectDialog = () => {
  data.subjectDialog.isEdit = false;
  data.subjectDialog.form = {
    id: null,
    name: '',
    majorId: null,
    description: ''
  };
  data.subjectDialog.visible = true;
};

// 显示编辑学科对话框
const editSubject = (row) => {
  data.subjectDialog.isEdit = true;
  data.subjectDialog.form = { ...row };
  data.subjectDialog.visible = true;
};

// 保存学科
const saveSubject = async () => {
  try {
    await subjectFormRef.value.validate();
    
    let res;
    if (data.subjectDialog.isEdit) {
      res = await request({
        url: '/major-management/subject',
        method: 'put',
        data: data.subjectDialog.form
      });
    } else {
      res = await request({
        url: '/major-management/subject',
        method: 'post',
        data: data.subjectDialog.form
      });
    }
    
    if (res.code === '200') {
      ElMessage.success(data.subjectDialog.isEdit ? '学科更新成功' : '学科创建成功');
      data.subjectDialog.visible = false;
      loadSubjects(); // 重新加载学科列表
    } else {
      ElMessage.error(res.msg || (data.subjectDialog.isEdit ? '学科更新失败' : '学科创建失败'));
    }
  } catch (error) {
    console.error(data.subjectDialog.isEdit ? '更新学科失败' : '创建学科失败:', error);
    ElMessage.error(data.subjectDialog.isEdit ? '学科更新失败' : '学科创建失败');
  }
};

// 删除学科
const deleteSubject = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除学科 "${row.name}" 吗？此操作不可恢复`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );
    
    const res = await request({
      url: `/major-management/subject/${row.id}`,
      method: 'delete'
    });
    
    if (res.code === '200') {
      ElMessage.success('学科删除成功');
      loadSubjects(); // 重新加载学科列表
    } else {
      ElMessage.error(res.msg || '学科删除失败');
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除学科失败:', error);
      ElMessage.error('学科删除失败');
    }
  }
};

onMounted(() => {
  loadMajors();
  loadSubjects();
});
</script>

<style scoped>
.major-management-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-content {
  padding: 10px 0;
}

.content-row {
  margin-bottom: 20px;
}

.management-card {
  min-height: 400px;
}

.dialog-footer {
  text-align: right;
}
</style>