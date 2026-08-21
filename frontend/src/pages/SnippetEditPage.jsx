import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getSnippet, updateSnippet } from '../api/snippet'
import { Code2, ChevronLeft, AlertCircle } from 'lucide-react'

const LANGUAGES = [
    'Java', 'Python', 'JavaScript', 'TypeScript', 'Kotlin',
    'Go', 'Rust', 'C', 'C++', 'SQL', 'Bash', 'HTML', 'CSS', 'YAML', 'JSON',
]

export default function SnippetEditPage() {
    const { snippetId } = useParams()
    const navigate = useNavigate()
    const queryClient = useQueryClient()
    const [form, setForm] = useState(null)
    const [error, setError] = useState(null)

    const { data: snippet, isLoading } = useQuery({
        queryKey: ['snippet', snippetId],
        queryFn: () => getSnippet(snippetId).then(r => r.data),
    })

    useEffect(() => {
        if (snippet) {
            setForm({
                title: snippet.title,
                description: snippet.description ?? '',
                code: snippet.code,
                language: snippet.language,
            })
        }
    }, [snippet])

    const mutation = useMutation({
        mutationFn: () => updateSnippet(snippetId, form),
        onSuccess: () => {
            queryClient.invalidateQueries(['snippet', snippetId])
            navigate(`/snippets/${snippetId}`)
        },
        onError: (err) => {
            setError(err?.response?.data?.message ?? '수정에 실패했습니다.')
        },
    })

    const handleChange = (e) => {
        setForm(prev => ({ ...prev, [e.target.name]: e.target.value }))
    }

    const handleSubmit = (e) => {
        e.preventDefault()
        setError(null)
        mutation.mutate()
    }

    if (isLoading || !form) return (
        <div className="max-w-3xl mx-auto animate-pulse">
            <div className="bg-white rounded-2xl border border-slate-200 p-8">
                <div className="h-6 bg-slate-200 rounded w-32 mb-6" />
                <div className="space-y-4">
                    <div className="h-10 bg-slate-200 rounded-xl" />
                    <div className="h-10 bg-slate-200 rounded-xl" />
                    <div className="h-64 bg-slate-900 rounded-xl" />
                </div>
            </div>
        </div>
    )

    return (
        <div className="max-w-3xl mx-auto">
            <div className="flex items-center gap-3 mb-6">
                <button
                    onClick={() => navigate(`/snippets/${snippetId}`)}
                    className="p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-lg transition"
                >
                    <ChevronLeft size={20} />
                </button>
                <div className="w-9 h-9 rounded-xl bg-violet-100 flex items-center justify-center">
                    <Code2 size={18} className="text-violet-600" />
                </div>
                <div>
                    <h1 className="text-xl font-bold text-slate-900">스니펫 수정</h1>
                    <p className="text-xs text-slate-400 mt-0.5">{snippet?.title}</p>
                </div>
            </div>

            <form onSubmit={handleSubmit}>
                <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
                    <div className="p-8 space-y-6">
                        <div>
                            <label className="block text-sm font-semibold text-slate-700 mb-2">언어</label>
                            <select
                                name="language"
                                value={form.language}
                                onChange={handleChange}
                                className="w-full px-3.5 py-2.5 bg-white border border-slate-200 rounded-xl text-sm
                                           focus:outline-none focus:ring-2 focus:ring-violet-400 text-slate-800"
                            >
                                {LANGUAGES.map(lang => (
                                    <option key={lang} value={lang}>{lang}</option>
                                ))}
                            </select>
                        </div>

                        <div>
                            <label className="block text-sm font-semibold text-slate-700 mb-2">제목</label>
                            <input
                                name="title"
                                value={form.title}
                                onChange={handleChange}
                                maxLength={200}
                                className="w-full px-3.5 py-2.5 bg-white border border-slate-200 rounded-xl text-sm
                                           focus:outline-none focus:ring-2 focus:ring-violet-400 placeholder-slate-400"
                            />
                        </div>

                        <div>
                            <label className="block text-sm font-semibold text-slate-700 mb-2">
                                설명 <span className="font-normal text-slate-400">(선택)</span>
                            </label>
                            <textarea
                                name="description"
                                value={form.description}
                                onChange={handleChange}
                                rows={3}
                                className="w-full px-3.5 py-2.5 bg-white border border-slate-200 rounded-xl text-sm
                                           focus:outline-none focus:ring-2 focus:ring-violet-400 placeholder-slate-400 resize-none"
                            />
                        </div>

                        <div>
                            <label className="block text-sm font-semibold text-slate-700 mb-2">코드</label>
                            <div className="rounded-xl overflow-hidden border border-slate-700 bg-slate-950">
                                <div className="flex items-center gap-2 px-4 py-2.5 bg-slate-800 border-b border-slate-700">
                                    <div className="flex gap-1.5">
                                        <div className="w-3 h-3 rounded-full bg-red-500" />
                                        <div className="w-3 h-3 rounded-full bg-yellow-500" />
                                        <div className="w-3 h-3 rounded-full bg-green-500" />
                                    </div>
                                    <span className="text-xs text-slate-400 font-mono ml-2">{form.language.toLowerCase()}</span>
                                </div>
                                <textarea
                                    name="code"
                                    value={form.code}
                                    onChange={handleChange}
                                    rows={16}
                                    spellCheck={false}
                                    className="w-full px-6 py-5 bg-transparent text-slate-200 text-sm font-mono leading-relaxed
                                               focus:outline-none resize-none"
                                />
                            </div>
                        </div>

                        {error && (
                            <div className="flex items-center gap-2 px-4 py-3 bg-red-50 border border-red-200 rounded-xl text-sm text-red-600">
                                <AlertCircle size={15} />
                                {error}
                            </div>
                        )}
                    </div>

                    <div className="px-8 py-5 bg-slate-50 border-t border-slate-100 flex justify-end gap-3">
                        <button
                            type="button"
                            onClick={() => navigate(`/snippets/${snippetId}`)}
                            className="px-5 py-2.5 text-sm font-medium text-slate-600 hover:text-slate-800
                                       hover:bg-slate-100 rounded-xl transition"
                        >
                            취소
                        </button>
                        <button
                            type="submit"
                            disabled={mutation.isPending || !form.title.trim() || !form.code.trim()}
                            className="px-6 py-2.5 text-sm font-semibold
                                       bg-gradient-to-r from-violet-600 to-indigo-600 text-white
                                       rounded-xl hover:opacity-90 transition shadow-sm
                                       disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                            {mutation.isPending ? '저장 중...' : '수정 완료'}
                        </button>
                    </div>
                </div>
            </form>
        </div>
    )
}
