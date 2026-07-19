import { useState } from 'react'

function stepFor(price: number): number {
  if (price < 1) return 0.001
  if (price < 10) return 0.01
  if (price < 100) return 0.1
  if (price < 1000) return 1
  if (price < 10000) return 10
  return 100
}

function decimalsFor(step: number): number {
  if (step >= 1) return 0
  if (step >= 0.1) return 1
  if (step >= 0.01) return 2
  return 3
}

interface PricePickerProps {
  current: number
  value: number
  onChange: (value: number) => void
}

export function PricePicker({ current, value, onChange }: PricePickerProps) {
  const [open, setOpen] = useState(false)

  const step = stepFor(current)
  const decimals = decimalsFor(step)
  const maxSteps = Math.min(40, Math.max(1, Math.round((current * 0.1) / step)))
  const center = Math.round(current / step) * step

  const options: number[] = []
  for (let i = maxSteps; i >= -maxSteps; i--) {
    options.push(Number((center + i * step).toFixed(decimals)))
  }

  return (
    <div className="price-picker">
      <button type="button" className="price-picker-btn" onClick={() => setOpen((o) => !o)}>
        ${value.toFixed(decimals)}
        <span className="caret">▾</span>
      </button>
      {open && (
        <ul className="price-picker-list">
          {options.map((option) => (
            <li key={option}>
              <button
                type="button"
                className={option === value ? 'active' : ''}
                onClick={() => {
                  onChange(option)
                  setOpen(false)
                }}
              >
                ${option.toFixed(decimals)}
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}
