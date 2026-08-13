import { useNavigate } from 'react-router-dom'
import dayjs from 'dayjs'

export default function PostCard({ post, onTagClick }) {
    const navigate = useNavigate()

    return (
        <div
            onClick={() => navigate(`/posts/${post.id}`)}
            className="bg-white border border-gray-200 rounded-xl p-5
                       hover:border-indigo-300 hover:shadow-sm cursor-pointer transition"
        >
            <div className="flex items-start justify-between gap-4">
                <div className="flex-1 min-w-0">
                    <h2 className="font-semibold text-gray-900 truncate">
                        {post.title}
                    </h2>
                    <p className="text-sm text-gray-500 mt-1">
                        {post.authorNickname} · {dayjs(post.createdAt).format('YYYY.MM.DD')}
                    </p>
                </div>
                <span className="text-xs text-gray-400 whitespace-nowrap">
                    조회 {post.viewCount}
                </span>
            </div>

            {post.tags?.length > 0 && (
                <div className="flex gap-1 mt-3 flex-wrap">
                    {post.tags.map(tag => (
                        <span
                            key={tag}
                            onClick={e => {
                                e.stopPropagation()
                                onTagClick?.(tag)
                            }}
                            className="px-2 py-0.5 bg-indigo-50 text-indigo-600
                                       text-xs rounded-full cursor-pointer hover:bg-indigo-100"
                        >
                            #{tag}
                        </span>
                    ))}
                </div>
            )}
        </div>
    )
}
