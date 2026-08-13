import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { login, logout, signup } from '../api/auth'
import useAuthStore from '../store/authStore'

export default function useAuth() {
    const navigate = useNavigate()
    const { login: loginStore, logout: logoutStore } = useAuthStore()
    const [loading, setLoading] = useState(false)
    const [error, setError] = useState('')

    const handleLogin = async (data) => {
        setError('')
        setLoading(true)
        try {
            const { data: tokens } = await login(data)
            loginStore(tokens.accessToken, tokens.refreshToken)
            navigate('/')
        } catch {
            setError('이메일 또는 비밀번호가 올바르지 않습니다.')
        } finally {
            setLoading(false)
        }
    }

    const handleSignup = async (data) => {
        setError('')
        setLoading(true)
        try {
            await signup(data)
            navigate('/login')
        } catch {
            setError('회원가입에 실패했습니다. 이미 사용 중인 이메일일 수 있습니다.')
        } finally {
            setLoading(false)
        }
    }

    const handleLogout = async () => {
        try {
            await logout()
        } finally {
            logoutStore()
            navigate('/login')
        }
    }

    return { handleLogin, handleSignup, handleLogout, loading, error, setError }
}
