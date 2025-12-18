# Expense Service

## Responsibility
Manages operational expenses for properties including mortgage interest, council tax, insurance, repairs, cleaning, utilities, and more.

## Features
- Track all property-related expenses
- Categorize expenses
- Filter by date range and category
- Support for receipt storage (future)
- OCR data extraction (future)

## Main APIs
- `POST /api/properties/{id}/expenses` - Create new expense
- `GET /api/properties/{id}/expenses?from=&to=&category=` - List expenses with filters
- `PUT /api/expenses/{id}` - Update expense
- `DELETE /api/expenses/{id}` - Delete expense

## Database
- Schema: `expense_db`
- Tables:
  - `expenses` - Main expense records
  - `expense_metadata` - Receipt URLs, OCR data

## Expense Categories
- Mortgage Interest
- Council Tax
- Insurance
- Repairs & Maintenance
- Cleaning
- Utilities
- Property Management Fees
- Legal & Professional Fees

## Port
8083
