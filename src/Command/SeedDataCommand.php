<?php

namespace App\Command;

use App\Entity\Admin;
use App\Entity\Appointment;
use App\Entity\Doctor;
use App\Entity\User;
use App\Repository\AdminRepository;
use App\Repository\AppointmentRepository;
use App\Repository\DoctorRepository;
use App\Repository\UserRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Component\Console\Attribute\AsCommand;
use Symfony\Component\Console\Command\Command;
use Symfony\Component\Console\Input\InputInterface;
use Symfony\Component\Console\Output\OutputInterface;
use Symfony\Component\PasswordHasher\Hasher\UserPasswordHasherInterface;

#[AsCommand(
    name: 'app:seed-data',
    description: 'Seeds the database with sample doctors and users'
)]
class SeedDataCommand extends Command
{
    public function __construct(
        private EntityManagerInterface $entityManager,
        private UserPasswordHasherInterface $passwordHasher,
        private AdminRepository $adminRepository,
        private DoctorRepository $doctorRepository,
        private UserRepository $userRepository,
        private AppointmentRepository $appointmentRepository
    ) {
        parent::__construct();
    }

    protected function execute(InputInterface $input, OutputInterface $output): int
    {
        $output->writeln('🌱 Seeding database with sample data...');

        // Create Admin
        $output->writeln('Creating admin account...');
        $existingAdmin = $this->adminRepository->findByEmail("admin@pinkshield.com");
        if (!$existingAdmin) {
            $admin = new Admin();
            $admin->setEmail("admin@pinkshield.com");
            $admin->setRoles(['ROLE_ADMIN']);
            $hashedPassword = $this->passwordHasher->hashPassword($admin, 'admin123');
            $admin->setPassword($hashedPassword);
            $this->entityManager->persist($admin);
            $this->entityManager->flush();
            $output->writeln("  ✓ Created: admin@pinkshield.com");
        } else {
            $output->writeln("  - Already exists: admin@pinkshield.com");
        }

        // Doctor specialties
        $specialties = [
            'Cardiology',
            'Dermatology',
            'Neurology',
            'Orthopedics',
            'Pediatrics',
            'Psychiatry',
            'Radiology',
            'Surgery',
            'Gastroenterology',
            'Oncology'
        ];

        // Check if doctors already exist
        $existingDoctors = $this->doctorRepository->findAll();
        if (count($existingDoctors) > 0) {
            $output->writeln('  - Doctors already exist, skipping...');
        } else {
            // Create 10 Doctors
            $output->writeln('Creating 10 doctors...');
            for ($i = 1; $i <= 10; $i++) {
                $doctor = new Doctor();
                $doctor->setEmail("doctor{$i}@pinkshield.com");
                $doctor->setFullName("Dr. Doctor {$i}");
                $doctor->setSpeciality($specialties[$i - 1]);
                $doctor->setRoles(['ROLE_DOCTOR']);
                $doctor->setStatus('active');
                
                $hashedPassword = $this->passwordHasher->hashPassword($doctor, 'password123');
                $doctor->setPassword($hashedPassword);
                
                $this->entityManager->persist($doctor);
                $output->writeln("  ✓ Created: doctor{$i}@pinkshield.com");
            }
            $this->entityManager->flush();
        }

        // Check if users already exist
        $existingUsers = $this->userRepository->findAll();
        if (count($existingUsers) > 0) {
            $output->writeln('  - Users already exist, skipping...');
        } else {
            // Create 10 Users
            $output->writeln('Creating 10 users...');
            for ($i = 1; $i <= 10; $i++) {
                $user = new User();
                $user->setEmail("user{$i}@pinkshield.com");
                $user->setFirstName("Patient");
                $user->setLastName("User {$i}");
                $user->setFullName("Patient User {$i}");
                $user->setPhone("+1-555-000-" . str_pad($i, 4, '0', STR_PAD_LEFT));
                $user->setAddress("123 Main Street, City {$i}, State");
                $user->setRoles(['ROLE_USER']);
                $user->setStatus('active');
                
                $hashedPassword = $this->passwordHasher->hashPassword($user, 'password123');
                $user->setPassword($hashedPassword);
                
                $this->entityManager->persist($user);
                $output->writeln("  ✓ Created: user{$i}@pinkshield.com");
            }
            $this->entityManager->flush();
        }
        
        // Create sample appointments for test users
        $output->writeln('Creating sample appointments...');
        $testUserAppointments = count($this->appointmentRepository->findByPatient("user1@pinkshield.com"));
        if ($testUserAppointments === 0) {
            $doctors = $this->doctorRepository->findAll();
            if (count($doctors) > 0) {
                // Create 3 appointments for each test user
                for ($userIdx = 1; $userIdx <= 5; $userIdx++) {
                    $doctorIndex = ($userIdx - 1) % count($doctors);
                    $doctor = $doctors[$doctorIndex];
                    
                    for ($aptIdx = 1; $aptIdx <= 3; $aptIdx++) {
                        $appointment = new Appointment();
                        $appointment->setPatientEmail("user{$userIdx}@pinkshield.com");
                        $appointment->setPatientName("Patient User {$userIdx}");
                        $appointment->setDoctorEmail($doctor->getEmail());
                        $appointment->setDoctorName($doctor->getFullName());
                        
                        // Create appointment dates in the future
                        $date = new \DateTime();
                        $date->add(new \DateInterval('P' . (5 + $userIdx * 2 + $aptIdx) . 'D'));
                        $appointment->setAppointmentDate($date);
                        
                        // Vary the status
                        $statuses = ['pending', 'confirmed', 'confirmed'];
                        $appointment->setStatus($statuses[$aptIdx % 3]);
                        
                        $appointment->setNotes("Sample appointment " . $aptIdx . " for user " . $userIdx);
                        
                        $this->entityManager->persist($appointment);
                    }
                }
                $this->entityManager->flush();
                $output->writeln("  ✓ Created sample appointments");
            }
        } else {
            $output->writeln("  - Test user appointments already exist, skipping...");
        }
        
        $output->writeln('');
        $output->writeln('<info>✅ Database seeded successfully!</info>');
        $output->writeln('');
        $output->writeln('Test Credentials:');
        $output->writeln('  Admin: admin@pinkshield.com (password: admin123)');
        $output->writeln('  Doctors: doctor1@pinkshield.com - doctor10@pinkshield.com (password: password123)');
        $output->writeln('  Users: user1@pinkshield.com - user10@pinkshield.com (password: password123)');

        return Command::SUCCESS;
    }
}
