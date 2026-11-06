# Core Business Workflows

ShareHub connects community members who offer useful items with recipients looking for them. Donors publish available donations, and recipients can browse, request, and manage their own pending requests.

## Domain Entities

| Entity | Service / Bounded Context | Description | Key Relationships |
|---|---|---|---|
| User | Account and Access | A community member acting as a donor or recipient | Donors own donations; recipients make resource requests |
| Donation | Community Giving | A resource offered to the community with an availability state | Belongs to one donor; may have an image and may be referenced by requests |
| DonationImage | Community Giving | An optional image associated with a donation | Belongs to one donation |
| ResourceRequest | Recipient Requests | A recipient's request for an available donation | Links one recipient to one donation |

## Service-to-Domain Mapping

| Service | Domain Context | Owned Entities | External Dependencies |
|---|---|---|---|
| `AuthService` | Account and Access | User | `UserRepository`, Spring Security authentication, HTTP session |
| `DonationService` | Community Giving | Donation, DonationImage | `DonationRepository`, `DonationImageRepository`, `UserRepository` |
| `ResourceRequestService` | Recipient Requests | ResourceRequest | `ResourceRequestRepository`, `DonationRepository`, `UserRepository` |

The application is a single Spring Boot deployment, not a set of independently communicating services. These contexts exchange data through shared repositories and PostgreSQL. `UserRepository` is the source of truth for account role and identity; `DonationRepository` owns listing state; `ResourceRequestRepository` owns request state. There is no separate API call, event, or notification from the request workflow to the donor.

## Primary Workflows

### Workflow 1: Donor shares a resource

1. A signed-in donor opens the donation form and provides listing details, optionally including an image.
2. The web controller checks the donor role and validates the form.
3. `DonationService` creates the donation with `AVAILABLE` status and associates it with the authenticated donor.
4. If supplied, the image must be a supported JPEG, PNG, or WebP file no larger than 5 MB; image content is persisted with the donation.
5. The donation becomes visible in available-donation discovery. Donors can edit their own listings and set availability to closed; closed listings are not returned by the available-listing query.

### Workflow 2: Recipient requests a resource

1. A signed-in recipient browses available donations and submits a request for a selected donation, optionally with a message.
2. The application requires the recipient role. `ResourceRequestService` checks the message length, recipient identity, donation existence and availability, and whether that recipient already has a pending or approved request for the same donation.
3. If all checks pass, the service persists a `PENDING` request. The recipient sees the request in their dashboard.
4. If a business check fails, the web flow redirects back to the dashboard with an error instead of creating the request.

The current implementation does not give donors a request inbox or actions to approve, reject, or mark a request fulfilled. `APPROVED`, `REJECTED`, and `FULFILLED` are values in the request status enum, but this workflow does not transition requests into those states. Donor listing closure is a separate action and does not update existing request status.

### Workflow 3: Recipient cancels a pending request

1. The recipient submits a cancellation for a request shown in their dashboard.
2. The application checks that the signed-in user is the request owner and that the request remains `PENDING`.
3. A matching pending request changes to `CANCELLED`. Otherwise, the action reports that cancellation was not possible.

## Cross-Service Data Flows

No separate services or gateway aggregation are present. Web controllers call the domain services directly; services share PostgreSQL through Spring Data repositories. For recipient discovery, `DonationService` queries available donations. For a request, `ResourceRequestService` checks the recipient and donation, then writes a request referencing both. The recipient dashboard later queries requests by recipient. The current request flow does not send request data to a donor-facing workflow or notification service.

## Business Workflow Sequence

<!-- mermaid-checked: every participant uses `participant Id as "Label"`, no \n in aliases/messages/notes, every alt/opt/loop closed by end, no `:` inside any alias -->
~~~mermaid
sequenceDiagram
    participant Donor as "Donor"
    participant App as "ShareHub Web App"
    participant DonationSvc as "Donation Service"
    participant Recipient as "Recipient"
    participant RequestSvc as "Request Service"
    participant DB as "PostgreSQL"

    Donor->>App: Submit donation details and optional image
    App->>DonationSvc: Check donor role and validated form
    DonationSvc->>DB: Save AVAILABLE donation and optional image
    DB-->>DonationSvc: Donation saved
    DonationSvc-->>App: Listing created
    App-->>Donor: Show success on dashboard

    Recipient->>App: Browse available donations
    App->>DonationSvc: Find AVAILABLE listings
    DonationSvc->>DB: Query available donations
    DB-->>DonationSvc: Available listing results
    DonationSvc-->>App: Donation list
    App-->>Recipient: Show available resources

    Recipient->>App: Submit request with optional message
    App->>RequestSvc: Check recipient and request rules
    RequestSvc->>DB: Check donation, recipient, active requests
    DB-->>RequestSvc: Current account and listing state
    alt All checks pass
        RequestSvc->>DB: Save PENDING request
        DB-->>RequestSvc: Request saved
        RequestSvc-->>App: Request created
        App-->>Recipient: Show request as PENDING
    else Invalid, unavailable, or duplicate request
        RequestSvc-->>App: Return business error
        App-->>Recipient: Show error without creating request
    end

    opt Recipient cancels while request is PENDING
        Recipient->>App: Cancel own request
        App->>RequestSvc: Check request owner and status
        RequestSvc->>DB: Load request and update to CANCELLED
        DB-->>RequestSvc: Updated request
        RequestSvc-->>App: Cancellation result
        App-->>Recipient: Show updated request status
    end

    opt Donor closes a listing separately
        Donor->>App: Close own donation
        App->>DonationSvc: Check ownership and close listing
        DonationSvc->>DB: Set donation to CLOSED
        DB-->>DonationSvc: Listing updated
        DonationSvc-->>App: Availability changed
        App-->>Donor: Show listing is no longer available
    end
    Note over Donor,Recipient: Donor approval and request notifications are not implemented
~~~

## Business Rules & Decision Logic

- **Roles:** only `DONOR` accounts create or manage donation listings; only `RECIPIENT` accounts submit or cancel requests.
- **Donation availability:** new donations start `AVAILABLE`. Donors can close their own listings; discovery returns only available donations.
- **Request eligibility:** a request requires an existing available donation and an authenticated recipient.
- **Duplicate prevention:** a recipient cannot create another active request for the same donation while an earlier request is `PENDING` or `APPROVED`.
- **Request message:** optional; maximum 1000 characters.
- **Cancellation:** only the request's recipient can cancel, and only while its status is `PENDING`.
- **Image upload:** optional; JPEG, PNG, or WebP; maximum size is 5 MB.
- **State model:** donations use `AVAILABLE` and `CLOSED`. Requests define `PENDING`, `APPROVED`, `REJECTED`, `FULFILLED`, and `CANCELLED`; the current exposed workflows create `PENDING` requests and can cancel them to `CANCELLED`.
- **Transactions and errors:** donation and request writes use transactional service methods. The web request flow catches expected business-rule errors and presents them on the dashboard; no donor notification or compensating workflow is configured.
