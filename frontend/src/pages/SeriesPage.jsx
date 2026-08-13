import { useParams, useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { getSeriesDetail } from '../api/series'
import { BookOpen, ChevronRight } from 'lucide-react'
import dayjs from 'dayjs'

export default function SeriesPage() {
    const { seriesId } = useParams()
    const navigate = useNavigate()

    const { data: series, isLoading } = useQuery({
        queryKey: ['series', seriesId],
        queryFn: () => getSeriesDetail(seriesId).then(r => r.data),
    })

    if (isLoading) return (
        <div className="animate-pulse">
            <div className="h-40 bg-white rounded-2xl border border-slate-200 mb-4" />
            <div className="bg-white rounded-2xl border border-slate-200">
                {[1,2,3].map(i => <div key={i} className="h-16 border-b border-slate-100" />)}
            </div>
        </div>
    )
    if (!series) return null

    return (
        <div>
            {/* 시리즈 헤더 */}
            <div className="bg-white border border-slate-200 rounded-2xl shadow-sm p-8 mb-5">
                <div className="flex items-start gap-5">
                    <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-indigo-500 to-violet-600
                                    flex items-center justify-center shadow-sm flex-shrink-0">
                        <BookOpen size={24} className="text-white" />
                    </div>
                    <div className="flex-1">
                        <h1 className="text-2xl font-extrabold text-slate-900 tracking-tight mb-1">
                            {series.title}
                        </h1>
                        <p className="text-sm text-slate-500">
                            <button
                                onClick={() => navigate(`/users/${series.authorUsername}`)}
                                className="font-semibold text-slate-700 hover:text-indigo-600 transition"
                            >
                                {series.authorNickname}
                            </button>
                            <span className="mx-2">·</span>
                            총 {series.posts?.length}편
                        </p>
                        {series.description && (
                            <p className="text-sm text-slate-600 mt-3 leading-relaxed">{series.description}</p>
                        )}
                    </div>
                </div>
            </div>

            {/* 글 목록 */}
            <div className="bg-white border border-slate-200 rounded-2xl shadow-sm overflow-hidden">
                {series.posts?.map((post, index) => (
                    <div
                        key={post.postId}
                        onClick={() => navigate(`/posts/${post.postId}`)}
                        className="flex items-center gap-4 px-6 py-4 border-b border-slate-100 last:border-none
                                   hover:bg-indigo-50/60 cursor-pointer transition group"
                    >
                        <span className="w-8 h-8 rounded-xl bg-indigo-100 text-indigo-600 text-sm font-bold
                                         flex items-center justify-center flex-shrink-0 group-hover:bg-indigo-200 transition">
                            {index + 1}
                        </span>
                        <div className="flex-1 min-w-0">
                            <p className="font-semibold text-slate-800 truncate group-hover:text-indigo-700 transition">
                                {post.title}
                            </p>
                            <p className="text-xs text-slate-400 mt-0.5">
                                {dayjs(post.createdAt).format('YYYY.MM.DD')}
                            </p>
                        </div>
                        <ChevronRight size={16} className="text-slate-300 group-hover:text-indigo-500 transition flex-shrink-0" />
                    </div>
                ))}
            </div>
        </div>
    )
}
