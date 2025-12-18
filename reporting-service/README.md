# Reporting Service

## Responsibility
Generates tax summaries, annual return reports, and persists them for viewing and downloading.

## Report Types

### Tax Summary Report
- Property income and expenses by tax year
- Deductible expenses breakdown
- Mortgage interest paid
- Capital allowances
- Net taxable profit/loss

### Annual Returns Report
- Multi-year performance analysis
- Year-over-year comparisons
- ROI trends
- Cashflow history
- Appreciation tracking

## Main APIs
- `POST /api/reports/tax-summary/{propertyId}?taxYear=2024` - Generate tax summary
- `POST /api/reports/annual-returns/{propertyId}?years=5` - Generate annual returns
- `GET /api/reports?propertyId=&type=&year=` - List reports with filters
- `GET /api/reports/{id}` - Get specific report

## Database
- Schema: `reporting_db`
- Tables:
  - `reports` - Persisted report metadata and payload

## Data Sources
- Fetches data from Portfolio Service
- Fetches data from Expense Service
- Fetches data from Analytics Service

## Output Formats
- JSON (default)
- PDF (future enhancement)
- Excel/CSV (future enhancement)

## Architecture
- Stateful service (persists reports)
- Async job processing for heavy reports (future: Kafka)
- Report caching for faster retrieval

## Port
8085
