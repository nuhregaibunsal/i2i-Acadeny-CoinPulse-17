import { useEffect, useRef, useState } from 'react'
import type { FormEvent } from 'react'
import ReactMarkdown from 'react-markdown'
import { ApiError } from '../api/client'
import { askAi } from '../api/endpoints'

type Role = 'user' | 'assistant' | 'error'

interface Message {
  role: Role
  content: string
}

export function ChatPanel() {
  const [messages, setMessages] = useState<Message[]>([])
  const [input, setInput] = useState('')
  const [busy, setBusy] = useState(false)
  const logRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    logRef.current?.scrollTo({ top: logRef.current.scrollHeight })
  }, [messages, busy])

  async function submit(event: FormEvent) {
    event.preventDefault()
    const question = input.trim()
    if (!question || busy) {
      return
    }
    setMessages((prev) => [...prev, { role: 'user', content: question }])
    setInput('')
    setBusy(true)
    try {
      const response = await askAi(question)
      setMessages((prev) => [...prev, { role: 'assistant', content: response.answer }])
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'The assistant is unavailable.'
      setMessages((prev) => [...prev, { role: 'error', content: message }])
    } finally {
      setBusy(false)
    }
  }

  return (
    <section className="panel chat">
      <div className="panel-head">
        <h2>Market Assistant</h2>
      </div>

      <div className="chat-log" ref={logRef}>
        {messages.length === 0 && !busy && (
          <p className="muted">Ask about your portfolio, recent trades, or market trends.</p>
        )}
        {messages.map((message, index) => (
          <div key={index} className={`bubble bubble-${message.role}`}>
            {message.role === 'assistant' ? (
              <ReactMarkdown>{message.content}</ReactMarkdown>
            ) : (
              message.content
            )}
          </div>
        ))}
        {busy && (
          <div className="bubble bubble-assistant loading">
            <span className="spinner" /> Thinking…
          </div>
        )}
      </div>

      <form className="chat-form" onSubmit={submit}>
        <input
          value={input}
          onChange={(event) => setInput(event.target.value)}
          placeholder="Ask the assistant…"
          disabled={busy}
        />
        <button type="submit" disabled={busy || !input.trim()}>
          Send
        </button>
      </form>
    </section>
  )
}
