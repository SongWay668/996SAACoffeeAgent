import request from '../utils/request'

export const productApi = {
  getAll() {
    return request({
      url: '/products',
      method: 'get'
    })
  },
  getAvailable() {
    return request({
      url: '/products/available',
      method: 'get'
    })
  },
  getById(id) {
    return request({
      url: `/products/${id}`,
      method: 'get'
    })
  },
  create(data) {
    return request({
      url: '/products',
      method: 'post',
      data
    })
  },
  update(id, data) {
    return request({
      url: `/products/${id}`,
      method: 'put',
      data
    })
  },
  delete(id) {
    return request({
      url: `/products/${id}`,
      method: 'delete'
    })
  }
}
