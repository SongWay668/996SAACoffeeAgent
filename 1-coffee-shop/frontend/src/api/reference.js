import request from '../utils/request'

export const referenceApi = {
  getByRefType(refType) {
    return request({
      url: `/reference/type/${refType}`,
      method: 'get'
    })
  },
  getAll() {
    return request({
      url: '/reference',
      method: 'get'
    })
  }
}
