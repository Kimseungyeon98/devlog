import { useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { toggleCommentLike } from '../../api/like'
import { updateComment } from '../../api/comment'
import { Heart, Trash2, Edit, MessageSquare } from 'lucide-react'
import useAuthStore from '../../store/authStore'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/ko'
import CommentForm from './CommentForm'

dayjs.extend(relativeTime)
dayjs.locale('ko')

function Avatar({ name, deleted }) {
    return (
        <div className={`w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold flex-shrink-0
            ${deleted
                ? 'bg-slate-100 text-slate-400'
                : 'bg-gradient-to-br from-indigo-400 to-violet-500 text-white'
            }`}>
            {deleted ? '?' : name?.slice(0, 1).toUpperCase()}
        </div>
    )
}

export default function CommentItem({ comment, postId, onDelete, onReply }) {
    const { isLoggedIn } = useAuthStore()
    const queryClient = useQueryClient()
    const [showReply, setShowReply] = useState(false)
    const [editing, setEditing] = useState(false)
    const [liked, setLiked] = useState(false)
    const [likeCount, setLikeCount] = useState(0)

    const likeMutation = useMutation({
        mutationFn: () => toggleCommentLike(comment.id),
        onSuccess: (res) => {
            setLiked(res.data)
            setLikeCount(prev => res.data ? prev + 1 : prev - 1)
        }
    })

    const editMutation = useMutation({
        mutationFn: (content) => updateComment(comment.id, { content }),
        onSuccess: () => {
            queryClient.invalidateQueries(['comments', postId])
            setEditing(false)
        }
    })

    return (
        <div className="pt-5 first:pt-0">
            <div className="flex items-start gap-3">
                <Avatar name={comment.authorNickname} deleted={comment.isDeleted} />

                <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 mb-1.5">
                        <span className={`text-sm font-semibold ${comment.isDeleted ? 'text-slate-400' : 'text-slate-800'}`}>
                            {comment.isDeleted ? '삭제된 사용자' : comment.authorNickname}
                        </span>
                        <span className="text-xs text-slate-400">
                            {dayjs(comment.createdAt).fromNow()}
                        </span>
                    </div>

                    {editing ? (
                        <CommentForm
                            onSubmit={(content) => editMutation.mutate(content)}
                            onCancel={() => setEditing(false)}
                            placeholder="댓글을 수정하세요..."
                        />
                    ) : (
                        <p className={`text-sm leading-relaxed ${comment.isDeleted ? 'text-slate-400 italic' : 'text-slate-700'}`}>
                            {comment.content}
                        </p>
                    )}

                    {!comment.isDeleted && !editing && (
                        <div className="flex items-center gap-3 mt-2">
                            {isLoggedIn && (
                                <button
                                    onClick={() => likeMutation.mutate()}
                                    className={`flex items-center gap-1 text-xs transition
                                        ${liked ? 'text-red-400' : 'text-slate-400 hover:text-red-400'}`}
                                >
                                    <Heart size={12} className={liked ? 'fill-red-400' : ''} />
                                    {likeCount > 0 && <span>{likeCount}</span>}
                                </button>
                            )}
                            {isLoggedIn && !comment.parentId && (
                                <button
                                    onClick={() => setShowReply(!showReply)}
                                    className="flex items-center gap-1 text-xs text-slate-400 hover:text-indigo-500 transition"
                                >
                                    <MessageSquare size={12} />
                                    답글
                                </button>
                            )}
                            {isLoggedIn && (
                                <>
                                    <button
                                        onClick={() => setEditing(true)}
                                        className="text-xs text-slate-400 hover:text-slate-600 transition"
                                    >
                                        <Edit size={12} />
                                    </button>
                                    <button
                                        onClick={() => onDelete(comment.id)}
                                        className="text-xs text-slate-400 hover:text-red-400 transition"
                                    >
                                        <Trash2 size={12} />
                                    </button>
                                </>
                            )}
                        </div>
                    )}

                    {showReply && (
                        <div className="mt-3">
                            <CommentForm
                                onSubmit={(content) => {
                                    onReply(content, comment.id)
                                    setShowReply(false)
                                }}
                                onCancel={() => setShowReply(false)}
                                placeholder="답글을 작성하세요..."
                            />
                        </div>
                    )}
                </div>
            </div>

            {/* 대댓글 */}
            {comment.children?.length > 0 && (
                <div className="ml-11 mt-4 pl-4 border-l-2 border-slate-100 space-y-4">
                    {comment.children.map(child => (
                        <CommentItem
                            key={child.id}
                            comment={{ ...child, parentId: comment.id }}
                            postId={postId}
                            onDelete={onDelete}
                            onReply={onReply}
                        />
                    ))}
                </div>
            )}
        </div>
    )
}
