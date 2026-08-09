# PulseBoard — Business Requirements Document

## 1. Product overview

PulseBoard is a simple incident-management system for a small engineering team.

When a service has an operational problem—such as slow responses, outages, or failed requests—team members should be able to create an incident, track its progress, record updates, and find similar incidents later.

The product’s purpose is to provide one clear record of an incident from discovery through resolution.

## 2. Business problem

Operational issues are often tracked informally in chat messages or personal notes. This makes it difficult to answer:

- What issues are currently active?
- How severe is each issue?
- Who is handling it?
- What has been investigated or changed?
- Has a similar problem occurred before?

PulseBoard centralizes this information in a lightweight system.

## 3. Users

### Team member

A registered user who can create incidents, view incidents, search incidents, and add updates to incidents.

### Incident owner

The team member responsible for progressing a particular incident. Usually this is the person who created it.

### Administrator (optional)

A privileged user who can manage or update any incident when needed.

## 4. Functional requirements

### 4.1 User access

- A user must register and log in before accessing incident information.
- A user must not be able to access protected features without being authenticated.
- The system must identify the user performing each action.

### 4.2 Create an incident

A team member must be able to create an incident with:

- A short, meaningful title
- A description of the problem
- The affected service or component
- A severity level
- An initial status
- One or more tags, if applicable

The system must record who created the incident and when.

### 4.3 View and list incidents

A team member must be able to:

- View the details of a single incident
- View a paginated list of incidents
- Filter incidents by status
- Filter incidents by severity
- See the incident owner and important timestamps

### 4.4 Update an incident

The incident owner must be able to update the incident’s details, including its title, description, severity, status, affected service, and tags.

For the first version, any authenticated user may view all incidents. Only the owner may update or resolve their own incident. An administrator may update any incident if the administrator role is implemented.

### 4.5 Incident status lifecycle

An incident must use one of the following statuses:

- `OPEN` — reported but not yet actively investigated
- `INVESTIGATING` — work is in progress
- `RESOLVED` — the issue is fixed or no longer active

A resolved incident remains visible for future reference.

### 4.6 Severity levels

An incident must use one of the following severity levels:

- `LOW` — minor impact; no urgent action
- `MEDIUM` — noticeable impact; action is needed
- `HIGH` — significant impact; urgent investigation
- `CRITICAL` — major outage or severe business impact

### 4.7 Incident timeline

A team member must be able to add progress updates to an incident.

Each update must include:

- The incident it belongs to
- The author
- The update message
- The timestamp

The incident detail view must display its timeline so a team member can understand what happened from creation to resolution.

For the first version, timeline updates are manual. Automatically recording every field or status change is not required.

### 4.8 Search

A team member must be able to search incidents using free text.

The search should consider:

- Incident title
- Incident description
- Affected service/component

The user should also be able to apply at least one filter, such as status or severity.

### 4.9 Operational visibility

The system must provide a way for the developer/operator to determine:

- Whether the application is running and ready to serve requests
- How many requests it receives
- Whether requests are failing
- How long requests take
- Whether the deployed application has resource or restart issues

## 5. Business rules

- Title, affected service, severity, and status are required when creating an incident.
- An incident cannot have an invalid severity or status.
- The creator becomes the initial owner of an incident.
- A timeline entry cannot exist without an associated incident.
- A resolved incident can be viewed and searched.
- Search is for discovery; the main incident record is the authoritative source when details conflict.
- Sensitive credentials and secrets must never be stored in source control.

## 6. Out of scope

The first release does not include:

- A user interface or mobile application
- Email, Slack, or push notifications
- Incident assignment to multiple users
- File attachments
- Comment editing or deletion
- Audit history for every field change
- Advanced reporting
- Automatic incident detection
- Integration with third-party alerting tools
- Multiple independent application services

## 7. Acceptance criteria

The first release is complete when a registered user can:

1. Log in.
2. Create an incident.
3. View, filter, and paginate incidents.
4. Update or resolve an incident they own.
5. Add and view timeline updates.
6. Search incidents using text and one filter.
7. Access the system through a deployed environment.
8. Observe basic application health and performance indicators.