const SLICES = 21
const STEP = 3

export function CoinBackdrop() {
  return (
    <div className="coin3d" aria-hidden="true">
      {Array.from({ length: SLICES }).map((_, index) => (
        <div
          key={index}
          className="coin-slice"
          style={{ transform: `translateZ(${(index - (SLICES - 1) / 2) * STEP}px)` }}
        />
      ))}
      <div className="coin-face coin-front">₿</div>
      <div className="coin-face coin-back">₿</div>
    </div>
  )
}
