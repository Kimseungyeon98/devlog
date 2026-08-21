import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getPost, deletePost } from '../api/post'
import { getComments, createComment, deleteComment } from '../api/comment'
import { togglePostLike, getPostLikeCount } from '../api/like'
import { getSeriesByPost } from '../api/series'
import { Heart, Edit, Trash2, MessageCircle, BookOpen, ChevronLeft, ChevronRight, Eye, Clock } from 'lucide-react'
import useAuthStore from '../store/authStore'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/ko'
import CommentItem from '../components/comment/CommentItem'
import CommentForm from '../components/comment/CommentForm'
import MarkdownRenderer from '../components/MarkdownRenderer'

dayjs.extend(relativeTime)
dayjs.locale('ko')

function Avatar({ name }) {
    return (
        <div className="w-10 h-10 rounded-full bg-gradient-to-br from-indigo-500 to-violet-600 flex items-center justify-center text-white font-bold text-sm flex-shrink-0">
            {name?.slice(0, 1).toUpperCase() ?? '?'}
        </div>
    )
}

function readingTime(content) {
    return Math.max(1, Math.round((content || '').split(/\s+/).length / 200))
}

export default function PostDetailPage() {
    const { postId } = useParams()
    const navigate = useNavigate()
    const queryClient = useQueryClient()
    const { isLoggedIn } = useAuthStore()
    const [liked, setLiked] = useState(false)

    const { data: post, isLoading } = useQuery({
        queryKey: ['post', postId],
        queryFn: () => getPost(postId).then(r => r.data),
    })

    const { data: comments = [] } = useQuery({
        queryKey: ['comments', postId],
        queryFn: () => getComments(postId).then(r => r.data),
    })

    const { data: likeCount = 0 } = useQuery({
        queryKey: ['likeCount', postId],
        queryFn: () => getPostLikeCount(postId).then(r => r.data),
    })

    const { data: series } = useQuery({
        queryKey: ['postSeries', postId],
        queryFn: () => getSeriesByPost(postId).then(r => r.data),
    })

    const likeMutation = useMutation({
        mutationFn: () => togglePostLike(postId),
        onSuccess: (res) => {
            setLiked(res.data)
            queryClient.invalidateQueries(['likeCount', postId])
        }
    })

    const deleteMutation = useMutation({
        mutationFn: () => deletePost(postId),
        onSuccess: () => navigate('/')
    })

    const commentMutation = useMutation({
        mutationFn: (data) => createComment(postId, data),
        onSuccess: () => queryClient.invalidateQueries(['comments', postId])
    })

    const deleteCommentMutation = useMutation({
        mutationFn: (commentId) => deleteComment(commentId),
        onSuccess: () => queryClient.invalidateQueries(['comments', postId])
    })

    const handleDelete = () => {
        if (window.confirm('게시글을 삭제할까요?')) deleteMutation.mutate()
    }

    const currentIndex = series?.posts?.findIndex(p => String(p.postId) === String(postId)) ?? -1
    const prevPost = currentIndex > 0 ? series.posts[currentIndex - 1] : null
    const nextPost = currentIndex !== -1 && currentIndex < (series?.posts?.length ?? 0) - 1
        ? series.posts[currentIndex + 1] : null

    if (isLoading) return (
        <div className="max-w-3xl mx-auto animate-pulse space-y-4">
            <div className="bg-white rounded-2xl p-10 border border-slate-200">
                <div className="h-8 bg-slate-200 rounded w-2/3 mb-6" />
                <div className="h-4 bg-slate-100 rounded w-1/3 mb-10" />
                {[1,2,3,4,5].map(i => <div key={i} className="h-4 bg-slate-100 rounded mb-3" />)}
            </div>
        </div>
    )

    return (
        <div className="max-w-3xl mx-auto">
            <article className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden mb-5">
                <div className="px-8 pt-10 pb-7">
                    {series && (
                        <button
                            onClick={() => navigate(`/series/${series.id}`)}
                            className="flex items-center gap-2 mb-5 px-3.5 py-1.5 bg-indigo-50 border border-indigo-100
                                       rounded-full text-sm font-medium text-indigo-600 hover:bg-indigo-100 transition w-fit"
                        >
                            <BookOpen size={13} />
                            {series.title}
                            <span className="text-indigo-400 text-xs">· {currentIndex + 1}/{series.posts?.length}편</span>
                        </button>
                    )}

                    <h1 className="text-[2rem] font-extrabold text-slate-900 leading-tight tracking-tight mb-5">
                        {post?.title}
                    </h1>

                    <div className="flex items-center justify-between gap-4 flex-wrap">
                        <div className="flex items-center gap-3">
                            <Avatar name={post?.authorNickname} />
                            <div>
                                <button
                                    onClick={() => navigate(`/users/${post?.authorUsername}`)}
                                    className="font-semibold text-slate-800 hover:text-indigo-600 transition text-sm block"
                                >
                                    {post?.authorNickname}
                                </button>
                                <div className="flex items-center gap-2 text-xs text-slate-400 mt-0.5">
                                    <span>{dayjs(post?.createdAt).format('YYYY년 M월 D일')}</span>
                                    <span>·</span>
                                    <Clock size={11} />
                                    <span>{readingTime(post?.content)}분 읽기</span>
                                    <span>·</span>
                                    <Eye size={11} />
                                    <span>{post?.viewCount}</span>
                                </div>
                            </div>
                        </div>
                        <div className="flex gap-1">
                            <button
                                onClick={() => navigate(`/posts/${postId}/edit`)}
                                className="flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium text-slate-500
                                           hover:text-indigo-600 hover:bg-indigo-50 rounded-lg transition"
                            >
                                <Edit size={13} /> 수정
                            </button>
                            <button
                                onClick={handleDelete}
                                className="flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium text-slate-400
                                           hover:text-red-500 hover:bg-red-50 rounded-lg transition"
                            >
                                <Trash2 size={13} /> 삭제
                            </button>
                        </div>
                    </div>

                    {post?.tags?.length > 0 && (
                        <div className="flex gap-2 flex-wrap mt-5">
                            {post.tags.map(tag => (
                                <span key={tag} className="px-3 py-1 bg-slate-100 text-slate-600 text-xs rounded-full font-medium">
                                    #{tag}
                                </span>
                            ))}
                        </div>
                    )}
                </div>

                <div className="px-8 py-8 border-t border-slate-100 prose-lg">
                    <MarkdownRenderer content={post?.content || ''} />
                </div>

                <div className="flex justify-center py-8 border-t border-slate-100">
                    <button
                        onClick={() => isLoggedIn && likeMutation.mutate()}
                        className={`flex items-center gap-2.5 px-8 py-3 rounded-full border-2 font-medium transition-all
                            ${liked
                                ? 'bg-red-50 border-red-300 text-red-500 shadow-sm'
                                : 'border-slate-200 text-slate-500 hover:border-red-200 hover:bg-red-50 hover:text-red-400'
                            } ${!isLoggedIn ? 'cursor-default' : 'cursor-pointer'}`}
                    >
                        <Heart size={18} className={liked ? 'fill-red-400' : ''} />
                        <span className="text-sm">{likeCount}</span>
                    </button>
                </div>
            </article>

            {series && (prevPost || nextPost) && (
                <div className="bg-white rounded-2xl border border-slate-200 p-5 mb-5">
                    <p className="text-[11px] font-bold text-slate-400 uppercase tracking-widest mb-3">시리즈 탐색</p>
                    <div className="grid grid-cols-2 gap-3">
                        {prevPost ? (
                            <button
                                onClick={() => navigate(`/posts/${prevPost.postId}`)}
                                className="flex items-center gap-2.5 p-4 border border-slate-200 rounded-xl
                                           hover:border-indigo-300 hover:bg-indigo-50 transition text-left group"
                            >
                                <ChevronLeft size={16} className="text-slate-400 group-hover:text-indigo-500 flex-shrink-0" />
                                <div className="min-w-0">
                                    <p className="text-[10px] text-slate-400 mb-1">이전 글</p>
                                    <p className="text-sm text-slate-700 truncate font-medium">{prevPost.title}</p>
                                </div>
                            </button>
                        ) : <div />}
                        {nextPost ? (
                            <button
                                onClick={() => navigate(`/posts/${nextPost.postId}`)}
                                className="flex items-center justify-end gap-2.5 p-4 border border-slate-200 rounded-xl
                                           hover:border-indigo-300 hover:bg-indigo-50 transition text-right group"
                            >
                                <div className="min-w-0">
                                    <p className="text-[10px] text-slate-400 mb-1 text-right">다음 글</p>
                                    <p className="text-sm text-slate-700 truncate font-medium">{nextPost.title}</p>
                                </div>
                                <ChevronRight size={16} className="text-slate-400 group-hover:text-indigo-500 flex-shrink-0" />
                            </button>
                        ) : <div />}
                    </div>
                </div>
            )}

            <div className="bg-white rounded-2xl border border-slate-200 shadow-sm p-8">
                <h2 className="font-bold text-slate-900 mb-6 flex items-center gap-2.5 text-base">
                    <MessageCircle size={18} className="text-indigo-500" />
                    댓글
                    <span className="text-sm font-normal text-slate-400">{comments.length}개</span>
                </h2>
                {isLoggedIn && (
                    <CommentForm
                        onSubmit={(content) => commentMutation.mutate({ content, parentId: null })}
                        placeholder="댓글을 작성하세요..."
                    />
                )}
                {comments.length > 0 && (
                    <div className="mt-8 space-y-6 divide-y divide-slate-100">
                        {comments.map(comment => (
                            <CommentItem
                                key={comment.id}
                                comment={comment}
                                postId={postId}
                                onDelete={(id) => deleteCommentMutation.mutate(id)}
                                onReply={(content, parentId) => commentMutation.mutate({ content, parentId })}
                            />
                        ))}
                    </div>
                )}
            </div>
        </div>
    )
}
