import { useEffect, useState } from 'react'
import { fetchPriceHistory } from '../api/endpoints'
import { CoinIcon } from './CoinIcon'
import { LineChart } from './LineChart'
import { useI18n } from '../i18n/I18nProvider'
import type { CryptoPrice, PricePoint } from '../api/types'

interface CoinDetailProps {
  price: CryptoPrice | null
  canTrade: boolean
  onTrade: (price: CryptoPrice) => void
}

const RANGES = ['1h', '1d', '1w']

export function CoinDetail({ price, canTrade, onTrade }: CoinDetailProps) {
  const { t } = useI18n()
  const [range, setRange] = useState('1d')
  const [history, setHistory] = useState<PricePoint[]>([])

  useEffect(() => {
    if (!price) {
      return
    }
    let active = true
    fetchPriceHistory(price.symbol, range)
      .then((series) => {
        if (active) setHistory(series)
      })
      .catch(() => {
        if (active) setHistory([])
      })
    return () => {
      active = false
    }
  }, [price?.symbol, range])

  if (!price) {
    return (
      <section className="panel coin-detail">
        <p className="muted">{t('coin.selectPrompt')}</p>
      </section>
    )
  }

  return (
    <section className="panel coin-detail">
      <div className="panel-head">
        <h2 className="modal-title">
          <CoinIcon symbol={price.symbol} size={26} />
          {price.symbol} · ${price.price.toLocaleString('en-US', { minimumFractionDigits: 2 })}
        </h2>
        {canTrade && (
          <button type="button" onClick={() => onTrade(price)}>
            {t('trade.trade')}
          </button>
        )}
      </div>

      <div className="range-tabs">
        {RANGES.map((option) => (
          <button
            key={option}
            type="button"
            className={range === option ? 'chip active' : 'chip'}
            onClick={() => setRange(option)}
          >
            {t(`range.${option}` as 'range.1h')}
          </button>
        ))}
      </div>

      {history.length >= 2 ? (
        <LineChart points={history} range={range} />
      ) : (
        <p className="muted small">{t('chart.noData')}</p>
      )}
    </section>
  )
}
