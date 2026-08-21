import api from './axios';

export const createSeries = (data) => api.post('/series', data);
export const updateSeries = (seriesId, data) => api.put(`/series/${seriesId}`, data);
export const deleteSeries = (seriesId) => api.delete(`/series/${seriesId}`);
export const getSeriesDetail = (seriesId) => api.get(`/series/${seriesId}`);
export const getUserSeries = (username) => api.get(`/series/user/${username}`);
export const addPostToSeries = (seriesId, data) => api.post(`/series/${seriesId}/posts`, data);
export const removePostFromSeries = (seriesId, postId) => api.delete(`/series/${seriesId}/posts/${postId}`);
export const getSeriesByPost = (postId) => api.get(`/series/post/${postId}`);
