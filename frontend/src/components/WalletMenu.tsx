import { useState } from 'react'
import { DepositModal } from './DepositModal'
import { TransferModal } from './TransferModal'
import { useI18n } from '../i18n/I18nProvider'

type ActiveModal = 'none' | 'deposit' | 'transfer'

export function WalletMenu() {
  const { t } = useI18n()
  const [open, setOpen] = useState(false)
  const [modal, setModal] = useState<ActiveModal>('none')

  return (
    <div className="wallet-menu">
      <button
        type="button"
        className={open ? 'nav-btn active' : 'nav-btn'}
        onClick={() => setOpen((value) => !value)}
      >
        {t('nav.wallet')} ▾
      </button>

      {open && <div className="menu-overlay" onClick={() => setOpen(false)} />}
      {open && (
        <div className="wallet-dropdown">
          <button
            type="button"
            className="menu-item"
            onClick={() => {
              setModal('deposit')
              setOpen(false)
            }}
          >
            {t('wallet.deposit')}
          </button>
          <button
            type="button"
            className="menu-item"
            onClick={() => {
              setModal('transfer')
              setOpen(false)
            }}
          >
            {t('wallet.transfer')}
          </button>
        </div>
      )}

      {modal === 'deposit' && <DepositModal onClose={() => setModal('none')} />}
      {modal === 'transfer' && <TransferModal onClose={() => setModal('none')} />}
    </div>
  )
}
