import { useCallback, useState } from 'react'
import { clearSession, getStoredUsername, getToken, storeSession } from '../api/client'

export function useSession() {
  const [username, setUsername] = useState<string | null>(() =>
    getToken() ? getStoredUsername() : null,
  )

  const signIn = useCallback((token: string, name: string) => {
    storeSession(token, name)
    setUsername(name)
  }, [])

  const signOut = useCallback(() => {
    clearSession()
    setUsername(null)
  }, [])

  return { username, isAuthenticated: username !== null, signIn, signOut }
}
