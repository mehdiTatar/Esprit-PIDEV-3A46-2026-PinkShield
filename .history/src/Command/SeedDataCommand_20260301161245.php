<?php

namespace App\Command;

use App\Entity\Admin;
use App\Entity\Appointment;
use App\Entity\Doctor;
use App\Entity\HealthLog;
use App\Entity\Notification;
use App\Entity\User;
use App\Entity\Parapharmacie;
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
                // the entity stores first/last name separately; build them explicitly
                $doctor->setFirstName('Doctor');
                $doctor->setLastName((string) $i);
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

        // Parapharmacy products list (realistic names, descriptions, prices)
        $products = [
            ['name' => 'SunShield SPF50 Sunscreen', 'description' => 'Broad-spectrum SPF50 sunscreen for face and body, non-greasy and water-resistant.', 'price' => 14.99],
            ['name' => 'Hydrating Face Moisturizer', 'description' => 'Lightweight daily moisturizer with hyaluronic acid for long-lasting hydration.', 'price' => 9.99],
            ['name' => 'Gentle Baby Wash', 'description' => 'Tear-free baby wash with natural chamomile to soothe and clean delicate skin.', 'price' => 7.50],
            ['name' => 'Antiseptic Wound Spray', 'description' => 'Fast-acting antiseptic spray to clean and protect minor cuts and abrasions.', 'price' => 6.25],
            ['name' => 'PainRelief Gel 50g', 'description' => 'Topical analgesic gel for muscle and joint pain relief.', 'price' => 8.99],
            ['name' => 'Multivitamin Adult 60 tabs', 'description' => 'Daily multivitamin to support general health and vitality.', 'price' => 12.50],
            ['name' => 'Omega-3 Fish Oil 1000mg (60)', 'description' => 'High-strength omega-3 capsules to support heart and brain health.', 'price' => 15.00],
            ['name' => 'Probiotic 30 capsules', 'description' => 'Live probiotics to support gut flora and digestion.', 'price' => 18.99],
            ['name' => 'Vitamin C 500mg (100)', 'description' => 'Immune support vitamin C tablets, antioxidant formula.', 'price' => 9.50],
            ['name' => 'Zinc 50mg (60)', 'description' => 'Zinc supplement to support immune function and skin health.', 'price' => 8.00],
            ['name' => 'Calcium + D3 (90)', 'description' => 'Bone support supplement combining calcium and vitamin D3.', 'price' => 11.00],
            ['name' => 'Sleep Aid Herbal (30)', 'description' => 'Natural herbal sleep aid with valerian and lemon balm.', 'price' => 10.00],
            ['name' => 'Cough Syrup 200ml', 'description' => 'Soothing cough syrup for cough and throat irritation.', 'price' => 7.99],
            ['name' => 'Nasal Saline Spray 100ml', 'description' => 'Saline nasal spray to relieve congestion and moisturize nasal passages.', 'price' => 5.49],
            ['name' => 'Eye Lubricant Drops 10ml', 'description' => 'Lubricating eye drops for dry and irritated eyes.', 'price' => 6.99],
            ['name' => 'Earwax Removal Drops 15ml', 'description' => 'Gentle ear drops to soften and remove excess earwax.', 'price' => 5.99],
            ['name' => 'Antifungal Cream 15g', 'description' => 'Effective topical cream for fungal skin infections.', 'price' => 6.49],
            ['name' => 'Diaper Rash Cream 50ml', 'description' => 'Protective ointment to soothe and prevent diaper rash.', 'price' => 8.49],
            ['name' => 'Hand Sanitizer 500ml', 'description' => 'Quick-drying alcohol sanitizer for effective hand hygiene.', 'price' => 6.99],
            ['name' => 'Hydrocortisone Cream 1% 15g', 'description' => 'Mild steroid cream for itching and minor skin irritation.', 'price' => 4.99],
            ['name' => 'Oral Rehydration Salts (10)', 'description' => 'Electrolyte replacement sachets to prevent dehydration.', 'price' => 3.99],
            ['name' => 'Digital Thermometer', 'description' => 'Fast and accurate digital thermometer for oral/axillary use.', 'price' => 19.99],
            ['name' => 'Cold & Flu Relief Tablets (20)', 'description' => 'Relieves common cold and flu symptoms: fever, ache, congestion.', 'price' => 8.99],
            ['name' => 'Vitamin D3 1000IU (120)', 'description' => 'Daily vitamin D3 supplement to support bone and immune health.', 'price' => 7.99],
            ['name' => 'Collagen Peptides 250g', 'description' => 'Hydrolyzed collagen powder to support skin, hair and joint health.', 'price' => 24.99],
            ['name' => 'Hyaluronic Acid Serum 30ml', 'description' => 'Anti-aging hydrating serum to plump and smooth skin.', 'price' => 19.99],
            ['name' => 'Acne Cleansing Gel 150ml', 'description' => 'Gentle acne cleanser with salicylic acid to clear pores.', 'price' => 9.99],
            ['name' => 'Sensitive Shampoo 250ml', 'description' => 'Mild shampoo for sensitive scalp and daily use.', 'price' => 6.50],
            ['name' => 'Conditioner 250ml', 'description' => 'Moisturizing conditioner for soft, manageable hair.', 'price' => 6.50],
            ['name' => 'Foot Repair Cream 75ml', 'description' => 'Intensive foot cream for cracked heels and dry skin.', 'price' => 7.50],
            ['name' => 'Insect Repellent Spray 150ml', 'description' => 'Long-lasting insect repellent for outdoor protection.', 'price' => 9.49],
            ['name' => 'Lip Balm SPF15', 'description' => 'Moisturizing lip balm with sun protection.', 'price' => 3.49],
            ['name' => 'Muscle Rub 100ml', 'description' => 'Cooling and warming rub for sore muscles.', 'price' => 8.99],
            ['name' => 'Small Heating Pad', 'description' => 'Reusable heating pad for targeted pain relief.', 'price' => 14.00],
            ['name' => 'Eye Makeup Remover 100ml', 'description' => 'Gentle eye makeup remover that cleans without irritation.', 'price' => 5.49],
            ['name' => 'Saline Nasal Rinse Pack', 'description' => 'Complete nasal rinse kit to clear sinuses and allergens.', 'price' => 7.99],
            ['name' => 'Electrolyte Drink Mix (10)', 'description' => 'Powdered electrolyte mix to rehydrate after exercise or illness.', 'price' => 6.99],
            ['name' => 'Iron Supplement (30)', 'description' => 'Iron tablets to support healthy blood and energy levels.', 'price' => 8.50],
            ['name' => 'Magnesium 250mg (60)', 'description' => 'Magnesium supplement for muscle function and relaxation.', 'price' => 9.00],
            ['name' => 'Digestive Enzymes 60 caps', 'description' => 'Digestive enzyme blend to aid nutrient absorption.', 'price' => 15.50],
            ['name' => 'Gentle Laxative 20 tabs', 'description' => 'Mild laxative for occasional constipation relief.', 'price' => 5.99],
            ['name' => 'Herbal Calm Tea (20)', 'description' => 'Soothing herbal tea blend to promote relaxation and sleep.', 'price' => 4.99],
            ['name' => 'Protein Powder 500g (Vanilla)', 'description' => 'Whey protein powder for muscle recovery and daily protein needs.', 'price' => 29.99],
            ['name' => 'Face Clay Mask 100ml', 'description' => 'Purifying clay mask to detoxify and refine pores.', 'price' => 8.99],
            ['name' => 'Antibacterial Wipes (50)', 'description' => 'Convenient antibacterial wipes for hands and surfaces.', 'price' => 4.99],
            ['name' => 'Deodorant Roll-on 50ml', 'description' => 'Alcohol-free deodorant with long-lasting protection.', 'price' => 5.99],
            ['name' => 'Toothpaste Sensitive 75ml', 'description' => 'Toothpaste formulated for sensitive teeth and enamel protection.', 'price' => 3.99],
            ['name' => 'Mouthwash Antiseptic 500ml', 'description' => 'Antiseptic mouthwash to freshen breath and reduce bacteria.', 'price' => 6.99],
            ['name' => 'Cooling Eye Gel 15ml', 'description' => 'Refreshing eye gel to reduce puffiness and soothe tired eyes.', 'price' => 8.99],
            ['name' => 'Nasal Decongestant Spray 15ml', 'description' => 'Fast-acting nasal decongestant for short-term relief.', 'price' => 7.49],
        ];

        // Seed parapharmacy products
        $output->writeln('Checking parapharmacy product catalog...');
        $productRepo = $this->entityManager->getRepository(Parapharmacie::class);
        $existingProducts = $productRepo->findAll();

        if (count($existingProducts) === 0) {
            $output->writeln('Creating 50 parapharmacy products...');
            foreach ($products as $p) {
                $product = new Parapharmacie();
                $product->setName($p['name']);
                $product->setDescription($p['description']);
                $product->setPrice(number_format($p['price'], 2, '.', ''));
                $this->entityManager->persist($product);
            }
            $this->entityManager->flush();
            $output->writeln('  ✓ Created 50 parapharmacy products');
        } elseif (count($existingProducts) < count($products)) {
            $toCreate = count($products) - count($existingProducts);
            $output->writeln(sprintf('Adding %d missing parapharmacy products...', $toCreate));
            for ($i = count($existingProducts); $i < count($products); $i++) {
                $p = $products[$i];
                $product = new Parapharmacie();
                $product->setName($p['name']);
                $product->setDescription($p['description']);
                $product->setPrice(number_format($p['price'], 2, '.', ''));
                $this->entityManager->persist($product);
            }
            $this->entityManager->flush();
            $output->writeln('  ✓ Added missing parapharmacy products');
        } else {
            $output->writeln('Updating existing parapharmacy products with realistic data...');
            for ($i = 0; $i < count($products); $i++) {
                if (!isset($existingProducts[$i])) break;
                $p = $products[$i];
                $prod = $existingProducts[$i];
                $prod->setName($p['name']);
                $prod->setDescription($p['description']);
                $prod->setPrice(number_format($p['price'], 2, '.', ''));
                // retain existing stock or set to random if zero
                if (!$prod->getStock()) {
                    $prod->setStock(rand(10, 100));
                }
                $this->entityManager->persist($prod);
            }
            $this->entityManager->flush();
            $output->writeln('  ✓ Updated parapharmacy catalog (first 50 entries)');
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
