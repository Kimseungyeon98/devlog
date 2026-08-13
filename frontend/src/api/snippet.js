import api from './axios'

export const getSnippets = (params) => api.get('/snippets', { params })
export const getSnippet = (id) => api.get(`/snippets/${id}`)
export const createSnippet = (data) => api.post('/snippets', data)
export const updateSnippet = (id, data) => api.put(`/snippets/${id}`, data)
export const deleteSnippet = (id) => api.delete(`/snippets/${id}`)
