# Discover Personal Loans – Pre-Qualification Application Specification

## Document Information

| Property         | Value                                           |
| ---------------- | ----------------------------------------------- |
| Product          | Discover Personal Loans                         |
| Feature          | Loan Pre-Qualification Application              |
| Version          | 1.0                                             |
| Type             | Functional Requirements Specification           |
| Application Flow | Personal Info → Employment Info → Get Qualified |

---

# 1. Overview

The Discover Personal Loan Pre-Qualification experience enables prospective borrowers to determine their eligibility for a personal loan through a multi-step application process.

The application collects:

* Loan request information
* Personal information
* Employment details
* Financial information
* Identity verification information

The process performs a soft credit inquiry and allows users to review available loan offers without impacting their credit score.

---

# 2. Goals

## Business Goals

* Increase loan application completion rates.
* Capture qualified borrower leads.
* Provide rapid qualification decisions.
* Support invitation-based marketing campaigns.

## User Goals

* Check loan eligibility.
* View estimated monthly payments.
* Obtain loan offers without affecting credit score.
* Complete the process in a few minutes.

---

# 3. Application Flow

```text
Step 1: Personal Info
    ├── Personal Invitation ID (Optional)
    ├── Loan Information
    ├── Personal Information
    ├── Electronic Consent
    └── Continue

Step 2: Employment Info
    ├── Employment Details
    ├── Income Information
    ├── Housing Information
    ├── Identity Verification
    └── Continue

Step 3: Get Qualified
    ├── Qualification Review
    └── Offer Results
```

---

# 4. Global Layout

## Header

### Branding

Display Discover Personal Loans branding.

### Customer Support

Display support phone number and operating hours.

Example:

```text
Questions? Call Us
1-800-975-0413

Mon–Fri 8am–11pm ET
Sat–Sun 9am–6pm ET
```

### Close Action

Allow users to exit the application.

---

# 5. Progress Indicator

Display application progress at the top of each screen.

## Steps

| Step | Label           |
| ---- | --------------- |
| 1    | Personal Info   |
| 2    | Employment Info |
| 3    | Get Qualified   |

## Behavior

* Current step highlighted.
* Completed steps visually indicated.
* Future steps disabled.

---

# 6. Step 1 – Personal Information

## 6.1 Personal Invitation ID

### Link

```text
Have a Personal Invitation ID #?
```

### Purpose

Allows users who received a pre-approved loan invitation to apply using their invitation identifier.

### Behavior

When selected:

1. Prompt for Invitation ID.
2. Validate invitation.
3. Associate application with offer campaign.
4. Optionally pre-populate offer information.

### Fields

| Field                  | Type | Required |
| ---------------------- | ---- | -------- |
| Personal Invitation ID | Text | Yes      |

---

## 6.2 Qualification Message

Display informational message:

```text
See if you qualify in minutes

No commitment — see your rate and monthly payment with no impact to your credit score.
```

---

## 6.3 Loan Information

### Loan Amount

| Property | Value    |
| -------- | -------- |
| Type     | Currency |
| Required | Yes      |

### Loan Purpose

| Property | Value    |
| -------- | -------- |
| Type     | Dropdown |
| Required | Yes      |

### Loan Term

User must select exactly one term.

Available options:

```text
36
48
60
72
84
```

Units:

```text
Months
```

---

## 6.4 Monthly Payment Estimator

### Action

```text
Estimate Your Monthly Payment
```

### Behavior

Calculate estimated payment using:

* Loan amount
* Loan term
* Loan purpose

---

## 6.5 APR Disclosure

Display disclosure text explaining:

* APR range
* Loan limits
* Example payment calculation
* Rate determination factors

### Example Constraints

| Property       | Value        |
| -------------- | ------------ |
| Minimum Amount | $2,500       |
| Maximum Amount | $40,000      |
| Minimum APR    | 6.99%        |
| Maximum APR    | 24.99%       |
| Terms          | 36–84 Months |

---

## 6.6 Personal Details

### Name Information

| Field          | Type     | Required |
| -------------- | -------- | -------- |
| First Name     | Text     | Yes      |
| Middle Initial | Text     | No       |
| Last Name      | Text     | Yes      |
| Suffix         | Dropdown | No       |

---

## 6.7 Address Information

| Field             | Type     | Required |
| ----------------- | -------- | -------- |
| Street Address    | Text     | Yes      |
| Apartment / Suite | Text     | No       |
| City              | Text     | Yes      |
| State             | Dropdown | Yes      |
| ZIP Code          | Text     | Yes      |

---

## 6.8 Contact Information

| Field                  | Type  | Required |
| ---------------------- | ----- | -------- |
| Preferred Phone Number | Phone |          |
| Email Address          | Email |          |

Required:

```text
Both fields required.
```

---

## 6.9 Electronic Consent

### Checkbox

Required checkbox acknowledging:

* Electronic records consent
* Electronic signatures consent
* Active email account availability
* Ability to access PDF documents
* Consent to receive electronic communications

### Required

```yaml
required: true
```

---

## 6.10 Legal Notices

Display:

### Consent to Contact

User agrees Discover may:

* Call applicant
* Send text messages
* Use automated dialing systems
* Leave prerecorded messages

### Privacy Notice

Display link to:

```text
Discover Personal Loans Consumer Privacy Notice
```

### Terms and Conditions

Display link to:

```text
Terms and Conditions
```

---

## 6.11 Continue Button

```text
Continue
```

### Behavior

Validate:

* Loan details
* Personal details
* Address information
* Contact information
* Consent acceptance

Then navigate to Employment Information.

### Footer Message

```text
This won't impact your credit score.
```

---

# 7. Step 2 – Employment Information

## 7.1 Greeting

Display dynamic greeting.

Example:

```text
Great {FirstName}, almost there
```

---

## 7.2 Financial Information

### Employment Type

| Property | Value    |
| -------- | -------- |
| Type     | Dropdown |
| Required | Yes      |

### Employer Name

| Property | Value |
| -------- | ----- |
| Type     | Text  |
| Required | Yes   |

### Occupation

| Property | Value    |
| -------- | -------- |
| Type     | Dropdown |
| Required | Yes      |

---

## 7.3 Income Information

### Total Annual Income

| Property | Value |
| -------- | ----- |
| Required | Yes   |

### Additional Household Income

| Property | Value |
| -------- | ----- |
| Required | No    |

### Monthly Housing Payment

| Property | Value |
| -------- | ----- |
| Required | Yes   |

---

## 7.4 Identity Verification

### Social Security Number

| Property | Value  |
| -------- | ------ |
| Type     | Masked |
| Required | Yes    |

### Date of Birth

| Property | Value |
| -------- | ----- |
| Type     | Date  |
| Required | Yes   |

### Citizenship

| Property | Value    |
| -------- | -------- |
| Type     | Dropdown |
| Required | Yes      |

---

## 7.5 SSN Help

Link:

```text
Why do we ask for SSN?
```

### Behavior

Display explanation that SSN is used for:

* Identity verification
* Credit qualification review
* Fraud prevention

---

## 7.6 Loan Restrictions Notice

Display:

```text
A Discover personal loan is intended for personal use and cannot be used to directly pay any Capital One account, secured loan, or post-secondary education expense.
```

---

## 7.7 Electronic Signature Disclosure

Display acknowledgment that:

* User reviewed loan agreement information.
* Discover may access credit reporting agencies.
* Electronic signature is legally binding.

---

## 7.8 Continue Button

### Behavior

Validate:

* Employment information
* Income information
* Housing payment
* SSN
* Date of birth
* Citizenship

Submit application for qualification review.

---

# 8. Step 3 – Qualification Results

## Purpose

Present qualification outcome.

## Possible States

### Approved

Display:

* Approved amount
* APR
* Loan term
* Estimated payment

### Conditionally Approved

Display:

* Additional requirements
* Required documentation

### More Information Required

Display:

* Missing information
* Next actions

### Declined

Display:

* Decision notification
* Alternative options if applicable

---

# 9. Validation Rules

## Loan Amount

```yaml
required: true
type: currency
minimum: 2500
maximum: 40000
```

## First Name

```yaml
required: true
maxLength: 50
```

## Last Name

```yaml
required: true
maxLength: 50
```

## Email

```yaml
required: true
format: email
```

## Phone Number

```yaml
required: true
format: us_phone
```

## ZIP Code

```yaml
required: true
length: 5
```

## SSN

```yaml
required: true
format: xxx-xx-xxxx
```

## Date of Birth

```yaml
required: true
minimumAge: 18
```

## Annual Income

```yaml
required: true
numeric: true
greaterThan: 0
```

---

# 10. Data Model

```typescript
interface LoanApplication {
  invitationId?: string;

  loan: {
    amount: number;
    purpose: string;
    termMonths: number;
  };

  applicant: {
    firstName: string;
    middleInitial?: string;
    lastName: string;
    suffix?: string;

    address: {
      street: string;
      apartment?: string;
      city: string;
      state: string;
      zipCode: string;
    };

    phoneNumber: string;
    email: string;
  };

  consent: {
    electronicConsentAccepted: boolean;
  };

  employment: {
    employmentType: string;
    employerName: string;
    occupation: string;
  };

  financial: {
    annualIncome: number;
    additionalIncome?: number;
    monthlyHousingPayment: number;
  };

  identity: {
    ssn: string;
    dateOfBirth: string;
    citizenship: string;
  };
}
```

---

# 11. Security Requirements

* Encrypt all PII at rest.
* Encrypt all requests via TLS.
* Mask SSN inputs.
* Implement audit logging.
* Protect against fraud and identity theft.
* Comply with lending regulations.

---

# 12. Accessibility Requirements

* WCAG 2.1 AA compliance.
* Full keyboard navigation.
* Screen-reader compatibility.
* Accessible labels and error messages.
* Focus management between steps.

---

# 13. Performance Requirements

| Metric                   | Target      |
| ------------------------ | ----------- |
| Initial Load             | < 2 seconds |
| Validation Response      | < 200ms     |
| Step Transition          | < 500ms     |
| Qualification Submission | < 3 seconds |

---

# 14. Acceptance Criteria

## Scenario: Complete Personal Information

```gherkin
Given all required personal information is provided
And electronic consent is accepted
When Continue is clicked
Then Employment Information is displayed
```

## Scenario: Invalid Email

```gherkin
Given an invalid email address
When Continue is clicked
Then an email validation error is displayed
```

## Scenario: Missing Consent

```gherkin
Given consent is not accepted
When Continue is clicked
Then submission is blocked
```

## Scenario: Complete Employment Information

```gherkin
Given all employment and identity information is valid
When Continue is clicked
Then the application is submitted
And qualification review begins
```

## Scenario: Invalid SSN

```gherkin
Given an invalid SSN
When submission is attempted
Then submission is blocked
And an error message is displayed
```
