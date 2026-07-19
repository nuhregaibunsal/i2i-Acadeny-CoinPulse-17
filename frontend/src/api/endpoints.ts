import { request } from './client'
import type {
  AiQueryResponse,
  AuthenticatedUser,
  ConditionalOrderView,
  CryptoPrice,
  LoginResponse,
  OrderRequest,
  OrderResponse,
  OrderSide,
  PortfolioResponse,
  PricePoint,
  RegisterResponse,
  TransactionRecord,
} from './types'

export function registerUser(username: string, password: string): Promise<RegisterResponse> {
  return request<RegisterResponse>('/auth/register', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  })
}

export function login(username: string, password: string): Promise<LoginResponse> {
  return request<LoginResponse>('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  })
}

export function fetchCurrentUser(): Promise<AuthenticatedUser> {
  return request<AuthenticatedUser>('/auth/me')
}

export function changePassword(currentPassword: string, newPassword: string): Promise<void> {
  return request<void>('/auth/password', {
    method: 'POST',
    body: JSON.stringify({ currentPassword, newPassword }),
  })
}

export function fetchPrices(): Promise<CryptoPrice[]> {
  return request<CryptoPrice[]>('/market/prices')
}

export function fetchPriceHistory(symbol: string, range: string): Promise<PricePoint[]> {
  return request<PricePoint[]>(`/market/prices/${symbol}/history?range=${range}`)
}

export function fetchTransactions(): Promise<TransactionRecord[]> {
  return request<TransactionRecord[]>('/trading/transactions')
}

export function createConditionalOrder(order: {
  symbol: string
  side: OrderSide
  targetPrice: number
  volume: number
}): Promise<ConditionalOrderView> {
  return request<ConditionalOrderView>('/trading/conditional-orders', {
    method: 'POST',
    body: JSON.stringify(order),
  })
}

export function fetchConditionalOrders(): Promise<ConditionalOrderView[]> {
  return request<ConditionalOrderView[]>('/trading/conditional-orders')
}

export function cancelConditionalOrder(id: number): Promise<void> {
  return request<void>(`/trading/conditional-orders/${id}`, { method: 'DELETE' })
}

export function executeOrder(order: OrderRequest): Promise<OrderResponse> {
  return request<OrderResponse>('/trading/orders', {
    method: 'POST',
    body: JSON.stringify(order),
  })
}

export function fetchPortfolio(): Promise<PortfolioResponse> {
  return request<PortfolioResponse>('/trading/portfolio')
}

export function deposit(amount: number): Promise<{ cashBalance: number }> {
  return request<{ cashBalance: number }>('/wallet/deposit', {
    method: 'POST',
    body: JSON.stringify({ amount }),
  })
}

export function transfer(toUsername: string, amount: number): Promise<{ cashBalance: number }> {
  return request<{ cashBalance: number }>('/wallet/transfer', {
    method: 'POST',
    body: JSON.stringify({ toUsername, amount }),
  })
}

export function askAi(question: string): Promise<AiQueryResponse> {
  return request<AiQueryResponse>('/ai/query', {
    method: 'POST',
    body: JSON.stringify({ question }),
  })
}
