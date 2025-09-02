# API Gateway – Role-Specific Endpoints

| HTTP Method | URL | Description | Role |
|-------------|-----|-------------|------|
| PUT | `/doctors/{doctorId}/verify` | Approve or reject doctor account verification | **ADMIN** |
| PUT | `/doctors/{doctorId}/activation` | Activate or deactivate a doctor account | **ADMIN** |
| GET | `/patients` | List all patients | **ADMIN** |
| POST | `/patients` | Register a new patient | **PATIENT** |
| GET | `/patients/{id}` | Get patient profile | **PATIENT** |
| PUT | `/patients/{id}` | Update patient profile | **PATIENT** |
| GET | `/patients/{patientId}/bookings` | List bookings for patient | **PATIENT** |
| POST | `/patients/book` | Book an appointment | **PATIENT** |
| POST | `/patients/book/{bookingId}/cancel` | Cancel booking | **PATIENT** |
| POST | `/patients/payments/intent` | Create payment intent for appointment | **PATIENT** |
| GET | `/patients/doctor/{doctorId}` | Get doctor profile | **PATIENT** |
| GET | `/patients/doctor/{doctorId}/availability` | Get doctor availability | **PATIENT** |
| GET | `/patients/doctor/{doctorId}/reviews` | Get doctor reviews | **PATIENT** |
| POST | `/appointments` | Book an appointment | **PATIENT** |
| PUT | `/appointments/{id}/cancel` | Cancel an appointment | **PATIENT/DOCTOR** |
| PUT | `/appointments/{id}/reschedule` | Reschedule an appointment | **PATIENT/DOCTOR** |
| PUT | `/appointments/{id}/confirm` | Confirm a pending appointment | **DOCTOR** |
| PUT | `/appointments/{id}/complete` | Complete a confirmed appointment | **DOCTOR** |
| GET | `/appointments/doctor/{doctorId}/upcoming` | List upcoming appointments for doctor | **DOCTOR** |
| GET | `/appointments/doctor/{doctorId}/past` | List past (completed/cancelled) appointments for doctor | **DOCTOR** |
| GET | `/appointments/patient/{patientId}/past` | Fetch past appointments for patient | **PATIENT** |
| GET | `/appointments/patient/{patientId}` | Fetch all appointments for patient | **PATIENT** |
| POST | `/doctors/{doctorId}/availability/slots` | Create availability slots | **DOCTOR** |
| GET | `/doctors/{doctorId}/availability/slots` | List availability slots | **DOCTOR** |
| PUT | `/doctors/{doctorId}/availability/slots/block` | Block or unblock availability slots | **DOCTOR** |
| POST | `/doctors/{doctorId}/prescriptions` | Create a prescription for a patient | **DOCTOR** |
| GET | `/doctors/{doctorId}/prescriptions` | List prescriptions created by doctor | **DOCTOR** |
| GET | `/doctors/{doctorId}/appointments/upcoming` | Fetch upcoming appointments via doctor-service | **DOCTOR** |
| GET | `/doctors/{doctorId}/appointments/history` | Fetch past appointments via doctor-service | **DOCTOR** |
| GET | `/doctors/{doctorId}/reviews` | Fetch doctor reviews | **DOCTOR** |
| POST | `/doctors/{doctorId}/reviews/refresh-rating` | Recalculate doctor’s rating | **DOCTOR** |
| POST | `/auth/register/doctor` | Register a doctor | **PUBLIC** |
| POST | `/auth/register/patient` | Register a patient | **PUBLIC** |
| POST | `/auth/login` | Login | **PUBLIC** |
| POST | `/auth/refresh` | Refresh access token | **PUBLIC** |
| GET | `/auth/me` | Get profile of authenticated user | **PUBLIC (requires JWT)** |
| POST | `/api/messaging/otp/send` | Send OTP | **PUBLIC** |
| POST | `/api/messaging/otp/verify` | Verify OTP | **PUBLIC** |
| POST | `/api/messaging/send-sms` | Send SMS (admin/internal) | **ADMIN/SYSTEM** |

---

# API Gateway – Role Permission Matrix

| URL / Endpoint | HTTP Method | ADMIN | DOCTOR | PATIENT | PUBLIC |
|----------------|-------------|-------|--------|---------|--------|
| /doctors/{doctorId}/verify | PUT | ✅ | ❌ | ❌ | ❌ |
| /doctors/{doctorId}/activation | PUT | ✅ | ❌ | ❌ | ❌ |
| /patients | GET | ✅ | ❌ | ❌ | ❌ |
| /patients | POST | ❌ | ❌ | ✅ | ❌ |
| /patients/{id} | GET | ❌ | ❌ | ✅ | ❌ |
| /patients/{id} | PUT | ❌ | ❌ | ✅ | ❌ |
| /patients/{patientId}/bookings | GET | ❌ | ❌ | ✅ | ❌ |
| /patients/book | POST | ❌ | ❌ | ✅ | ❌ |
| /patients/book/{bookingId}/cancel | POST | ❌ | ❌ | ✅ | ❌ |
| /patients/payments/intent | POST | ❌ | ❌ | ✅ | ❌ |
| /patients/doctor/{doctorId} | GET | ❌ | ❌ | ✅ | ❌ |
| /patients/doctor/{doctorId}/availability | GET | ❌ | ❌ | ✅ | ❌ |
| /patients/doctor/{doctorId}/reviews | GET | ❌ | ❌ | ✅ | ❌ |
| /appointments | POST | ❌ | ❌ | ✅ | ❌ |
| /appointments/{id}/cancel | PUT | ❌ | ✅ | ✅ | ❌ |
| /appointments/{id}/reschedule | PUT | ❌ | ✅ | ✅ | ❌ |
| /appointments/{id}/confirm | PUT | ❌ | ✅ | ❌ | ❌ |
| /appointments/{id}/complete | PUT | ❌ | ✅ | ❌ | ❌ |
| /appointments/doctor/{doctorId}/upcoming | GET | ❌ | ✅ | ❌ | ❌ |
| /appointments/doctor/{doctorId}/past | GET | ❌ | ✅ | ❌ | ❌ |
| /appointments/patient/{patientId}/past | GET | ❌ | ❌ | ✅ | ❌ |
| /appointments/patient/{patientId} | GET | ❌ | ❌ | ✅ | ❌ |
| /doctors/{doctorId}/availability/slots | POST | ❌ | ✅ | ❌ | ❌ |
| /doctors/{doctorId}/availability/slots | GET | ❌ | ✅ | ❌ | ❌ |
| /doctors/{doctorId}/availability/slots/block | PUT | ❌ | ✅ | ❌ | ❌ |
| /doctors/{doctorId}/prescriptions | POST | ❌ | ✅ | ❌ | ❌ |
| /doctors/{doctorId}/prescriptions | GET | ❌ | ✅ | ❌ | ❌ |
| /doctors/{doctorId}/appointments/upcoming | GET | ❌ | ✅ | ❌ | ❌ |
| /doctors/{doctorId}/appointments/history | GET | ❌ | ✅ | ❌ | ❌ |
| /doctors/{doctorId}/reviews | GET | ❌ | ✅ | ❌ | ❌ |
| /doctors/{doctorId}/reviews/refresh-rating | POST | ❌ | ✅ | ❌ | ❌ |
| /auth/register/doctor | POST | ❌ | ❌ | ❌ | ✅ |
| /auth/register/patient | POST | ❌ | ❌ | ❌ | ✅ |
| /auth/login | POST | ❌ | ❌ | ❌ | ✅ |
| /auth/refresh | POST | ❌ | ❌ | ❌ | ✅ |
| /auth/me | GET | ❌ | ❌ | ❌ | ✅ (JWT required) |
| /api/messaging/otp/send | POST | ❌ | ❌ | ❌ | ✅ |
| /api/messaging/otp/verify | POST | ❌ | ❌ | ❌ | ✅ |
| /api/messaging/send-sms | POST | ✅ | ❌ | ❌ | ❌ |
