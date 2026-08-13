import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { createPost } from '../api/post'
import PostEditor from '../components/post/PostEditor'

const DRAFT_KEY = 'draft:new'

export default function PostCreatePage() {
    const navigate = useNavigate()
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState('')

    const handleSubmit = async (form, publish) => {
        if (!form.title.trim()) return setError('제목을 입력해주세요.')
        if (!form.content.trim()) return setError('내용을 입력해주세요.')
        setLoading(true)
        setError('')
        try {
            const { data: postId } = await createPost({ ...form, publish })
            localStorage.removeItem(DRAFT_KEY)
            navigate(`/posts/${postId}`)
        } catch {
            setError('게시글 작성에 실패했습니다.')
        } finally {
            setLoading(false)
        }
    }

    return (
        <PostEditor
            draftKey={DRAFT_KEY}
            onSubmit={handleSubmit}
            onCancel={() => navigate(-1)}
            loading={loading}
            error={error}
        />
    )
}
