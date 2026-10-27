# CampusBrain – Smart Campus Management Platform

CampusBrain is a modern, premium academic resource platform designed exclusively for **Computer Science & Engineering (CSE)** students and faculty administrators.

---

## 🌟 Key Features

### 1. Student Portal (`/`)
- **Direct Access**: Students land straight on the dashboard with **no login or registration required**.
- **Clean Navigation**: Professional branding, live sync connection indicator, and quick navigation.
- **Prominent Search**: Instant debounced search querying titles, subjects, descriptions, and filenames.
- **4 Distinct Academic Categories**:
  - 📚 **Notes**: Comprehensive lecture summaries and conceptual study guides.
  - 🧪 **Lab Manuals**: Experiment instructions, coding exercises, and viva references.
  - 📝 **Assignments**: Semester problem sets, project briefs, and coursework tasks.
  - 📑 **Previous Question Papers**: Past university semester exam papers and model papers.
- **Multi-Faceted Filtering**: Filter by **Academic Year** (1st to 4th Year), **Semester** (Sem 1 to Sem 8), **Subject** (dynamic), and **File Format** (PDF, DOCX, PPTX, XLSX, TXT).
- **Resource Cards**: Displays title, description, subject badge, academic year, semester, category badge, file format, file size, and upload date.
- **View & Download**:
  - **View**: Opens an in-app preview modal (in-line PDF viewer, text viewer, and Office document viewer).
  - **Download**: Directly downloads the actual file with its original filename.
- **Strictly Read-Only**: No upload, edit, delete, faculty details, or administrative controls on the Student Portal.
- **No Admin Login Button**: The student interface has zero admin links or buttons to preserve role separation.

---

### 2. Direct File Upload & Management Portal (`/upload` & `/admin`)
- **Direct Access (No Admin Login Required)**: Accessible immediately at `/upload` or `/admin` without requiring username/password authentication or login screens.
- **Upload Material Button**: Positioned directly in the main navigation bar of the Student Dashboard for instant access.
- **Management Console**:
  - **Overview Statistics**: Total uploaded materials, published count, draft/unpublished count, and category-wise counts.
  - **Upload New Material**:
    - Direct file upload from computer via drag-and-drop or file selector.
    - **Automatic File Format Detection**: When a file is selected, the platform automatically detects its file format (PDF, DOC, DOCX, PPT, PPTX, XLS, XLSX, TXT), filename, and file size.
    - Fields: Title, Optional Description, Academic Year, Semester, Subject, Material Category (Notes, Lab Manuals, Assignments, Previous Question Papers), and Publish Immediately toggle.
  - **Manage Materials**:
    - Comprehensive data table with search and filtering by Category and Publication Status.
    - **Toggle Publish / Unpublish**: One-click switch with instant status updates.
    - **Edit Details**: Modal to update metadata (Title, Description, Subject, Year, Semester, Category).
    - **Replace File**: Modal allowing file replacement from computer with automatic format detection.
    - **Delete**: Confirmation modal before permanent deletion of data and physical file.

---

### 3. Real-Time Automatic Synchronization (SSE)
- When an administrator uploads, publishes, edits, unpublishes, or deletes an academic material:
  - Updates automatically appear on the Student Portal in the correct category **without requiring students to refresh the page manually**.
  - Powered by **Server-Sent Events (SSE)** at `/api/materials/sync`.
  - Non-intrusive toast notifications notify students of new uploads or updates.
  - Unpublished (draft) materials remain completely hidden from students.

---

## 🛠️ Technology Stack

- **Frontend**: HTML5, CSS3 (Modern SaaS dark theme with responsive flex/grid layouts), Vanilla JavaScript (ES6+).
- **Backend**: Java 21, Spring Boot 3.5.6 (Spring Web, Spring Data JPA, HikariCP).
- **Persistence**:
  - **Persistent Storage**: Physical files stored securely in `./uploads` with unique UUIDs and anti-path-traversal protection.
  - **Dual Database Architecture**:
    - Automatically connects to **MySQL** (`localhost:3306`) when MySQL service is running.
    - Automatically falls back to persistent embedded file storage (`./data/campusbrain`) if MySQL is offline.
    - Includes `src/main/resources/schema-mysql.sql` for MySQL table creation.
- **Build Tool**: Apache Maven with Maven Wrapper (`./mvnw.cmd` / `./mvnw`).

---

## 🚀 How to Run CampusBrain

### Option 1: Run Pre-Built Executable JAR (Recommended)
```powershell
java -jar target/campusbrain-1.0.0.jar
```

### Option 2: Run via Maven Wrapper
```powershell
.\mvnw.cmd spring-boot:run
```

Once started, open your web browser:
- **Student Dashboard**: [http://localhost:8080/](http://localhost:8080/)
- **Direct File Upload**: [http://localhost:8080/upload](http://localhost:8080/upload) (No login required)
- **Manage Materials**: [http://localhost:8080/admin](http://localhost:8080/admin)

---

## 📁 Project Structure

```
CAMPUS BRAIN NEW/
├── pom.xml                                    # Maven configuration & dependencies
├── mvnw.cmd                                  # Windows Maven Wrapper
├── target/
│   └── campusbrain-1.0.0.jar                 # Packaged executable Spring Boot application
├── uploads/                                  # Persistent directory for uploaded academic files
├── data/                                     # Persistent embedded database directory
└── src/
    └── main/
        ├── java/com/campusbrain/
        │   ├── CampusBrainApplication.java   # Spring Boot Application entrypoint
        │   ├── config/
        │   │   ├── DataSourceConfig.java     # Smart Dual Database configuration (MySQL / Embedded fallback)
        │   │   ├── DataInitializer.java     # Default Admin & CSE sample materials seeder
        │   │   └── WebConfig.java           # MVC view routing & security interceptor registration
        │   ├── controller/
        │   │   ├── StudentMaterialController.java  # Student REST endpoints & SSE sync
        │   │   ├── AdminAuthController.java        # Admin authentication endpoints
        │   │   └── AdminMaterialController.java    # Admin management, upload, replace, delete
        │   ├── dto/                          # Data Transfer Objects
        │   ├── entity/                       # JPA entities (AcademicMaterial, AdminUser, MaterialCategory)
        │   ├── exception/                    # Global Exception Handler
        │   ├── repository/                   # Spring Data JPA Repositories
        │   ├── security/                     # Token authentication & PBKDF2 password hashing
        │   └── service/                      # Business logic, file storage & SSE broadcast
        └── resources/
            ├── application.properties        # Application configuration & upload limits
            ├── schema-mysql.sql              # MySQL DDL table schema
            └── static/
                ├── index.html                # Student Dashboard (Read-only, no admin button)
                ├── css/student.css           # Premium SaaS dark styling for student portal
                ├── js/student.js             # Student portal logic, filters, and SSE sync
                └── admin/
                    ├── login.html            # Dedicated secure Admin login page
                    ├── index.html            # Admin management console
                    ├── css/admin.css         # Admin console styling
                    └── js/admin.js           # Admin console operations & file auto-detection
```

---

## 🔒 Security Features

1. **Strict Separation of Portals**:
   - Zero administrative controls or login links on the Student Dashboard.
   - Dedicated login gateway at `/admin/login`.
   - Admin REST endpoints protected by `AuthInterceptor` with cryptographically secure session tokens.
2. **File Safety & Sanitization**:
   - File extensions validated against allowed types: `pdf`, `doc`, `docx`, `ppt`, `pptx`, `xls`, `xlsx`, `txt`.
   - Path traversal prevention (`..` checks and root directory resolution).
   - Maximum upload size configured to 100MB in `application.properties`.
3. **Password Security**:
   - PBKDF2 with HMAC-SHA256 and unique 16-byte random salts with 100,000 iterations.
