import { useEffect, useState } from 'react'
import { Avatar } from './Avatar'
import { useI18n } from '../i18n/I18nProvider'

interface WelcomeToastProps {
  name: string
  avatar: string | null
}

export function WelcomeToast({ name, avatar }: WelcomeToastProps) {
  const { t } = useI18n()
  const [visible, setVisible] = useState(true)

  useEffect(() => {
    const timer = window.setTimeout(() => setVisible(false), 5000)
    return () => window.clearTimeout(timer)
  }, [])

  if (!visible) {
    return null
  }

  return (
    <div className="welcome-toast">
      <Avatar avatar={avatar} name={name} size={36} />
      <span className="welcome-text">{t('welcome', { name })}</span>
    </div>
  )
}
