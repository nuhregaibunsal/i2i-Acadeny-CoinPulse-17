import { useCallback, useEffect, useRef, useState } from 'react'
import { ApiError } from '../api/client'
import { fetchPrices } from '../api/endpoints'
import type { CryptoPrice } from '../api/types'

const POLL_INTERVAL_MS = 15000

interface UsePricesResult {
  prices: CryptoPrice[]
  error: string | null
  loading: boolean
  lastUpdated: Date | null
  refresh: () => void
}

export function usePrices(): UsePricesResult {
  const [prices, setPrices] = useState<CryptoPrice[]>([])
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)
  const [lastUpdated, setLastUpdated] = useState<Date | null>(null)
  const inFlight = useRef(false)

  const load = useCallback(async () => {
    if (inFlight.current) {
      return
    }
    inFlight.current = true
    setLoading(true)
    try {
      const next = await fetchPrices()
      setPrices(next)
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

  return { prices, error, loading, lastUpdated, refresh: load }
}
