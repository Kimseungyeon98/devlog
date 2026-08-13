import api from './axios'

export const getMyStats = () => api.get('/stats/my')
