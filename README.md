Hospital Management System

A full-stack hospital management application built with Spring Boot, PostgreSQL, React, and JWT authentication. The system provides role-based access for administrators, doctors, and patients to manage hospital profiles and appointments.

Features

JWT-based authentication

BCrypt password hashing

Role-based authorization

Admin, Doctor, and Patient roles

Doctor management

Patient management

Appointment scheduling

Appointment status updates

Patient and doctor profiles

React dashboard

PostgreSQL persistence

Dockerized deployment

GitHub Actions CI/CD

Tech Stack

Backend

Java 17

Spring Boot 4

Spring Security

Spring Data JPA / Hibernate

PostgreSQL

JJWT

Lombok

Maven

Frontend

React 18

React Router

Axios

Tailwind CSS

Recharts

Vite

DevOps

Docker

Docker Compose

GitHub Actions

AWS EC2

Architecture

React Frontend
      |
      | REST API + JWT
      v
Spring Boot Backend
      |
      | JPA / Hibernate
      v
PostgreSQL

Project Structure

HospitalManagementSystem/
├── backend/
├── frontend/
├── docker-compose.yml
└── .github/workflows/

Deployment

A GitHub Actions workflow is included for building the frontend and deploying the application to an AWS EC2 instance using Docker Compose.

Future Improvements

Improved exception handling

API documentation with Swagger/OpenAPI

Automated integration tests

Externalized production secrets

Enhanced deployment monitoring

License

This project does not currently declare a specific license.
