# 🏥 Doctor Appointment Booking System

A **gRPC-based microservice** for booking doctor appointments, built with **Java**, **Micronaut**, **ScyllaDB**, and **NATS** for real-time notifications.

---

## 📦 Tech Stack

| Layer | Technology |
|---|---|
| Framework | Micronaut (Java) |
| Communication | gRPC (Protocol Buffers) |
| Database | ScyllaDB (Cassandra-compatible) |
| Messaging | NATS Pub/Sub |
| Geocoding | Photon API (photon.komoot.io) |
| Password Hashing | BCrypt |
| Build Tool | Gradle |

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│                     gRPC Clients                        │
│              (Postman / grpcurl / Mobile)               │
└────────────────────────┬────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────┐
│              BasicAuthInterceptor                       │
│         (Validates credentials on every call)           │
└────────────────────────┬────────────────────────────────┘
                         │
          ┌──────────────┼──────────────┐
          ▼              ▼              ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│   Patient    │ │    Doctor    │ │ Appointment  │
│   Service    │ │   Service    │ │   Service    │
└──────┬───────┘ └──────┬───────┘ └──────┬───────┘
       │                │                │
       └────────────────┼────────────────┘
                        ▼
             ┌─────────────────┐
             │    ScyllaDB     │
             │  (3 keyspaces)  │
             └─────────────────┘
                        │
                        ▼
             ┌─────────────────┐
             │      NATS       │
             │  Notifications  │
             └─────────────────┘
```

---

## 📁 Project Structure

```
src/main/java/com/doctorappointment/
├── auth/
│   ├── BasicAuthInterceptor.java       # gRPC auth interceptor
│   └── BasicAuthValidator.java         # Credential validation
├── patient/
│   ├── constant/                       # Schema & query constants
│   ├── dto/                            # PatientModel, PatientRequest
│   ├── grpc/                           # PatientGrpcService
│   ├── helper/                         # PatientGrpcHelper
│   ├── repository/                     # PatientRepository
│   └── service/                        # PatientService
├── doctor/
│   ├── constant/                       # Schema & query constants
│   ├── dto/                            # DoctorModel, DoctorRequest
│   ├── grpc/                           # DoctorGrpcService
│   ├── helper/                         # DoctorGrpcHelper
│   ├── repository/                     # DoctorRepository
│   └── service/
│       ├── DoctorService.java
│       └── GeocodingService.java       # Photon API integration
├── appointment/
│   ├── constant/                       # AppointmentStatus, Schema, Query
│   ├── dto/                            # AppointmentModel, AppointmentRequest
│   ├── enums/                          # CancelledAppointmentEnum
│   ├── grpc/                           # AppointmentGrpcService
│   ├── helper/                         # AppointmentGrpcHelper
│   ├── repository/                     # AppointmentRepository (3 tables)
│   └── service/
│       ├── AppointmentService.java
│       └── ValidateNewAppointment.java
├── notification/
│   ├── NotificationPublisher.java      # NATS publisher
│   ├── NotificationSubscriber.java     # NATS subscriber
│   ├── NotificationSubject.java        # Subject constants
│   └── event/
│       └── AppointmentEvent.java       # Event record
└── config/
    └── ScyllaDbConfig.java
```

---

## 🗄️ Database Schema

### ScyllaDB Tables

#### `patients`
| Column | Type | Notes |
|---|---|---|
| patient_id | uuid | Primary Key |
| first_name | text | |
| last_name | text | |
| email | text | Indexed |
| password | text | BCrypt hashed |
| phone_number | text | |
| address | text | |
| is_deleted | boolean | Soft delete |

#### `doctors`
| Column | Type | Notes |
|---|---|---|
| doctor_id | uuid | Primary Key |
| first_name | text | |
| last_name | text | |
| email | text | Indexed |
| password | text | BCrypt hashed |
| phone_number | text | |
| address | text | |
| specialization | text | |
| clinic_address | text | Area name |
| clinic_name | text | Specific clinic |
| clinic_building | text | Indexed (unique) |
| latitude | double | Auto-geocoded |
| longitude | double | Auto-geocoded |
| daily_limit | int | Default: 10 |
| is_deleted | boolean | Soft delete |

#### `appointments` (main table)
| Column | Type | Notes |
|---|---|---|
| appointment_id | uuid | Primary Key |
| patient_id | uuid | |
| doctor_id | uuid | |
| appointment_date | text | YYYY-MM-DD |
| status | text | PENDING/CONFIRMED/REJECTED/CANCELLED |
| notes | text | Patient notes |
| reason | text | Cancel/reject reason |
| cancelled_by | text | PATIENT |
| created_at | timestamp | |

#### `appointments_by_patient` (lookup table)
| Column | Type | Notes |
|---|---|---|
| patient_id | uuid | Partition Key |
| appointment_id | uuid | |
| appointment_date | text | |
| status | text | |

#### `appointments_by_doctor` (lookup table)
| Column | Type | Notes |
|---|---|---|
| doctor_id | uuid | Partition Key |
| appointment_date | text | Clustering Key |
| appointment_id | uuid | |
| status | text | |

---

## 🔌 gRPC Services

### PatientService
| Method | Request | Description |
|---|---|---|
| `RegisterPatient` | `RegisterPatientRequest` | Register a new patient |
| `PatientLogin` | `PatientLoginRequest` | Authenticate patient |
| `GetPatientById` | `GetPatientByIdRequest` | Fetch patient by ID |
| `UpdatePatientById` | `UpdatePatientRequest` | Update patient profile |
| `DeletePatientById` | `GetPatientByIdRequest` | Soft delete patient |

### DoctorService
| Method | Request | Description |
|---|---|---|
| `RegisterDoctor` | `RegisterDoctorRequest` | Register with auto-geocoding |
| `DoctorLogin` | `DoctorLoginRequest` | Authenticate doctor |
| `GetDoctorById` | `GetDoctorByIdRequest` | Fetch doctor by ID |
| `GetDoctorByEmail` | `GetByDoctorEmailRequest` | Fetch doctor by email |
| `UpdateDoctorById` | `UpdateDoctorRequest` | Update with re-geocoding |
| `DeleteDoctorById` | `GetDoctorByIdRequest` | Soft delete doctor |
| `GetDoctorsByLocation` | `LocationRequest` | Search by place name |
| `GetNearestDoctor` | `NearestLocationRequest` | Find nearest doctors |
| `GetDoctorAvailability` | `DoctorAvailabilityRequest` | Check booking slots |

### AppointmentService
| Method | Request | Description |
|---|---|---|
| `RequestAppointment` | `AppointmentServiceCreateRequest` | Patient books appointment |
| `ConfirmAppointment` | `AppointmentActionRequest` | Doctor confirms |
| `RejectAppointment` | `AppointmentActionRequest` | Doctor rejects |
| `CancelAppointment` | `AppointmentServiceCancelRequest` | Patient cancels only |
| `RescheduleAppointment` | `AppointmentRescheduleRequest` | Doctor reschedules only |
| `GetAppointmentById` | `GetAppointmentByIdRequest` | Fetch single appointment |
| `GetPatientAppointments` | `GetByPatientIdRequest` | All patient appointments |
| `GetDoctorAppointments` | `GetByDoctorIdRequest` | All doctor appointments |

---

## 🔔 NATS Notification Subjects

| Subject | Trigger | Recipient |
|---|---|---|
| `appointment.doctor.requested` | Patient books appointment | Doctor |
| `appointment.patient.confirmed` | Doctor confirms | Patient |
| `appointment.patient.rejected` | Doctor rejects | Patient |
| `appointment.patient.rescheduled` | Doctor reschedules | Patient |
| `appointment.doctor.cancelled` | Patient cancels | Doctor |

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- Gradle 8+
- Docker (for ScyllaDB and NATS)

### 1. Start ScyllaDB

```bash
docker run -d --name scylladb \
  -p 9042:9042 \
  scylladb/scylla
```

### 2. Start NATS

```bash
docker run -d --name nats \
  -p 4222:4222 \
  nats:latest
```

### 3. Create Keyspace and Tables

```sql
CREATE KEYSPACE doctor_appointment
  WITH replication = {'class': 'SimpleStrategy', 'replication_factor': 1};

USE doctor_appointment;

-- patients table
CREATE TABLE patients (
    patient_id uuid PRIMARY KEY,
    first_name text,
    last_name text,
    email text,
    password text,
    phone_number text,
    address text,
    is_deleted boolean
);
CREATE INDEX patient_by_email ON patients(email);

-- doctors table
CREATE TABLE doctors (
    doctor_id uuid PRIMARY KEY,
    first_name text,
    last_name text,
    email text,
    password text,
    phone_number text,
    address text,
    specialization text,
    clinic_address text,
    clinic_name text,
    clinic_building text,
    latitude double,
    longitude double,
    daily_limit int,
    is_deleted boolean
);
CREATE INDEX doctor_by_email ON doctors(email);
CREATE INDEX clinic_building_index ON doctors(clinic_building);

-- appointments main table
CREATE TABLE appointments (
    appointment_id uuid PRIMARY KEY,
    patient_id uuid,
    doctor_id uuid,
    appointment_date text,
    status text,
    notes text,
    reason text,
    cancelled_by text,
    created_at timestamp
);

-- lookup table by patient
CREATE TABLE appointments_by_patient (
    patient_id uuid,
    appointment_id uuid,
    appointment_date text,
    status text,
    PRIMARY KEY (patient_id, appointment_id)
);

-- lookup table by doctor
CREATE TABLE appointments_by_doctor (
    doctor_id uuid,
    appointment_date text,
    appointment_id uuid,
    status text,
    PRIMARY KEY ((doctor_id, appointment_date), appointment_id)
);
```

### 4. Configure `application.yml`

```yaml
micronaut:
  application:
    name: doctor-appointment

grpc:
  server:
    port: 50051

cassandra:
  default:
    contact-points:
      - localhost:9042
    local-datacenter: datacenter1
    keyspace: doctor_appointment

nats:
  default:
    addresses:
      - "nats://localhost:4222"
```

### 5. Build and Run

```bash
./gradlew clean build
./gradlew run
```

---

## 🔐 Authentication

All gRPC calls (except `RegisterPatient`, `RegisterDoctor`) require **HTTP Basic Auth** via gRPC metadata:

```
Key:   authorization
Value: Basic <base64(email:password)>
```

Generate the token:
```bash
echo -n "user@example.com:password123" | base64
```

---

## 🧪 Testing with grpcurl

### Register a doctor
```bash
grpcurl -plaintext \
  -d '{
    "doctor_first_name": "John",
    "doctor_last_name": "Doe",
    "doctor_email": "john@example.com",
    "password": "pass1234",
    "doctor_phone_number": "9800000000",
    "doctor_address": "Kathmandu",
    "specialization": "Cardiology",
    "clinic_address": "Kalanki, Kathmandu",
    "clinic_name": "City Heart Hospital",
    "clinic_building": "blue building, first floor"
  }' \
  localhost:50051 \
  com.doctorappointment.DoctorService/RegisterDoctor
```

### Find nearest doctors
```bash
grpcurl -plaintext \
  -H 'authorization: Basic <token>' \
  -d '{
    "location_name": "Kalanki, Kathmandu",
    "radius_km": 10.0,
    "limit": 5
  }' \
  localhost:50051 \
  com.doctorappointment.DoctorService/GetNearestDoctor
```

### Book an appointment
```bash
grpcurl -plaintext \
  -H 'authorization: Basic <token>' \
  -d '{
    "patient_id": "<patient-uuid>",
    "doctor_id": "<doctor-uuid>",
    "date": "2026-08-01",
    "notes": "Regular checkup"
  }' \
  localhost:50051 \
  com.doctorappointment.AppointmentService/RequestAppointment
```

---

## 🌍 Location Features

Doctors register with a **place name** instead of raw coordinates:

```json
{
  "clinic_address": "Kalanki, Kathmandu",
  "clinic_name": "City Hospital",
  "clinic_building": "white building, second floor"
}
```

- Coordinates are auto-resolved via the **Photon geocoding API**
- Location is validated to be **within Nepal** (lat: 26.3–30.5, lon: 80.0–88.2)
- Duplicate clinic buildings are **prevented** via a secondary index
- Distance search uses the **Haversine formula** (ScyllaDB has no native geospatial support)

---

## 📋 Appointment Rules

| Action | Who | Allowed Statuses   |
|---|---|--------------------|
| Request | Patient | CANCELLED only     |
| Confirm | Doctor (assigned) | PENDING only       |
| Reject | Doctor (assigned) | PENDING only       |
| Cancel | Patient (assigned) | PENDING, CONFIRMED |
| Reschedule | Doctor (assigned) | PENDING, CONFIRMED |

---

## 🔑 Key Design Decisions

- **Three-table ScyllaDB design** for appointments — main table + two lookup tables partitioned by `patient_id` and `(doctor_id, date)` for efficient queries without `ALLOW FILTERING`
- **Prepared statements** used throughout for safety and performance
- **Named query constants** in `*Query` classes to prevent typo bugs
- **Layered architecture**: Schema → Query → Model → DTO → Repository → Service → Helper → GrpcService
- **BCrypt** for password hashing
- **gRPC metadata keys are lowercase** — `"authorization"` not `"Authorization"`
- **NATS fire-and-forget** — notification failures never break the main appointment flow
- **Soft deletes** for both patients and doctors

---

