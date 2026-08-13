import api from './axios';

export const getProfile = (username) => api.get(`/users/${username}/profile`);
export const getMe = () => api.get('/users/me');
export const updateProfile = (data) => api.put('/users/me', data);
export const follow = (followingId) => api.post(`/users/${followingId}/follow`);
export const unfollow = (followingId) => api.delete(`/users/${followingId}/follow`);
