import apiClient from './clients'

export const departementsAPI = {
  getAll() {
    return apiClient.get('/departements')
  },
  getById(id: number) {
    return apiClient.get(`/departements/${id}`)
  },
  create(payload: { nomDepartement: string }) {
    return apiClient.post('/departements', payload)
  },
  update(id: number, payload: { nomDepartement: string }) {
    return apiClient.put(`/departements/${id}`, payload)
  },
  delete(id: number) {
    return apiClient.delete(`/departements/${id}`)
  }
}
