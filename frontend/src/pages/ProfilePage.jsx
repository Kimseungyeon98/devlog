import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { getProfile, getMe, updateProfile, follow, unfollow } from '../api/user'
import { getMyPosts } from '../api/post'
import { getUserSeries } from '../api/series'
import useAuthStore from '../store/authStore'
import { Edit2, Check, X, BookOpen, FileText, Users, UserCheck } from 'lucide-react'
import dayjs from 'dayjs'

function Avatar({ name, size = 'lg' }) {
    const sz = size === 'lg' ? 'w-20 h-20 text-3xl' : 'w-10 h-10 text-base'
    return (
        <div className={`${sz} rounded-full bg-gradient-to-br from-indigo-500 to-violet-600
                         flex items-center justify-center text-white font-bold ring-4 ring-white flex-shrink-0`}>
            {name?.slice(0, 1).toUpperCase() ?? '?'}
        </div>
    )
}

function StatItem({ icon: Icon, label, value }) {
    return (
        <div className="flex flex-col items-center gap-1">
            <div className="flex items-center gap-1.5 text-slate-700">
                <Icon size={15} className="text-indigo-500" />
                <span className="text-xl font-bold">{value ?? 0}</span>
            </div>
            <span className="text-xs text-slate-400">{label}</span>
        </div>
    )
}

export default function ProfilePage() {
    const { username } = useParams()
    const navigate = useNavigate()
    const queryClient = useQueryClient()
    const { isLoggedIn } = useAuthStore()
    const [editing, setEditing] = useState(false)
    const [form, setForm] = useState({ nickname: '', bio: '' })
    const [isFollowing, setIsFollowing] = useState(false)
    const [activeTab, setActiveTab] = useState('posts')

    const { data: profile, isLoading } = useQuery({
        queryKey: ['profile', username],
        queryFn: () => getProfile(username).then(r => r.data),
    })

    useEffect(() => {
        if (profile?.isFollowing != null) setIsFollowing(profile.isFollowing)
    }, [profile?.isFollowing])

    const { data: me } = useQuery({
        queryKey: ['me'],
        queryFn: () => getMe().then(r => r.data),
        enabled: isLoggedIn,
    })

    const { data: postsData } = useQuery({
        queryKey: ['myPosts', username],
        queryFn: () => getMyPosts({ page: 0, size: 50 }).then(r => r.data),
        enabled: !!profile && me?.username === username && activeTab === 'posts',
    })

    const { data: seriesList = [] } = useQuery({
        queryKey: ['userSeries', username],
        queryFn: () => getUserSeries(username).then(r => r.data),
        enabled: activeTab === 'series',
    })

    const followMutation = useMutation({
        mutationFn: () => isFollowing ? unfollow(profile.id) : follow(profile.id),
        onSuccess: () => {
            setIsFollowing(prev => !prev)
            queryClient.invalidateQueries(['profile', username])
        }
    })

    const updateMutation = useMutation({
        mutationFn: () => updateProfile(form),
        onSuccess: () => {
            queryClient.invalidateQueries(['profile', username])
            setEditing(false)
        }
    })

    const handleEditStart = () => {
        setForm({ nickname: profile.nickname, bio: profile.bio || '' })
        setEditing(true)
    }

    const isMyProfile = me?.username === username

    if (isLoading) return (
        <div className="animate-pulse">
            <div className="h-28 bg-gradient-to-r from-indigo-100 to-violet-100 rounded-2xl mb-4" />
            <div className="bg-white rounded-2xl border border-slate-200 p-8">
                <div className="w-20 h-20 rounded-full bg-slate-200 mb-4" />
                <div className="h-6 bg-slate-200 rounded w-32 mb-2" />
                <div className="h-4 bg-slate-100 rounded w-24" />
            </div>
        </div>
    )

    return (
        <div>
            <div className="h-28 rounded-2xl bg-gradient-to-r from-indigo-500 via-violet-500 to-purple-500 mb-0 relative overflow-hidden">
                <div className="absolute inset-0 opacity-20"
                     style={{ backgroundImage: 'radial-gradient(circle at 20% 50%, white 1px, transparent 1px), radial-gradient(circle at 80% 20%, white 1px, transparent 1px)', backgroundSize: '30px 30px' }} />
            </div>

            <div className="bg-white rounded-b-2xl border border-t-0 border-slate-200 shadow-sm px-8 pb-8 mb-5">
                <div className="flex items-end justify-between -mt-10 mb-5 flex-wrap gap-4">
                    <Avatar name={profile?.nickname} />

                    <div className="flex gap-2 mb-1">
                        {isMyProfile ? (
                            editing ? (
                                <>
                                    <button
                                        onClick={() => updateMutation.mutate()}
                                        className="flex items-center gap-1.5 px-4 py-2 text-sm font-medium
                                                   bg-indigo-600 text-white rounded-xl hover:bg-indigo-700 transition"
                                    >
                                        <Check size={14} /> 저장
                                    </button>
                                    <button
                                        onClick={() => setEditing(false)}
                                        className="flex items-center gap-1.5 px-4 py-2 text-sm font-medium
                                                   border border-slate-200 text-slate-600 rounded-xl hover:bg-slate-50 transition"
                                    >
                                        <X size={14} /> 취소
                                    </button>
                                </>
                            ) : (
                                <button
                                    onClick={handleEditStart}
                                    className="flex items-center gap-1.5 px-4 py-2 text-sm font-medium
                                               border border-slate-200 text-slate-600 rounded-xl hover:bg-slate-50 transition"
                                >
                                    <Edit2 size={14} /> 프로필 수정
                                </button>
                            )
                        ) : isLoggedIn && (
                            <button
                                onClick={() => followMutation.mutate()}
                                className={`px-5 py-2 text-sm font-medium rounded-xl transition
                                    ${isFollowing
                                        ? 'border border-slate-200 text-slate-600 hover:bg-slate-50'
                                        : 'bg-gradient-to-r from-indigo-600 to-violet-600 text-white hover:opacity-90 shadow-sm'
                                    }`}
                            >
                                {isFollowing ? '팔로잉' : '팔로우'}
                            </button>
                        )}
                    </div>
                </div>

                {editing ? (
                    <input
                        value={form.nickname}
                        onChange={e => setForm({ ...form, nickname: e.target.value })}
                        className="text-2xl font-bold border-b-2 border-indigo-400 outline-none pb-1 mb-1 w-full max-w-xs"
                    />
                ) : (
                    <h1 className="text-2xl font-bold text-slate-900">{profile?.nickname}</h1>
                )}
                <p className="text-sm text-slate-400 mt-0.5">@{username}</p>

                <div className="mt-4">
                    {editing ? (
                        <textarea
                            value={form.bio}
                            onChange={e => setForm({ ...form, bio: e.target.value })}
                            placeholder="소개를 입력하세요..."
                            rows={2}
                            className="w-full text-sm border border-slate-200 rounded-xl px-4 py-3
                                       focus:outline-none focus:ring-2 focus:ring-indigo-400 resize-none"
                        />
                    ) : (
                        <p className="text-sm text-slate-500 leading-relaxed">
                            {profile?.bio || '소개가 없어요.'}
                        </p>
                    )}
                </div>

                <div className="flex gap-8 mt-6 pt-6 border-t border-slate-100">
                    <StatItem icon={Users} label="팔로워" value={profile?.followerCount} />
                    <StatItem icon={UserCheck} label="팔로잉" value={profile?.followingCount} />
                    <StatItem icon={FileText} label="게시글" value={postsData?.totalElements} />
                </div>
            </div>

            <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
                <div className="flex border-b border-slate-100">
                    {['posts', 'series'].map(tab => (
                        <button
                            key={tab}
                            onClick={() => setActiveTab(tab)}
                            className={`flex-1 py-3.5 text-sm font-semibold transition
                                ${activeTab === tab
                                    ? 'text-indigo-600 border-b-2 border-indigo-500 bg-indigo-50/50'
                                    : 'text-slate-500 hover:text-slate-700 hover:bg-slate-50'
                                }`}
                        >
                            {tab === 'posts' ? '게시글' : '시리즈'}
                        </button>
                    ))}
                </div>

                <div className="p-6">
                    {activeTab === 'posts' && (
                        isMyProfile ? (
                            postsData?.content?.length === 0 ? (
                                <p className="text-sm text-slate-400 py-6 text-center">작성한 게시글이 없어요.</p>
                            ) : (
                                <div className="space-y-1">
                                    {postsData?.content?.map(post => (
                                        <div
                                            key={post.id}
                                            onClick={() => navigate(`/posts/${post.id}`)}
                                            className="flex items-center justify-between px-4 py-3.5 hover:bg-slate-50 rounded-xl cursor-pointer group transition"
                                        >
                                            <div className="min-w-0">
                                                <p className="text-sm font-medium text-slate-800 group-hover:text-indigo-700 transition truncate">{post.title}</p>
                                                <p className="text-xs text-slate-400 mt-0.5">{dayjs(post.createdAt).format('YYYY.MM.DD')}</p>
                                            </div>
                                            <span className={`text-xs px-2.5 py-1 rounded-full font-medium flex-shrink-0 ml-3
                                                ${post.status === 'PUBLISHED'
                                                    ? 'bg-green-100 text-green-700'
                                                    : 'bg-slate-100 text-slate-500'}`}>
                                                {post.status === 'PUBLISHED' ? '발행됨' : '임시저장'}
                                            </span>
                                        </div>
                                    ))}
                                </div>
                            )
                        ) : (
                            <p className="text-sm text-slate-400 py-6 text-center">게시글 목록은 본인만 볼 수 있어요.</p>
                        )
                    )}

                    {activeTab === 'series' && (
                        seriesList.length === 0 ? (
                            <p className="text-sm text-slate-400 py-6 text-center">작성한 시리즈가 없어요.</p>
                        ) : (
                            <div className="space-y-2">
                                {seriesList.map(s => (
                                    <div
                                        key={s.id}
                                        onClick={() => navigate(`/series/${s.id}`)}
                                        className="flex items-center gap-3 px-4 py-3.5 hover:bg-slate-50 rounded-xl cursor-pointer group transition"
                                    >
                                        <div className="p-2 bg-indigo-50 rounded-lg group-hover:bg-indigo-100 transition flex-shrink-0">
                                            <BookOpen size={15} className="text-indigo-600" />
                                        </div>
                                        <div className="flex-1 min-w-0">
                                            <p className="text-sm font-medium text-slate-800 group-hover:text-indigo-700 transition truncate">{s.title}</p>
                                            <p className="text-xs text-slate-400">{s.postCount}편</p>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )
                    )}
                </div>
            </div>
        </div>
    )
}
