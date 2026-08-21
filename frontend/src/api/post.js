import api from './axios';

export const getPosts = (params) => api.get('/posts', { params });
export const getPost = (postId) => api.get(`/posts/${postId}`);
export const getMyPosts = (params) => api.get('/posts/me', { params });
export const createPost = (data) => api.post('/posts', data);
export const updatePost = (postId, data) => api.put(`/posts/${postId}`, data);
export const deletePost = (postId) => api.delete(`/posts/${postId}`);
