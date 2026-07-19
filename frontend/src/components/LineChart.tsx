import { useRef, useState } from 'react'
import type { MouseEvent } from 'react'
import type { PricePoint } from '../api/types'

interface LineChartProps {
  points: PricePoint[]
  range: string
  width?: number
  height?: number
}

function money(value: number): string {
  return value.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function formatTime(time: number, range: string): string {
  const date = new Date(time)
  if (range === '1w') {
    return date.toLocaleString([], { day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit' })
  }
  return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

export function LineChart({ points, range, width = 320, height = 120 }: LineChartProps) {
  const [hover, setHover] = useState<number | null>(null)
  const plotRef = useRef<HTMLDivElement>(null)

  if (points.length < 2) {
    return null
  }

  const prices = points.map((point) => point.price)
  const min = Math.min(...prices)
  const max = Math.max(...prices)
  const range01 = max - min || 1
  const stepX = width / (points.length - 1)

  const coords = prices.map((value, index) => ({
    x: index * stepX,
    y: height - ((value - min) / range01) * (height - 8) - 4,
  }))

  const line = coords
    .map((point, index) => `${index === 0 ? 'M' : 'L'} ${point.x.toFixed(1)} ${point.y.toFixed(1)}`)
    .join(' ')
  const area = `${line} L ${width} ${height} L 0 ${height} Z`
  const rising = prices[prices.length - 1] >= prices[0]
  const stroke = rising ? 'var(--success)' : 'var(--danger)'

  const ticks = 4
  const yLabels = Array.from({ length: ticks }, (_, i) => max - (range01 * i) / (ticks - 1))
  const xCount = Math.min(4, points.length)
  const xLabels = Array.from({ length: xCount }, (_, i) => {
    const index = Math.round((i * (points.length - 1)) / (xCount - 1))
    return points[index].time
  })
  const gridLines = Array.from({ length: ticks }, (_, i) => (i * height) / (ticks - 1))

  function onMove(event: MouseEvent<HTMLDivElement>) {
    const rect = plotRef.current?.getBoundingClientRect()
    if (!rect) {
      return
    }
    const ratio = (event.clientX - rect.left) / rect.width
    const index = Math.max(0, Math.min(points.length - 1, Math.round(ratio * (points.length - 1))))
    setHover(index)
  }

  const hoverPoint = hover !== null ? points[hover] : null
  const hoverLeft = hover !== null ? (hover / (points.length - 1)) * 100 : 0

  return (
    <div className="chart-wrap">
      <div className="chart-y">
        {yLabels.map((value, index) => (
          <span key={index}>${money(value)}</span>
        ))}
      </div>
      <div className="chart-body">
        <div
          className="chart-plot"
          ref={plotRef}
          onMouseMove={onMove}
          onMouseLeave={() => setHover(null)}
        >
          <svg className="line-chart" viewBox={`0 0 ${width} ${height}`} preserveAspectRatio="none" role="img">
            <defs>
              <linearGradient id="chart-fill" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stopColor={stroke} stopOpacity="0.25" />
                <stop offset="100%" stopColor={stroke} stopOpacity="0" />
              </linearGradient>
            </defs>
            {gridLines.map((y, index) => (
              <line key={index} x1="0" y1={y} x2={width} y2={y} stroke="var(--border)" strokeWidth="0.5" />
            ))}
            <path d={area} fill="url(#chart-fill)" />
            <path
              d={line}
              fill="none"
              stroke={stroke}
              strokeWidth="2"
              strokeLinejoin="round"
              strokeLinecap="round"
            />
            {hover !== null && (
              <>
                <line
                  x1={coords[hover].x}
                  y1="0"
                  x2={coords[hover].x}
                  y2={height}
                  stroke="var(--accent)"
                  strokeWidth="1"
                />
                <circle cx={coords[hover].x} cy={coords[hover].y} r="3.5" fill="var(--accent)" />
              </>
            )}
          </svg>
          {hoverPoint && (
            <div className="chart-tip" style={{ left: `${hoverLeft}%` }}>
              <strong>${money(hoverPoint.price)}</strong>
              <span>{formatTime(hoverPoint.time, range)}</span>
            </div>
          )}
        </div>
        <div className="chart-x">
          {xLabels.map((time, index) => (
            <span key={index}>{formatTime(time, range)}</span>
          ))}
        </div>
      </div>
    </div>
  )
}
