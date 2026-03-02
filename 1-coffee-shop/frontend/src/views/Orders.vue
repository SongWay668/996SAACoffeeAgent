<template>
  <div class="orders-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>订单管理</span>
          <el-button type="primary" @click="showAddDialog">创建订单</el-button>
        </div>
      </template>

      <el-table :data="orders" v-loading="loading" border>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="orderId" label="订单编号" width="150" />
        <el-table-column prop="userId" label="用户ID" width="80" />
        <el-table-column prop="productName" label="产品名称" width="150" />
        <el-table-column prop="quantity" label="数量" width="70" />
        <el-table-column prop="unitPrice" label="单价" width="80" />
        <el-table-column prop="totalPrice" label="总价" width="80" />
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑订单' : '创建订单'" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="订单编号">
          <el-input v-model="form.orderId" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="用户ID">
          <el-input-number v-model="form.userId" :min="1" />
        </el-form-item>
        <el-form-item label="产品ID">
          <el-input-number v-model="form.productId" :min="1" />
        </el-form-item>
        <el-form-item label="产品名称">
          <el-input v-model="form.productName" />
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="form.quantity" :min="1" />
        </el-form-item>
        <el-form-item label="单价">
          <el-input-number v-model="form.unitPrice" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
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
import { orderApi } from '../api/order'

const orders = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref({})

const fetchOrders = async () => {
  loading.value = true
  try {
    const res = await orderApi.getAll()
    if (res.code === 200) {
      orders.value = res.data
    }
  } catch (error) {
    ElMessage.error('获取订单列表失败')
  } finally {
    loading.value = false
  }
}

const showAddDialog = () => {
  isEdit.value = false
  form.value = { orderId: `ORD${Date.now()}`, quantity: 1, unitPrice: 0 }
  dialogVisible.value = true
}

const showEditDialog = (row) => {
  isEdit.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    form.value.totalPrice = form.value.unitPrice * form.value.quantity
    if (isEdit.value) {
      await orderApi.update(form.value.id, form.value)
      ElMessage.success('更新成功')
    } else {
      await orderApi.create(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchOrders()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除该订单吗?', '提示', { type: 'warning' })
    await orderApi.delete(id)
    ElMessage.success('删除成功')
    fetchOrders()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped>
.orders-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
