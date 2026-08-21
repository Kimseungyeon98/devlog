import api from './axios';

export const togglePostLike = (postId) => api.post(`/posts/${postId}/likes`);
export const getPostLikeCount = (postId) => api.get(`/posts/${postId}/likes`);
export const toggleCommentLike = (commentId) => api.post(`/comments/${commentId}/likes`);
