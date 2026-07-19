import { useTheme } from '../theme/ThemeProvider'

export function ThemeToggle() {
  const { theme, toggleTheme } = useTheme()

  return (
    <button
      type="button"
      className={`theme-toggle theme-${theme}`}
      onClick={toggleTheme}
      aria-label="Toggle theme"
      title="Toggle theme"
    >
      <span className="theme-icon theme-icon-sun">☀️</span>
      <span className="theme-icon theme-icon-moon">🌙</span>
      <span className="theme-knob" />
    </button>
  )
}
