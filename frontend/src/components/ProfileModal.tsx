import { useRef, useState } from 'react'
import type { ChangeEvent } from 'react'
import { Avatar } from './Avatar'
import { useI18n } from '../i18n/I18nProvider'
import { useProfile } from '../profile/ProfileProvider'

interface ProfileModalProps {
  username: string | null
  onClose: () => void
}

export function ProfileModal({ username, onClose }: ProfileModalProps) {
  const { t } = useI18n()
  const { displayName, avatar, setDisplayName, setAvatar } = useProfile()
  const [name, setName] = useState(displayName)
  const [preview, setPreview] = useState<string | null>(avatar)
  const inputRef = useRef<HTMLInputElement>(null)

  function onFile(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0]
    if (!file) {
      return
    }
    const reader = new FileReader()
    reader.onload = () => setPreview(reader.result as string)
    reader.readAsDataURL(file)
  }

  function save() {
    setDisplayName(name.trim())
    if (preview) {
      setAvatar(preview)
    }
    onClose()
  }

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal" onClick={(event) => event.stopPropagation()}>
        <div className="modal-head">
          <h2>{t('profile.title')}</h2>
          <button type="button" className="close-x" onClick={onClose} aria-label="Close">
            ✕
          </button>
        </div>

        <div className="profile-photo-row">
          <Avatar avatar={preview} name={name || username || ''} size={72} />
          <button type="button" className="secondary" onClick={() => inputRef.current?.click()}>
            {t('profile.upload')}
          </button>
          <input ref={inputRef} type="file" accept="image/*" hidden onChange={onFile} />
        </div>

        <label htmlFor="display-name">{t('profile.displayName')}</label>
        <input
          id="display-name"
          value={name}
          onChange={(event) => setName(event.target.value)}
          placeholder={t('profile.displayNamePlaceholder')}
        />

        <button type="button" className="save-btn" onClick={save}>
          {t('profile.save')}
        </button>
      </div>
    </div>
  )
}
