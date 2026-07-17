import { useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { ApiError } from '../api/client'
import { executeOrder } from '../api/endpoints'
import type { CryptoPrice, OrderSide, PortfolioResponse } from '../api/types'

interface TradeModalProps {
  price: CryptoPrice
  portfolio: PortfolioResponse | null
  onClose: () => void
  onExecuted: () => void
}

export function TradeModal({ price, portfolio, onClose, onExecuted }: TradeModalProps) {
  const held = useMemo(() => {
    const holding = portfolio?.holdings.find((item) => item.symbol === price.symbol)
    return holding ? holding.volume : 0
  }, [portfolio, price.symbol])

  const canSell = held > 0
  const [side, setSide] = useState<OrderSide>('BUY')
  const [volume, setVolume] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [result, setResult] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  const numericVolume = Number(volume)
  const estimated =
    Number.isFinite(numericVolume) && numericVolume > 0 ? numericVolume * price.price : 0

  async function submit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setResult(null)
    if (!(numericVolume > 0)) {
      setError('Enter a volume greater than zero.')
      return
    }
    setBusy(true)
    try {
      const order = await executeOrder({ symbol: price.symbol, side, volume: numericVolume })
      setResult(
        `${order.side} ${order.volume} ${order.symbol} for $${order.totalValue.toFixed(2)}. ` +
          `Cash balance: $${order.cashBalance.toFixed(2)}.`,
      )
      setVolume('')
      onExecuted()
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Order failed. Please try again.')
    } finally {
      setBusy(false)
    }
  }

  function selectSide(next: OrderSide) {
    setSide(next)
    setError(null)
    setResult(null)
  }

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal" onClick={(event) => event.stopPropagation()}>
        <div className="modal-head">
          <h2>
            {price.symbol} · ${price.price.toLocaleString('en-US', { minimumFractionDigits: 2 })}
          </h2>
          <button type="button" className="link" onClick={onClose} aria-label="Close">
            ✕
          </button>
        </div>

        <div className="tabs">
          <button
            type="button"
            className={side === 'BUY' ? 'tab active' : 'tab'}
            onClick={() => selectSide('BUY')}
          >
            Buy
          </button>
          <button
            type="button"
            className={side === 'SELL' ? 'tab active' : 'tab'}
            onClick={() => selectSide('SELL')}
            disabled={!canSell}
          >
            Sell
          </button>
        </div>

        <p className="muted small">
          {canSell ? `You hold ${held} ${price.symbol}.` : `You don't hold any ${price.symbol} yet.`}
        </p>

        <form onSubmit={submit}>
          <label htmlFor="volume">Volume ({price.symbol})</label>
          <input
            id="volume"
            value={volume}
            onChange={(event) => setVolume(event.target.value)}
            inputMode="decimal"
            placeholder="0.00"
            autoFocus
          />
          <p className="muted small">
            Estimated {side === 'BUY' ? 'cost' : 'proceeds'}: ${estimated.toFixed(2)}
          </p>

          {error && <p className="alert alert-error">{error}</p>}
          {result && <p className="alert alert-notice">{result}</p>}

          <button type="submit" disabled={busy}>
            {busy ? <span className="spinner" /> : null}
            {busy ? 'Executing…' : 'Execute Order'}
          </button>
        </form>
      </div>
    </div>
  )
}
