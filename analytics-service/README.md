# Analytics Service

## Responsibility
Computes leveraged ROI, cashflow metrics, and portfolio-level analytics for PropertyIQ platform.

## Core Calculations

### Property-Level Metrics
- **Cash-on-Cash Return**: Annual cashflow / Cash invested
- **Appreciation Return**: (Current value - Purchase price) / Cash invested
- **Principal Paydown Return**: Mortgage principal paid / Cash invested
- **Total Leveraged ROI (Annualized)**: Combined return accounting for leverage
- **Monthly Cashflow**: Rent - Operating expenses - Interest

### Portfolio-Level Metrics
- Aggregated ROI across all properties
- Total equity position
- Diversification metrics
- Risk assessment

## Formulas
- Cash invested = Down payment + Closing costs + Other initial costs
- Annual net cashflow = Rent - Opex - Interest
- Appreciation = Current value - Purchase price
- Principal paydown = Annual principal paydown × Years held
- Total return = Cashflow total + Appreciation + Principal paydown - Cash invested
- Annualized ROI = (1 + Total return / Cash invested)^(1/Years) - 1

## Main APIs
- `GET /api/analytics/property/{id}` - Property-level analytics
- `GET /api/analytics/portfolio` - Portfolio-level analytics

## Architecture
- Stateless service
- Fetches data from Portfolio Service and Expense Service
- Uses Redis for caching computed metrics
- No persistent database

## Port
8084
