import { useState } from 'react'
import { ApiError } from '../api/client'
import { changePassword } from '../api/endpoints'
import { useI18n } from '../i18n/I18nProvider'

interface PasswordModalProps {
  onClose: () => void
}

export function PasswordModal({ onClose }: PasswordModalProps) {
  const { t } = useI18n()
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [notice, setNotice] = useState<string | null>(null)

  async function submit() {
    setError(null)
    setNotice(null)
    if (!currentPassword || !newPassword) {
      return
    }
    setBusy(true)
    try {
      await changePassword(currentPassword, newPassword)
      setNotice(t('profile.passwordChanged'))
      setCurrentPassword('')
      setNewPassword('')
    } catch (err) {
      setError(err instanceof ApiError ? err.message : t('profile.passwordError'))
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal" onClick={(event) => event.stopPropagation()}>
        <div className="modal-head">
          <h2>{t('profile.password')}</h2>
          <button type="button" className="close-x" onClick={onClose} aria-label="Close">
            ✕
          </button>
        </div>

        <label htmlFor="current-password">{t('profile.currentPassword')}</label>
        <input
          id="current-password"
          type="password"
          value={currentPassword}
          onChange={(event) => setCurrentPassword(event.target.value)}
          autoComplete="current-password"
        />

        <label htmlFor="new-password">{t('profile.newPassword')}</label>
        <input
          id="new-password"
          type="password"
          value={newPassword}
          onChange={(event) => setNewPassword(event.target.value)}
          autoComplete="new-password"
        />

        {error && <p className="alert alert-error">{error}</p>}
        {notice && <p className="alert alert-notice">{notice}</p>}

        <button
          type="button"
          className="save-btn"
          onClick={submit}
          disabled={busy || !currentPassword || !newPassword}
        >
          {busy ? <span className="spinner" /> : t('profile.save')}
        </button>
      </div>
    </div>
  )
}
