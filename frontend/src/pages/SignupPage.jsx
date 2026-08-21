import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { signup } from '../api/auth'
import { Eye, EyeOff, CheckCircle2 } from 'lucide-react'

export default function SignupPage() {
    const navigate = useNavigate()
    const [form, setForm] = useState({ username: '', email: '', password: '', nickname: '' })
    const [error, setError] = useState('')
    const [loading, setLoading] = useState(false)
    const [showPw, setShowPw] = useState(false)
    const [done, setDone] = useState(false)

    const handleSubmit = async (e) => {
        e.preventDefault()
        setError('')
        setLoading(true)
        try {
            await signup(form)
            setDone(true)
            setTimeout(() => navigate('/login'), 1800)
        } catch (err) {
            setError(err.response?.data?.message || '회원가입에 실패했습니다.')
        } finally {
            setLoading(false)
        }
    }

    const fields = [
        { key: 'username', label: '아이디', type: 'text', placeholder: '영문, 숫자, 언더스코어' },
        { key: 'nickname', label: '닉네임', type: 'text', placeholder: '표시될 이름' },
        { key: 'email', label: '이메일', type: 'email', placeholder: 'email@example.com' },
    ]

    return (
        <div className="min-h-screen bg-gradient-to-br from-slate-50 via-indigo-50 to-violet-50 flex items-center justify-center px-4 py-10">
            <div className="w-full max-w-sm">
                <div className="text-center mb-8">
                    <div className="inline-flex items-center justify-center w-12 h-12 rounded-2xl
                                    bg-gradient-to-br from-indigo-600 to-violet-600 shadow-lg mb-4">
                        <span className="text-white font-black text-lg">D</span>
                    </div>
                    <h1 className="text-2xl font-extrabold bg-gradient-to-r from-indigo-600 to-violet-600
                                   bg-clip-text text-transparent">DevLog</h1>
                    <p className="text-sm text-slate-500 mt-1">지금 시작해보세요</p>
                </div>

                <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-8">
                    {done ? (
                        <div className="text-center py-6">
                            <CheckCircle2 size={48} className="text-green-500 mx-auto mb-4" />
                            <p className="font-bold text-slate-900 text-lg">가입 완료!</p>
                            <p className="text-sm text-slate-500 mt-1">로그인 페이지로 이동합니다...</p>
                        </div>
                    ) : (
                        <>
                            <h2 className="text-lg font-bold text-slate-900 mb-6">회원가입</h2>

                            <form onSubmit={handleSubmit} className="space-y-4">
                                {fields.map(({ key, label, type, placeholder }) => (
                                    <div key={key}>
                                        <label className="block text-sm font-semibold text-slate-700 mb-1.5">{label}</label>
                                        <input
                                            type={type}
                                            value={form[key]}
                                            onChange={e => setForm({ ...form, [key]: e.target.value })}
                                            className="w-full px-4 py-2.5 border border-slate-200 rounded-xl text-sm
                                                       focus:outline-none focus:ring-2 focus:ring-indigo-400 focus:border-transparent
                                                       placeholder-slate-400 transition"
                                            placeholder={placeholder}
                                            required
                                        />
                                    </div>
                                ))}

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
                                            placeholder="8~20자"
                                            required
                                        />
                                        <button type="button" onClick={() => setShowPw(v => !v)}
                                                className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600">
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
                                    className="w-full py-2.5 bg-gradient-to-r from-indigo-600 to-violet-600 text-white
                                               text-sm font-semibold rounded-xl hover:opacity-90 disabled:opacity-60 transition shadow-sm"
                                >
                                    {loading ? '처리 중...' : '회원가입'}
                                </button>
                            </form>

                            <p className="text-center text-sm text-slate-500 mt-6">
                                이미 계정이 있으신가요?{' '}
                                <Link to="/login" className="text-indigo-600 font-semibold hover:underline">로그인</Link>
                            </p>
                        </>
                    )}
                </div>
            </div>
        </div>
    )
}
