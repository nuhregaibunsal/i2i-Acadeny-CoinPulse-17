import { useEffect, useMemo, useState } from 'react'
import { ApiError } from '../api/client'
import { fetchTransactions } from '../api/endpoints'
import { CoinIcon } from './CoinIcon'
import { useI18n } from '../i18n/I18nProvider'
import type { TransactionRecord, TransactionType } from '../api/types'

function money(value: number): string {
  return value.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function isTrade(type: TransactionType): boolean {
  return type === 'BUY' || type === 'SELL'
}

export function TransactionsView() {
  const { t } = useI18n()
  const [transactions, setTransactions] = useState<TransactionRecord[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [coin, setCoin] = useState('')
  const [from, setFrom] = useState('')
  const [to, setTo] = useState('')

  useEffect(() => {
    fetchTransactions()
      .then(setTransactions)
      .catch((err) => setError(err instanceof ApiError ? err.message : 'Failed to load transactions.'))
      .finally(() => setLoading(false))
  }, [])

  const filtered = useMemo(() => {
    return transactions.filter((transaction) => {
      if (coin && !(transaction.symbol ?? '').toLowerCase().includes(coin.trim().toLowerCase())) {
        return false
      }
      const date = new Date(transaction.createdAt)
      if (from && date < new Date(from)) {
        return false
      }
      if (to && date > new Date(`${to}T23:59:59`)) {
        return false
      }
      return true
    })
  }, [transactions, coin, from, to])

  function typeLabel(type: TransactionType): string {
    switch (type) {
      case 'BUY':
        return t('trade.buy')
      case 'SELL':
        return t('trade.sell')
      case 'DEPOSIT':
        return t('history.deposit')
      case 'SEND':
        return t('history.send')
      case 'RECEIVE':
        return t('history.receive')
    }
  }

  function typeBadgeClass(type: TransactionType): string {
    return type === 'SELL' || type === 'SEND' ? 'badge badge-sell' : 'badge badge-buy'
  }

  return (
    <section className="panel">
      <div className="panel-head">
        <h2>{t('history.title')}</h2>
      </div>

      <div className="filter-row">
        <input
          className="filter-input"
          value={coin}
          onChange={(event) => setCoin(event.target.value)}
          placeholder={t('history.coin')}
        />
        <label className="filter-date">
          <span className="muted small">{t('history.from')}</span>
          <input type="date" value={from} onChange={(event) => setFrom(event.target.value)} />
        </label>
        <label className="filter-date">
          <span className="muted small">{t('history.to')}</span>
          <input type="date" value={to} onChange={(event) => setTo(event.target.value)} />
        </label>
      </div>

      {error && <p className="alert alert-error">{error}</p>}

      {loading ? (
        <p className="muted">{t('history.loading')}</p>
      ) : filtered.length === 0 ? (
        <p className="muted">{t('history.empty')}</p>
      ) : (
        <div className="table-scroll">
          <table className="holdings">
            <thead>
              <tr>
                <th>{t('history.date')}</th>
                <th>{t('history.type')}</th>
                <th>{t('portfolio.asset')}</th>
                <th>{t('portfolio.volume')}</th>
                <th>{t('portfolio.price')}</th>
                <th>{t('history.total')}</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((transaction, index) => (
                <tr key={index}>
                  <td>{new Date(transaction.createdAt).toLocaleString()}</td>
                  <td>
                    <span className={typeBadgeClass(transaction.type)}>{typeLabel(transaction.type)}</span>
                  </td>
                  <td>
                    {isTrade(transaction.type) && transaction.symbol ? (
                      <span className="coin-id">
                        <CoinIcon symbol={transaction.symbol} size={20} />
                        {transaction.symbol}
                      </span>
                    ) : transaction.type === 'SEND' ? (
                      `→ ${transaction.symbol}`
                    ) : transaction.type === 'RECEIVE' ? (
                      `← ${transaction.symbol}`
                    ) : (
                      'USD'
                    )}
                  </td>
                  <td>{transaction.volume ?? '—'}</td>
                  <td>{transaction.price != null ? `$${money(transaction.price)}` : '—'}</td>
                  <td>${money(transaction.totalValue)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  )
}
