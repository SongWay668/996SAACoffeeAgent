import { createRouter, createWebHistory } from 'vue-router'
import Users from '../views/Users.vue'
import Products from '../views/Products.vue'
import Orders from '../views/Orders.vue'
import Feedback from '../views/Feedback.vue'

const routes = [
  { path: '/', redirect: '/users' },
  { path: '/users', component: Users },
  { path: '/products', component: Products },
  { path: '/orders', component: Orders },
  { path: '/feedback', component: Feedback }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
