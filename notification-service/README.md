# Notification Service

## Responsibility
Handles email notifications, alerts, and reminders for PropertyIQ platform.

## Notification Types

### Email Notifications
- Report generation completion
- Tax filing reminders
- Mortgage refinance opportunities
- High-rate mortgage alerts
- Monthly portfolio summaries
- Expense receipt confirmations

### Alert Types
- Property value changes
- Unusual expense patterns
- ROI threshold alerts
- Maintenance reminders
- Insurance renewal notifications

## Main APIs
- `POST /api/notifications/email` - Send email (internal use)
- `POST /api/notifications/alert` - Send alert notification

## Event Subscriptions (Future)
- `ReportGenerated` event
- `HighRateMortgageDetected` event
- `ExpenseThresholdExceeded` event
- `PropertyValueUpdated` event

## Configuration
Requires environment variables:
- `MAIL_USERNAME` - Email service username
- `MAIL_PASSWORD` - Email service password

## Architecture
- Stateless service
- Event-driven (future: Kafka/RabbitMQ)
- Template-based emails
- Support for multiple notification channels (email, SMS, push)

## Port
8086

## Notes
- This service is optional for initial MVP
- Can start with simple cron jobs or manual triggers
- Full event-driven architecture to be implemented in later phases
