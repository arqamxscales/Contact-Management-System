# Start new feature
git checkout develop
git pull origin develop
git checkout -b feature/auth-backend

# During work
git add .
git commit -m "feat(auth): add user registration endpoint"

# When feature done
git checkout develop
git merge feature/auth-backend
git push origin develop
git branch -d feature/auth-backend# API Endpoints (Initial Draft)

Base URL: `/api`

## Auth
- `POST /auth/register`
- `POST /auth/login`

## Contacts
- `GET /contacts`
- `GET /contacts/{id}`
- `POST /contacts`
- `PUT /contacts/{id}`
- `DELETE /contacts/{id}`
