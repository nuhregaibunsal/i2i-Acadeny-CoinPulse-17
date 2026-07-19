import { useCallback, useEffect, useState } from 'react'
import { ApiError } from '../api/client'
import { fetchPortfolio } from '../api/endpoints'
import type { PortfolioResponse } from '../api/types'

const POLL_INTERVAL_MS = 6000

interface UsePortfolioResult {
  portfolio: PortfolioResponse | null
  error: string | null
  loading: boolean
  refresh: () => void
}

export function usePortfolio(enabled = true): UsePortfolioResult {
  const [portfolio, setPortfolio] = useState<PortfolioResponse | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(enabled)

  const load = useCallback(async () => {
    if (!enabled) {
      return
    }
    setLoading(true)
    try {
      setPortfolio(await fetchPortfolio())
      setError(null)
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Failed to load portfolio.')
    } finally {
      setLoading(false)
    }
  }, [enabled])

  useEffect(() => {
    if (!enabled) {
      return
    }
    load()
    const timer = window.setInterval(load, POLL_INTERVAL_MS)
    return () => window.clearInterval(timer)
  }, [enabled, load])

  return { portfolio, error, loading, refresh: load }
}
