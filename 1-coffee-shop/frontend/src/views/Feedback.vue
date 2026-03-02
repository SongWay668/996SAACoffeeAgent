<template>
  <div class="feedback-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>反馈管理</span>
          <el-button type="primary" @click="showAddDialog">添加反馈</el-button>
        </div>
      </template>

      <el-table :data="feedbacks" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column prop="feedbackType" label="反馈类型" width="100">
          <template #default="{ row }">
            <el-tag>{{ getFeedbackTypeText(row.feedbackType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="rating" label="评分" width="100">
          <template #default="{ row }">
            <el-rate v-model="row.rating" disabled show-score />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="内容" show-overflow-tooltip />
        <el-table-column prop="solution" label="处理方案" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑反馈' : '添加反馈'" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="用户ID">
          <el-input-number v-model="form.userId" :min="1" />
        </el-form-item>
        <el-form-item label="反馈类型">
          <el-select v-model="form.feedbackType">
            <el-option label="产品反馈" :value="1" />
            <el-option label="服务反馈" :value="2" />
            <el-option label="投诉" :value="3" />
            <el-option label="建议" :value="4" />
            <el-option label="口味反馈" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="评分">
          <el-rate v-model="form.rating" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="处理方案">
          <el-input v-model="form.solution" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { feedbackApi } from '../api/feedback'

const feedbacks = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({})

const fetchFeedbacks = async () => {
  loading.value = true
  try {
    const res = await feedbackApi.getAll()
    if (res.code === 200) {
      feedbacks.value = res.data
    }
  } catch (error) {
    ElMessage.error('获取反馈列表失败')
  } finally {
    loading.value = false
  }
}

const getFeedbackTypeText = (type) => {
  const types = {
    1: '产品反馈',
    2: '服务反馈',
    3: '投诉',
    4: '建议',
    5: '口味反馈'
  }
  return types[type] || '未知'
}

const showAddDialog = () => {
  isEdit.value = false
  form.value = { feedbackType: 1, rating: 5 }
  dialogVisible.value = true
}

const showEditDialog = (row) => {
  isEdit.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    if (isEdit.value) {
      await feedbackApi.update(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await feedbackApi.create(form.value)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    fetchFeedbacks()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该反馈吗?', '提示', { type: 'warning' })
    await feedbackApi.delete(id)
    ElMessage.success('删除成功')
    fetchFeedbacks()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  fetchFeedbacks()
})
</script>

<style scoped>
.feedback-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
