# 📚 DoCoroFile - Document Sharing Platform

<p align="center"><img src="https://socialify.git.ci/vanphatit/docorofile/image?custom_description=Sharing+academic+documents&amp;description=1&amp;font=Source+Code+Pro&amp;issues=1&amp;language=1&amp;name=1&amp;owner=1&amp;pattern=Circuit+Board&amp;stargazers=1&amp;theme=Light" alt="project-image"></p>

**DoCoroFile** is a web-based platform that helps students efficiently search, share, and manage academic documents in a transparent and secure way. Beyond being a document repository, it fosters a learning community where users can review, comment, and discuss documents.

---

## 🧩 Features

- 🔍 Search documents by keyword
- 📤 Upload and manage personal documents
- ⭐ Rate and comment on documents
- 💾 Save favorite documents
- 🏫 Follow documents by course or university
- 🔒 User account and role management
- 💸 Membership upgrade and payment via VNPay
- 📈 Report management and system statistics (admin)

---
## 📎 Project Documentation

You can access all system design documents, diagrams, and reports here:  
🔗 [Google Drive Folder](https://drive.google.com/drive/folders/1FFxfOzoSdiWaB6_uZDFRXQT_BEx-Bp-D?usp=sharing)

---

## 🧪 Tech Stack

| Layer              | Technology                      |
|--------------------|----------------------------------|
| Backend            | Java 17, Spring Boot 3, JPA, Spring Security |
| Frontend (View)    | Thymeleaf, HTML, CSS, JS        |
| Database           | MySQL 8                         |
| Authentication     | JWT + OAuth2 (Google Login)     |
| Chat               | WebSocket                       |
| Payment Gateway    | VNPay                           |
| Deployment         | Docker, Docker Compose          |
| File Handling      | Multipart Upload + PDF render + Local Storage|
| Mail Service       | SMTP (TLS)                      |

---
### 📐 Design Patterns Used

This project applies several design patterns to improve scalability and maintainability:

| Pattern              | Description                                                                 |
|----------------------|-----------------------------------------------------------------------------|
| **Factory Method**   | Dynamically creates users based on roles (Admin, Moderator, Member).        |
| **Strategy**         | Handles user updates with different strategies based on user role.          |
| **Builder**          | Builds complex DTOs or response objects in a readable way.                  |
| **Singleton**        | (Spring - Bean) Utility and configuration classes such as `JwtUtil`, `UploadFileUtil`, etc. |
| **Adapter**          | Maps entities to response DTOs and vice versa.                              |
| **Observer**         | Sends real-time notifications when a document is reported or approved.      |

---
## 🚀 Run Locally (Dockerized)

### ⚙️ Prerequisites

- Docker & Docker Compose
- Open ports `3305`, `9091`
- `.env` file must define:
  - `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
  - `JWT_SECRET`
  - `DOCUMENT_UPLOAD_DIR`, `DOCUMENT_ACCESS_URL`
  - `SPRING_MAIL_HOST`, `SPRING_MAIL_USERNAME`, `SPRING_MAIL_PASSWORD`
  - `PAYMENT_VNPAY_*` fields

### 📦 Build & Start

```bash
docker-compose up --build -d
```

Access the app at: `http://localhost:9091`  
MySQL: `localhost:3305`, database: `docorofile`

---

## 🔐 Roles & Access

- **Guest**: view/search documents, register
- **Member**: upload, save, comment, upgrade membership
- **Moderator**: review reports, approve documents
- **Admin**: full system management, logs, statistics, packages

---

## 📁 Project Structure (Spring Boot)

```
src/
├── controller/           # REST + Thymeleaf controllers
├── service/              # Business logic
├── repository/           # JPA Repositories
├── model/                # Entities
├── dto/                  # Request/Response DTOs
├── config/               # Security & configs
├── utils/                # Helper classes
└── resources/
    ├── static/           # Static assets
    ├── templates/        # Thymeleaf HTML views
    └── application.properties
```

---

## 🧑‍💻 Authors

- Lê Văn Phát - 22110196  
- Huỳnh Thanh Duy - 22110118  
- Trần Như Quỳnh - 22110218  
- Nguyễn Chí Thanh - 22110226

> University of Technology and Education HCMC – Faculty of Information Technology  
> Supervisor: Ph.D. Mai Anh Tho

---
## 📷 Screenshots
![z6602000661605_7bd43bf874ad7fd67bceb5f1673bb625](https://github.com/user-attachments/assets/eb5ae77e-3c96-4195-a48f-362c9f97e66d)
![z6602000699766_c76695df99605432b1d21409428e23ac](https://github.com/user-attachments/assets/c3443199-9bcb-4b95-9742-734e92f54481)
![z6602000661573_d4d39862d98f1c6a61fd9a49384f450a](https://github.com/user-attachments/assets/7a892f26-504b-4bbf-a792-a714aa3dd935)
![z6602000837912_82211d5811aab3731194632cca568572](https://github.com/user-attachments/assets/a98d5ebd-422c-4fd0-9ad5-54b3355182be)
![z6602000785516_b264cf334c00c6b1e8df9a8b0a7719d1](https://github.com/user-attachments/assets/7592d89f-d69d-42ff-adb0-e3427ae423a7)
![z6602000737684_e5fdc21dd96aad644b6a60a12b73741c](https://github.com/user-attachments/assets/9303fb50-6cfb-483e-8bf0-a6ec38543886)
![z6602000737620_a7900360eaf36cc69f21503212cf823d](https://github.com/user-attachments/assets/20149224-c475-434c-87fd-0f1477f5bc8a)
![z6602000699781_a522f78dc4244026c7bdda3c98259470](https://github.com/user-attachments/assets/b7f45d74-8fd9-4b24-a901-fc2ef1194419)


---

## 📜 License

For educational and internal research purposes only.
