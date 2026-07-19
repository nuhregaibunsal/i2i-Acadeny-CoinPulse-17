import { useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { ApiError } from '../api/client'
import { createConditionalOrder, executeOrder } from '../api/endpoints'
import { CoinIcon } from './CoinIcon'
import { PricePicker } from './PricePicker'
import { useI18n } from '../i18n/I18nProvider'
import type { CryptoPrice, OrderSide, PortfolioResponse } from '../api/types'

type OrderType = 'instant' | 'conditional'

interface TradeModalProps {
  price: CryptoPrice
  portfolio: PortfolioResponse | null
  onClose: () => void
  onExecuted: () => void
}

export function TradeModal({ price, portfolio, onClose, onExecuted }: TradeModalProps) {
  const { t } = useI18n()

  const held = useMemo(() => {
    const holding = portfolio?.holdings.find((item) => item.symbol === price.symbol)
    return holding ? holding.volume : 0
  }, [portfolio, price.symbol])

  const canSell = held > 0
  const [side, setSide] = useState<OrderSide>('BUY')
  const [orderType, setOrderType] = useState<OrderType>('instant')
  const [amount, setAmount] = useState('')
  const [quantity, setQuantity] = useState('')
  const [target, setTarget] = useState(price.price)
  const [error, setError] = useState<string | null>(null)
  const [result, setResult] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  function onAmountChange(value: string) {
    setAmount(value)
    const numeric = Number(value)
    setQuantity(numeric > 0 && price.price > 0 ? (numeric / price.price).toFixed(8) : '')
  }

  function onQuantityChange(value: string) {
    setQuantity(value)
    const numeric = Number(value)
    setAmount(numeric > 0 ? (numeric * price.price).toFixed(2) : '')
  }

  async function submit(event: FormEvent) {
    event.preventDefault()
    setError(null)
    setResult(null)
    const volume = Number(quantity)
    if (!(volume > 0)) {
      setError(t('trade.invalidAmount'))
      return
    }
    if (orderType === 'conditional' && !(target > 0)) {
      setError(t('trade.invalidAmount'))
      return
    }
    setBusy(true)
    try {
      if (orderType === 'conditional') {
        await createConditionalOrder({ symbol: price.symbol, side, targetPrice: target, volume })
        setResult(t('trade.placed'))
      } else {
        const order = await executeOrder({ symbol: price.symbol, side, volume })
        setResult(
          `${t(side === 'BUY' ? 'trade.buy' : 'trade.sell')} ${order.volume} ${order.symbol} · $${order.totalValue.toFixed(2)}`,
        )
      }
      setAmount('')
      setQuantity('')
      setTarget(price.price)
      onExecuted()
    } catch (err) {
      setError(err instanceof ApiError ? err.message : t('trade.failed'))
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
          <h2 className="modal-title">
            <CoinIcon symbol={price.symbol} size={26} />
            {price.symbol} · ${price.price.toLocaleString('en-US', { minimumFractionDigits: 2 })}
          </h2>
          <button type="button" className="close-x" onClick={onClose} aria-label="Close">
            ✕
          </button>
        </div>

        <div className="tabs">
          <button
            type="button"
            className={side === 'BUY' ? 'tab active' : 'tab'}
            onClick={() => selectSide('BUY')}
          >
            {t('trade.buy')}
          </button>
          <button
            type="button"
            className={side === 'SELL' ? 'tab active' : 'tab'}
            onClick={() => selectSide('SELL')}
            disabled={!canSell}
          >
            {t('trade.sell')}
          </button>
        </div>

        <div className="mode-toggle">
          <button
            type="button"
            className={orderType === 'instant' ? 'chip active' : 'chip'}
            onClick={() => setOrderType('instant')}
          >
            {t('trade.instant')}
          </button>
          <button
            type="button"
            className={orderType === 'conditional' ? 'chip active' : 'chip'}
            onClick={() => setOrderType('conditional')}
          >
            {t('trade.conditional')}
          </button>
        </div>

        <p className="muted small">
          {canSell
            ? t('trade.youHold', { volume: held, symbol: price.symbol })
            : t('trade.dontHold', { symbol: price.symbol })}
        </p>

        <form onSubmit={submit}>
          {orderType === 'conditional' && (
            <>
              <label>{t('trade.targetPrice')}</label>
              <PricePicker current={price.price} value={target} onChange={setTarget} />
            </>
          )}

          <label htmlFor="trade-amount">{t('trade.amount')}</label>
          <input
            id="trade-amount"
            value={amount}
            onChange={(event) => onAmountChange(event.target.value)}
            inputMode="decimal"
            placeholder="1000"
          />

          <label htmlFor="trade-quantity">{t('trade.volume', { symbol: price.symbol })}</label>
          <input
            id="trade-quantity"
            value={quantity}
            onChange={(event) => onQuantityChange(event.target.value)}
            inputMode="decimal"
            placeholder="0.00"
          />

          {error && <p className="alert alert-error">{error}</p>}
          {result && <p className="alert alert-notice">{result}</p>}

          <button type="submit" disabled={busy}>
            {busy ? <span className="spinner" /> : null}
            {busy ? t('trade.executing') : t('trade.execute')}
          </button>
        </form>
      </div>
    </div>
  )
}
