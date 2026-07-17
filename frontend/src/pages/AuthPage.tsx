import { useState } from 'react'
import type { FormEvent } from 'react'
import { ApiError } from '../api/client'
import { login, registerUser } from '../api/endpoints'

type Mode = 'login' | 'register'

interface AuthPageProps {
  onAuthenticated: (token: string, username: string) => void
}

export function AuthPage({ onAuthenticated }: AuthPageProps) {
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
        setNotice(`Account created with a starting balance of $${created.startingBalance.toFixed(2)}.`)
      }
      const session = await login(username, password)
      onAuthenticated(session.token, session.username)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Something went wrong. Please try again.')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="auth-wrap">
      <form className="auth-card" onSubmit={handleSubmit}>
        <h2>{mode === 'login' ? 'Sign in' : 'Create account'}</h2>

        <label htmlFor="username">Username</label>
        <input
          id="username"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          autoComplete="username"
          required
        />

        <label htmlFor="password">Password</label>
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
          {busy ? 'Please wait…' : mode === 'login' ? 'Sign in' : 'Create account'}
        </button>

        <p className="auth-switch">
          {mode === 'login' ? (
            <>
              No account?{' '}
              <button type="button" className="link" onClick={() => switchMode('register')}>
                Create one
              </button>
            </>
          ) : (
            <>
              Already registered?{' '}
              <button type="button" className="link" onClick={() => switchMode('login')}>
                Sign in
              </button>
            </>
          )}
        </p>
      </form>
    </div>
  )
}
