import ReactMarkdown from 'react-markdown'
import remarkGfm from 'remark-gfm'
import rehypeHighlight from 'rehype-highlight'
import 'highlight.js/styles/github.css'

const components = {
    h1: ({ node, ...props }) => <h1 className="text-2xl font-bold mt-8 mb-4 text-gray-900" {...props} />,
    h2: ({ node, ...props }) => <h2 className="text-xl font-bold mt-6 mb-3 text-gray-900" {...props} />,
    h3: ({ node, ...props }) => <h3 className="text-lg font-semibold mt-5 mb-2 text-gray-900" {...props} />,
    p: ({ node, ...props }) => <p className="mb-4 leading-7 text-gray-800" {...props} />,
    code: ({ node, inline, ...props }) =>
        inline
            ? <code className="bg-gray-100 text-pink-600 px-1.5 py-0.5 rounded text-sm font-mono" {...props} />
            : <code className="font-mono text-sm" {...props} />,
    pre: ({ node, ...props }) => <pre className="rounded-lg overflow-x-auto mb-4 text-sm" {...props} />,
    ul: ({ node, ...props }) => <ul className="list-disc pl-6 mb-4 space-y-1" {...props} />,
    ol: ({ node, ...props }) => <ol className="list-decimal pl-6 mb-4 space-y-1" {...props} />,
    li: ({ node, ...props }) => <li className="leading-7 text-gray-800" {...props} />,
    blockquote: ({ node, ...props }) => (
        <blockquote className="border-l-4 border-indigo-300 pl-4 italic text-gray-600 mb-4 bg-indigo-50 py-2 rounded-r" {...props} />
    ),
    a: ({ node, ...props }) => <a className="text-indigo-600 hover:underline" target="_blank" rel="noopener noreferrer" {...props} />,
    hr: ({ node, ...props }) => <hr className="border-gray-200 my-6" {...props} />,
    table: ({ node, ...props }) => (
        <div className="overflow-x-auto mb-4">
            <table className="min-w-full border-collapse border border-gray-200 text-sm" {...props} />
        </div>
    ),
    th: ({ node, ...props }) => <th className="border border-gray-200 px-3 py-2 bg-gray-50 font-semibold text-left" {...props} />,
    td: ({ node, ...props }) => <td className="border border-gray-200 px-3 py-2" {...props} />,
    img: ({ node, ...props }) => <img className="max-w-full rounded-lg my-4" {...props} />,
}

export default function MarkdownRenderer({ content }) {
    return (
        <div className="text-gray-800">
            <ReactMarkdown
                remarkPlugins={[remarkGfm]}
                rehypePlugins={[rehypeHighlight]}
                components={components}
            >
                {content}
            </ReactMarkdown>
        </div>
    )
}
