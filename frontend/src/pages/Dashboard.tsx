import { useState } from 'react'
import { ChatPanel } from '../components/ChatPanel'
import { MarketList } from '../components/MarketList'
import { PortfolioPanel } from '../components/PortfolioPanel'
import { TradeModal } from '../components/TradeModal'
import { usePortfolio } from '../hooks/usePortfolio'
import type { CryptoPrice } from '../api/types'

export function Dashboard() {
  const [selected, setSelected] = useState<CryptoPrice | null>(null)
  const { portfolio, loading, error, refresh } = usePortfolio()

  return (
    <div className="dashboard-wrap">
      <div className="dashboard">
        <div className="dashboard-col">
          <MarketList onSelect={setSelected} />
        </div>
        <div className="dashboard-col">
          <PortfolioPanel portfolio={portfolio} loading={loading} error={error} onRefresh={refresh} />
        </div>
      </div>

      <ChatPanel />

      {selected && (
        <TradeModal
          price={selected}
          portfolio={portfolio}
          onClose={() => setSelected(null)}
          onExecuted={refresh}
        />
      )}
    </div>
  )
}
