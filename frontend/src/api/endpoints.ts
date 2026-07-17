import { request } from './client'
import type {
  AiQueryResponse,
  AuthenticatedUser,
  CryptoPrice,
  LoginResponse,
  OrderRequest,
  OrderResponse,
  PortfolioResponse,
  RegisterResponse,
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

export function fetchPrices(): Promise<CryptoPrice[]> {
  return request<CryptoPrice[]>('/market/prices')
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

export function askAi(question: string): Promise<AiQueryResponse> {
  return request<AiQueryResponse>('/ai/query', {
    method: 'POST',
    body: JSON.stringify({ question }),
  })
}
