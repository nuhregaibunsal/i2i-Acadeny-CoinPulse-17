import { useState } from 'react'
import { CoinDetail } from '../components/CoinDetail'
import { MarketList } from '../components/MarketList'
import { OrdersView } from '../components/OrdersView'
import { PortfolioPanel } from '../components/PortfolioPanel'
import { TradeModal } from '../components/TradeModal'
import { TransactionsView } from '../components/TransactionsView'
import { usePortfolio } from '../hooks/usePortfolio'
import type { CryptoPrice } from '../api/types'

interface DashboardProps {
  view: 'market' | 'portfolio' | 'history' | 'orders'
  canTrade: boolean
}

export function Dashboard({ view, canTrade }: DashboardProps) {
  const [selected, setSelected] = useState<CryptoPrice | null>(null)
  const [tradeCoin, setTradeCoin] = useState<CryptoPrice | null>(null)
  const { portfolio, loading, error, refresh } = usePortfolio(canTrade)

  return (
    <div className="view">
      {view === 'market' && (
        <div className={selected ? 'market-layout has-selection' : 'market-layout'}>
          <div className="market-col">
            <MarketList
              onSelect={(coin) =>
                setSelected((prev) => (prev?.symbol === coin.symbol ? null : coin))
              }
              canTrade={canTrade}
              selected={selected}
            />
          </div>
          <div className="detail-col">
            <CoinDetail price={selected} canTrade={canTrade} onTrade={setTradeCoin} />
          </div>
        </div>
      )}
      {view === 'portfolio' && (
        <PortfolioPanel
          portfolio={portfolio}
          loading={loading}
          error={error}
          onRefresh={refresh}
          onSelect={setTradeCoin}
        />
      )}
      {view === 'history' && <TransactionsView />}
      {view === 'orders' && <OrdersView />}

      {tradeCoin && canTrade && (
        <TradeModal
          price={tradeCoin}
          portfolio={portfolio}
          onClose={() => setTradeCoin(null)}
          onExecuted={refresh}
        />
      )}
    </div>
  )
}
