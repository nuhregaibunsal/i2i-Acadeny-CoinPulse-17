import { createContext, useContext, useEffect, useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import { messages } from './messages'
import type { Lang, MessageKey } from './messages'

const STORAGE_KEY = 'cryptopal.lang'

type Vars = Record<string, string | number>

interface I18nValue {
  lang: Lang
  setLang: (lang: Lang) => void
  t: (key: MessageKey, vars?: Vars) => string
}

const I18nContext = createContext<I18nValue>({ lang: 'tr', setLang: () => {}, t: (key) => key })

export function I18nProvider({ children }: { children: ReactNode }) {
  const [lang, setLang] = useState<Lang>(
    () => (localStorage.getItem(STORAGE_KEY) as Lang | null) ?? 'tr',
  )

  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, lang)
    document.documentElement.lang = lang
  }, [lang])

  const value = useMemo<I18nValue>(() => {
    const t = (key: MessageKey, vars?: Vars) => {
      let text: string = messages[lang][key] ?? messages.en[key] ?? key
      if (vars) {
        for (const [name, replacement] of Object.entries(vars)) {
          text = text.replace(`{${name}}`, String(replacement))
        }
      }
      return text
    }
    return { lang, setLang, t }
  }, [lang])

  return <I18nContext.Provider value={value}>{children}</I18nContext.Provider>
}

export function useI18n(): I18nValue {
  return useContext(I18nContext)
}
