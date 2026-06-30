# Persistence Model

# Pricing Orchestration Service

Version: 1.0

---

## Database

PostgreSQL

## Migration

Flyway

## Schema

`pricing_orchestration`

---

# Design Note

The pricing-orchestration-service is primarily event-driven. Its persistence needs are focused on storing pricing offers returned by the Decision Platform so the UI can retrieve them, and storing the selected offer reference for the hard pull and final decision steps.

---

# Tables (Derived from Implementation)

## Table: pricing_offer

Stores each offer returned by the Decision Platform Pricing Engine for an application.

```sql
CREATE TABLE pricing_offer
(
    pricing_offer_id     UUID            PRIMARY KEY,
    application_id       UUID            NOT NULL,
    approved_amount      DECIMAL(12,2)   NOT NULL,
    interest_rate        DECIMAL(5,4)    NOT NULL,
    apr                  DECIMAL(5,4)    NOT NULL,
    term_months          INTEGER         NOT NULL,
    monthly_repayment    DECIMAL(12,2)   NOT NULL,
    total_repayable      DECIMAL(12,2)   NOT NULL,
    offer_expiry_date    TIMESTAMP,
    pricing_model_ref    VARCHAR(100),
    bureau_snapshot_ref  VARCHAR(100),
    selected             BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at           TIMESTAMP       NOT NULL
);
```

`selected` is set to `true` when the applicant confirms the offer via `ConsentCapturedEvent`.

---

# Indexes

## pricing_offer

```
IDX_PRICING_OFFER_APPLICATION_ID    ON pricing_offer (application_id)
IDX_PRICING_OFFER_SELECTED          ON pricing_offer (application_id, selected)
```

---

# Data Retention

| Table | Retention | Reason |
|-------|-----------|--------|
| `pricing_offer` | 7 years | Audit trail of offers presented |

---

# External State

The service reads application data (applicant reference, loan request details) from `application-management-service` when assembling the `PricingRequest`. It does not duplicate applicant or loan request data in its own schema.
