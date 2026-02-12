# PinkShield - Medical Management System

A comprehensive Symfony-based medical management system with role-based access control (RBAC) for Patients, Doctors, and Administrators.

## 🎯 Features

### ✅ Implemented
- **3 User Entities with Full CRUD**
  - User (Patient) - with email, fullName, phone
  - Doctor - with email, fullName, speciality
  - Admin - with email only
  
- **Authentication & Authorization**
  - Role-based login system
  - Automatic dashboard redirection based on roles
  - Session management with "Remember Me"
  - Password hashing (bcrypt)
  
- **Role-Based Access Control**
  - ROLE_USER - Patient access
  - ROLE_DOCTOR - Doctor access
  - ROLE_ADMIN - Full system administration
  
- **Dashboards**
  - `/user/dashboard` - Patient dashboard
  - `/doctor/dashboard` - Doctor dashboard
  - `/admin/dashboard` - Admin dashboard with management options
  
- **Navigation Bar**
  - Dynamic navbar based on user role
  - Edit Profile functionality for each role
  - Logout button
  - Placeholder buttons for:
    - Gestion Blog & Forum
    - Gestion Rendez-vous & Parapharmacy
  - Admin-specific management links
  
- **CRUD Operations**
  - Users: List, Create, Read, Update, Delete
  - Doctors: List, Create, Read, Update, Delete
  - Admins: List, Create, Read, Update, Delete
  - Only admins can perform management operations
  - Users can edit their own profiles

### 📋 Placeholder Features (Ready for Integration)
- Blog & Forum Management
- Appointment & Parapharmacy Management

## 📁 Project Structure

```
src/
├── Controller/
│   ├── AuthController.php       # Login, Register
│   ├── DashboardController.php  # Role-based dashboard routing
│   ├── UserController.php       # User CRUD
│   ├── DoctorController.php     # Doctor CRUD
│   └── AdminController.php      # Admin CRUD
├── Entity/
│   ├── User.php                 # Patient entity
│   ├── Doctor.php               # Doctor entity
│   └── Admin.php                # Admin entity
└── Repository/
    ├── UserRepository.php
    ├── DoctorRepository.php
    └── AdminRepository.php

templates/
├── base.html.twig               # Base template with navbar
├── auth/
│   ├── login.html.twig
│   └── register.html.twig
├── dashboard/
│   ├── admin.html.twig
│   ├── doctor.html.twig
│   └── user.html.twig
├── user/
│   ├── index.html.twig
│   ├── form.html.twig
│   └── show.html.twig
├── doctor/
│   ├── index.html.twig
│   ├── form.html.twig
│   └── show.html.twig
└── admin/
    ├── index.html.twig
    ├── form.html.twig
    └── show.html.twig
```

## 🚀 Installation & Setup

### 1. Database Setup
Execute the `schema.sql` file in phpMyAdmin or MySQL:
```sql
SOURCE schema.sql;
```

Or run using MySQL command line:
```bash
mysql -u root pinkshield_db < schema.sql
```

### 2. Install Dependencies (if not already done)
```bash
composer install
```

### 3. Clear Cache
```bash
php bin/console cache:clear
```

### 4. Start Symfony Server
```bash
symfony server:start
```

Or use PHP built-in server:
```bash
php -S localhost:8000 -t public
```

### 5. Access the Application
- **URL**: `http://localhost:8000/login` (or port 8000 in Symfony guide)
- **Test Credentials**: Register an account first

## 🔐 Security Configuration

### Authentication
- Form-based login with email
- Chain provider for multiple user types
- Password hashing using bcrypt

### Authorization
Access control rules in `config/packages/security.yaml`:
- `/admin/*` → ROLE_ADMIN only
- `/doctor/*` → ROLE_DOCTOR only
- `/user/*` → ROLE_USER only
- `/login` & `/register` → PUBLIC_ACCESS

## 🔗 Routes

### Authentication
- `GET/POST /login` - User login
- `GET/POST /register` - User/Doctor registration
- `GET /logout` - User logout
- `GET /dashboard` - Auto-redirect to role dashboard

### User Dashboard
- `GET /user/dashboard` - Patient dashboard
- `GET /user/` - User list (admin only)
- `GET/POST /user/new` - Create user (admin only)
- `GET/POST /user/{id}/edit` - Edit user profile
- `GET /user/{id}` - View user details (admin only)
- `GET /user/{id}/delete` - Delete user (admin only)

### Doctor Dashboard
- `GET /doctor/dashboard` - Doctor dashboard
- `GET /doctor/` - Doctor list (admin only)
- `GET/POST /doctor/new` - Create doctor (admin only)
- `GET/POST /doctor/{id}/edit` - Edit doctor profile
- `GET /doctor/{id}` - View doctor details (admin only)
- `GET /doctor/{id}/delete` - Delete doctor (admin only)

### Admin Dashboard
- `GET /admin/dashboard` - Admin dashboard
- `GET /admin/manage-admins` - Admin list
- `GET/POST /admin/new` - Create admin
- `GET/POST /admin/{id}/edit` - Edit admin
- `GET /admin/{id}` - View admin details
- `GET /admin/{id}/delete` - Delete admin

## 📊 Database Tables

### user
- `id` (int, PK, AI)
- `email` (varchar, unique)
- `password` (varchar)
- `full_name` (varchar)
- `phone` (varchar, nullable)
- `roles` (json)

### doctor
- `id` (int, PK, AI)
- `email` (varchar, unique)
- `password` (varchar)
- `full_name` (varchar)
- `speciality` (varchar)
- `roles` (json)

### admin
- `id` (int, PK, AI)
- `email` (varchar, unique)
- `password` (varchar)
- `roles` (json)

## 🎨 UI Features

### Bootstrap 5 Integration
- Responsive navigation bar
- Card-based layouts
- Form styling
- Table styling for data management
- Color scheme: Pink (#e91e63) accent color

### Navbar Features
- **Logged-in Users**: Dynamic navbar based on role
- **Admin Menu**: Dropdown with management links
- **Profile Menu**: Edit profile and logout
- **Placeholder Buttons**: Ready for integration

## 🔄 Next Steps - Integration Points

To integrate the Blog & Forum and Appointment modules:

1. **Create new entities** in `src/Entity/`
2. **Create new controllers** in `src/Controller/`
3. **Update routes** to point to your new modules
4. **Replace placeholder links** in `base.html.twig`:
   - `<!-- Gestion Blog & Forum -->` → Your blog route
   - `<!-- Gestion Rendez-vous & Parapharmacy -->` → Your appointment route

## 🛠️ Customization

### Change Colors
Edit `templates/base.html.twig` and replace `#e91e63` with your preferred color.

### Change Database Connection
Edit `.env.local`:
```
DATABASE_URL="mysql://root:password@127.0.0.1:3306/pinkshield_db"
```

### Add New Fields to Entities
Edit the entity files in `src/Entity/` and update the database schema.

## 📝 Notes

- All passwords are automatically hashed using bcrypt
- Email addresses are unique per entity
- Forms include validation
- Delete actions require confirmation
- Admin users have full system access
- Session timeout is set to 7 days with "Remember Me"

## 🆘 Troubleshooting

If you encounter issues:

1. **Clear cache**: `php bin/console cache:clear`
2. **Create schema**: Execute `schema.sql` in MySQL
3. **Check permissions**: Ensure `var/` directory is writable
4. **Verify database**: Ensure `pinkshield_db` exists and is accessible

## 📞 Support

For issues or questions, refer to the Symfony documentation:
- https://symfony.com/doc/current/
- https://symfony.com/doc/current/security.html
- https://symfony.com/doc/current/doctrine.html

---

**PinkShield v1.0** - Built with Symfony 7.4
