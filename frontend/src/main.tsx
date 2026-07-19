import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'
import { ThemeProvider } from './theme/ThemeProvider'
import { I18nProvider } from './i18n/I18nProvider'
import { ProfileProvider } from './profile/ProfileProvider'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <ThemeProvider>
      <I18nProvider>
        <ProfileProvider>
          <App />
        </ProfileProvider>
      </I18nProvider>
    </ThemeProvider>
  </StrictMode>,
)
