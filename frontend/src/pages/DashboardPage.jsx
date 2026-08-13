import { useQuery } from '@tanstack/react-query'
import { getMyStats } from '../api/stat'
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts'
import { FileText, Eye, TrendingUp } from 'lucide-react'

function StatCard({ icon: Icon, label, value, gradient }) {
    return (
        <div className="bg-white rounded-2xl border border-slate-200 shadow-sm p-6 flex items-center gap-4">
            <div className={`w-12 h-12 rounded-xl flex items-center justify-center ${gradient} text-white shadow-sm`}>
                <Icon size={20} />
            </div>
            <div>
                <p className="text-sm text-slate-500 font-medium">{label}</p>
                <p className="text-2xl font-extrabold text-slate-900 mt-0.5">{value.toLocaleString()}</p>
            </div>
        </div>
    )
}

function CustomTooltip({ active, payload }) {
    if (!active || !payload?.length) return null
    const d = payload[0].payload
    return (
        <div className="bg-white border border-slate-200 rounded-xl shadow-lg px-4 py-3 text-sm">
            <p className="font-semibold text-slate-800 mb-1 max-w-48 truncate">{d.fullTitle}</p>
            <p className="text-indigo-600 font-medium">{d.viewCount.toLocaleString()} 조회</p>
        </div>
    )
}

export default function DashboardPage() {
    const { data: stats, isLoading } = useQuery({
        queryKey: ['myStats'],
        queryFn: () => getMyStats().then(r => r.data),
    })

    if (isLoading) return (
        <div className="animate-pulse space-y-4">
            <div className="grid grid-cols-2 gap-4">
                {[1,2].map(i => <div key={i} className="h-24 bg-white rounded-2xl border border-slate-200" />)}
            </div>
            <div className="h-72 bg-white rounded-2xl border border-slate-200" />
        </div>
    )

    const chartData = (stats?.posts ?? []).slice(0, 10).map(p => ({
        title: p.title.length > 12 ? p.title.slice(0, 12) + '…' : p.title,
        fullTitle: p.title,
        viewCount: p.viewCount,
    }))

    return (
        <div className="space-y-5">
            <div className="flex items-center gap-3">
                <div className="w-9 h-9 rounded-xl bg-indigo-100 flex items-center justify-center">
                    <TrendingUp size={18} className="text-indigo-600" />
                </div>
                <h1 className="text-xl font-bold text-slate-900">내 통계</h1>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <StatCard icon={FileText} label="발행된 글" value={stats?.totalPosts ?? 0}
                          gradient="bg-gradient-to-br from-indigo-500 to-violet-600" />
                <StatCard icon={Eye} label="총 조회수" value={stats?.totalViews ?? 0}
                          gradient="bg-gradient-to-br from-emerald-500 to-teal-600" />
            </div>

            {chartData.length > 0 ? (
                <div className="bg-white rounded-2xl border border-slate-200 shadow-sm p-6">
                    <h2 className="text-sm font-bold text-slate-700 mb-5">
                        조회수 Top {chartData.length}
                    </h2>
                    <ResponsiveContainer width="100%" height={280}>
                        <BarChart data={chartData} margin={{ top: 4, right: 8, left: 0, bottom: 64 }}>
                            <CartesianGrid strokeDasharray="3 3" stroke="#f1f5f9" />
                            <XAxis dataKey="title" tick={{ fontSize: 11, fill: '#94a3b8' }}
                                   angle={-35} textAnchor="end" interval={0} />
                            <YAxis tick={{ fontSize: 11, fill: '#94a3b8' }} allowDecimals={false} />
                            <Tooltip content={<CustomTooltip />} cursor={{ fill: '#eef2ff' }} />
                            <Bar dataKey="viewCount" fill="url(#barGrad)" radius={[6, 6, 0, 0]}>
                                <defs>
                                    <linearGradient id="barGrad" x1="0" y1="0" x2="0" y2="1">
                                        <stop offset="0%" stopColor="#6366f1" />
                                        <stop offset="100%" stopColor="#8b5cf6" />
                                    </linearGradient>
                                </defs>
                            </Bar>
                        </BarChart>
                    </ResponsiveContainer>
                </div>
            ) : (
                <div className="bg-white rounded-2xl border border-slate-200 p-16 text-center">
                    <div className="w-14 h-14 rounded-2xl bg-slate-100 flex items-center justify-center mx-auto mb-4">
                        <FileText size={24} className="text-slate-400" />
                    </div>
                    <p className="text-slate-500 font-medium">발행된 글이 없습니다</p>
                    <p className="text-slate-400 text-sm mt-1">첫 글을 발행하면 통계가 표시됩니다</p>
                </div>
            )}

            {(stats?.posts ?? []).length > 0 && (
                <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">
                    <div className="px-6 py-4 border-b border-slate-100">
                        <h2 className="text-sm font-bold text-slate-700">전체 조회수 순위</h2>
                    </div>
                    <div className="divide-y divide-slate-100">
                        {stats.posts.map((p, i) => (
                            <div key={p.postId} className="flex items-center gap-4 px-6 py-3.5 hover:bg-slate-50 transition">
                                <span className={`text-xs font-bold w-6 text-right
                                    ${i === 0 ? 'text-yellow-500' : i === 1 ? 'text-slate-400' : i === 2 ? 'text-orange-400' : 'text-slate-300'}`}>
                                    {i + 1}
                                </span>
                                <p className="flex-1 text-sm text-slate-800 truncate">{p.title}</p>
                                <span className="flex items-center gap-1.5 text-xs font-medium text-slate-500">
                                    <Eye size={12} />
                                    {p.viewCount.toLocaleString()}
                                </span>
                            </div>
                        ))}
                    </div>
                </div>
            )}
        </div>
    )
}
