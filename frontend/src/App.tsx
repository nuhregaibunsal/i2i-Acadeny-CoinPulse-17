import { useState } from 'react'
import { useSession } from './auth/useSession'
import { useI18n } from './i18n/I18nProvider'
import { useProfile } from './profile/ProfileProvider'
import { AuthPage } from './pages/AuthPage'
import { Dashboard } from './pages/Dashboard'
import { ChatWidget } from './components/ChatWidget'
import { ThemeToggle } from './components/ThemeToggle'
import { ProfileMenu } from './components/ProfileMenu'
import { WalletMenu } from './components/WalletMenu'
import { WelcomeToast } from './components/WelcomeToast'
import './App.css'

type View = 'market' | 'portfolio' | 'history' | 'orders'

function App() {
  const { username, isAuthenticated, signIn, signOut } = useSession()
  const { lang, setLang, t } = useI18n()
  const { displayName, avatar } = useProfile()
  const [guest, setGuest] = useState(false)
  const [view, setView] = useState<View>('market')

  const inApp = isAuthenticated || guest
  const canTrade = isAuthenticated
  const name = displayName || username || ''

  function leaveSession() {
    signOut()
    setGuest(false)
    setView('market')
  }

  return (
    <div className="app">
      <header className="app-header">
        <div className="brand">
          <h1>CryptoPal</h1>
          {inApp && (
            <nav className="nav">
              <button
                type="button"
                className={view === 'market' ? 'nav-btn active' : 'nav-btn'}
                onClick={() => setView('market')}
              >
                {t('nav.market')}
              </button>
              {isAuthenticated && (
                <button
                  type="button"
                  className={view === 'portfolio' ? 'nav-btn active' : 'nav-btn'}
                  onClick={() => setView('portfolio')}
                >
                  {t('nav.portfolio')}
                </button>
              )}
              {isAuthenticated && (
                <button
                  type="button"
                  className={view === 'history' ? 'nav-btn active' : 'nav-btn'}
                  onClick={() => setView('history')}
                >
                  {t('nav.history')}
                </button>
              )}
              {isAuthenticated && (
                <button
                  type="button"
                  className={view === 'orders' ? 'nav-btn active' : 'nav-btn'}
                  onClick={() => setView('orders')}
                >
                  {t('nav.orders')}
                </button>
              )}
              {isAuthenticated && <WalletMenu />}
            </nav>
          )}
        </div>

        <div className="header-controls">
          <ThemeToggle />
          <button
            type="button"
            className="icon-btn lang-btn"
            onClick={() => setLang(lang === 'tr' ? 'en' : 'tr')}
            aria-label="Toggle language"
            title="Toggle language"
          >
            {lang === 'tr' ? 'EN' : 'TR'}
          </button>
          {guest && !isAuthenticated && (
            <button type="button" className="secondary" onClick={leaveSession}>
              {t('nav.signIn')}
            </button>
          )}
        </div>
      </header>

      <main className="app-main">
        {inApp ? (
          <Dashboard view={view} canTrade={canTrade} />
        ) : (
          <AuthPage onAuthenticated={signIn} onGuest={() => setGuest(true)} />
        )}
      </main>

      {isAuthenticated && <WelcomeToast name={name} avatar={avatar} />}

      {isAuthenticated && <ProfileMenu username={username} onSignOut={leaveSession} />}
      {isAuthenticated && <ChatWidget />}
    </div>
  )
}

export default App
