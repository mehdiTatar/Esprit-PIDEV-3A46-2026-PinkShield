<?php

namespace App\Controller;

use App\Repository\AdminRepository;
use App\Repository\AppointmentRepository;
use App\Repository\BlogPostRepository;
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
    public function adminDashboard(
        UserRepository        $userRepository,
        DoctorRepository      $doctorRepository,
        AdminRepository       $adminRepository,
        AppointmentRepository $appointmentRepository,
        BlogPostRepository    $blogPostRepository,
        RatingRepository      $ratingRepository,
    ): Response {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        // ── Basic counts ──────────────────────────────────────
        $totalUsers        = count($userRepository->findAll());
        $totalDoctors      = count($doctorRepository->findAll());
        $totalAdmins       = count($adminRepository->findAll());
        $totalBlogPosts    = count($blogPostRepository->findAll());
        $totalAppointments = $appointmentRepository->countTotal();

        // ── Status breakdowns ─────────────────────────────────
        $usersByStatus        = $userRepository->countByStatus();
        $appointmentsByStatus = $appointmentRepository->countByStatus();

        // ── Charts: registrations by month (last 6 months) ───
        $registrationLabels = [];
        $registrationCounts = [];
        for ($i = 5; $i >= 0; $i--) {
            $registrationLabels[] = (new \DateTimeImmutable("first day of -$i month"))->format('M Y');
            $registrationCounts[] = 0; // placeholder — User has no createdAt column
        }

        // ── Top 5 doctors by appointment count ───────────────
        $topDoctors = $appointmentRepository->findTopDoctors(5);

        // ── Recent 5 patients ─────────────────────────────────
        $recentUsers = $userRepository->findRecentUsers(5);

        // ── Average rating ────────────────────────────────────
        $allRatings = $ratingRepository->findAll();
        $avgRating  = count($allRatings)
            ? round(array_sum(array_map(fn($r) => $r->getRating(), $allRatings)) / count($allRatings), 1)
            : 0;

        return $this->render('dashboard/admin.html.twig', [
            'title'                => 'Admin Dashboard',
            'totalUsers'           => $totalUsers,
            'totalDoctors'         => $totalDoctors,
            'totalAdmins'          => $totalAdmins,
            'totalBlogPosts'       => $totalBlogPosts,
            'totalAppointments'    => $totalAppointments,
            'usersByStatus'        => $usersByStatus,
            'appointmentsByStatus' => $appointmentsByStatus,
            'registrationLabels'   => $registrationLabels,
            'registrationCounts'   => $registrationCounts,
            'topDoctors'           => $topDoctors,
            'recentUsers'          => $recentUsers,
            'avgRating'            => $avgRating,
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
