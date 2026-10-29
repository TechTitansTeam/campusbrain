# CampusBrain – Pragati University CSE Academic Platform

[![Live Service](https://img.shields.io/badge/Render-24%2F7%20Live-brightgreen?logo=render&style=flat-square)](https://campusbrain-pragatiuniversity.onrender.com)
[![Database](https://img.shields.io/badge/Neon-Cloud%20PostgreSQL-00E599?logo=postgresql&style=flat-square)](https://neon.tech)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&style=flat-square)](https://openjdk.org)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-6DB33F?logo=springboot&style=flat-square)](https://spring.io/projects/spring-boot)

**CampusBrain** is the official academic resource and digital knowledge platform designed and developed exclusively for the **Department of Computer Science & Engineering (CSE) at Pragati University**.

---

## 🌐 Live Production Links

| Portal | URL | Description |
| :--- | :--- | :--- |
| **🎓 Student Portal** | [campusbrain-pragatiuniversity.onrender.com](https://campusbrain-pragatiuniversity.onrender.com) | Direct access for CSE students (No login required) |
| **🛠️ Admin / Upload Portal** | [campusbrain-pragatiuniversity.onrender.com/upload](https://campusbrain-pragatiuniversity.onrender.com/upload) | Faculty & administrator resource management |

---

## 🌟 Key Features

### 1. Student Knowledge Portal (`/`)
- **Frictionless Access**: Zero login, registration, or credentials needed for students to browse and study.
- **Pragati University CSE Branding**: Polished, modern university interface built with responsive dark theme styling.
- **Real-Time Live Sync**: Synchronizes instantly using **Server-Sent Events (SSE)** when new materials are published, edited, or removed by faculty.
- **Instant Search & Multi-Faceted Filters**:
  - Live debounced search across subjects, document titles, descriptions, and filenames.
  - Multi-filtering by **Academic Year** (1st to 4th Year), **Semester** (Sem 1 to Sem 8), **Subject**, and **Category**.
  - Dynamic interactive filter chips with one-click clear options.
- **4 Core Academic Pillars**:
  - 📚 **Notes**: Curated lecture summaries and core subject concepts.
  - 🧪 **Lab Manuals**: Experiment manuals, code implementations (e.g., C++, Java, Python), and viva questions.
  - 📝 **Assignments**: Coursework problem sheets, homework assignments, and tutorial sets.
  - 📑 **Previous Question Papers**: Official university examination question papers and model test papers.

---

### 2. In-App Document Viewer (No Download Needed)
- 📄 **Word Documents (`.docx`)**: Powered by an integrated **Mammoth.js** rendering engine that reads and displays Word files directly in the browser as formatted, readable HTML with headers, tables, lists, and code blocks.
- 📕 **PDF Documents (`.pdf`)**: Embedded native PDF reader with zoom, page navigation, and text search.
- 📊 **PowerPoint & Excel (`.pptx`, `.xlsx`, `.doc`)**: Integrated cloud viewer for instant in-browser slide presentations and spreadsheets.
- ⬇️ **Direct Download**: One-click download button available in both the resource card and within the preview modal.

---

### 3. Faculty & Admin Management Console (`/upload` & `/admin`)
- **Direct Upload Portal**: Quick-access resource upload page for department faculty and teaching assistants.
- **Automatic File Analysis**: Automatically detects file format (`.pdf`, `.docx`, `.pptx`, `.xlsx`, `.txt`), file size, and clean original filenames upon file selection.
- **Overview Statistics Dashboard**:
  - Total Materials, Published Materials, Draft Materials, and category breakdown counters with real-time updates.
- **Material Management Operations**:
  - 👁️ **In-App Preview**: Preview uploaded files directly in the admin dashboard before publishing.
  - 🔄 **Toggle Publish / Unpublish**: Instantly publish to students or retract to draft status with one click.
  - ✏️ **Edit Metadata**: Modify subject, academic year, semester, title, category, or description.
  - 📁 **Replace File**: Swap out an existing document with a newer revision without losing metadata or view counters.
  - 🗑️ **Delete Material**: Safely delete material records and permanent cloud file storage.

---

### 4. 100% Permanent Cloud Storage Architecture
- **Neon Cloud PostgreSQL Integration**:
  - Unlike ephemeral container filesystems that wipe local files on server restarts or redeploys, CampusBrain stores all documents as permanent binary byte streams (`byte[] fileData` / `VARBINARY`) directly inside **Neon Serverless PostgreSQL**.
  - Materials and documents **never disappear** on server reboots, container spins, or code redeployments.
  - Automatic fallback to local embedded database during offline local development.

---

## 🛠️ Technology Stack

| Layer | Technologies |
| :--- | :--- |
| **Backend** | Java 21, Spring Boot 3.5.6, Spring Data JPA, Hibernate ORM 6.6 |
| **Database** | Neon Serverless PostgreSQL (Production Cloud) / H2 Embedded (Local Development) |
| **Real-Time** | Server-Sent Events (SSE) via `SseEmitter` |
| **Frontend** | HTML5, CSS3 (Modern University SaaS theme), Vanilla JavaScript (ES6+) |
| **Document Rendering** | Mammoth.js (Word `.docx` in-browser parser), Native PDF viewer, Google Docs cloud viewer |
| **Hosting & Deployment** | Render Cloud Platform (Docker containerized, auto-deploying from GitHub `main` branch) |
| **Build Tool** | Apache Maven with Maven Wrapper (`./mvnw.cmd` / `./mvnw`) |

---

## 🚀 Running Locally

### Prerequisites
- **JDK 21** or later installed
- Git installed

### 1. Clone the Repository
```bash
git clone https://github.com/TechTitansTeam/campusbrain.git
cd campusbrain
```

### 2. Build and Run
Using Maven Wrapper on Windows:
```powershell
.\mvnw.cmd spring-boot:run
```
Or on Linux/macOS:
```bash
./mvnw spring-boot:run
```

### 3. Access in Browser
- **Student Dashboard**: [http://localhost:8080/](http://localhost:8080/)
- **Faculty / Upload Portal**: [http://localhost:8080/upload](http://localhost:8080/upload)
- **Admin Management Console**: [http://localhost:8080/admin](http://localhost:8080/admin)

---

## 📁 Repository Structure

```
campusbrain/
├── pom.xml                                    # Maven configuration & PostgreSQL dependencies
├── mvnw.cmd / mvnw                           # Cross-platform Maven wrappers
├── Dockerfile                                # Production container build definition for Render
├── README.md                                 # Platform documentation & deployment guide
└── src/
    └── main/
        ├── java/com/campusbrain/
        │   ├── CampusBrainApplication.java   # Application entrypoint
        │   ├── config/
        │   │   ├── DataSourceConfig.java     # Smart cloud PostgreSQL connection & URL parser
        │   │   ├── DataInitializer.java     # Admin account & initial data seeder
        │   │   └── WebConfig.java           # MVC route mappings
        │   ├── controller/
        │   │   ├── StudentMaterialController.java  # Student REST endpoints & SSE sync stream
        │   │   ├── AdminMaterialController.java    # Admin upload, edit, replace, delete, view
        │   │   ├── AdminStatsController.java       # Admin dashboard analytics
        │   │   └── AdminAuthController.java        # Faculty authentication endpoints
        │   ├── dto/                          # Data Transfer Objects
        │   ├── entity/                       # JPA entities (AcademicMaterial with byte[] fileData)
        │   ├── repository/                   # Spring Data JPA interfaces
        │   ├── security/                     # Session token authentication & password hashing
        │   └── service/                      # Business logic, file storage & SSE broadcast
        └── resources/
            ├── application.properties        # Server port, database URL, and 100MB upload limits
            └── static/
                ├── index.html                # Student Portal (Pragati University CSE)
                ├── css/student.css           # Student portal styles & document viewer modals
                ├── js/student.js             # Client search, filters, SSE listener, Mammoth viewer
                └── admin/
                    ├── index.html            # Faculty Admin dashboard
                    ├── css/admin.css         # Admin styling & preview modals
                    └── js/admin.js           # Admin operations, auto-format detection & viewer
```

---

## 🔒 Security & Reliability

1. **Role Separation**: Students have strictly read-only access. Upload and management controls are isolated to administrative routes.
2. **File Sanitization**: All uploaded files undergo strict path-traversal sanitization (`..` stripping) and extension verification (`.pdf`, `.docx`, `.pptx`, `.xlsx`, `.txt`).
3. **Data Durability**: Files are stored as byte arrays directly in cloud PostgreSQL, guaranteeing 100% data persistence independent of server lifecycle.
4. **Token Security**: Faculty endpoints are protected by secure session tokens with PBKDF2 with HMAC-SHA256 password hashing.

---

## 👨‍💻 Developed For

**Pragati University**  
Department of Computer Science & Engineering (CSE)  
*Designed and built with ❤️ by Tech Titans Team.*
