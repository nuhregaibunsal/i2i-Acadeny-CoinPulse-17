export type OrderSide = 'BUY' | 'SELL'

export interface RegisterResponse {
  username: string
  startingBalance: number
}

export interface LoginResponse {
  token: string
  username: string
}

export interface AuthenticatedUser {
  userId: number
  username: string
}

export interface CryptoPrice {
  symbol: string
  price: number
}

export interface OrderRequest {
  symbol: string
  side: OrderSide
  volume: number
}

export interface OrderResponse {
  symbol: string
  side: OrderSide
  volume: number
  price: number
  totalValue: number
  cashBalance: number
}

export interface HoldingView {
  symbol: string
  volume: number
  currentPrice: number
  value: number
}

export interface PortfolioResponse {
  cashBalance: number
  holdingsValue: number
  holdings: HoldingView[]
}

export interface AiQueryResponse {
  answer: string
}
