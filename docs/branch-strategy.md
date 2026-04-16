# Branch Strategy

## Branch Naming Convention

- `main` — Final production-ready code
- `develop` — Integration branch
- `feature/week1-project-setup`
- `feature/auth-backend`
- `feature/auth-frontend`
- `feature/contacts-backend`
- `feature/contacts-frontend`
- `feature/search-filter-pagination`
- `feature/logging-slf4j`
- `feature/exception-handling`
- `feature/unit-tests`
- `feature/sonarqube-integration`
- `hotfix/bug-description`

## Rules

1. Never commit directly to `main`.
2. Every new feature branch starts from `develop`.
3. Open PR to `develop` when feature is done.
4. Before weekly sync-up, merge `develop` into `main`.
5. Use descriptive branch names.

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
