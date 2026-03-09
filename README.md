# DentaLink — Dental Appointment and Payment Management System

DentaLink is a full-stack dental clinic management system designed to streamline appointment scheduling, service management, and online payment processing for a single dental clinic. The system allows patients to register, browse available dental services, book appointments with dentists, and complete payments securely using the PayMongo payment gateway.

The platform also provides administrators with management tools for services, dentists, appointments, and payment monitoring through a role-based dashboard.

The system supports both web and mobile platforms, which communicate with a centralized Spring Boot backend API.

---

# Technology Stack

| Layer | Technology | Version |
|------|-------------|--------|
| Backend Framework | Spring Boot | 3.x |
| Backend Language | Java | 21 |
| Security | Spring Security, JWT, BCrypt | — |
| ORM | Spring Data JPA / Hibernate | 6.x |
| Database | PostgreSQL | 14+ |
| Web Frontend | React | 18 |
| HTTP Client | Axios | — |
| Mobile Development | Kotlin (Android) | API Level 34 |
| Mobile Networking | Retrofit | — |
| Payment Gateway | PayMongo | Sandbox |
| Maps Integration | Google Maps JavaScript API | — |
| Email Service | SMTP (JavaMailSender) | — |
| Build Tool | Maven | — |

---

# Project Structure

```
IT342_DentaLink_G2_Riconalla/
├── backend/        Spring Boot backend API
├── web/            React web application
├── mobile/         Android mobile application
├── docs/           System design documents (SDD, ERD, diagrams, wireframes)
└── README.md
```

---

# System Overview

DentaLink provides a centralized digital platform for managing dental clinic appointments and payments.

Patients can:

- Register an account using email/password or Google OAuth
- Log in securely using JWT authentication
- Browse available dental services
- Book appointments with dentists
- Complete payments through PayMongo
- Receive email confirmations after successful payment
- View their appointment history
- Manage profile information

Administrators can:

- Manage dental services
- Upload service images
- Manage dentist records
- Monitor appointments and payment records
- View operational statistics in the admin dashboard

All clients communicate with a centralized backend API that manages authentication, business logic, and database operations.

---

# Core System Features

## Authentication

- Patient registration and login
- JWT-based authentication
- BCrypt password hashing
- Google OAuth login support
- Role-based access control (ADMIN and PATIENT)

---

## Service Management (Admin)

Administrators can:

- Create dental services
- Update service information
- Upload service images
- Delete services

Services are publicly visible to patients.

---

## Dentist Management (Admin)

Administrators can:

- Add dentist records
- Update dentist information
- Set dentist status (Active or Inactive)
- Delete dentist records

Dentists do not have login accounts and are used only for appointment assignment.

---

## Appointment Booking

Patients can book appointments by selecting:

- A dental service
- A dentist
- A date and time

The system prevents double-booking using a database constraint.

Appointment lifecycle:

```
PENDING_PAYMENT → CONFIRMED → COMPLETED / CANCELLED
```

Appointments are confirmed automatically after successful payment verification.

---

## Payment Integration

Payments are processed using **PayMongo Sandbox**.

Payment flow:

1. Patient books an appointment.
2. Backend creates a PayMongo payment intent.
3. Patient is redirected to PayMongo checkout.
4. PayMongo sends a webhook upon successful payment.
5. Backend verifies the webhook and updates the appointment status.
6. Confirmation email is sent to the patient.

Administrators cannot manually confirm payments.

---

## Email Notifications

The system sends automated emails using SMTP for:

- Patient registration confirmation
- Appointment confirmation after payment

---

## Google Maps Integration

The web application displays the clinic location using Google Maps.

The mobile application opens navigation using Google Maps intent.

---

## File Upload

The system supports file uploads for:

- Service images (uploaded by administrators)
- Patient profile pictures

Restrictions:

- JPEG or PNG only
- File size validation enforced

---

# API Overview

All backend endpoints follow this base path:

```
/api/v1
```

---

## Authentication Endpoints

| Method | Endpoint | Description |
|------|-----------|------------|
| POST | `/auth/register` | Register a new patient |
| POST | `/auth/login` | Login using email and password |
| POST | `/auth/google` | Google OAuth login |
| GET | `/auth/me` | Retrieve authenticated user profile |
| POST | `/auth/logout` | Logout user |

---

## Service Endpoints

| Method | Endpoint | Description |
|------|-----------|------------|
| GET | `/services` | List all dental services |
| GET | `/services/{id}` | Retrieve a single service |
| POST | `/services` | Create a new service (Admin) |
| PUT | `/services/{id}` | Update service information (Admin) |
| DELETE | `/services/{id}` | Delete service (Admin) |
| POST | `/services/{id}/upload-image` | Upload service image |

---

## Dentist Endpoints

| Method | Endpoint | Description |
|------|-----------|------------|
| GET | `/dentists` | Retrieve all dentist records |
| GET | `/dentists/{id}` | Retrieve a single dentist |
| POST | `/dentists` | Add dentist record (Admin) |
| PUT | `/dentists/{id}` | Update dentist information (Admin) |
| DELETE | `/dentists/{id}` | Delete dentist record (Admin) |

---

## Appointment Endpoints

| Method | Endpoint | Description |
|------|-----------|------------|
| POST | `/appointments` | Book an appointment |
| GET | `/appointments` | Retrieve appointments |
| GET | `/appointments/{id}` | Retrieve appointment details |
| PUT | `/appointments/{id}/status` | Update appointment status (Admin) |

---

## Payment Endpoints

| Method | Endpoint | Description |
|------|-----------|------------|
| POST | `/payments/create-intent` | Create PayMongo payment intent |
| POST | `/payments/webhook` | PayMongo webhook handler |
| GET | `/payments` | Retrieve payment records (Admin) |

---

# Database Design

The system database consists of five core tables:

- `user`
- `dentist`
- `service`
- `appointment`
- `payment`

Key relationships include:

- A **user** can have many appointments.
- A **service** can be referenced by multiple appointments.
- A **dentist** can handle multiple appointments.
- Each **appointment** can have one corresponding payment record.

A database constraint prevents dentists from being double-booked:

```
UNIQUE (dentist_id, appointment_datetime)
```

---

# Setup Instructions

## Prerequisites

- Java 21
- Maven
- Node.js and npm
- PostgreSQL database
- Android Studio (for mobile development)

---

# Backend Setup

Navigate to the backend directory:

```
cd backend
```

Configure database connection in:

```
src/main/resources/application.properties
```

Example configuration:

```
spring.datasource.url=jdbc:postgresql://localhost:5432/dentalink
spring.datasource.username=postgres
spring.datasource.password=yourpassword
```

Run the backend server:

```
mvn clean install
mvn spring-boot:run
```

Backend will run at:

```
http://localhost:8080
```

---

# Web Frontend Setup

Navigate to the web directory:

```
cd web
```

Install dependencies:

```
npm install
```

Start the development server:

```
npm run dev
```

The web application will run at:

```
http://localhost:5173
```

---

# Mobile Application Setup

1. Open the `mobile` folder in Android Studio.
2. Configure the Retrofit API base URL to point to the backend server.
3. Run the application using an emulator or physical device with Android API Level 34.

---

# Deployment Architecture

| Component | Platform |
|------|-----------|
| Backend API | Render |
| Database | Supabase PostgreSQL |
| Web Frontend | Vercel |
| Mobile Application | Android APK distribution |

---

# Author

Frances Anne Briones Riconalla  
IT342 — System Integration and Architecture  
Cebu Institute of Technology – University