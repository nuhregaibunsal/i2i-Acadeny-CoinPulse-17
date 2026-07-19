import { useState } from 'react'
import { useI18n } from '../i18n/I18nProvider'

interface Slice {
  label: string
  value: number
  volume?: number
  profitLoss?: number
}

interface DonutChartProps {
  slices: Slice[]
}

const COLORS = ['#7b8194', '#4f8cff', '#a855f7', '#2ecc71', '#f7c948', '#ff5f56', '#22d3ee']

function money(value: number): string {
  return value.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

export function DonutChart({ slices }: DonutChartProps) {
  const { t } = useI18n()
  const [active, setActive] = useState<number | null>(null)

  const total = slices.reduce((sum, slice) => sum + slice.value, 0)
  if (total <= 0) {
    return null
  }

  const radius = 55
  const circumference = 2 * Math.PI * radius
  let acc = 0
  const current = active !== null ? slices[active] : null

  return (
    <div className="donut-wrap">
      <div className="donut-holder">
        <svg className="donut" viewBox="0 0 140 140" role="img">
          {slices.map((slice, index) => {
            const fraction = slice.value / total
            const dash = fraction * circumference
            const offset = -acc * circumference
            acc += fraction
            return (
              <circle
                key={slice.label}
                cx="70"
                cy="70"
                r={radius}
                fill="none"
                stroke={COLORS[index % COLORS.length]}
                strokeWidth={active === index ? 26 : 20}
                strokeDasharray={`${dash} ${circumference - dash}`}
                strokeDashoffset={offset}
                transform="rotate(-90 70 70)"
                onMouseEnter={() => setActive(index)}
                onMouseLeave={() => setActive(null)}
              />
            )
          })}
        </svg>
        {current && (
          <div className="donut-tip">
            <strong>{current.label}</strong>
            <span>${money(current.value)}</span>
            {current.volume !== undefined && current.volume > 0 && (
              <span>
                {current.volume} {current.label}
              </span>
            )}
            {current.profitLoss !== undefined && (
              <span className={current.profitLoss >= 0 ? 'pnl-up' : 'pnl-down'}>
                {t('portfolio.pnl')}: {current.profitLoss >= 0 ? '+' : ''}${money(current.profitLoss)}
              </span>
            )}
          </div>
        )}
      </div>
      <ul className="donut-legend">
        {slices.map((slice, index) => (
          <li key={slice.label}>
            <span className="dot" style={{ background: COLORS[index % COLORS.length] }} />
            {slice.label} · ${money(slice.value)} · {((slice.value / total) * 100).toFixed(1)}%
          </li>
        ))}
      </ul>
    </div>
  )
}
