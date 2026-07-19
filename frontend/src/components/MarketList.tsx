import { useMemo, useState } from 'react'
import { usePrices } from '../hooks/usePrices'
import type { Direction } from '../hooks/usePrices'
import { CoinIcon } from './CoinIcon'
import { useI18n } from '../i18n/I18nProvider'
import type { CryptoPrice } from '../api/types'

interface MarketListProps {
  onSelect: (price: CryptoPrice) => void
  canTrade: boolean
  selected: CryptoPrice | null
}

const ARROW: Record<Direction, string> = { up: '▲', down: '▼', flat: '' }

function formatPrice(value: number): string {
  return value.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

export function MarketList({ onSelect, canTrade, selected }: MarketListProps) {
  const { t } = useI18n()
  const { prices, directions, error, loading, lastUpdated, refresh } = usePrices()
  const [query, setQuery] = useState('')

  const filtered = useMemo(
    () => prices.filter((price) => price.symbol.toLowerCase().includes(query.trim().toLowerCase())),
    [prices, query],
  )

  return (
    <section className="panel market-panel">
      <div className="panel-head">
        <h2>{t('market.title')}</h2>
        <div className="panel-head-actions">
          {lastUpdated && (
            <span className="muted small">
              {t('market.updated', { time: lastUpdated.toLocaleTimeString() })}
            </span>
          )}
          <button type="button" className="secondary" onClick={refresh} disabled={loading}>
            {loading ? <span className="spinner" /> : t('market.refresh')}
          </button>
        </div>
      </div>

      <input
        className="market-search"
        value={query}
        onChange={(event) => setQuery(event.target.value)}
        placeholder={t('market.search')}
      />

      {!canTrade && <p className="guest-notice">{t('market.guestNotice')}</p>}
      {error && prices.length === 0 && <p className="alert alert-error">{error}</p>}

      {prices.length === 0 && loading ? (
        <div className="skeleton-list">
          {Array.from({ length: 5 }).map((_, index) => (
            <div key={index} className="skeleton-row" />
          ))}
        </div>
      ) : filtered.length === 0 ? (
        <p className="muted">{t('market.empty')}</p>
      ) : (
        <ul className="price-list">
          {filtered.map((price) => {
            const dir = directions[price.symbol] ?? 'flat'
            const active = selected?.symbol === price.symbol
            return (
              <li key={price.symbol}>
                <button
                  type="button"
                  className={active ? 'price-row active' : 'price-row'}
                  onClick={() => onSelect(price)}
                >
                  <span className="coin-id">
                    <CoinIcon symbol={price.symbol} />
                    <span className="symbol">{price.symbol}</span>
                  </span>
                  <span className={`price price-${dir}`}>
                    ${formatPrice(price.price)}
                    {dir !== 'flat' && <span className="dir">{ARROW[dir]}</span>}
                  </span>
                </button>
              </li>
            )
          })}
        </ul>
      )}
    </section>
  )
}
