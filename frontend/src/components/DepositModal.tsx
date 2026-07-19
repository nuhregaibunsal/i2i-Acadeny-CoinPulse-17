import { useState } from 'react'
import { ApiError } from '../api/client'
import { deposit } from '../api/endpoints'
import { useI18n } from '../i18n/I18nProvider'

interface DepositModalProps {
  onClose: () => void
}

export function DepositModal({ onClose }: DepositModalProps) {
  const { t } = useI18n()
  const [amount, setAmount] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [notice, setNotice] = useState<string | null>(null)

  async function submit() {
    setError(null)
    setNotice(null)
    const value = Number(amount)
    if (!(value > 0)) {
      setError(t('trade.invalidAmount'))
      return
    }
    setBusy(true)
    try {
      const result = await deposit(value)
      setNotice(t('wallet.depositDone', { balance: result.cashBalance.toFixed(2) }))
      setAmount('')
    } catch (err) {
      setError(err instanceof ApiError ? err.message : t('wallet.error'))
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal" onClick={(event) => event.stopPropagation()}>
        <div className="modal-head">
          <h2>{t('wallet.deposit')}</h2>
          <button type="button" className="close-x" onClick={onClose} aria-label="Close">
            ✕
          </button>
        </div>

        <label htmlFor="deposit-amount">{t('wallet.amount')}</label>
        <input
          id="deposit-amount"
          value={amount}
          onChange={(event) => setAmount(event.target.value)}
          inputMode="decimal"
          placeholder="1000"
          autoFocus
        />

        {error && <p className="alert alert-error">{error}</p>}
        {notice && <p className="alert alert-notice">{notice}</p>}

        <button type="button" className="save-btn" onClick={submit} disabled={busy}>
          {busy ? <span className="spinner" /> : t('wallet.confirm')}
        </button>
      </div>
    </div>
  )
}
