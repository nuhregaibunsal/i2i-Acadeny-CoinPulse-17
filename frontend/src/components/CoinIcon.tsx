import { useState } from 'react'

const ICON_BASE = 'https://cdn.jsdelivr.net/npm/cryptocurrency-icons@0.18.1/128/color'

interface CoinIconProps {
  symbol: string
  size?: number
}

export function CoinIcon({ symbol, size = 30 }: CoinIconProps) {
  const [broken, setBroken] = useState(false)

  if (broken) {
    return (
      <span className="coin-fallback" style={{ width: size, height: size }}>
        {symbol.charAt(0)}
      </span>
    )
  }

  return (
    <img
      className="coin-icon"
      src={`${ICON_BASE}/${symbol.toLowerCase()}.png`}
      width={size}
      height={size}
      alt={symbol}
      loading="lazy"
      onError={() => setBroken(true)}
    />
  )
}
