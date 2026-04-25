# Branch Strategy

## Branch Model

- `main` is the stable production branch in this repository.
- If someone refers to `master`, treat it as the same long-lived release branch concept.
- `develop` stays the integration branch for day-to-day work.
- Backend changes should live in backend-scoped feature branches, and frontend changes should live in frontend-scoped feature branches.

## Branch Naming Convention

- `main` — Final production-ready code
- `develop` — Integration branch
- `feature/week1-project-setup` — repository setup and shared changes
- `feature/auth-backend` — backend auth work under `backend/`
- `feature/auth-frontend` — frontend auth work under `frontend/`
- `feature/contacts-backend` — backend contact work under `backend/`
- `feature/contacts-frontend` — frontend contact work under `frontend/`
- `feature/search-filter-pagination` — shared behavior spanning folders
- `feature/logging-slf4j` — logging updates in backend
- `feature/exception-handling` — backend error handling
- `feature/unit-tests` — test coverage updates across folders
- `feature/sonarqube-integration` — quality tooling and reports
- `hotfix/bug-description` — urgent production fix

## Rules

1. Never commit directly to `main`.
2. Every new feature branch starts from `develop`.
3. Open PR to `develop` when feature is done.
4. Before weekly sync-up, merge `develop` into `main`.
5. Use descriptive branch names.
6. Keep backend work in backend-scoped branches and frontend work in frontend-scoped branches whenever possible.

## Feature Flow

```bash
git checkout develop
git pull origin develop
git checkout -b feature/auth-backend

git add .
git commit -m "feat(auth): add user registration endpoint"

git checkout develop
git merge feature/auth-backend
git push origin develop
git branch -d feature/auth-backend
```
