import { useState } from 'react'
import { MarketList } from '../components/MarketList'
import type { CryptoPrice } from '../api/types'

export function Dashboard() {
  const [selected, setSelected] = useState<CryptoPrice | null>(null)

  return (
    <div className="dashboard">
      <div className="dashboard-col">
        <MarketList onSelect={setSelected} />
      </div>
      <div className="dashboard-col">
        {selected ? (
          <section className="panel">
            <div className="panel-head">
              <h2>{selected.symbol}</h2>
            </div>
            <p className="muted">Trading modal coming next.</p>
          </section>
        ) : (
          <section className="panel">
            <p className="muted">Select a coin to trade.</p>
          </section>
        )}
      </div>
    </div>
  )
}
