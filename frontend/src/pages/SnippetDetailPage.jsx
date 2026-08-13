import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getSnippet, deleteSnippet } from '../api/snippet'
import { Copy, Check, Trash2, Edit, Eye, Clock, Code2 } from 'lucide-react'
import useAuthStore from '../store/authStore'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/ko'

dayjs.extend(relativeTime)
dayjs.locale('ko')

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

function Avatar({ name }) {
    return (
        <div className="w-8 h-8 rounded-full bg-gradient-to-br from-violet-500 to-indigo-600 flex items-center justify-center text-white font-bold text-sm flex-shrink-0">
            {name?.slice(0, 1).toUpperCase() ?? '?'}
        </div>
    )
}

export default function SnippetDetailPage() {
    const { snippetId } = useParams()
    const navigate = useNavigate()
    const queryClient = useQueryClient()
    const { isLoggedIn } = useAuthStore()
    const [copied, setCopied] = useState(false)

    const { data: snippet, isLoading } = useQuery({
        queryKey: ['snippet', snippetId],
        queryFn: () => getSnippet(snippetId).then(r => r.data),
    })

    const deleteMutation = useMutation({
        mutationFn: () => deleteSnippet(snippetId),
        onSuccess: () => {
            queryClient.invalidateQueries(['snippets'])
            navigate('/snippets')
        }
    })

    const handleCopy = () => {
        navigator.clipboard.writeText(snippet.code)
        setCopied(true)
        setTimeout(() => setCopied(false), 2000)
    }

    const handleDelete = () => {
        if (window.confirm('스니펫을 삭제할까요?')) deleteMutation.mutate()
    }

    if (isLoading) return (
        <div className="max-w-4xl mx-auto animate-pulse">
            <div className="bg-white rounded-2xl border border-slate-200 p-8">
                <div className="h-6 bg-slate-200 rounded w-16 mb-4" />
                <div className="h-8 bg-slate-200 rounded w-1/2 mb-6" />
                <div className="h-64 bg-slate-900 rounded-xl" />
            </div>
        </div>
    )

    if (!snippet) return null

    return (
        <div className="max-w-4xl mx-auto">
            <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
                <div className="px-8 pt-8 pb-6">
                    <div className="flex items-center justify-between mb-4 flex-wrap gap-3">
                        <span className={`text-xs font-bold px-3 py-1.5 rounded-full ${LANG_COLOR[snippet.language] ?? LANG_COLOR.default}`}>
                            {snippet.language}
                        </span>
                        <div className="flex gap-1.5">
                            {isLoggedIn && (
                                <>
                                    <button
                                        onClick={() => navigate(`/snippets/${snippetId}/edit`)}
                                        className="flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium text-slate-500 hover:text-violet-600 hover:bg-violet-50 rounded-lg transition"
                                    >
                                        <Edit size={13} /> 수정
                                    </button>
                                    <button
                                        onClick={handleDelete}
                                        className="flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium text-slate-400 hover:text-red-500 hover:bg-red-50 rounded-lg transition"
                                    >
                                        <Trash2 size={13} /> 삭제
                                    </button>
                                </>
                            )}
                        </div>
                    </div>

                    <h1 className="text-2xl font-extrabold text-slate-900 tracking-tight mb-4">
                        {snippet.title}
                    </h1>

                    {snippet.description && (
                        <p className="text-sm text-slate-600 leading-relaxed mb-4">
                            {snippet.description}
                        </p>
                    )}

                    <div className="flex items-center gap-3">
                        <Avatar name={snippet.authorNickname} />
                        <div>
                            <button
                                onClick={() => navigate(`/users/${snippet.authorUsername}`)}
                                className="text-sm font-semibold text-slate-800 hover:text-violet-600 transition"
                            >
                                {snippet.authorNickname}
                            </button>
                            <div className="flex items-center gap-2 text-xs text-slate-400 mt-0.5">
                                <Clock size={11} />
                                <span>{dayjs(snippet.createdAt).fromNow()}</span>
                                <span>·</span>
                                <Eye size={11} />
                                <span>{snippet.viewCount}</span>
                            </div>
                        </div>
                    </div>
                </div>

                <div className="mx-8 mb-8 rounded-2xl overflow-hidden border border-slate-700 bg-slate-950 shadow-lg">
                    <div className="flex items-center justify-between px-4 py-2.5 bg-slate-800 border-b border-slate-700">
                        <div className="flex items-center gap-2">
                            <div className="flex gap-1.5">
                                <div className="w-3 h-3 rounded-full bg-red-500" />
                                <div className="w-3 h-3 rounded-full bg-yellow-500" />
                                <div className="w-3 h-3 rounded-full bg-green-500" />
                            </div>
                            <span className="text-xs text-slate-400 font-mono ml-2">{snippet.language.toLowerCase()}</span>
                        </div>
                        <button
                            onClick={handleCopy}
                            className={`flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium rounded-lg transition
                                ${copied
                                    ? 'bg-green-600 text-white'
                                    : 'bg-slate-700 text-slate-300 hover:bg-slate-600'
                                }`}
                        >
                            {copied ? <Check size={13} /> : <Copy size={13} />}
                            {copied ? '복사됨!' : '복사'}
                        </button>
                    </div>

                    <div className="overflow-x-auto">
                        <pre className="px-6 py-5 text-sm font-mono text-slate-200 leading-relaxed">
                            <code>{snippet.code}</code>
                        </pre>
                    </div>
                </div>
            </div>
        </div>
    )
}
