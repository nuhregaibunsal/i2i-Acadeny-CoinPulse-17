import { useState } from 'react'
import { ApiError } from '../api/client'
import { transfer } from '../api/endpoints'
import { useI18n } from '../i18n/I18nProvider'

interface TransferModalProps {
  onClose: () => void
}

export function TransferModal({ onClose }: TransferModalProps) {
  const { t } = useI18n()
  const [recipient, setRecipient] = useState('')
  const [amount, setAmount] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [notice, setNotice] = useState<string | null>(null)

  async function submit() {
    setError(null)
    setNotice(null)
    const value = Number(amount)
    if (!recipient.trim() || !(value > 0)) {
      setError(t('trade.invalidAmount'))
      return
    }
    setBusy(true)
    try {
      const result = await transfer(recipient.trim(), value)
      setNotice(t('wallet.transferDone', { balance: result.cashBalance.toFixed(2) }))
      setAmount('')
      setRecipient('')
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
          <h2>{t('wallet.transfer')}</h2>
          <button type="button" className="close-x" onClick={onClose} aria-label="Close">
            ✕
          </button>
        </div>

        <label htmlFor="transfer-recipient">{t('wallet.recipient')}</label>
        <input
          id="transfer-recipient"
          value={recipient}
          onChange={(event) => setRecipient(event.target.value)}
          placeholder="username"
          autoFocus
        />

        <label htmlFor="transfer-amount">{t('wallet.amount')}</label>
        <input
          id="transfer-amount"
          value={amount}
          onChange={(event) => setAmount(event.target.value)}
          inputMode="decimal"
          placeholder="1000"
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
