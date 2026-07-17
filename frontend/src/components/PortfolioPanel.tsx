import type { PortfolioResponse } from '../api/types'

interface PortfolioPanelProps {
  portfolio: PortfolioResponse | null
  loading: boolean
  error: string | null
  onRefresh: () => void
}

function money(value: number): string {
  return value.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

export function PortfolioPanel({ portfolio, loading, error, onRefresh }: PortfolioPanelProps) {
  const total = portfolio ? portfolio.cashBalance + portfolio.holdingsValue : 0

  return (
    <section className="panel">
      <div className="panel-head">
        <h2>Portfolio</h2>
        <button type="button" className="secondary" onClick={onRefresh} disabled={loading}>
          {loading ? <span className="spinner" /> : 'Refresh'}
        </button>
      </div>

      {error && <p className="alert alert-error">{error}</p>}

      {!portfolio ? (
        <p className="muted">Loading…</p>
      ) : (
        <>
          <div className="stat-row">
            <div className="stat">
              <span className="muted small">Cash</span>
              <strong>${money(portfolio.cashBalance)}</strong>
            </div>
            <div className="stat">
              <span className="muted small">Holdings</span>
              <strong>${money(portfolio.holdingsValue)}</strong>
            </div>
            <div className="stat">
              <span className="muted small">Total</span>
              <strong>${money(total)}</strong>
            </div>
          </div>

          {portfolio.holdings.length === 0 ? (
            <p className="muted">No holdings yet. Buy a coin to get started.</p>
          ) : (
            <table className="holdings">
              <thead>
                <tr>
                  <th>Asset</th>
                  <th>Volume</th>
                  <th>Price</th>
                  <th>Value</th>
                </tr>
              </thead>
              <tbody>
                {portfolio.holdings.map((holding) => (
                  <tr key={holding.symbol}>
                    <td>{holding.symbol}</td>
                    <td>{holding.volume}</td>
                    <td>${money(holding.currentPrice)}</td>
                    <td>${money(holding.value)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </>
      )}
    </section>
  )
}
