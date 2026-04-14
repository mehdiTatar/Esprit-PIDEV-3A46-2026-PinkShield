# 💊 PinkShield - Modern Healthcare & Commerce Management System

## ✨ Project Overview

PinkShield is a modern, professionally designed JavaFX desktop application for managing healthcare appointments, pharmacy products, and user wishlists. The application features a sleek SaaS-style dashboard with a dark sidebar and modern UI components.

## 🎨 Features

### Dashboard Layout
- **Sidebar Navigation**: Modern dark-themed sidebar (200px fixed width) with smooth hover effects
- **Header**: Professional header with PinkShield branding and quick search
- **Center Content Area**: Dynamic content loading with StackPane
- **Color Scheme**: 
  - Primary: #2d3436 (Dark Gray)
  - Accent: #e84393 (PinkShield Pink)
  - Background: #f5f6fa (Light Gray)

### Modules

#### 1. **Appointments Module** (📅)
- Create, read, update, and delete appointments
- Real-time search by patient or doctor name
- Date and time validation
- Doctor availability checking
- Status management (pending, confirmed, cancelled)
- Input validation with error alerts

#### 2. **Parapharmacie Module** (💊)
- Manage pharmacy products
- Track product inventory and pricing
- Real-time search by product name
- Prevent duplicate product entries
- Professional card-based form layout

#### 3. **Wishlist Module** (❤️)
- Manage user wishlists for pharmacy items
- Link users to pharmacy products
- Prevent duplicate wishlist entries
- Real-time search and filtering

## 🗄️ Database

**Database Name**: `pinkshield_db`
**Connection**: `jdbc:mysql://localhost:3306/pinkshield_db`
**User**: `root`
**Password**: (empty)

### Tables
- `appointment`: Patient/doctor appointments with status tracking
- `parapharmacie`: Pharmacy products with inventory
- `wishlist`: User wishlist items linked to products

## 🚀 How to Run

### Option 1: Using the Batch File (Recommended)

Simply double-click the `RUN_APP.bat` file in the project root directory:

```
C:\Users\driss\IdeaProjects\Projet_java\RUN_APP.bat
```

This will automatically:
- Set up the correct Java 25 environment
- Configure JavaFX modules
- Start the application

### Option 2: Manual Command Line

Open PowerShell and run:

```powershell
$env:JAVA_HOME = "C:\Users\driss\.jdks\openjdk-ea-25+36-3489"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
cd C:\Users\driss\IdeaProjects\Projet_java

$javaFxPath = "C:\Users\driss\.m2\repository\org\openjfx\javafx-controls\21.0.2\javafx-controls-21.0.2-win.jar;C:\Users\driss\.m2\repository\org\openjfx\javafx-graphics\21.0.2\javafx-graphics-21.0.2-win.jar;C:\Users\driss\.m2\repository\org\openjfx\javafx-fxml\21.0.2\javafx-fxml-21.0.2-win.jar;C:\Users\driss\.m2\repository\org\openjfx\javafx-base\21.0.2\javafx-base-21.0.2-win.jar"

& "C:\Users\driss\.jdks\openjdk-ea-25+36-3489\bin\java.exe" --enable-native-access=javafx.graphics --module-path "$javaFxPath" --add-modules javafx.controls,javafx.fxml -cp "target\classes;C:\Users\driss\.m2\repository\com\mysql\mysql-connector-j\8.0.33\mysql-connector-j-8.0.33.jar" org.example.MainApp
```

### Option 3: Using Maven

```bash
set JAVA_HOME=C:\Users\driss\.jdks\openjdk-ea-25+36-3489
mvn clean compile
mvn javafx:run
```

## 🛠️ Build & Compile

To rebuild the project:

```powershell
$env:JAVA_HOME = "C:\Users\driss\.jdks\openjdk-ea-25+36-3489"
$env:Path = "$env:JAVA_HOME\bin;C:\maven\bin;$env:Path"
cd C:\Users\driss\IdeaProjects\Projet_java
mvn clean compile -DskipTests
```

## 📁 Project Structure

```
Projet_java/
├── src/
│   └── main/
│       ├── java/org/example/
│       │   ├── MainApp.java                 # Application entry point
│       │   ├── DashboardController.java     # Dashboard navigation controller
│       │   ├── Appointment.java             # Appointment entity
│       │   ├── AppointmentController.java   # Appointment UI controller
│       │   ├── ServiceAppointment.java      # Appointment JDBC service
│       │   ├── Parapharmacie.java          # Product entity
│       │   ├── ParapharmacieController.java # Product UI controller
│       │   ├── ServiceParapharmacie.java   # Product JDBC service
│       │   ├── Wishlist.java               # Wishlist entity
│       │   ├── WishlistController.java     # Wishlist UI controller
│       │   └── ServiceWishlist.java        # Wishlist JDBC service
│       └── resources/
│           ├── Dashboard.fxml              # Main dashboard layout
│           ├── appointment.fxml            # Appointments view
│           ├── parapharmacie.fxml         # Products view
│           ├── wishlist.fxml              # Wishlist view
│           └── style.css                  # Global styling
├── pom.xml                                 # Maven configuration
├── RUN_APP.bat                             # Convenience launcher
└── README.md                               # This file
```

## 🎯 Architecture

### 3-Layer Architecture
1. **Entity Layer**: Java POJOs representing database tables
2. **Service Layer**: JDBC operations (CRUD) for database access
3. **Controller Layer**: JavaFX event handlers and UI logic

### Key Technologies
- **Language**: Java 25
- **GUI Framework**: JavaFX 21.0.2
- **Database**: MySQL 8.0
- **Build Tool**: Maven 3.9.6
- **JDBC Driver**: MySQL Connector/J 8.0.33

## 🔐 Database Setup

Before running the application, ensure your MySQL database is set up:

```sql
CREATE DATABASE pinkshield_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE pinkshield_db;

CREATE TABLE `appointment` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `patient_email` varchar(255) NOT NULL,
  `patient_name` varchar(255) NOT NULL,
  `doctor_email` varchar(255) NOT NULL,
  `doctor_name` varchar(255) NOT NULL,
  `appointment_date` datetime NOT NULL,
  `status` varchar(50) NOT NULL,
  `notes` text DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `parapharmacie` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nom` varchar(255) NOT NULL,
  `prix` double NOT NULL,
  `stock` int(11) NOT NULL,
  `description` text DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_nom` (`nom`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `wishlist` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `parapharmacie_id` int(11) NOT NULL,
  `added_at` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_wishlist` (`user_id`, `parapharmacie_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

## ✅ Input Validation

All modules implement comprehensive input validation:

- **Email Validation**: Ensures valid email format (contains @)
- **Numeric Validation**: Price must be Double, IDs must be Integer
- **Required Fields**: All fields must be filled before submission
- **Date/Time Validation**: Proper HH:mm format for appointment times
- **Uniqueness Checks**: Prevents duplicate entries via database queries
- **Error Alerts**: Clear user feedback with JavaFX Alert dialogs

## 🔍 Search Features

All modules include real-time search with FilteredList:

- **Appointments**: Filter by patient name or doctor name
- **Parapharmacie**: Filter by product name
- **Wishlist**: Filter by user ID or product ID

## 🎨 Styling

The application includes a comprehensive CSS theme (`style.css`) with:

- Smooth button hover effects
- Professional form inputs with focus states
- Custom TableView styling with header colors
- Card-based layout components
- Rounded corners and subtle shadows
- Consistent color palette (Pink/Dark Gray)
- Responsive spacing and padding

## 📝 Notes

- The application gracefully handles database connection errors during initialization
- Empty table displays are expected when database is not available
- All database operations are wrapped in try-catch blocks with error alerts
- The application automatically exits when the logout button is clicked

## 🐛 Troubleshooting

### Application won't start?
1. Ensure MySQL is running and accessible at `localhost:3306`
2. Verify the database `pinkshield_db` exists
3. Check that Java 25 is properly installed
4. Verify all JAR dependencies are downloaded (Maven will do this automatically)

### JavaFX errors?
- Ensure JavaFX modules are present in the Maven repository
- Try running Maven: `mvn dependency:resolve`

### Database errors?
- Check MySQL connection string in service classes
- Verify user credentials (root / empty password)
- Ensure tables are created with the provided SQL

## 👨‍💻 Development

The project uses Maven for dependency management. To add new dependencies:

1. Edit `pom.xml`
2. Run `mvn dependency:resolve`
3. Rebuild with `mvn clean compile`

## 📄 License

Academic project for educational purposes.

---

**Enjoy PinkShield! 💖**

