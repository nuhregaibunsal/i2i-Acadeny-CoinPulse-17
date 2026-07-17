import { useSession } from './auth/useSession'
import { AuthPage } from './pages/AuthPage'
import './App.css'

function App() {
  const { username, isAuthenticated, signIn, signOut } = useSession()

  return (
    <div className="app">
      <header className="app-header">
        <h1>CryptoPal</h1>
        {isAuthenticated && (
          <div className="session">
            <span className="session-user">{username}</span>
            <button type="button" className="secondary" onClick={signOut}>
              Sign out
            </button>
          </div>
        )}
      </header>
      <main className="app-main">
        {isAuthenticated ? (
          <p className="placeholder">Market, portfolio and assistant coming next.</p>
        ) : (
          <AuthPage onAuthenticated={signIn} />
        )}
      </main>
    </div>
  )
}

export default App
