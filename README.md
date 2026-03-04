# PinkShield – Medical Management Platform

![Symfony](https://img.shields.io/badge/Symfony-6.4-black?logo=symfony)
![PHP](https://img.shields.io/badge/PHP-8.2+-777BB4?logo=php&logoColor=white)
![Bootstrap](https://img.shields.io/badge/Bootstrap-5.3-7952B3?logo=bootstrap&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-green)

---

## Overview

This project was developed as part of the **PIDEV – 3rd Year Engineering Program** at **Esprit School of Engineering** (Academic Year 2025–2026).

**PinkShield** is a full-stack web application built with Symfony that provides a comprehensive medical management platform. It enables patients to book appointments, track their health, browse a parapharmacy catalog, and interact through a medical blog & forum. Doctors can manage their schedules, view ratings, and monitor patient activity. Administrators have full control over users, content, and system health through a feature-rich command center dashboard.

---

## Features

| Module                             | Description                                               | Roles          |
|------------------------------------|-----------------------------------------------------------|----------------|
| Authentication (Login / Register)  | Secure login with reCAPTCHA, 2FA, Face ID                 | All            |
| Role-Based Dashboards              | Dedicated dashboards with KPIs and charts                 | Admin, Doctor, Patient |
| User / Doctor / Admin CRUD         | Full management of all user types                         | Admin          |
| Appointment Management             | Book, confirm, cancel appointments with calendar          | Doctor, Patient|
| Blog & Forum                       | Share knowledge, ask questions, comment with threading     | All            |
| Parapharmacy / Product Catalogue   | Browse, search, and filter medical products               | Doctor, Patient|
| Health Tracking                    | Daily vitals and health metrics monitoring                | Patient        |
| Doctor Ratings                     | Rate and review doctors                                   | Patient        |
| Wishlist                           | Save favorite products                                    | Patient        |
| Notifications (real-time polling)  | System alerts, appointment reminders                      | All            |
| AI Health Assistant                | WebLLM-powered chatbot for health queries                 | All            |

---

## Tech Stack

### Frontend
- **HTML5 / CSS3** – Semantic markup with custom dark-theme design system
- **JavaScript (ES6+)** – Client-side interactivity, AJAX validation, Chart.js dashboards
- **Bootstrap 5.3** – Responsive grid and UI components
- **Font Awesome 6** – Icon library
- **Google Fonts (Inter, Orbitron)** – Modern typography
- **Chart.js** – Dashboard charts (doughnut, bar, line)
- **face-api.js** – Face recognition authentication
- **WebLLM** – Browser-based AI health assistant

### Backend
- **PHP 8.2+** – Server-side language
- **Symfony 6.4** – PHP framework (MVC architecture)
- **Doctrine ORM** – Database abstraction and entity management
- **Twig** – Template engine
- **MySQL 8.0** – Relational database
- **Google reCAPTCHA v2** – Bot protection
- **Symfony Mailer** – Email notifications
- **PHPUnit** – Unit & integration testing
- **PHPStan (Level 5)** – Static analysis

---

## Architecture

```
src/
├── Command/                        # Console commands (SeedData)
├── Controller/
│   ├── AuthController.php          # Login, Register, Password Reset
│   ├── AdminController.php         # Admin CRUD + Blog management
│   ├── DashboardController.php     # Role-based dashboards
│   ├── DoctorController.php        # Doctor CRUD
│   ├── UserController.php          # Patient CRUD
│   ├── AppointmentController.php   # Appointment management
│   ├── BlogController.php          # Blog & Forum
│   ├── NotificationController.php  # Notifications + API
│   ├── ParapharmacieController.php # Product catalogue
│   ├── RatingController.php        # Doctor ratings
│   ├── TrackingController.php      # Health tracking
│   └── WishlistController.php      # Wishlist
├── Entity/                         # Doctrine entities (User, Doctor, Admin, etc.)
├── Form/                           # Symfony form types
├── Repository/                     # Database query repositories
└── Service/                        # Business logic services

templates/
├── base.html.twig                  # Global layout (navbar, sidebar, footer)
├── auth/                           # Login & Registration pages
├── dashboard/                      # Role dashboards (admin, doctor, user)
├── appointment/                    # Appointment views
├── blog/                           # Blog & Forum views
├── admin/                          # Admin management views
├── doctor/ & user/                 # Entity CRUD views
├── parapharmacy/ & wishlist/       # Product & wishlist views
└── rating/ & tracking/             # Rating & health tracking views

config/
├── packages/                       # Bundle configurations
├── routes/                         # Route definitions
└── services.yaml                   # Dependency injection

public/
├── css/                            # Stylesheets
├── js/                             # JavaScript files
└── images/                         # Static assets
```

---

## Contributors

| Name            | Role       |
|-----------------|------------|
| Fedy Drissi     | Developer  |
| Nede Kadri      | Developer  |

---

## Academic Context

Developed at **Esprit School of Engineering – Tunisia**

**PIDEV – 3A46 | 2025–2026**

This project was built as part of the integrated development project (Projet Intégré de Développement) during the 3rd year of the engineering program. It demonstrates full-stack web development skills using the Symfony framework, database management with Doctrine ORM, and modern frontend design practices.

---

## Getting Started

### Prerequisites
- PHP 8.2+
- Composer
- MySQL 8.0+
- Symfony CLI *(optional but recommended)*
- XAMPP *(alternative local server)*

### 1. Clone the Repository

```bash
git clone https://github.com/<your-username>/Esprit-PIDEV-3A46-2026-PinkShield.git
cd Esprit-PIDEV-3A46-2026-PinkShield
```

### 2. Install Dependencies

```bash
composer install
```

### 3. Configure Environment

```bash
cp .env .env.local
```

Edit `.env.local`:

```dotenv
DATABASE_URL="mysql://root:@127.0.0.1:3306/pinkshield_db"
RECAPTCHA_SITE_KEY=your_site_key
RECAPTCHA_SECRET_KEY=your_secret_key
APP_ENV=dev
```

### 4. Set Up the Database

```bash
php bin/console doctrine:database:create
php bin/console doctrine:migrations:migrate
# (Optional) Load sample data
mysql -u root pinkshield_db < pinkshield_db.sql
```

### 5. Start the Server

```bash
symfony server:start
# OR
php -S localhost:8000 -t public
```

### 6. Access the App

| URL                                    | Description        |
|----------------------------------------|--------------------|
| `http://localhost:8000/login`           | Login page         |
| `http://localhost:8000/register`        | Registration       |
| `http://localhost:8000/admin/dashboard` | Admin dashboard    |
| `http://localhost:8000/doctor/dashboard`| Doctor dashboard   |
| `http://localhost:8000/user/dashboard`  | Patient dashboard  |

---

## Acknowledgments

- **Esprit School of Engineering** – Academic supervision and program framework
- [Symfony](https://symfony.com/) – PHP framework
- [Bootstrap](https://getbootstrap.com/) – Frontend framework
- [Chart.js](https://www.chartjs.org/) – Data visualization
- [Font Awesome](https://fontawesome.com/) – Icons
- [face-api.js](https://github.com/justadudewhohacks/face-api.js) – Face recognition
- [WebLLM](https://webllm.mlc.ai/) – Browser-based AI assistant
