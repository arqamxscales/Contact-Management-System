
<img width="363" height="139" alt="download" src="https://github.com/user-attachments/assets/ab5df551-3569-4181-8145-b699464c5ee0" />




# Contact Management System

A full-stack web application for managing contacts efficiently.  
Built during the 10 Pearls Internship Program.

## Tech Stack

**Backend:** Java, Spring Boot, Spring Data JPA & Hibernate, SLF4J & Logback, JUnit & Mockito  
**Frontend:** React.js  
**Database:** SQL Server  
**Tools:** SonarQube, Git

## Features

- User Registration & Login (Email/Phone)
- JWT Authentication & Authorization
- Contact CRUD Operations (Create, Read, Update, Delete)
- Paginated Contact List with Search & Filter
- Application-wide Logging (SLF4J/Logback)
- Global Exception Handling
- Unit Tests (Controllers, Services, Repositories)

## Project Structure

See folder structure in repository root.

## Setup Instructions

Comprehensive setup instructions are available in [SETUP.md](./SETUP.md).

### Quick Start
1. Clone the repository
2. Set up environment files (`.env`, `application-dev.properties`)
3. Configure SQL Server database
4. Run backend: `mvn spring-boot:run`
5. Run frontend: `npm run dev`

## Git Workflow

This project uses a feature-branch workflow:
- **main**: Production releases
- **develop**: Integration branch for features
- **feature/\***: Feature branches

See [branch-strategy.md](./docs/branch-strategy.md) for detailed guidelines.

## Security

All sensitive files are protected via `.gitignore`:
- Environment variables (`.env`, `.env.local`)
- Development configuration (`application-dev.properties`)
- API keys and secrets

Use example files (`.env.example`, `*.properties.example`) as templates for local setup.

## Author

Mohammad Arqam Javed — 10 Pearls Internship Program 2026
