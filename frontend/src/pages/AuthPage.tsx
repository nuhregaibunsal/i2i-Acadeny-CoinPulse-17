import { useState } from 'react'
import type { FormEvent } from 'react'
import { ApiError } from '../api/client'
import { login, registerUser } from '../api/endpoints'
import { CoinBackdrop } from '../components/CoinBackdrop'
import { useI18n } from '../i18n/I18nProvider'

type Mode = 'login' | 'register'

interface AuthPageProps {
  onAuthenticated: (token: string, username: string) => void
  onGuest: () => void
}

export function AuthPage({ onAuthenticated, onGuest }: AuthPageProps) {
  const { t } = useI18n()
  const [mode, setMode] = useState<Mode>('login')
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [notice, setNotice] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  function switchMode(next: Mode) {
    setMode(next)
    setError(null)
    setNotice(null)
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setNotice(null)
    setBusy(true)
    try {
      if (mode === 'register') {
        const created = await registerUser(username, password)
        setNotice(t('auth.accountCreated', { balance: created.startingBalance.toFixed(2) }))
      }
      const session = await login(username, password)
      onAuthenticated(session.token, session.username)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : t('auth.genericError'))
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="auth-wrap">
      <form className="auth-card glow-card" onSubmit={handleSubmit}>
        <CoinBackdrop />
        <h2>{mode === 'login' ? t('auth.signIn') : t('auth.createAccount')}</h2>

        <label htmlFor="username">{t('auth.username')}</label>
        <input
          id="username"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          autoComplete="username"
          required
        />

        <label htmlFor="password">{t('auth.password')}</label>
        <input
          id="password"
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
          required
        />

        {error && <p className="alert alert-error">{error}</p>}
        {notice && <p className="alert alert-notice">{notice}</p>}

        <button type="submit" disabled={busy}>
          {busy ? <span className="spinner" /> : null}
          {busy ? t('auth.pleaseWait') : mode === 'login' ? t('auth.signIn') : t('auth.createAccount')}
        </button>

        <p className="auth-switch">
          {mode === 'login' ? (
            <>
              {t('auth.noAccount')}{' '}
              <button type="button" className="link" onClick={() => switchMode('register')}>
                {t('auth.createOne')}
              </button>
            </>
          ) : (
            <>
              {t('auth.alreadyRegistered')}{' '}
              <button type="button" className="link" onClick={() => switchMode('login')}>
                {t('auth.signIn')}
              </button>
            </>
          )}
        </p>

        <div className="auth-divider" />

        <button type="button" className="secondary guest-btn" onClick={onGuest}>
          {t('auth.continueAsGuest')}
        </button>
      </form>
    </div>
  )
}
