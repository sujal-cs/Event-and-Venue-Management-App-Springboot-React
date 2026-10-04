# Event and Venue Management Platform

A full-stack project built with Spring Boot, PostgreSQL in Docker, and React with a dark theme.

## Features

- Venue and event management
- Automatic venue-overlap validation
- Admin account for creating events
- Student registration for events
- Responsive dark UI

## Tech stack

- Backend: Java 17 + Spring Boot 3
- Database: PostgreSQL running in Docker
- Frontend: React + Vite

## Start the database

```bash
docker compose up -d
```

The PostgreSQL container is exposed on port 5433 to avoid conflicts with any existing local database installation.

## Start the backend

```bash
cd backend
mvn spring-boot:run
```

The API is configured to run on port 8081 to avoid conflicts with other local services.

## Start the frontend

```bash
cd frontend
npm install
npm run dev
```

## Default accounts

- Admin: admin@college.test / admin123
- Student: student@college.test / student123

## Key behavior

The backend checks whether a venue already has an event overlapping in time before creating a new event. If the time range overlaps, the request is rejected automatically.
