import request from '../utils/request'

export const orderApi = {
  getAll() {
    return request({
      url: '/orders',
      method: 'get'
    })
  },
  getByUserId(userId) {
    return request({
      url: `/orders/user/${userId}`,
      method: 'get'
    })
  },
  getById(id) {
    return request({
      url: `/orders/${id}`,
      method: 'get'
    })
  },
  create(data) {
    return request({
      url: '/orders',
      method: 'post',
      data
    })
  },
  update(id, data) {
    return request({
      url: `/orders/${id}`,
      method: 'put',
      data
    })
  },
  delete(id) {
    return request({
      url: `/orders/${id}`,
      method: 'delete'
    })
  }
}
