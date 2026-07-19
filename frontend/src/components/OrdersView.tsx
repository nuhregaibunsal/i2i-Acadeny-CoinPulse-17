import { useCallback, useEffect, useState } from 'react'
import { ApiError } from '../api/client'
import { cancelConditionalOrder, fetchConditionalOrders } from '../api/endpoints'
import { CoinIcon } from './CoinIcon'
import { useI18n } from '../i18n/I18nProvider'
import type { ConditionalOrderView } from '../api/types'

function money(value: number): string {
  return value.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

export function OrdersView() {
  const { t } = useI18n()
  const [orders, setOrders] = useState<ConditionalOrderView[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  const load = useCallback(async () => {
    try {
      setOrders(await fetchConditionalOrders())
      setError(null)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Failed to load orders.')
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    load()
    const timer = window.setInterval(load, 15000)
    return () => window.clearInterval(timer)
  }, [load])

  async function cancel(id: number) {
    await cancelConditionalOrder(id)
    load()
  }

  function statusLabel(status: string): string {
    const key = `orders.${status.toLowerCase()}`
    return t(key as 'orders.pending')
  }

  return (
    <section className="panel">
      <div className="panel-head">
        <h2>{t('orders.title')}</h2>
        <button type="button" className="secondary" onClick={load} disabled={loading}>
          {loading ? <span className="spinner" /> : t('market.refresh')}
        </button>
      </div>

      {error && <p className="alert alert-error">{error}</p>}

      {orders.length === 0 ? (
        <p className="muted">{t('orders.empty')}</p>
      ) : (
        <div className="table-scroll">
          <table className="holdings">
            <thead>
              <tr>
                <th>{t('portfolio.asset')}</th>
                <th>{t('history.type')}</th>
                <th>{t('orders.condition')}</th>
                <th>{t('portfolio.volume')}</th>
                <th>{t('orders.status')}</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {orders.map((order) => (
                <tr key={order.id}>
                  <td>
                    <span className="coin-id">
                      <CoinIcon symbol={order.symbol} size={20} />
                      {order.symbol}
                    </span>
                  </td>
                  <td>
                    <span className={order.side === 'BUY' ? 'badge badge-buy' : 'badge badge-sell'}>
                      {order.side === 'BUY' ? t('trade.buy') : t('trade.sell')}
                    </span>
                  </td>
                  <td>
                    {order.direction === 'ABOVE' ? '↑' : '↓'} ${money(order.targetPrice)}
                  </td>
                  <td>{order.volume}</td>
                  <td>{statusLabel(order.status)}</td>
                  <td>
                    {order.status === 'PENDING' && (
                      <button type="button" className="secondary small-btn" onClick={() => cancel(order.id)}>
                        {t('orders.cancel')}
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  )
}
