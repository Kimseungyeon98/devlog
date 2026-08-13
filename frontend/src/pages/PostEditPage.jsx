import { useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { getPost, updatePost } from '../api/post'
import PostEditor from '../components/post/PostEditor'

export default function PostEditPage() {
    const { postId } = useParams()
    const navigate = useNavigate()
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState('')
    const draftKey = `draft:${postId}`

    const { data: post, isLoading } = useQuery({
        queryKey: ['post', postId],
        queryFn: () => getPost(postId).then(r => r.data),
    })

    const handleSubmit = async (form, publish) => {
        if (!form.title.trim()) return setError('제목을 입력해주세요.')
        if (!form.content.trim()) return setError('내용을 입력해주세요.')
        setLoading(true)
        setError('')
        try {
            await updatePost(postId, { ...form, publish })
            localStorage.removeItem(draftKey)
            navigate(`/posts/${postId}`)
        } catch {
            setError('게시글 수정에 실패했습니다.')
        } finally {
            setLoading(false)
        }
    }

    if (isLoading) return <div className="text-center text-gray-400 py-20">불러오는 중...</div>
    if (!post) return null

    return (
        <PostEditor
            draftKey={draftKey}
            initialValues={{ title: post.title, content: post.content, tags: post.tags || [] }}
            onSubmit={handleSubmit}
            onCancel={() => navigate(-1)}
            loading={loading}
            error={error}
            submitLabel="수정 완료"
        />
    )
}
