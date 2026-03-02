import request from '../utils/request'

export const userApi = {
  getAll() {
    return request({
      url: '/users',
      method: 'get'
    })
  },
  getById(id) {
    return request({
      url: `/users/${id}`,
      method: 'get'
    })
  },
  create(data) {
    return request({
      url: '/users',
      method: 'post',
      data
    })
  },
  update(id, data) {
    return request({
      url: `/users/${id}`,
      method: 'put',
      data
    })
  },
  delete(id) {
    return request({
      url: `/users/${id}`,
      method: 'delete'
    })
  }
}
