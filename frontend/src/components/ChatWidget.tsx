import { useEffect, useRef, useState } from 'react'
import type { FormEvent } from 'react'
import ReactMarkdown from 'react-markdown'
import { ApiError } from '../api/client'
import { askAi } from '../api/endpoints'
import { useI18n } from '../i18n/I18nProvider'
import type { MessageKey } from '../i18n/messages'

type Role = 'user' | 'assistant' | 'error'

interface Message {
  role: Role
  content: string
}

const PRESETS: MessageKey[] = ['chat.preset.portfolio', 'chat.preset.trades', 'chat.preset.trend']

export function ChatWidget() {
  const { t } = useI18n()
  const [open, setOpen] = useState(false)
  const [messages, setMessages] = useState<Message[]>([])
  const [input, setInput] = useState('')
  const [busy, setBusy] = useState(false)
  const logRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    logRef.current?.scrollTo({ top: logRef.current.scrollHeight })
  }, [messages, busy, open])

  async function send(question: string) {
    const trimmed = question.trim()
    if (!trimmed || busy) {
      return
    }
    setMessages((prev) => [...prev, { role: 'user', content: trimmed }])
    setInput('')
    setBusy(true)
    try {
      const response = await askAi(trimmed)
      setMessages((prev) => [...prev, { role: 'assistant', content: response.answer }])
    } catch (err) {
      const message = err instanceof ApiError ? err.message : t('chat.unavailable')
      setMessages((prev) => [...prev, { role: 'error', content: message }])
    } finally {
      setBusy(false)
    }
  }

  function onSubmit(event: FormEvent) {
    event.preventDefault()
    send(input)
  }

  return (
    <>
      {open && (
        <div className="chat-widget">
          <div className="chat-widget-head">
            <span className="bot-badge" aria-hidden="true">🤖</span>
            <h2>{t('chat.title')}</h2>
            <button type="button" className="close-x" onClick={() => setOpen(false)} aria-label="Close">
              ✕
            </button>
          </div>

          <div className="chat-log" ref={logRef}>
            {messages.length === 0 && !busy && <p className="muted">{t('chat.empty')}</p>}
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
                <span className="spinner" /> {t('chat.thinking')}
              </div>
            )}
          </div>

          <div className="preset-marquee">
            <div className="preset-track">
              {[...PRESETS, ...PRESETS].map((key, index) => (
                <button
                  key={index}
                  type="button"
                  className="chip"
                  onClick={() => send(t(key))}
                  disabled={busy}
                >
                  {t(key)}
                </button>
              ))}
            </div>
          </div>

          <form className="chat-form" onSubmit={onSubmit}>
            <input
              value={input}
              onChange={(event) => setInput(event.target.value)}
              placeholder={t('chat.placeholder')}
              disabled={busy}
            />
            <button type="submit" disabled={busy || !input.trim()}>
              {t('chat.send')}
            </button>
          </form>
        </div>
      )}

      <button
        type="button"
        className="chat-fab"
        onClick={() => setOpen((value) => !value)}
        aria-label={t('chat.title')}
      >
        {open ? '✕' : '🤖'}
      </button>
    </>
  )
}
