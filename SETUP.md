# PinkShield Setup Guide

## Quick Start

Follow these steps to get your PinkShield application running:

### Step 1: Create Database Schema

**Option A: Using phpMyAdmin**
1. Open phpMyAdmin (usually at `http://localhost/phpmyadmin`)
2. Select the `pinkshield_db` database
3. Click on the "SQL" tab
4. Copy and paste the contents of `schema.sql` file
5. Click "Go" to execute

**Option B: Using MySQL Command Line**
```bash
mysql -u root pinkshield_db < schema.sql
```

**Option C: Using PHP Console**
```bash
php bin/console doctrine:schema:update --force
```

### Step 2: Install Composer Dependencies

```bash
composer install
```

### Step 3: Clear Cache

```bash
php bin/console cache:clear
```

### Step 4: Start the Server

**Option A: Using Symfony CLI (Recommended)**
```bash
symfony server:start
```

**Option B: Using PHP Built-in Server**
```bash
php -S localhost:8000 -t public
```

### Step 5: Access the Application

Open your browser and navigate to:
```
http://localhost:8000/login
```

## Creating Test Accounts

### For Patients (Users)
1. Go to `/register`
2. Select "Patient" as account type
3. Fill in:
   - Email: `patient@example.com`
   - Full Name: `John Doe`
   - Phone: `+33123456789` (optional)
   - Password: `password123`
4. Login with these credentials

### For Doctors
1. Go to `/register`
2. Select "Doctor" as account type
3. Fill in:
   - Email: `doctor@example.com`
   - Full Name: `Dr. Jane Smith`
   - Speciality: `Cardiology`
   - Password: `password123`
4. Login with these credentials

### For Admins
You'll need to create an admin directly in the database or through the admin panel (requires existing admin account).

**Database Method:**
```sql
INSERT INTO admin (email, password, roles) VALUES (
  'admin@example.com',
  '$2y$13$YOUR_HASHED_PASSWORD_HERE',
  '["ROLE_ADMIN"]'
);
```

Or use an online bcrypt generator to hash `password123` and use that value.

## Project Features at a Glance

| Feature | Patient | Doctor | Admin |
|---------|---------|--------|-------|
| View Dashboard | ✅ | ✅ | ✅ |
| Edit Profile | ✅ | ✅ | ✅ |
| Manage Users | ❌ | ❌ | ✅ |
| Manage Doctors | ❌ | ❌ | ✅ |
| Manage Admins | ❌ | ❌ | ✅ |
| Blog & Forum | 🔜 | 🔜 | 🔜 |
| Appointments | 🔜 | 🔜 | 🔜 |

## File Structure Overview

```
Pink/
├── config/
│   └── packages/
│       └── security.yaml          # Security configuration
├── src/
│   ├── Controller/                # All controllers
│   ├── Entity/                    # User, Doctor, Admin entities
│   └── Repository/                # Data access layer
├── templates/
│   ├── base.html.twig            # Navigation bar
│   ├── auth/                      # Login/Register
│   ├── dashboard/                 # Role dashboards
│   ├── user/                      # Patient CRUD
│   ├── doctor/                    # Doctor CRUD
│   └── admin/                     # Admin CRUD
├── public/
│   └── index.php                  # Entry point
├── .env.local                     # Database configuration
├── schema.sql                     # Database schema
└── README.md                      # Full documentation
```

## Common Issues & Solutions

### Issue: "Access denied" at login
**Solution**: Verify database tables exist using:
```bash
php bin/console doctrine:schema:update --dump-sql
```

### Issue: "Class not found" error
**Solution**: Clear cache and regenerate autoloader:
```bash
php bin/console cache:clear
composer dump-autoload
```

### Issue: Password hash error during registration
**Solution**: Ensure `symfony/security-bundle` is installed:
```bash
composer require symfony/security-bundle
```

### Issue: Cannot connect to database
**Solution**: Check `.env.local` file:
```
DATABASE_URL="mysql://root:@127.0.0.1:3306/pinkshield_db?serverVersion=8.0&charset=utf8mb4"
```

## Customizing Placeholder Features

### Replace "Gestion Blog & Forum"

1. Create new controller: `src/Controller/BlogController.php`
2. Create new templates: `templates/blog/` directory
3. In `templates/base.html.twig`, find:
   ```twig
   <a class="nav-link" href="#">Gestion Blog & Forum</a>
   ```
4. Replace with:
   ```twig
   <a class="nav-link" href="{{ path('blog_index') }}">Gestion Blog & Forum</a>
   ```

### Replace "Gestion Rendez-vous & Parapharmacy"

1. Create new controller: `src/Controller/AppointmentController.php`
2. Create new templates: `templates/appointment/` directory
3. In `templates/base.html.twig`, find:
   ```twig
   <a class="nav-link" href="#">Gestion Rendez-vous & Parapharmacy</a>
   ```
4. Replace with:
   ```twig
   <a class="nav-link" href="{{ path('appointment_index') }}">Gestion Rendez-vous & Parapharmacy</a>
   ```

## Testing the System

### 1. Patient Login Flow
- Register as patient
- Verify redirect to `/user/dashboard`
- Check navbar shows patient options
- Edit profile and verify changes

### 2. Doctor Login Flow
- Register as doctor
- Verify redirect to `/doctor/dashboard`
- Check navbar shows doctor options
- Edit profile and verify changes

### 3. Admin Login Flow
- Login as admin (if created)
- Verify redirect to `/admin/dashboard`
- Check admin-specific management links
- Create, edit, and delete users/doctors

## Environment Variables

### Default .env.local
```env
APP_ENV=dev
APP_SECRET=your_secret_here
DATABASE_URL="mysql://root:@127.0.0.1:3306/pinkshield_db?serverVersion=8.0&charset=utf8mb4"
```

## Performance Tips

1. Use database indexes on frequently searched fields
2. Enable query caching in production
3. Minify CSS/JS assets: `php bin/console assets:install`
4. Use asset versioning for cache busting

## Security Reminders

1. Change `APP_SECRET` in production
2. Use strong passwords
3. Enable HTTPS in production
4. Implement CSRF protection (already built-in)
5. Sanitize all user inputs
6. Keep Symfony updated

## Next Steps

1. ✅ Complete the setup
2. ✅ Test the application
3. 🔄 Integrate your Blog & Forum module
4. 🔄 Integrate your Appointment & Parapharmacy module
5. 📝 Add additional features as needed

## Support & Documentation

- **Symfony Docs**: https://symfony.com/doc/
- **Bootstrap Docs**: https://getbootstrap.com/docs/
- **Doctrine ORM**: https://www.doctrine-project.org/projects/doctrine-orm/

---

**Good Luck with PinkShield!** 🎉
