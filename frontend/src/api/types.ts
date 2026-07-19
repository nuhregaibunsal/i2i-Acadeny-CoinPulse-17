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
  avgBuyPrice: number
  profitLoss: number
  profitLossPct: number
}

export interface PortfolioResponse {
  cashBalance: number
  holdingsValue: number
  totalProfitLoss: number
  holdings: HoldingView[]
}

export interface AiQueryResponse {
  answer: string
}

export interface PricePoint {
  price: number
  time: number
}

export type TransactionType = 'BUY' | 'SELL' | 'DEPOSIT' | 'SEND' | 'RECEIVE'

export interface TransactionRecord {
  type: TransactionType
  symbol: string | null
  volume: number | null
  price: number | null
  totalValue: number
  createdAt: string
}

export interface ConditionalOrderView {
  id: number
  symbol: string
  side: OrderSide
  direction: 'ABOVE' | 'BELOW'
  targetPrice: number
  volume: number
  status: string
  createdAt: string
}
