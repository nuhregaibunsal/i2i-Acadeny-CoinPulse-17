# CryptoPal

A real-time cryptocurrency trading and AI-insights platform. Users can monitor live
market prices, execute buy/sell orders against a simulated balance, track their
portfolio, and ask an AI assistant about their account and market trends.

## Architecture

- **CryptoPal Web App** — Single-Page Application (React + TypeScript + Vite).
- **CryptoPal Core** — Spring Boot modular monolith (auth, market, trading, ai modules).
- **External Data Provider** — local Ticker Engine generating realistic price movements.
- **Redis** — in-memory cache for session tokens and latest prices.
- **PostgreSQL** — source of truth for users, wallets, holdings, transactions, price history.
- **Google Gemini** — LLM layer for account/market insights.

## Prerequisites

- Docker & Docker Compose
- JDK 21 & Maven (backend)
- Node.js 20+ (frontend)

## Getting Started

```bash
# 1. Configure environment
cp .env.example .env      # then edit values (DB password, GEMINI_API_KEY, ...)

# 2. Start infrastructure (PostgreSQL + Redis)
docker compose up -d

# The DDL scripts in infra/db/init run automatically on first startup.
```

Backend and frontend run instructions will be added as those modules land.

## Project Structure

```
cryptopal/
├── docker-compose.yml       # postgres + redis
├── .env.example             # environment variable template
├── infra/db/init/           # DDL scripts (auto-run by postgres on init)
├── backend/                 # Spring Boot core (added in later steps)
├── frontend/                # React SPA (added in later steps)
└── e2e/                     # Selenium tests (added in later steps)
```

## Environment Variables

See [`.env.example`](.env.example) for the full list. No secrets are hardcoded;
all configuration is read from environment variables.
