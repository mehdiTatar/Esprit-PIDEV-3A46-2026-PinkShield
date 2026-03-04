# PinkShield — Medical Management System

![Symfony](https://img.shields.io/badge/Symfony-7.4-black?logo=symfony)
![PHP](https://img.shields.io/badge/PHP-8.2+-777BB4?logo=php&logoColor=white)
![Bootstrap](https://img.shields.io/badge/Bootstrap-5.3-7952B3?logo=bootstrap&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-green)

A full-featured Symfony 7 medical management platform with role-based access control (RBAC) for **Patients**, **Doctors**, and **Administrators**.

---

## ✨ Features

| Module | Status | Roles |
|---|---|---|
| Authentication (Login / Register) | ✅ Live | All |
| Role-Based Dashboards | ✅ Live | Admin, Doctor, Patient |
| User / Doctor / Admin CRUD | ✅ Live | Admin |
| Appointment Management | ✅ Live | Doctor, Patient |
| Blog & Forum | ✅ Live | All |
| Parapharmacy / Product Catalogue | ✅ Live | Doctor, Patient |
| Health Tracking | ✅ Live | Patient |
| Doctor Ratings | ✅ Live | Patient |
| Wishlist | ✅ Live | Patient |
| Notifications (real-time polling) | ✅ Live | All |

---

## 🏗️ Project Structure

```
src/
├── Controller/
│   ├── AuthController.php          # Login, Register
│   ├── AdminController.php         # Admin CRUD
│   ├── DoctorController.php        # Doctor CRUD
│   ├── UserController.php          # Patient CRUD
│   ├── AppointmentController.php   # Appointments
│   ├── BlogController.php          # Blog & Forum
│   ├── ForumController.php         # Forum threads
│   ├── NotificationController.php  # Notifications + API
│   ├── ParapharmacyController.php  # Product catalogue
│   ├── RatingController.php        # Doctor ratings
│   ├── TrackingController.php      # Health tracking
│   └── WishlistController.php      # Wishlist
├── Entity/
│   ├── User.php                    # Patient entity
│   ├── Doctor.php                  # Doctor entity
│   ├── Admin.php                   # Admin entity
│   ├── Appointment.php
│   ├── Blog.php / Forum.php
│   ├── Notification.php
│   ├── Product.php
│   ├── Rating.php
│   ├── Tracking.php
│   └── Wishlist.php
└── Repository/                     # One repository per entity

templates/
├── base.html.twig                  # Global layout (navbar, sidebar, footer)
├── auth/                           # Login & Registration pages
├── dashboard/                      # Role dashboards (admin, doctor, user)
├── appointment/                    # Appointment CRUD views
├── blog/ & notification/           # Blog & notification views
├── doctor/ & user/ & admin/        # Entity CRUD views
├── parapharmacy/ & wishlist/       # Product & wishlist views
├── rating/ & tracking/             # Rating & health tracking views
└── ...
```

---

## 🚀 Quick Start

### Prerequisites
- PHP 8.2+
- Composer
- MySQL 8.0+
- Symfony CLI *(optional but recommended)*

### 1. Install Dependencies

```bash
composer install
```

### 2. Configure Environment

Copy `.env` and edit your local settings:

```bash
cp .env .env.local
```

Edit `.env.local`:

```dotenv
# Database connection
DATABASE_URL="mysql://root:YOUR_PASSWORD@127.0.0.1:3306/pinkshield_db"

# Google reCAPTCHA (get keys at https://www.google.com/recaptcha)
RECAPTCHA_SITE_KEY=your_site_key
RECAPTCHA_SECRET_KEY=your_secret_key

# App environment
APP_ENV=dev
APP_SECRET=your_random_secret_here
```

### 3. Set Up the Database

```bash
# Create the database
php bin/console doctrine:database:create

# Run migrations
php bin/console doctrine:migrations:migrate

# (Optional) Load sample data
mysql -u root pinkshield_db < pinkshield_db.sql
```

### 4. Start the Server

```bash
# Using Symfony CLI (recommended)
symfony server:start

# Or PHP built-in server
php -S localhost:8000 -t public
```

### 5. Access the App

| URL | Description |
|---|---|
| `http://localhost:8000/login` | Login page |
| `http://localhost:8000/register` | Registration |
| `http://localhost:8000/admin/dashboard` | Admin dashboard |
| `http://localhost:8000/doctor/dashboard` | Doctor dashboard |
| `http://localhost:8000/user/dashboard` | Patient dashboard |

---

## 🐳 Docker (Optional)

A `compose.yaml` is provided for containerised development:

```bash
docker compose up -d
```

Override settings in `compose.override.yaml` for local customisation.

---

## 🔐 Security & Access Control

| Path | Required Role |
|---|---|
| `/admin/*` | `ROLE_ADMIN` |
| `/doctor/*` | `ROLE_DOCTOR` |
| `/user/*` | `ROLE_USER` |
| `/login`, `/register` | Public |

- Passwords hashed with **bcrypt**
- Session timeout: **7 days** with "Remember Me"
- Google **reCAPTCHA v2** on login

---

## 📊 Key Database Tables

| Table | Key Fields |
|---|---|
| `user` | id, email, password, full_name, phone, roles |
| `doctor` | id, email, password, full_name, speciality, roles |
| `admin` | id, email, password, roles |
| `appointment` | id, user_id, doctor_id, date, status |
| `blog` | id, title, content, author, created_at |
| `notification` | id, user_id, title, message, is_read, created_at |
| `product` | id, name, description, price, category |
| `rating` | id, user_id, doctor_id, score, comment |
| `tracking` | id, user_id, metric, value, recorded_at |
| `wishlist` | id, user_id, product_id |

---

## 🎨 Customisation

### Change the Colour Scheme

Edit the CSS variables in `templates/base.html.twig`:

```css
:root {
    --primary:      #4CAF87;   /* Main brand colour */
    --primary-dark: #3a8f6f;   /* Hover / dark variant */
    --secondary:    #B8E6DB;   /* Navbar background */
    --light-bg:     #f4fdf8;   /* Page background */
}
```

### Add a New Module

1. Create entity in `src/Entity/`
2. Generate migration: `php bin/console make:migration`
3. Create controller in `src/Controller/`
4. Add templates in `templates/<module>/`
5. Add sidebar link in `templates/base.html.twig`

---

## 🛠️ Useful Commands

```bash
# Clear cache
php bin/console cache:clear

# List all routes
php bin/console debug:router

# Create a new migration
php bin/console make:migration

# Apply migrations
php bin/console doctrine:migrations:migrate

# Check security vulnerabilities
composer audit
```

---

## 🆘 Troubleshooting

| Problem | Solution |
|---|---|
| Blank page / 500 error | Run `php bin/console cache:clear` |
| Database connection error | Check `DATABASE_URL` in `.env.local` |
| `var/` permission denied | `chmod -R 777 var/` (Linux/Mac) |
| reCAPTCHA not loading | Verify `RECAPTCHA_SITE_KEY` in `.env.local` |
| Migrations fail | Ensure DB exists: `php bin/console doctrine:database:create` |

---

## 📚 Resources

- [Symfony Documentation](https://symfony.com/doc/current/)
- [Symfony Security](https://symfony.com/doc/current/security.html)
- [Doctrine ORM](https://symfony.com/doc/current/doctrine.html)
- [Bootstrap 5](https://getbootstrap.com/docs/5.3/)

---

**PinkShield v2.0** — Built with ❤️ using Symfony 7.4
