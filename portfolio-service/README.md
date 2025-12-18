# Portfolio Service

## Responsibility
Manages properties, mortgages, and investment details for PropertyIQ platform.

## Domain
- **Property**: purchase price, dates, current value, location
- **Mortgage**: lender, rate, term, monthly payment, loan details
- **Investment**: downpayment, closing costs, equity tracking

## Main APIs
- `POST /api/properties` - Create new property
- `GET /api/properties` - List all properties for user
- `GET /api/properties/{id}` - Get property details
- `PUT /api/properties/{id}` - Update property
- `PATCH /api/properties/{id}/value` - Update current valuation
- `GET /api/portfolio/summary` - Portfolio-level aggregates

## Database
- Schema: `portfolio_db`
- Tables:
  - `properties`
  - `mortgages`
  - `investments`

## Port
8082
