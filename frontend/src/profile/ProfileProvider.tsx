import { createContext, useContext, useState } from 'react'
import type { ReactNode } from 'react'

const NAME_KEY = 'cryptopal.displayName'
const AVATAR_KEY = 'cryptopal.avatar'

interface ProfileValue {
  displayName: string
  avatar: string | null
  setDisplayName: (name: string) => void
  setAvatar: (data: string) => void
}

const ProfileContext = createContext<ProfileValue>({
  displayName: '',
  avatar: null,
  setDisplayName: () => {},
  setAvatar: () => {},
})

export function ProfileProvider({ children }: { children: ReactNode }) {
  const [displayName, setDisplayNameState] = useState(() => localStorage.getItem(NAME_KEY) ?? '')
  const [avatar, setAvatarState] = useState<string | null>(() => localStorage.getItem(AVATAR_KEY))

  function setDisplayName(name: string) {
    localStorage.setItem(NAME_KEY, name)
    setDisplayNameState(name)
  }

  function setAvatar(data: string) {
    localStorage.setItem(AVATAR_KEY, data)
    setAvatarState(data)
  }

  return (
    <ProfileContext.Provider value={{ displayName, avatar, setDisplayName, setAvatar }}>
      {children}
    </ProfileContext.Provider>
  )
}

export function useProfile(): ProfileValue {
  return useContext(ProfileContext)
}
