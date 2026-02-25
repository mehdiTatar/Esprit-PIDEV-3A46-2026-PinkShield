<?php

namespace App\Command;

use App\Entity\Admin;
use App\Entity\Appointment;
use App\Entity\Doctor;
use App\Entity\HealthLog;
use App\Entity\Notification;
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

        // Create sample health logs
        $output->writeln('Creating sample health logs...');
        $users = $this->userRepository->findAll();
        if (count($users) > 0) {
            $moodStressData = [
                ['mood' => 5, 'stress' => 1, 'activities' => 'Morning workout, yoga, meditation'],
                ['mood' => 4, 'stress' => 2, 'activities' => 'Work meeting, lunch break, walked to the park'],
                ['mood' => 3, 'stress' => 3, 'activities' => 'Regular day at work, some challenging tasks'],
                ['mood' => 3, 'stress' => 4, 'activities' => 'Stressful meeting, tight deadlines'],
                ['mood' => 4, 'stress' => 2, 'activities' => 'Gym session, dinner with friends'],
                ['mood' => 5, 'stress' => 1, 'activities' => 'Day off, relaxation, reading'],
                ['mood' => 2, 'stress' => 5, 'activities' => 'Difficult project, lack of sleep'],
                ['mood' => 4, 'stress' => 1, 'activities' => 'Swimming, healthy meals, good sleep'],
            ];

            foreach ($users as $user) {
                for ($i = 0; $i < 5; $i++) {
                    $data = $moodStressData[array_rand($moodStressData)];
                    $log = new HealthLog();
                    $log->setUserEmail($user->getEmail());
                    $log->setMood($data['mood']);
                    $log->setStress($data['stress']);
                    $log->setActivities($data['activities']);
                    
                    // Set created date in the past
                    $date = new \DateTime();
                    $date->sub(new \DateInterval('P' . ($i + 1) . 'D'));
                    $log->setCreatedAt($date);
                    
                    $this->entityManager->persist($log);
                }
            }
            $this->entityManager->flush();
            $output->writeln("  ✓ Created sample health logs for all users");
        }

        // Create sample notifications
        $output->writeln('Creating sample notifications...');
        $users = $this->userRepository->findAll();
        $doctors = $this->doctorRepository->findAll();

        if (count($users) > 0) {
            $userNotifications = [
                ['title' => 'Appointment Reminder', 'message' => 'You have an appointment scheduled with Dr. Smith tomorrow at 2:00 PM. Please arrive 15 minutes early.', 'type' => 'info', 'icon' => 'fas fa-calendar-alt'],
                ['title' => 'Prescription Ready', 'message' => 'Your prescription is now available for pickup at the pharmacy.', 'type' => 'success', 'icon' => 'fas fa-pills'],
                ['title' => 'Health Report Updated', 'message' => 'Your latest health report has been updated. Review your health metrics in the dashboard.', 'type' => 'success', 'icon' => 'fas fa-chart-line'],
                ['title' => 'Lab Results Available', 'message' => 'Your lab test results are now available. Please consult your doctor for interpretation.', 'type' => 'warning', 'icon' => 'fas fa-flask'],
                ['title' => 'Appointment Cancelled', 'message' => 'Your appointment scheduled for next Monday has been cancelled. Please reschedule if needed.', 'type' => 'danger', 'icon' => 'fas fa-calendar-times'],
                ['title' => 'Health Goal Progress', 'message' => 'Great job! You\'ve achieved 80% of your weekly health goals.', 'type' => 'success', 'icon' => 'fas fa-trophy'],
                ['title' => 'Doctor Review Request', 'message' => 'Dr. Johnson has shared feedback on your recent appointment. Read the full review in your messages.', 'type' => 'info', 'icon' => 'fas fa-comment-medical'],
                ['title' => 'Medication Reminder', 'message' => 'Time to take your scheduled medication. Set a reminder on your phone.', 'type' => 'warning', 'icon' => 'fas fa-clock'],
            ];

            foreach ($users as $user) {
                for ($i = 0; $i < 3; $i++) {
                    $notifData = $userNotifications[array_rand($userNotifications)];
                    $notification = new Notification();
                    $notification->setUser($user);
                    $notification->setTitle($notifData['title']);
                    $notification->setMessage($notifData['message']);
                    $notification->setType($notifData['type']);
                    $notification->setIcon($notifData['icon']);
                    $notification->setIsRead(rand(0, 1) === 1);

                    // Set created dates spread over the last week
                    $date = new \DateTime();
                    $date->sub(new \DateInterval('PT' . rand(1, 168) . 'H'));
                    $notification->setCreatedAt($date);

                    $this->entityManager->persist($notification);
                }
            }
            $this->entityManager->flush();
            $output->writeln("  ✓ Created sample notifications for all users");
        }

        if (count($doctors) > 0) {
            $doctorNotifications = [
                ['title' => 'New Patient Appointment', 'message' => 'You have a new appointment scheduled. Patient: John Doe, Date: Tomorrow at 10:00 AM', 'type' => 'info', 'icon' => 'fas fa-calendar-plus'],
                ['title' => 'Lab Test Results Ready', 'message' => 'Results from lab tests for patient Smith are now available for review.', 'type' => 'success', 'icon' => 'fas fa-flask'],
                ['title' => 'Patient Message', 'message' => 'You have a new message from patient Johnson regarding their prescription.', 'type' => 'info', 'icon' => 'fas fa-envelope-medical'],
                ['title' => 'Prescription Refill', 'message' => 'Patient Wilson has requested a refill on their prescription. Please review and approve.', 'type' => 'warning', 'icon' => 'fas fa-prescription-bottle'],
                ['title' => 'Appointment Change', 'message' => 'Patient Brown has rescheduled their appointment to next Friday at 3:00 PM.', 'type' => 'info', 'icon' => 'fas fa-calendar-check'],
                ['title' => 'Patient Portal Activity', 'message' => 'Your patient portal has been updated with latest health records for review.', 'type' => 'success', 'icon' => 'fas fa-folder-medical'],
            ];

            foreach ($doctors as $doctor) {
                for ($i = 0; $i < 2; $i++) {
                    // Convert Doctor to User for notification (doctors should have corresponding user entry or we use admin)
                    $notifData = $doctorNotifications[array_rand($doctorNotifications)];
                    $notification = new Notification();
                    // For now, skip doctor notifications since they might not have user records
                    // This can be extended based on your architecture
                }
            }
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
