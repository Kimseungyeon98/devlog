import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { login } from '../api/auth'
import useAuthStore from '../store/authStore'
import { Eye, EyeOff } from 'lucide-react'

export default function LoginPage() {
    const navigate = useNavigate()
    const { login: loginStore } = useAuthStore()
    const [form, setForm] = useState({ email: '', password: '' })
    const [error, setError] = useState('')
    const [loading, setLoading] = useState(false)
    const [showPw, setShowPw] = useState(false)

    const handleSubmit = async (e) => {
        e.preventDefault()
        setError('')
        setLoading(true)
        try {
            const { data } = await login(form)
            loginStore(data.accessToken, data.refreshToken)
            navigate('/')
        } catch {
            setError('이메일 또는 비밀번호가 올바르지 않습니다.')
        } finally {
            setLoading(false)
        }
    }

    return (
        <div className="min-h-screen bg-gradient-to-br from-slate-50 via-indigo-50 to-violet-50 flex items-center justify-center px-4">
            <div className="w-full max-w-sm">

                {/* 로고 */}
                <div className="text-center mb-8">
                    <div className="inline-flex items-center justify-center w-12 h-12 rounded-2xl
                                    bg-gradient-to-br from-indigo-600 to-violet-600 shadow-lg mb-4">
                        <span className="text-white font-black text-lg">D</span>
                    </div>
                    <h1 className="text-2xl font-extrabold bg-gradient-to-r from-indigo-600 to-violet-600
                                   bg-clip-text text-transparent">DevLog</h1>
                    <p className="text-sm text-slate-500 mt-1">개발자들의 지식 공유 플랫폼</p>
                </div>

                <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-8">
                    <h2 className="text-lg font-bold text-slate-900 mb-6">로그인</h2>

                    <form onSubmit={handleSubmit} className="space-y-4">
                        <div>
                            <label className="block text-sm font-semibold text-slate-700 mb-1.5">이메일</label>
                            <input
                                type="email"
                                value={form.email}
                                onChange={e => setForm({ ...form, email: e.target.value })}
                                className="w-full px-4 py-2.5 border border-slate-200 rounded-xl text-sm
                                           focus:outline-none focus:ring-2 focus:ring-indigo-400 focus:border-transparent
                                           placeholder-slate-400 transition"
                                placeholder="email@example.com"
                                required
                            />
                        </div>

                        <div>
                            <label className="block text-sm font-semibold text-slate-700 mb-1.5">비밀번호</label>
                            <div className="relative">
                                <input
                                    type={showPw ? 'text' : 'password'}
                                    value={form.password}
                                    onChange={e => setForm({ ...form, password: e.target.value })}
                                    className="w-full px-4 py-2.5 pr-10 border border-slate-200 rounded-xl text-sm
                                               focus:outline-none focus:ring-2 focus:ring-indigo-400 focus:border-transparent
                                               placeholder-slate-400 transition"
                                    placeholder="비밀번호 입력"
                                    required
                                />
                                <button
                                    type="button"
                                    onClick={() => setShowPw(v => !v)}
                                    className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600"
                                >
                                    {showPw ? <EyeOff size={16} /> : <Eye size={16} />}
                                </button>
                            </div>
                        </div>

                        {error && (
                            <div className="px-4 py-3 bg-red-50 border border-red-100 rounded-xl">
                                <p className="text-sm text-red-600">{error}</p>
                            </div>
                        )}

                        <button
                            type="submit"
                            disabled={loading}
                            className="w-full py-2.5 bg-gradient-to-r from-indigo-600 to-violet-600 text-white text-sm
                                       font-semibold rounded-xl hover:opacity-90 disabled:opacity-60 transition shadow-sm"
                        >
                            {loading ? '로그인 중...' : '로그인'}
                        </button>
                    </form>

                    <div className="relative my-5">
                        <div className="absolute inset-0 flex items-center">
                            <div className="w-full border-t border-slate-100" />
                        </div>
                        <div className="relative flex justify-center">
                            <span className="bg-white px-3 text-xs text-slate-400">또는</span>
                        </div>
                    </div>

                    <a
                        href="/oauth2/authorization/github"
                        className="flex items-center justify-center gap-2.5 w-full py-2.5
                                   border border-slate-200 rounded-xl text-sm font-medium text-slate-700
                                   hover:bg-slate-50 transition"
                    >
                        <svg viewBox="0 0 24 24" className="w-4 h-4" fill="currentColor">
                            <path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0 0 24 12c0-6.63-5.37-12-12-12z"/>
                        </svg>
                        GitHub으로 로그인
                    </a>

                    <p className="text-center text-sm text-slate-500 mt-6">
                        계정이 없으신가요?{' '}
                        <Link to="/signup" className="text-indigo-600 font-semibold hover:underline">
                            회원가입
                        </Link>
                    </p>
                </div>
            </div>
        </div>
    )
}
