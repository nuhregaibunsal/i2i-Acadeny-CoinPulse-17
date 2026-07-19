import { CoinIcon } from './CoinIcon'
import { DonutChart } from './DonutChart'
import { useI18n } from '../i18n/I18nProvider'
import type { CryptoPrice, PortfolioResponse } from '../api/types'

interface PortfolioPanelProps {
  portfolio: PortfolioResponse | null
  loading: boolean
  error: string | null
  onRefresh: () => void
  onSelect: (price: CryptoPrice) => void
}

function money(value: number): string {
  return value.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function pnlClass(value: number): string {
  if (value > 0) return 'pnl-up'
  if (value < 0) return 'pnl-down'
  return ''
}

function withSign(value: number): string {
  return `${value > 0 ? '+' : ''}${money(value)}`
}

export function PortfolioPanel({ portfolio, loading, error, onRefresh, onSelect }: PortfolioPanelProps) {
  const { t } = useI18n()
  const total = portfolio ? portfolio.cashBalance + portfolio.holdingsValue : 0

  return (
    <section className="panel">
      <div className="panel-head">
        <h2>{t('portfolio.title')}</h2>
        <button type="button" className="secondary" onClick={onRefresh} disabled={loading}>
          {loading ? <span className="spinner" /> : t('market.refresh')}
        </button>
      </div>

      {error && !portfolio && <p className="alert alert-error">{error}</p>}

      {!portfolio ? (
        <p className="muted">{t('portfolio.loading')}</p>
      ) : (
        <>
          <div className="stat-row">
            <div className="stat">
              <span className="muted small">{t('portfolio.cash')}</span>
              <strong>${money(portfolio.cashBalance)}</strong>
            </div>
            <div className="stat">
              <span className="muted small">{t('portfolio.holdings')}</span>
              <strong>${money(portfolio.holdingsValue)}</strong>
            </div>
            <div className="stat">
              <span className="muted small">{t('portfolio.total')}</span>
              <strong>${money(total)}</strong>
            </div>
            <div className="stat">
              <span className="muted small">{t('portfolio.pnl')}</span>
              <strong className={pnlClass(portfolio.totalProfitLoss)}>
                ${withSign(portfolio.totalProfitLoss)}
              </strong>
            </div>
          </div>

          {portfolio.holdings.length === 0 ? (
            <p className="muted">{t('portfolio.noHoldings')}</p>
          ) : (
            <>
            <DonutChart
              slices={[
                { label: t('portfolio.cash'), value: portfolio.cashBalance },
                ...portfolio.holdings.map((holding) => ({
                  label: holding.symbol,
                  value: holding.value,
                  volume: holding.volume,
                  profitLoss: holding.profitLoss,
                })),
              ]}
            />
            <div className="table-scroll">
              <table className="holdings">
                <thead>
                  <tr>
                    <th>{t('portfolio.asset')}</th>
                    <th>{t('portfolio.volume')}</th>
                    <th>{t('portfolio.price')}</th>
                    <th>{t('portfolio.value')}</th>
                    <th>{t('portfolio.pnl')}</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {portfolio.holdings.map((holding) => (
                    <tr key={holding.symbol}>
                      <td>
                        <span className="coin-id">
                          <CoinIcon symbol={holding.symbol} size={22} />
                          {holding.symbol}
                        </span>
                      </td>
                      <td>{holding.volume}</td>
                      <td>${money(holding.currentPrice)}</td>
                      <td>${money(holding.value)}</td>
                      <td className={pnlClass(holding.profitLoss)}>
                        {holding.profitLoss > 0 ? '+' : ''}
                        {holding.profitLossPct.toFixed(2)}%
                      </td>
                      <td>
                        <button
                          type="button"
                          className="secondary small-btn"
                          onClick={() =>
                            onSelect({ symbol: holding.symbol, price: holding.currentPrice })
                          }
                        >
                          {t('trade.buy')}/{t('trade.sell')}
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            </>
          )}
        </>
      )}
    </section>
  )
}
