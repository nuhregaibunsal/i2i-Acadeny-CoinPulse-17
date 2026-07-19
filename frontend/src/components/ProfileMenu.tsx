import { useState } from 'react'
import { Avatar } from './Avatar'
import { PasswordModal } from './PasswordModal'
import { ProfileModal } from './ProfileModal'
import { useI18n } from '../i18n/I18nProvider'
import { useProfile } from '../profile/ProfileProvider'

interface ProfileMenuProps {
  username: string | null
  onSignOut: () => void
}

type ActiveModal = 'none' | 'profile' | 'password'

export function ProfileMenu({ username, onSignOut }: ProfileMenuProps) {
  const { t } = useI18n()
  const { displayName, avatar } = useProfile()
  const [open, setOpen] = useState(false)
  const [modal, setModal] = useState<ActiveModal>('none')

  const name = displayName || username || ''

  return (
    <div className="profile-menu">
      {open && <div className="menu-overlay" onClick={() => setOpen(false)} />}
      {open && (
        <div className="profile-menu-pop">
          <button
            type="button"
            className="menu-item"
            onClick={() => {
              setModal('profile')
              setOpen(false)
            }}
          >
            {t('menu.editProfile')}
          </button>
          <button
            type="button"
            className="menu-item"
            onClick={() => {
              setModal('password')
              setOpen(false)
            }}
          >
            {t('profile.password')}
          </button>
          <button
            type="button"
            className="menu-item"
            onClick={() => {
              setOpen(false)
              onSignOut()
            }}
          >
            {t('menu.signOut')}
          </button>
        </div>
      )}

      <button type="button" className="profile-chip" onClick={() => setOpen((value) => !value)}>
        <Avatar avatar={avatar} name={name} size={34} />
        <span className="profile-chip-name">{name}</span>
      </button>

      {modal === 'profile' && <ProfileModal username={username} onClose={() => setModal('none')} />}
      {modal === 'password' && <PasswordModal onClose={() => setModal('none')} />}
    </div>
  )
}
