import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { getSnippets } from '../api/snippet'
import { Code2, Search, Eye, Plus, X } from 'lucide-react'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/ko'
import useAuthStore from '../store/authStore'

dayjs.extend(relativeTime)
dayjs.locale('ko')

const LANGUAGES = [
    'Java', 'Python', 'JavaScript', 'TypeScript', 'Kotlin',
    'Go', 'Rust', 'C', 'C++', 'SQL', 'Bash', 'HTML', 'CSS', 'YAML', 'JSON',
]

const LANG_COLOR = {
    Java: 'bg-orange-100 text-orange-700',
    Python: 'bg-blue-100 text-blue-700',
    JavaScript: 'bg-yellow-100 text-yellow-700',
    TypeScript: 'bg-blue-100 text-blue-800',
    Kotlin: 'bg-violet-100 text-violet-700',
    Go: 'bg-cyan-100 text-cyan-700',
    Rust: 'bg-orange-100 text-orange-800',
    SQL: 'bg-green-100 text-green-700',
    Bash: 'bg-gray-100 text-gray-700',
    default: 'bg-slate-100 text-slate-600',
}

function LangBadge({ lang }) {
    return (
        <span className={`text-xs font-semibold px-2.5 py-1 rounded-full ${LANG_COLOR[lang] ?? LANG_COLOR.default}`}>
            {lang}
        </span>
    )
}

export default function SnippetsPage() {
    const navigate = useNavigate()
    const { isLoggedIn } = useAuthStore()
    const [keyword, setKeyword] = useState('')
    const [input, setInput] = useState('')
    const [language, setLanguage] = useState('')
    const [page, setPage] = useState(0)

    const { data, isLoading } = useQuery({
        queryKey: ['snippets', keyword, language, page],
        queryFn: () => getSnippets({ keyword, language, page, size: 12 }).then(r => r.data),
    })

    const handleSearch = (e) => {
        e.preventDefault()
        setKeyword(input.trim())
        setPage(0)
    }

    return (
        <div>
            <div className="flex items-center justify-between mb-6">
                <div className="flex items-center gap-3">
                    <div className="w-9 h-9 rounded-xl bg-violet-100 flex items-center justify-center">
                        <Code2 size={18} className="text-violet-600" />
                    </div>
                    <div>
                        <h1 className="text-xl font-bold text-slate-900">코드 스니펫</h1>
                        <p className="text-xs text-slate-400 mt-0.5">유용한 코드 조각을 공유하세요</p>
                    </div>
                </div>
                {isLoggedIn && (
                    <button
                        onClick={() => navigate('/snippets/new')}
                        className="flex items-center gap-2 px-4 py-2 text-sm font-semibold
                                   bg-gradient-to-r from-violet-600 to-indigo-600 text-white
                                   rounded-xl hover:opacity-90 transition shadow-sm"
                    >
                        <Plus size={15} />
                        스니펫 등록
                    </button>
                )}
            </div>

            <form onSubmit={handleSearch} className="flex gap-2 mb-5">
                <div className="relative flex-1">
                    <Search size={15} className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400" />
                    <input
                        value={input}
                        onChange={e => setInput(e.target.value)}
                        placeholder="제목 또는 설명 검색..."
                        className="w-full pl-10 pr-4 py-2.5 bg-white border border-slate-200 rounded-xl text-sm
                                   focus:outline-none focus:ring-2 focus:ring-violet-400 placeholder-slate-400 shadow-sm"
                    />
                </div>
                <button type="submit"
                        className="px-5 py-2.5 bg-violet-600 text-white text-sm font-medium rounded-xl hover:bg-violet-700 transition shadow-sm">
                    검색
                </button>
            </form>

            <div className="flex flex-wrap gap-2 mb-6">
                <button
                    onClick={() => { setLanguage(''); setPage(0) }}
                    className={`px-3 py-1.5 text-xs font-semibold rounded-full transition
                        ${!language ? 'bg-violet-600 text-white shadow-sm' : 'bg-white border border-slate-200 text-slate-600 hover:border-violet-300'}`}
                >
                    전체
                </button>
                {LANGUAGES.map(lang => (
                    <button
                        key={lang}
                        onClick={() => { setLanguage(lang); setPage(0) }}
                        className={`px-3 py-1.5 text-xs font-semibold rounded-full transition
                            ${language === lang
                                ? 'bg-violet-600 text-white shadow-sm'
                                : 'bg-white border border-slate-200 text-slate-600 hover:border-violet-300'
                            }`}
                    >
                        {lang}
                    </button>
                ))}
                {language && (
                    <button onClick={() => { setLanguage(''); setPage(0) }}
                            className="flex items-center gap-1 px-3 py-1.5 text-xs font-medium text-slate-500 hover:text-red-500 transition">
                        <X size={12} /> 초기화
                    </button>
                )}
            </div>

            {isLoading ? (
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                    {[1,2,3,4,5,6].map(i => (
                        <div key={i} className="bg-white rounded-2xl border border-slate-200 p-5 animate-pulse">
                            <div className="h-4 bg-slate-200 rounded w-16 mb-3" />
                            <div className="h-5 bg-slate-200 rounded w-3/4 mb-2" />
                            <div className="h-4 bg-slate-100 rounded w-full mb-1" />
                            <div className="h-20 bg-slate-100 rounded mt-3" />
                        </div>
                    ))}
                </div>
            ) : data?.content?.length === 0 ? (
                <div className="bg-white rounded-2xl border border-slate-200 text-center py-24">
                    <Code2 size={36} className="text-slate-300 mx-auto mb-3" />
                    <p className="text-slate-500 font-medium">스니펫이 없어요.</p>
                </div>
            ) : (
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                    {data?.content?.map(snippet => (
                        <div
                            key={snippet.id}
                            onClick={() => navigate(`/snippets/${snippet.id}`)}
                            className="group bg-white rounded-2xl border border-slate-200 p-5
                                       hover:border-violet-300 hover:shadow-md transition-all cursor-pointer"
                        >
                            <div className="flex items-center justify-between mb-3">
                                <LangBadge lang={snippet.language} />
                                <span className="flex items-center gap-1 text-xs text-slate-400">
                                    <Eye size={11} />
                                    {snippet.viewCount}
                                </span>
                            </div>

                            <h2 className="font-bold text-slate-900 group-hover:text-violet-700 transition mb-1.5 line-clamp-1">
                                {snippet.title}
                            </h2>

                            {snippet.description && (
                                <p className="text-xs text-slate-500 line-clamp-2 mb-3 leading-relaxed">
                                    {snippet.description}
                                </p>
                            )}

                            <div className="bg-slate-900 rounded-xl p-3 overflow-hidden">
                                <pre className="text-xs text-slate-300 font-mono line-clamp-4 leading-relaxed overflow-hidden">
                                    {snippet.description ? '' : ''}
                                </pre>
                            </div>

                            <div className="flex items-center justify-between mt-3">
                                <span className="text-xs text-slate-400">{snippet.authorNickname}</span>
                                <span className="text-xs text-slate-400">{dayjs(snippet.createdAt).fromNow()}</span>
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {data && data.totalPages > 1 && (
                <div className="flex justify-center items-center gap-2 mt-10">
                    <button onClick={() => setPage(p => p - 1)} disabled={page === 0}
                            className="px-4 py-2 text-sm font-medium border border-slate-200 bg-white rounded-xl hover:bg-slate-50 disabled:opacity-40 transition">
                        이전
                    </button>
                    <span className="text-sm text-slate-500 px-2">{page + 1} / {data.totalPages}</span>
                    <button onClick={() => setPage(p => p + 1)} disabled={page >= data.totalPages - 1}
                            className="px-4 py-2 text-sm font-medium border border-slate-200 bg-white rounded-xl hover:bg-slate-50 disabled:opacity-40 transition">
                        다음
                    </button>
                </div>
            )}
        </div>
    )
}
