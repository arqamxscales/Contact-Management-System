# Setup Guide - Contact Management System

## Prerequisites
- Java 11+
- Maven 3.6+
- Node.js 16+
- SQL Server 2019+
- Git

## Backend Setup

### 1. Environment Configuration
Copy the example configuration files and update them with your values:

```bash
cp backend/src/main/resources/application.properties.example backend/src/main/resources/application.properties
cp backend/src/main/resources/application-dev.properties.example backend/src/main/resources/application-dev.properties
```

### 2. Environment Variables
Create a `.env` file in the `backend/` directory:

```bash
cp backend/.env.example backend/.env
```

Update `backend/.env` with your actual credentials:
```
DB_URL=jdbc:sqlserver://your-server:1433;databaseName=contact_db;encrypt=true;trustServerCertificate=true
DB_USERNAME=your_username
DB_PASSWORD=your_strong_password
JWT_SECRET=your_long_secure_secret_key_here
JWT_EXPIRATION_MS=86400000
```

### 3. Build Backend
```bash
cd backend
mvn clean install
```

### 4. Run Backend
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

Backend will run on `http://localhost:8080`

## Frontend Setup

### 1. Environment Configuration
```bash
cp frontend/.env.example frontend/.env.local
```

Update `frontend/.env.local`:
```
VITE_API_BASE_URL=http://localhost:8080/api
```

### 2. Install Dependencies
```bash
cd frontend
npm install
```

### 3. Run Development Server
```bash
npm run dev
```

Frontend will be available at `http://localhost:5173`

## Database Setup

### 1. Create Database
```sql
CREATE DATABASE contact_db;
```

### 2. Configure Connection
Update the `DB_URL` environment variable with your SQL Server connection string.

### 3. Migrations
The application uses Hibernate JPA with `ddl-auto=update`. Schemas will be created automatically on first run.

## Git Workflow

### Starting a New Feature
```bash
git checkout develop
git pull origin develop
git checkout -b feature/your-feature-name
```

### Committing Changes
```bash
git add .
git commit -m "feat(scope): description"
```

### Finishing a Feature
```bash
git checkout develop
git merge feature/your-feature-name
git push origin develop
git branch -d feature/your-feature-name
```

## Security Notes

⚠️ **IMPORTANT**: Never commit `.env` files or configuration files with actual secrets.

All sensitive files are gitignored:
- `.env` and `.env.local`
- `application-dev.properties`
- `*.key`, `*.pem`, `*.crt`

Always use example files (`.env.example`, `application-dev.properties.example`) for setup guidance.

## Troubleshooting

### Backend Connection Issues
- Ensure SQL Server is running
- Check connection string in `.env`
- Verify database exists

### Frontend CORS Issues
- Ensure backend is running on `http://localhost:8080`
- Check `VITE_API_BASE_URL` in `.env.local`

### Maven Build Failures
- Clear Maven cache: `mvn clean`
- Check Java version: `java -version`
- Verify all dependencies: `mvn dependency:tree`

## Support
For issues, check the documentation in `/docs` or create an issue on GitHub.
