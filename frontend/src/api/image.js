import api from './axios'

export const uploadImage = (file) => {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/images', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
    })
}
