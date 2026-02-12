<?php

namespace App\Controller;

use App\Repository\AdminRepository;
use App\Repository\AppointmentRepository;
use App\Repository\DoctorRepository;
use App\Repository\RatingRepository;
use App\Repository\UserRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\RedirectResponse;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

class DashboardController extends AbstractController
{
    #[Route('/', name: 'home')]
    #[Route('/dashboard', name: 'dashboard')]
    public function index(): RedirectResponse
    {
        $user = $this->getUser();
        
        if (!$user) {
            return $this->redirectToRoute('login');
        }
        
        // Redirect based on role
        if ($this->isGranted('ROLE_ADMIN')) {
            return $this->redirectToRoute('admin_dashboard');
        } elseif ($this->isGranted('ROLE_DOCTOR')) {
            return $this->redirectToRoute('doctor_dashboard');
        } elseif ($this->isGranted('ROLE_USER')) {
            return $this->redirectToRoute('user_dashboard');
        }

        return $this->redirectToRoute('login');
    }

    #[Route('/admin/dashboard', name: 'admin_dashboard')]
    public function adminDashboard(UserRepository $userRepository, DoctorRepository $doctorRepository, AdminRepository $adminRepository): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        
        return $this->render('dashboard/admin.html.twig', [
            'title' => 'Admin Dashboard',
            'totalUsers' => count($userRepository->findAll()),
            'totalDoctors' => count($doctorRepository->findAll()),
            'totalAdmins' => count($adminRepository->findAll()),
        ]);
    }

    #[Route('/doctor/dashboard', name: 'doctor_dashboard')]
    public function doctorDashboard(RatingRepository $ratingRepository, AppointmentRepository $appointmentRepository): Response
    {
        $this->denyAccessUnlessGranted('ROLE_DOCTOR');
        
        $doctor = $this->getUser();
        $doctorEmail = $doctor->getEmail();
        
        // Get ratings
        $averageRating = $ratingRepository->getAverageRating($doctor);
        $ratingCount = $ratingRepository->getRatingCount($doctor);
        
        // Get appointment statistics
        $patientCount = $appointmentRepository->countUniquePatientsByDoctor($doctorEmail);
        $appointmentCount = $appointmentRepository->countScheduledByDoctor($doctorEmail);
        
        return $this->render('dashboard/doctor.html.twig', [
            'title' => 'Doctor Dashboard',
            'averageRating' => $averageRating,
            'ratingCount' => $ratingCount,
            'patientCount' => $patientCount,
            'appointmentCount' => $appointmentCount,
        ]);
    }

    #[Route('/user/dashboard', name: 'user_dashboard')]
    public function userDashboard(AppointmentRepository $appointmentRepository): Response
    {
        $this->denyAccessUnlessGranted('ROLE_USER');
        
        $user = $this->getUser();
        $appointments = $appointmentRepository->findByPatient($user->getUserIdentifier());
        
        // Count confirmed appointments only (not cancelled)
        $scheduledAppointments = count(array_filter($appointments, fn($apt) => $apt->getStatus() !== 'cancelled'));
        
        return $this->render('dashboard/user.html.twig', [
            'title' => 'Patient Dashboard',
            'scheduledAppointments' => $scheduledAppointments,
        ]);
    }
}
