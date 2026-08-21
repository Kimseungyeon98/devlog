import { useState, useEffect, useRef, useCallback } from 'react'
import { X, Eye, Edit3, RotateCcw, Clock, Image } from 'lucide-react'
import MarkdownRenderer from '../MarkdownRenderer'
import dayjs from 'dayjs'
import { uploadImage } from '../../api/image'

export default function PostEditor({
    initialValues = {},
    onSubmit,
    loading,
    error,
    onCancel,
    submitLabel = '발행',
    draftKey,
}) {
    const [form, setForm] = useState({
        title: initialValues.title || '',
        content: initialValues.content || '',
        tags: initialValues.tags || [],
    })
    const [tagInput, setTagInput] = useState('')
    const [preview, setPreview] = useState(false)
    const [savedAt, setSavedAt] = useState(null)
    const [showRestore, setShowRestore] = useState(false)
    const [savedDraft, setSavedDraft] = useState(null)
    const [imageUploading, setImageUploading] = useState(false)
    const initialized = useRef(false)
    const textareaRef = useRef(null)
    const fileInputRef = useRef(null)

    useEffect(() => {
        if (!draftKey) { initialized.current = true; return }
        const raw = localStorage.getItem(draftKey)
        if (!raw) { initialized.current = true; return }
        try {
            const draft = JSON.parse(raw)
            const differs =
                draft.title !== (initialValues.title || '') ||
                draft.content !== (initialValues.content || '')
            if (differs && (draft.title || draft.content)) {
                setSavedDraft(draft)
                setShowRestore(true)
            } else {
                localStorage.removeItem(draftKey)
            }
        } catch {
            localStorage.removeItem(draftKey)
        }
        initialized.current = true
    // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [])

    useEffect(() => {
        if (!draftKey || !initialized.current) return
        const timer = setTimeout(() => {
            localStorage.setItem(draftKey, JSON.stringify(form))
            setSavedAt(dayjs().format('HH:mm:ss'))
        }, 3000)
        return () => clearTimeout(timer)
    }, [form, draftKey])

    const restoreDraft = () => {
        setForm({ title: savedDraft.title || '', content: savedDraft.content || '', tags: savedDraft.tags || [] })
        setShowRestore(false)
    }

    const dismissDraft = () => {
        setShowRestore(false)
        if (draftKey) localStorage.removeItem(draftKey)
    }

    const handleTagKeyDown = (e) => {
        if (e.key === 'Enter' || e.key === ',') {
            e.preventDefault()
            const tag = tagInput.trim().toLowerCase()
            if (tag && !form.tags.includes(tag)) {
                setForm(prev => ({ ...prev, tags: [...prev.tags, tag] }))
            }
            setTagInput('')
        }
    }

    const removeTag = (tag) => setForm(prev => ({ ...prev, tags: prev.tags.filter(t => t !== tag) }))

    const insertImageUrl = useCallback((url) => {
        const ta = textareaRef.current
        if (!ta) return
        const start = ta.selectionStart
        const before = form.content.substring(0, start)
        const after = form.content.substring(ta.selectionEnd)
        const snippet = `![이미지](${url})`
        setForm(prev => ({ ...prev, content: before + snippet + after }))
        setTimeout(() => { ta.selectionStart = ta.selectionEnd = start + snippet.length; ta.focus() }, 0)
    }, [form.content])

    const handleImageFile = async (file) => {
        if (!file || !file.type.startsWith('image/')) return
        setImageUploading(true)
        try {
            const { data } = await uploadImage(file)
            insertImageUrl(data.url)
        } catch { /* silent */ } finally {
            setImageUploading(false)
        }
    }

    const handlePaste = (e) => {
        const item = Array.from(e.clipboardData?.items || []).find(i => i.type.startsWith('image/'))
        if (item) { e.preventDefault(); handleImageFile(item.getAsFile()) }
    }

    return (
        <div className="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden">

            {/* 복원 배너 */}
            {showRestore && (
                <div className="flex items-center justify-between px-6 py-3 bg-amber-50 border-b border-amber-200">
                    <div className="flex items-center gap-2 text-amber-700 text-sm">
                        <RotateCcw size={14} />
                        <span>저장된 임시글이 있습니다. 복원하시겠어요?</span>
                    </div>
                    <div className="flex gap-2">
                        <button onClick={restoreDraft}
                                className="px-3 py-1 bg-amber-500 text-white rounded-lg text-xs hover:bg-amber-600 transition">
                            복원
                        </button>
                        <button onClick={dismissDraft}
                                className="px-3 py-1 text-amber-600 hover:bg-amber-100 rounded-lg text-xs transition">
                            무시
                        </button>
                    </div>
                </div>
            )}

            <div className="p-8">
                {/* 제목 */}
                <input
                    value={form.title}
                    onChange={e => setForm(prev => ({ ...prev, title: e.target.value }))}
                    placeholder="제목을 입력하세요"
                    className="w-full text-3xl font-extrabold border-none outline-none placeholder-slate-300
                               text-slate-900 tracking-tight mb-5"
                />

                <hr className="border-slate-100 mb-4" />

                {/* 태그 */}
                <div className="flex flex-wrap gap-2 mb-5">
                    {form.tags.map(tag => (
                        <span key={tag}
                              className="flex items-center gap-1 px-3 py-1 bg-indigo-50 text-indigo-600 text-sm rounded-full font-medium">
                            #{tag}
                            <button onClick={() => removeTag(tag)} className="hover:text-indigo-800">
                                <X size={12} />
                            </button>
                        </span>
                    ))}
                    <input
                        value={tagInput}
                        onChange={e => setTagInput(e.target.value)}
                        onKeyDown={handleTagKeyDown}
                        placeholder="태그 입력 후 Enter"
                        className="text-sm outline-none placeholder-slate-400 flex-1 min-w-32"
                    />
                </div>

                <hr className="border-slate-100 mb-4" />

                {/* 툴바 */}
                <div className="flex items-center justify-between mb-3">
                    <div className="flex gap-1 border border-slate-200 rounded-lg p-0.5">
                        <button
                            onClick={() => setPreview(false)}
                            className={`flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium rounded-md transition
                                ${!preview ? 'bg-indigo-600 text-white shadow-sm' : 'text-slate-500 hover:text-slate-700'}`}
                        >
                            <Edit3 size={13} /> 작성
                        </button>
                        <button
                            onClick={() => setPreview(true)}
                            className={`flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium rounded-md transition
                                ${preview ? 'bg-indigo-600 text-white shadow-sm' : 'text-slate-500 hover:text-slate-700'}`}
                        >
                            <Eye size={13} /> 미리보기
                        </button>
                    </div>

                    {!preview && (
                        <button
                            type="button"
                            onClick={() => fileInputRef.current?.click()}
                            disabled={imageUploading}
                            className="flex items-center gap-1.5 text-xs text-slate-500 hover:text-indigo-600
                                       px-3 py-1.5 rounded-lg hover:bg-indigo-50 transition border border-slate-200"
                        >
                            <Image size={13} />
                            {imageUploading ? '업로드 중...' : '이미지'}
                        </button>
                    )}
                    <input ref={fileInputRef} type="file" accept="image/*" className="hidden"
                           onChange={e => handleImageFile(e.target.files?.[0])} />
                </div>

                {/* 에디터 / 프리뷰 */}
                {preview ? (
                    <div className="min-h-96 py-2 border border-slate-100 rounded-xl px-4">
                        {form.content
                            ? <MarkdownRenderer content={form.content} />
                            : <p className="text-slate-400 italic text-sm py-4">내용을 입력하면 미리보기가 표시됩니다.</p>
                        }
                    </div>
                ) : (
                    <textarea
                        ref={textareaRef}
                        value={form.content}
                        onChange={e => setForm(prev => ({ ...prev, content: e.target.value }))}
                        onPaste={handlePaste}
                        placeholder="마크다운으로 작성하세요... (이미지 붙여넣기 가능)"
                        rows={22}
                        className="w-full text-sm text-slate-800 border border-slate-100 rounded-xl px-4 py-3
                                   outline-none resize-none placeholder-slate-300 font-mono leading-relaxed
                                   focus:ring-2 focus:ring-indigo-400 focus:border-transparent transition"
                    />
                )}

                {error && (
                    <div className="mt-3 px-4 py-3 bg-red-50 border border-red-100 rounded-xl">
                        <p className="text-sm text-red-600">{error}</p>
                    </div>
                )}

                {/* 하단 버튼 */}
                <div className="flex justify-between items-center mt-5 pt-5 border-t border-slate-100">
                    <div className="flex items-center gap-3">
                        <button onClick={onCancel}
                                className="px-4 py-2 text-sm text-slate-600 hover:bg-slate-100 rounded-xl transition">
                            취소
                        </button>
                        {savedAt && (
                            <span className="flex items-center gap-1.5 text-xs text-slate-400">
                                <Clock size={11} />
                                자동저장 {savedAt}
                            </span>
                        )}
                    </div>
                    <div className="flex gap-2">
                        <button
                            onClick={() => onSubmit(form, false)}
                            disabled={loading}
                            className="px-4 py-2 text-sm font-medium border border-slate-200 rounded-xl
                                       hover:bg-slate-50 disabled:opacity-50 transition"
                        >
                            임시저장
                        </button>
                        <button
                            onClick={() => onSubmit(form, true)}
                            disabled={loading}
                            className="px-5 py-2 text-sm font-semibold bg-gradient-to-r from-indigo-600 to-violet-600
                                       text-white rounded-xl hover:opacity-90 disabled:opacity-50 transition shadow-sm"
                        >
                            {loading ? '저장 중...' : submitLabel}
                        </button>
                    </div>
                </div>
            </div>
        </div>
    )
}
