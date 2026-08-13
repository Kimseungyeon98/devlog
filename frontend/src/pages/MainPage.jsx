import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { getPosts } from '../api/post'
import { Search, Eye, Heart, Clock, Tag, X } from 'lucide-react'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/ko'

dayjs.extend(relativeTime)
dayjs.locale('ko')

function Avatar({ name, size = 7 }) {
    return (
        <div className={`w-${size} h-${size} rounded-full bg-gradient-to-br from-indigo-500 to-violet-600
                         flex items-center justify-center text-white text-xs font-semibold flex-shrink-0`}>
            {name?.slice(0, 1).toUpperCase() ?? '?'}
        </div>
    )
}

function readingTime(content) {
    const words = (content || '').split(/\s+/).length
    return Math.max(1, Math.round(words / 200))
}

function PostCard({ post, onTagClick }) {
    const navigate = useNavigate()
    const excerpt = (post.content || '').replace(/[#*`>\-\[\]!]/g, '').slice(0, 120).trim()

    return (
        <article
            onClick={() => navigate(`/posts/${post.id}`)}
            className="group bg-white rounded-2xl border border-slate-200 p-6
                       hover:border-indigo-300 hover:shadow-md transition-all duration-200 cursor-pointer"
        >
            <div className="flex items-center gap-2.5 mb-3">
                <Avatar name={post.authorNickname} />
                <div>
                    <span className="text-sm font-medium text-slate-700">{post.authorNickname}</span>
                    <div className="flex items-center gap-1.5 text-xs text-slate-400">
                        <span>{dayjs(post.createdAt).fromNow()}</span>
                        <span>·</span>
                        <Clock size={11} />
                        <span>{readingTime(post.content)}분 읽기</span>
                    </div>
                </div>
            </div>

            <h2 className="text-lg font-bold text-slate-900 mb-2 group-hover:text-indigo-700 transition-colors line-clamp-2">
                {post.title}
            </h2>

            {excerpt && (
                <p className="text-sm text-slate-500 leading-relaxed line-clamp-2 mb-4">
                    {excerpt}
                </p>
            )}

            <div className="flex items-center justify-between">
                <div className="flex gap-1.5 flex-wrap">
                    {post.tags?.slice(0, 4).map(t => (
                        <span
                            key={t}
                            onClick={e => { e.stopPropagation(); onTagClick(t) }}
                            className="px-2.5 py-1 bg-slate-100 text-slate-600 text-xs rounded-full
                                       hover:bg-indigo-100 hover:text-indigo-700 transition cursor-pointer"
                        >
                            #{t}
                        </span>
                    ))}
                </div>
                <div className="flex items-center gap-3 text-xs text-slate-400">
                    <span className="flex items-center gap-1">
                        <Eye size={12} />
                        {post.viewCount?.toLocaleString() ?? 0}
                    </span>
                </div>
            </div>
        </article>
    )
}

export default function MainPage() {
    const navigate = useNavigate()
    const [keyword, setKeyword] = useState('')
    const [tag, setTag] = useState('')
    const [input, setInput] = useState('')
    const [page, setPage] = useState(0)

    const { data, isLoading } = useQuery({
        queryKey: ['posts', keyword, tag, page],
        queryFn: () => getPosts({ keyword, tag, page, size: 10 }).then(r => r.data),
    })

    const handleSearch = (e) => {
        e.preventDefault()
        setKeyword(input.trim())
        setPage(0)
    }

    const handleTagClick = (t) => {
        setTag(t)
        setPage(0)
    }

    return (
        <div>
            <form onSubmit={handleSearch} className="mb-8">
                <div className="flex gap-2">
                    <div className="relative flex-1">
                        <Search size={16} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" />
                        <input
                            value={input}
                            onChange={e => setInput(e.target.value)}
                            placeholder="제목이나 내용으로 검색..."
                            className="w-full pl-10 pr-4 py-2.5 bg-white border border-slate-200 rounded-xl text-sm
                                       focus:outline-none focus:ring-2 focus:ring-indigo-400 focus:border-transparent
                                       placeholder-slate-400 shadow-sm"
                        />
                    </div>
                    <button
                        type="submit"
                        className="px-5 py-2.5 bg-indigo-600 text-white text-sm font-medium
                                   rounded-xl hover:bg-indigo-700 transition shadow-sm"
                    >
                        검색
                    </button>
                </div>

                {tag && (
                    <div className="flex items-center gap-2 mt-3">
                        <span className="text-xs text-slate-500">태그 필터:</span>
                        <span className="flex items-center gap-1.5 px-3 py-1 bg-indigo-100 text-indigo-700
                                         text-xs rounded-full font-medium">
                            <Tag size={11} />
                            {tag}
                            <button
                                type="button"
                                onClick={() => { setTag(''); setPage(0) }}
                                className="hover:text-indigo-900"
                            >
                                <X size={12} />
                            </button>
                        </span>
                    </div>
                )}
            </form>

            {isLoading ? (
                <div className="space-y-4">
                    {[1,2,3].map(i => (
                        <div key={i} className="bg-white rounded-2xl border border-slate-200 p-6 animate-pulse">
                            <div className="flex gap-2 mb-3">
                                <div className="w-7 h-7 rounded-full bg-slate-200" />
                                <div className="flex-1 space-y-1">
                                    <div className="h-3 bg-slate-200 rounded w-24" />
                                    <div className="h-2.5 bg-slate-100 rounded w-16" />
                                </div>
                            </div>
                            <div className="h-5 bg-slate-200 rounded mb-2 w-3/4" />
                            <div className="h-4 bg-slate-100 rounded w-full mb-1" />
                            <div className="h-4 bg-slate-100 rounded w-2/3" />
                        </div>
                    ))}
                </div>
            ) : data?.content?.length === 0 ? (
                <div className="text-center py-24">
                    <div className="w-16 h-16 bg-slate-100 rounded-full flex items-center justify-center mx-auto mb-4">
                        <Search size={24} className="text-slate-400" />
                    </div>
                    <p className="text-slate-500 font-medium">게시글이 없어요.</p>
                    <p className="text-slate-400 text-sm mt-1">다른 키워드로 검색해보세요.</p>
                </div>
            ) : (
                <div className="space-y-4">
                    {data?.content?.map(post => (
                        <PostCard key={post.id} post={post} onTagClick={handleTagClick} />
                    ))}
                </div>
            )}

            {data && data.totalPages > 1 && (
                <div className="flex justify-center items-center gap-2 mt-10">
                    <button
                        onClick={() => setPage(p => p - 1)}
                        disabled={page === 0}
                        className="px-4 py-2 text-sm font-medium border border-slate-200 bg-white rounded-xl
                                   hover:bg-slate-50 disabled:opacity-40 disabled:cursor-not-allowed transition"
                    >
                        이전
                    </button>
                    <div className="flex gap-1">
                        {Array.from({ length: Math.min(data.totalPages, 5) }, (_, i) => {
                            const p = page < 3 ? i : page - 2 + i
                            if (p >= data.totalPages) return null
                            return (
                                <button
                                    key={p}
                                    onClick={() => setPage(p)}
                                    className={`w-9 h-9 text-sm font-medium rounded-xl transition
                                        ${p === page
                                            ? 'bg-indigo-600 text-white shadow-sm'
                                            : 'bg-white border border-slate-200 text-slate-600 hover:bg-slate-50'
                                        }`}
                                >
                                    {p + 1}
                                </button>
                            )
                        })}
                    </div>
                    <button
                        onClick={() => setPage(p => p + 1)}
                        disabled={page >= data.totalPages - 1}
                        className="px-4 py-2 text-sm font-medium border border-slate-200 bg-white rounded-xl
                                   hover:bg-slate-50 disabled:opacity-40 disabled:cursor-not-allowed transition"
                    >
                        다음
                    </button>
                </div>
            )}
        </div>
    )
}
