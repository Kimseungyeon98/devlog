import { Link, useNavigate } from 'react-router-dom'
import { Bell, PenSquare, LogOut, BarChart2, Code2 } from 'lucide-react'
import useAuthStore from '../../store/authStore'
import { logout } from '../../api/auth'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { getUnreadCount } from '../../api/notification'
import { getMe } from '../../api/user'
import { useEffect, useRef } from 'react'
import { fetchEventSource } from '@microsoft/fetch-event-source'

function Avatar({ name, size = 'sm' }) {
    const initials = name?.slice(0, 1).toUpperCase() ?? '?'
    const sz = size === 'sm' ? 'w-8 h-8 text-sm' : 'w-10 h-10 text-base'
    return (
        <div className={`${sz} rounded-full bg-gradient-to-br from-indigo-500 to-violet-600
                         flex items-center justify-center text-white font-semibold flex-shrink-0`}>
            {initials}
        </div>
    )
}

export default function Navbar() {
    const { isLoggedIn, logout: logoutStore } = useAuthStore()
    const navigate = useNavigate()
    const queryClient = useQueryClient()
    const ctrlRef = useRef(null)

    const { data: unreadCount } = useQuery({
        queryKey: ['unreadCount'],
        queryFn: () => getUnreadCount().then(r => r.data),
        enabled: isLoggedIn,
    })

    const { data: me } = useQuery({
        queryKey: ['me'],
        queryFn: () => getMe().then(r => r.data),
        enabled: isLoggedIn,
    })

    useEffect(() => {
        if (!isLoggedIn) return
        const token = localStorage.getItem('accessToken')
        const ctrl = new AbortController()
        ctrlRef.current = ctrl
        fetchEventSource('/api/notifications/subscribe', {
            headers: { Authorization: `Bearer ${token}` },
            signal: ctrl.signal,
            onmessage(event) {
                if (event.event === 'notification') {
                    queryClient.invalidateQueries({ queryKey: ['unreadCount'] })
                }
            },
            onerror() { ctrl.abort() },
        })
        return () => ctrl.abort()
    }, [isLoggedIn, queryClient])

    const handleLogout = async () => {
        ctrlRef.current?.abort()
        try { await logout() } finally {
            logoutStore()
            navigate('/login')
        }
    }

    return (
        <nav className="sticky top-0 z-50 bg-white/90 backdrop-blur-md border-b border-slate-200/80 shadow-sm">
            <div className="max-w-5xl mx-auto px-4 sm:px-6 h-14 flex items-center justify-between">
                {/* 로고 + 네비 */}
                <div className="flex items-center gap-4">
                    <Link to="/" className="flex items-center gap-1.5 group">
                        <span className="text-lg font-extrabold bg-gradient-to-r from-indigo-600 to-violet-600
                                         bg-clip-text text-transparent tracking-tight">
                            DevLog
                        </span>
                    </Link>
                    <Link
                        to="/snippets"
                        className="flex items-center gap-1.5 px-3 py-1.5 text-sm font-medium text-slate-500
                                   hover:text-violet-600 hover:bg-violet-50 rounded-lg transition"
                    >
                        <Code2 size={15} />
                        스니펷
                    </Link>
                </div>

                <div className="flex items-center gap-1">
                    {isLoggedIn ? (
                        <>
                            {/* 글쓰기 */}
                            <Link
                                to="/write"
                                className="flex items-center gap-1.5 px-4 py-2 text-sm font-medium
                                           bg-gradient-to-r from-indigo-600 to-violet-600 text-white
                                           rounded-lg hover:opacity-90 transition shadow-sm"
                            >
                                <PenSquare size={14} strokeWidth={2.5} />
                                글쓰기
                            </Link>

                            {/* 대시보드 */}
                            <Link to="/dashboard"
                                  className="p-2 text-slate-500 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg transition"
                                  title="내 통계">
                                <BarChart2 size={19} />
                            </Link>

                            {/* 알림 */}
                            <Link to="/notifications"
                                  className="relative p-2 text-slate-500 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg transition"
                                  title="알림">
                                <Bell size={19} />
                                {unreadCount > 0 && (
                                    <span className="absolute top-1 right-1 min-w-4 h-4 px-0.5
                                                     bg-red-500 text-white text-[10px] font-bold
                                                     rounded-full flex items-center justify-center">
                                        {unreadCount > 9 ? '9+' : unreadCount}
                                    </span>
                                )}
                            </Link>

                            {/* 아바타 */}
                            <Link to={`/users/${me?.username}`} className="p-1">
                                <Avatar name={me?.nickname} />
                            </Link>

                            {/* 로그아웃 */}
                            <button
                                onClick={handleLogout}
                                className="p-2 text-slate-400 hover:text-red-500 hover:bg-red-50 rounded-lg transition"
                                title="로그아웃"
                            >
                                <LogOut size={17} />
                            </button>
                        </>
                    ) : (
                        <>
                            <Link to="/login"
                                  className="px-4 py-2 text-sm text-slate-600 hover:text-indigo-600 hover:bg-slate-100 rounded-lg transition font-medium">
                                로그인
                            </Link>
                            <Link to="/signup"
                                  className="px-4 py-2 text-sm font-medium bg-gradient-to-r from-indigo-600 to-violet-600
                                             text-white rounded-lg hover:opacity-90 transition shadow-sm">
                                회원가입
                            </Link>
                        </>
                    )}
                </div>
            </div>
        </nav>
    )
}
