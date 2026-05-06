# API Endpoints (Current)

Base URL: `/api`

This document tracks the endpoints used by the frontend and backend in the current internship scope.

## Auth

- `POST /auth/register`
	- Creates a new user account.
- `POST /auth/login`
	- Logs user in and returns auth payload.
- `POST /auth/refresh`
	- Refreshes access token using refresh token.
- `POST /auth/logout-all`
	- Revokes all active user sessions/tokens.
- `GET /auth/profile/{userId}`
	- Returns profile details for the authenticated user.
- `POST /auth/profile/{userId}/change-password`
	- Changes user password.

## Contacts

- `GET /contacts`
	- List contacts (optional `search`).
- `GET /contacts/paged`
	- Paginated contacts list.
	- Query params used by UI:
		- `page`
		- `size`
		- `search`
		- `emailLabel`
		- `phoneLabel`
- `GET /contacts/{id}`
	- Get contact by ID.
- `POST /contacts`
	- Create contact.
- `PUT /contacts/{id}`
	- Update contact.
- `DELETE /contacts/{id}`
	- Delete single contact.
- `POST /contacts/batch-delete`
	- Delete multiple contacts with payload `{ contactIds: number[], requireConfirmation: boolean }`.
- `POST /contacts/export`
	- Export selected contacts as CSV (binary/blob response).

## Branching Note (PR History Discipline)

To keep a clean 8-week internship history:

1. `main` stays stable and release-ready.
2. `develop` is the integration branch.
3. Daily work is done on feature branches (for example `feature/frontend/contacts`).
4. Keep commits focused (2-3 per day as planned), and include tests in the same feature scope.
