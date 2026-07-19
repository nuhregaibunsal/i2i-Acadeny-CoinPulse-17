import { useCallback, useEffect, useRef, useState } from 'react'
import { ApiError } from '../api/client'
import { fetchPrices } from '../api/endpoints'
import type { CryptoPrice } from '../api/types'

const POLL_INTERVAL_MS = 3000

export type Direction = 'up' | 'down' | 'flat'

interface UsePricesResult {
  prices: CryptoPrice[]
  directions: Record<string, Direction>
  error: string | null
  loading: boolean
  lastUpdated: Date | null
  refresh: () => void
}

export function usePrices(): UsePricesResult {
  const [prices, setPrices] = useState<CryptoPrice[]>([])
  const [directions, setDirections] = useState<Record<string, Direction>>({})
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)
  const [lastUpdated, setLastUpdated] = useState<Date | null>(null)
  const previous = useRef<Record<string, number>>({})
  const inFlight = useRef(false)

  const load = useCallback(async () => {
    if (inFlight.current) {
      return
    }
    inFlight.current = true
    setLoading(true)
    try {
      const next = await fetchPrices()
      const nextDirections: Record<string, Direction> = {}
      for (const price of next) {
        const prev = previous.current[price.symbol]
        nextDirections[price.symbol] =
          prev === undefined || prev === price.price ? 'flat' : price.price > prev ? 'up' : 'down'
        previous.current[price.symbol] = price.price
      }
      setPrices(next)
      setDirections(nextDirections)
      setLastUpdated(new Date())
      setError(null)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Failed to load prices.')
    } finally {
      setLoading(false)
      inFlight.current = false
    }
  }, [])

  useEffect(() => {
    load()
    const timer = window.setInterval(load, POLL_INTERVAL_MS)
    return () => window.clearInterval(timer)
  }, [load])

  return { prices, directions, error, loading, lastUpdated, refresh: load }
}
