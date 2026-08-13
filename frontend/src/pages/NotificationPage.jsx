import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { getNotifications, readNotification } from '../api/notification'
import { Bell, BellOff } from 'lucide-react'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/ko'

dayjs.extend(relativeTime)
dayjs.locale('ko')

const typeLabel = {
    LIKE_POST: '게시글 좋아요',
    LIKE_COMMENT: '댓글 좋아요',
    COMMENT: '새 댓글',
    COMMENT_REPLY: '답글',
    POST_PUBLISHED: '새 글 발행',
}

const typeStyle = {
    LIKE_POST:      'bg-red-100 text-red-600',
    LIKE_COMMENT:   'bg-pink-100 text-pink-600',
    COMMENT:        'bg-blue-100 text-blue-600',
    COMMENT_REPLY:  'bg-indigo-100 text-indigo-600',
    POST_PUBLISHED: 'bg-green-100 text-green-700',
}

function getPath(notification) {
    if (['LIKE_POST', 'COMMENT', 'COMMENT_REPLY', 'POST_PUBLISHED'].includes(notification.type)) {
        return `/posts/${notification.targetId}`
    }
    return null
}

export default function NotificationPage() {
    const navigate = useNavigate()
    const queryClient = useQueryClient()

    const { data: notifications = [], isLoading } = useQuery({
        queryKey: ['notifications'],
        queryFn: () => getNotifications().then(r => r.data),
    })

    const readMutation = useMutation({
        mutationFn: (id) => readNotification(id),
        onSuccess: () => {
            queryClient.invalidateQueries(['notifications'])
            queryClient.invalidateQueries(['unreadCount'])
        }
    })

    const handleClick = (n) => {
        if (!n.isRead) readMutation.mutate(n.id)
        const path = getPath(n)
        if (path) navigate(path)
    }

    if (isLoading) return (
        <div className="bg-white rounded-2xl border border-slate-200 p-8 animate-pulse">
            {[1,2,3].map(i => (
                <div key={i} className="flex gap-3 mb-4">
                    <div className="h-5 w-16 bg-slate-200 rounded-full" />
                    <div className="flex-1 h-5 bg-slate-100 rounded" />
                </div>
            ))}
        </div>
    )

    return (
        <div className="max-w-2xl mx-auto">
            <div className="flex items-center gap-3 mb-6">
                <div className="w-9 h-9 rounded-xl bg-indigo-100 flex items-center justify-center">
                    <Bell size={18} className="text-indigo-600" />
                </div>
                <h1 className="text-xl font-bold text-slate-900">알림</h1>
                {notifications.filter(n => !n.isRead).length > 0 && (
                    <span className="px-2.5 py-1 bg-red-500 text-white text-xs font-bold rounded-full">
                        {notifications.filter(n => !n.isRead).length}
                    </span>
                )}
            </div>

            {notifications.length === 0 ? (
                <div className="bg-white rounded-2xl border border-slate-200 text-center py-24">
                    <BellOff size={36} className="text-slate-300 mx-auto mb-3" />
                    <p className="text-slate-500 font-medium">알림이 없어요</p>
                    <p className="text-slate-400 text-sm mt-1">새로운 활동이 있으면 알려드릴게요</p>
                </div>
            ) : (
                <div className="bg-white rounded-2xl border border-slate-200 shadow-sm divide-y divide-slate-100 overflow-hidden">
                    {notifications.map(n => (
                        <div
                            key={n.id}
                            onClick={() => handleClick(n)}
                            className={`flex items-start gap-4 px-6 py-4 cursor-pointer transition
                                ${n.isRead
                                    ? 'hover:bg-slate-50'
                                    : 'bg-indigo-50/60 hover:bg-indigo-50'
                                }`}
                        >
                            <span className={`text-xs font-semibold px-2.5 py-1 rounded-full whitespace-nowrap flex-shrink-0
                                           ${typeStyle[n.type] ?? 'bg-slate-100 text-slate-500'}`}>
                                {typeLabel[n.type] ?? n.type}
                            </span>

                            <div className="flex-1 min-w-0">
                                <p className="text-sm text-slate-700 leading-relaxed">{n.content}</p>
                                <p className="text-xs text-slate-400 mt-1">{dayjs(n.createdAt).fromNow()}</p>
                            </div>

                            {!n.isRead && (
                                <div className="w-2 h-2 rounded-full bg-indigo-500 mt-1.5 flex-shrink-0" />
                            )}
                        </div>
                    ))}
                </div>
            )}
        </div>
    )
}
