import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import { createSnippet } from '../api/snippet'
import { Code2, ChevronLeft, AlertCircle } from 'lucide-react'

const LANGUAGES = [
    'Java', 'Python', 'JavaScript', 'TypeScript', 'Kotlin',
    'Go', 'Rust', 'C', 'C++', 'SQL', 'Bash', 'HTML', 'CSS', 'YAML', 'JSON',
]

export default function SnippetCreatePage() {
    const navigate = useNavigate()
    const [form, setForm] = useState({ title: '', description: '', code: '', language: 'JavaScript' })
    const [error, setError] = useState(null)

    const mutation = useMutation({
        mutationFn: () => createSnippet(form),
        onSuccess: (res) => {
            navigate(`/snippets/${res.data}`)
        },
        onError: (err) => {
            setError(err?.response?.data?.message ?? '스니펷 등록에 실패했습니다.')
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

    return (
        <div className="max-w-3xl mx-auto">
            {/* 헤더 */}
            <div className="flex items-center gap-3 mb-6">
                <button
                    onClick={() => navigate('/snippets')}
                    className="p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-lg transition"
                >
                    <ChevronLeft size={20} />
                </button>
                <div className="w-9 h-9 rounded-xl bg-violet-100 flex items-center justify-center">
                    <Code2 size={18} className="text-violet-600" />
                </div>
                <div>
                    <h1 className="text-xl font-bold text-slate-900">스니펷 등록</h1>
                    <p className="text-xs text-slate-400 mt-0.5">유용한 코드 조각을 공유해보세요</p>
                </div>
            </div>

            <form onSubmit={handleSubmit}>
                <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
                    <div className="p-8 space-y-6">
                        {/* 언어 선택 */}
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

                        {/* 제목 */}
                        <div>
                            <label className="block text-sm font-semibold text-slate-700 mb-2">제목</label>
                            <input
                                name="title"
                                value={form.title}
                                onChange={handleChange}
                                placeholder="스니펷 제목을 입력하세요"
                                maxLength={200}
                                className="w-full px-3.5 py-2.5 bg-white border border-slate-200 rounded-xl text-sm
                                           focus:outline-none focus:ring-2 focus:ring-violet-400 placeholder-slate-400"
                            />
                        </div>

                        {/* 설명 */}
                        <div>
                            <label className="block text-sm font-semibold text-slate-700 mb-2">
                                설명 <span className="font-normal text-slate-400">(선택)</span>
                            </label>
                            <textarea
                                name="description"
                                value={form.description}
                                onChange={handleChange}
                                placeholder="이 코드 스니펷에 대해 설명해주세요"
                                rows={3}
                                className="w-full px-3.5 py-2.5 bg-white border border-slate-200 rounded-xl text-sm
                                           focus:outline-none focus:ring-2 focus:ring-violet-400 placeholder-slate-400 resize-none"
                            />
                        </div>

                        {/* 코드 */}
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
                                    placeholder="// 코드를 입력하세요"
                                    rows={16}
                                    spellCheck={false}
                                    className="w-full px-6 py-5 bg-transparent text-slate-200 text-sm font-mono leading-relaxed
                                               focus:outline-none resize-none placeholder-slate-600"
                                />
                            </div>
                        </div>

                        {/* 에러 */}
                        {error && (
                            <div className="flex items-center gap-2 px-4 py-3 bg-red-50 border border-red-200 rounded-xl text-sm text-red-600">
                                <AlertCircle size={15} />
                                {error}
                            </div>
                        )}
                    </div>

                    {/* 하단 액션 */}
                    <div className="px-8 py-5 bg-slate-50 border-t border-slate-100 flex justify-end gap-3">
                        <button
                            type="button"
                            onClick={() => navigate('/snippets')}
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
                            {mutation.isPending ? '등록 중...' : '스니펷 등록'}
                        </button>
                    </div>
                </div>
            </form>
        </div>
    )
}
