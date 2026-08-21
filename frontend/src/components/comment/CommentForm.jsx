import { useState } from 'react'
import { Send } from 'lucide-react'

export default function CommentForm({ onSubmit, placeholder = '댓글을 작성하세요...', onCancel }) {
    const [content, setContent] = useState('')

    const handleSubmit = (e) => {
        e.preventDefault()
        if (!content.trim()) return
        onSubmit(content)
        setContent('')
    }

    return (
        <form onSubmit={handleSubmit}>
            <textarea
                value={content}
                onChange={e => setContent(e.target.value)}
                placeholder={placeholder}
                rows={3}
                className="w-full px-4 py-3 border border-slate-200 rounded-xl text-sm
                           focus:outline-none focus:ring-2 focus:ring-indigo-400 focus:border-transparent
                           resize-none placeholder-slate-400 transition"
            />
            <div className="flex justify-end gap-2 mt-2">
                {onCancel && (
                    <button
                        type="button"
                        onClick={onCancel}
                        className="px-4 py-2 text-sm text-slate-500 hover:bg-slate-100 rounded-lg transition"
                    >
                        취소
                    </button>
                )}
                <button
                    type="submit"
                    className="flex items-center gap-1.5 px-4 py-2 text-sm font-medium
                               bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition"
                >
                    <Send size={13} />
                    등록
                </button>
            </div>
        </form>
    )
}
