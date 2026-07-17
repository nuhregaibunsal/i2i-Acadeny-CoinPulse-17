import { usePrices } from '../hooks/usePrices'
import type { CryptoPrice } from '../api/types'

interface MarketListProps {
  onSelect: (price: CryptoPrice) => void
}

function formatPrice(value: number): string {
  return value.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

export function MarketList({ onSelect }: MarketListProps) {
  const { prices, error, loading, lastUpdated, refresh } = usePrices()

  return (
    <section className="panel">
      <div className="panel-head">
        <h2>Market</h2>
        <div className="panel-head-actions">
          {lastUpdated && (
            <span className="muted small">Updated {lastUpdated.toLocaleTimeString()}</span>
          )}
          <button type="button" className="secondary" onClick={refresh} disabled={loading}>
            {loading ? <span className="spinner" /> : 'Refresh'}
          </button>
        </div>
      </div>

      {error && <p className="alert alert-error">{error}</p>}

      {prices.length === 0 && loading ? (
        <div className="skeleton-list">
          {Array.from({ length: 5 }).map((_, i) => (
            <div key={i} className="skeleton-row" />
          ))}
        </div>
      ) : (
        <ul className="price-list">
          {prices.map((price) => (
            <li key={price.symbol}>
              <button type="button" className="price-row" onClick={() => onSelect(price)}>
                <span className="symbol">{price.symbol}</span>
                <span className="price">${formatPrice(price.price)}</span>
              </button>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
