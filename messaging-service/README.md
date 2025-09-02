# Messaging Service 📩

The **Messaging Service** is a core microservice in the Healthcare Application.  
It handles **all patient-doctor communication needs**, including notifications, appointment updates, OTP verification, and reminders using **Twilio SMS/WhatsApp API**.

---

## 🚀 Features

- **Appointment Notifications**
    - Notify patients & doctors on appointment booking, updates, and cancellations.

- **OTP (One-Time Password)**
    - Generate & send OTP for login/registration/verification.
    - Validate OTP with expiry mechanism.

- **Reminders**
    - Send reminders before appointment time.
    - Configurable reminder intervals (e.g., 1 hour, 24 hours).

- **Broadcasts**
    - Admin can send announcements (e.g., health campaigns, maintenance alerts).

- **Error Handling**
    - Centralized exception handling with meaningful error messages.
    - Retry mechanism for failed deliveries.

- **Scalability**
    - Kafka integration for asynchronous event-driven communication (appointments → notifications).
    - Can easily plug in WhatsApp, email, or push notifications.

---

## 🛠 Tech Stack

- **Backend**: Spring Boot 3.x, Java 17
- **Messaging**: Twilio (SMS/WhatsApp)
- **Database**: PostgreSQL (for OTPs & message logs)
- **Communication**: Kafka (event-driven microservices)
- **Security**: Spring Security (JWT)
- **Build Tool**: Maven

---

## 📦 Project Structure
```
messaging-service
├── src/main/java/com/healthcare/messaging
│ ├── config # Twilio, Kafka, and app configs
│ ├── controller # REST endpoints
│ ├── dto # Request/Response DTOs
│ ├── entity # JPA entities (MessageLog, Otp)
│ ├── exception # Custom exceptions
│ ├── repository # Spring Data JPA repos
│ ├── service # Business logic
│ └── MessagingServiceApplication.java
└── src/main/resources
├── application.yml
└── logback-spring.xml
```
---

## 📡 API Endpoints

1. Send SMS

POST /api/messages/send
```
{
"to": "+919899999999",
"message": "Your appointment with Dr. Smith is confirmed for 12:00 PM."
}
```

Response
```
{
"status": "SENT",
"sid": "SMXXXXXXXXXXXXXXXXXXXXXXXX"
}
```

2. Send OTP

POST /api/otp/send
```
{
"phoneNumber": "+919899999999"
}
```

Response
```
{
"status": "OTP_SENT",
"otp": "123456",
"expiresAt": "2025-08-28T10:15:00"
}
```

3. Verify OTP

POST /api/otp/verify
```
{
"phoneNumber": "+919899999999",
"otp": "123456"
}
```

Response
```
{
"status": "VERIFIED"
}
```
---

## 📝 Logs & Monitoring

All messages (SMS/OTP) stored in message_log table.

Failed messages retried & logged with error cause.

SLF4J + Logback used for structured logging.

---

## 🔮 Future Enhancements

WhatsApp messaging via Twilio.

Email notifications.

Push notifications (Firebase).

Multilingual templates.

Integration with external EHR/CRM systems.

---

## ✅ If you don’t want .env to affect your app:

Do NOT add spring-dotenv dependency yet.

Without it, Spring Boot will completely ignore .env.

Only application.yml will be considered.

Keep your .env file in project root (just for reference or future use).
```
# Twilio Credentials
TWILIO_ACCOUNT_SID=your_account_sid
TWILIO_AUTH_TOKEN=your_auth_token
TWILIO_PHONE_NUMBER=+1234567890

# Database
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/messaging_service
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=Abhi@123

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
MESSAGING_TOPIC=appointments.notifications
```

Run the app normally:

mvn spring-boot:run


It will only read from application.yml.

## ✅ Later, when you want to switch to .env:

Add the spring-dotenv dependency in pom.xml:
```
<dependency>
  <groupId>me.paulschwarz</groupId>
  <artifactId>spring-dotenv</artifactId>
  <version>3.0.0</version>
</dependency>
```

Update application.yml to use environment variables like:
```
twilio:
account-sid: ${TWILIO_ACCOUNT_SID:default_sid}
auth-token: ${TWILIO_AUTH_TOKEN:default_token}
phone-number: ${TWILIO_PHONE_NUMBER:+1000000000}
```

Then your .env file values will override them.