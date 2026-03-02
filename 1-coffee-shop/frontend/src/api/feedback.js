import request from '../utils/request'

export const feedbackApi = {
  getAll() {
    return request({
      url: '/feedback',
      method: 'get'
    })
  },
  getByUserId(userId) {
    return request({
      url: `/feedback/user/${userId}`,
      method: 'get'
    })
  },
  getById(id) {
    return request({
      url: `/feedback/${id}`,
      method: 'get'
    })
  },
  create(data) {
    return request({
      url: '/feedback',
      method: 'post',
      data
    })
  },
  update(id, data) {
    return request({
      url: `/feedback/${id}`,
      method: 'put',
      data
    })
  },
  delete(id) {
    return request({
      url: `/feedback/${id}`,
      method: 'delete'
    })
  }
}
