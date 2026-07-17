import { useSession } from './auth/useSession'
import { AuthPage } from './pages/AuthPage'
import { Dashboard } from './pages/Dashboard'
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
        {isAuthenticated ? <Dashboard /> : <AuthPage onAuthenticated={signIn} />}
      </main>
    </div>
  )
}

export default App
