import api from './axios';

export const getComments = (postId) => api.get(`/posts/${postId}/comments`);
export const createComment = (postId, data) => api.post(`/posts/${postId}/comments`, data);
export const updateComment = (commentId, data) => api.put(`/comments/${commentId}`, data);
export const deleteComment = (commentId) => api.delete(`/comments/${commentId}`);
