<?php

namespace App\Controller;

use App\Entity\DailyTracking;
use App\Repository\DailyTrackingRepository;
use App\Service\NotificationService;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

class TrackingController extends AbstractController
{
    private EntityManagerInterface $entityManager;
    private NotificationService $notificationService;

    public function __construct(EntityManagerInterface $entityManager, NotificationService $notificationService)
    {
        $this->entityManager = $entityManager;
        $this->notificationService = $notificationService;
    }

    #[Route('/tracking', name: 'tracking_index', methods: ['GET', 'POST'])]
    public function index(Request $request, DailyTrackingRepository $repository): Response
    {
        $this->denyAccessUnlessGranted('ROLE_USER');

        if ($request->isMethod('POST')) {
            $data = $request->request->all();

            /** @var \App\Entity\User $user */
            $user = $this->getUser();
            $tracking = new DailyTracking();
            $tracking->setUser($user);
            $tracking->setDate(new \DateTime());
            $tracking->setMood((int) $data['mood']);
            $tracking->setStress((int) $data['stress']);
            $tracking->setActivities($data['activities'] ?? null);
            $tracking->setAnxietyLevel(isset($data['anxiety_level']) ? (int) $data['anxiety_level'] : null);
            $tracking->setFocusLevel(isset($data['focus_level']) ? (int) $data['focus_level'] : null);
            $tracking->setMotivationLevel(isset($data['motivation_level']) ? (int) $data['motivation_level'] : null);
            $tracking->setSocialInteractionLevel(isset($data['social_interaction_level']) ? (int) $data['social_interaction_level'] : null);
            $tracking->setSleepHours(isset($data['sleep_hours']) ? (int) $data['sleep_hours'] : null);
            $tracking->setEnergyLevel(isset($data['energy_level']) ? (int) $data['energy_level'] : null);
            $tracking->setSymptoms($data['symptoms'] ?? null);
            $tracking->setMedicationTaken(isset($data['medication_taken']) ? (bool) $data['medication_taken'] : null);
            $tracking->setAppetiteLevel(isset($data['appetite_level']) ? (int) $data['appetite_level'] : null);
            $tracking->setWaterIntake(isset($data['water_intake']) ? (int) $data['water_intake'] : null);
            $tracking->setPhysicalActivityLevel(isset($data['physical_activity_level']) ? (int) $data['physical_activity_level'] : null);
            $tracking->setCreatedAt(new \DateTime());
            $tracking->setUpdatedAt(new \DateTime());

            $this->entityManager->persist($tracking);
            $this->entityManager->flush();

            /** @var \App\Entity\User $user */
            $user = $this->getUser();
            $this->notificationService->notifyAdmins(
                'New Health Tracking Entry',
                $user->getFullName() . ' logged daily health: mood ' . $data['mood'] . '/10, stress ' . $data['stress'] . '/10',
                'info',
                'fas fa-heartbeat'
            );

            return $this->redirectToRoute('tracking_index');
        }

        /** @var \App\Entity\User $user */
        $user = $this->getUser();
        $stats = $repository->getStatistics($user);
        $suggestion = $this->getSuggestion($stats);
        $recentEntries = $repository->findRecentByUser($user, 10);
        $advancedStats = $repository->getAdvancedStatistics($user);
        $weeklyData = $repository->getWeeklyStatistics($user);

        return $this->render('tracking/index.html.twig', [
            'stats' => $stats,
            'suggestion' => $suggestion,
            'recentEntries' => $recentEntries,
            'advancedStats' => $advancedStats,
            'weeklyData' => $weeklyData,
        ]);
    }

    #[Route('/tracking/edit/{id}', name: 'tracking_edit', methods: ['GET', 'POST'])]
    public function edit(int $id, Request $request, DailyTrackingRepository $repository): Response
    {
        $this->denyAccessUnlessGranted('ROLE_USER');
        
        $tracking = $repository->find($id);
        
        if (!$tracking || $tracking->getUser() !== $this->getUser()) {
            throw $this->createAccessDeniedException('You cannot edit this entry.');
        }

        if ($request->isMethod('POST')) {
            $data = $request->request->all();
            
            $tracking->setMood((int) $data['mood']);
            $tracking->setStress((int) $data['stress']);
            $tracking->setActivities($data['activities'] ?? null);
            $tracking->setAnxietyLevel(isset($data['anxiety_level']) ? (int) $data['anxiety_level'] : null);
            $tracking->setFocusLevel(isset($data['focus_level']) ? (int) $data['focus_level'] : null);
            $tracking->setMotivationLevel(isset($data['motivation_level']) ? (int) $data['motivation_level'] : null);
            $tracking->setSocialInteractionLevel(isset($data['social_interaction_level']) ? (int) $data['social_interaction_level'] : null);
            $tracking->setSleepHours(isset($data['sleep_hours']) ? (int) $data['sleep_hours'] : null);
            $tracking->setEnergyLevel(isset($data['energy_level']) ? (int) $data['energy_level'] : null);
            $tracking->setSymptoms($data['symptoms'] ?? null);
            $tracking->setMedicationTaken(isset($data['medication_taken']) ? (bool) $data['medication_taken'] : null);
            $tracking->setAppetiteLevel(isset($data['appetite_level']) ? (int) $data['appetite_level'] : null);
            $tracking->setWaterIntake(isset($data['water_intake']) ? (int) $data['water_intake'] : null);
            $tracking->setPhysicalActivityLevel(isset($data['physical_activity_level']) ? (int) $data['physical_activity_level'] : null);
            $tracking->setUpdatedAt(new \DateTime());

            $this->entityManager->flush();

            return $this->redirectToRoute('tracking_index');
        }

        return $this->render('tracking/edit.html.twig', [
            'tracking' => $tracking,
        ]);
    }

    #[Route('/tracking/delete/{id}', name: 'tracking_delete', methods: ['POST'])]
    public function delete(int $id, DailyTrackingRepository $repository): Response
    {
        $this->denyAccessUnlessGranted('ROLE_USER');
        
        $tracking = $repository->find($id);
        
        if (!$tracking || $tracking->getUser() !== $this->getUser()) {
            throw $this->createAccessDeniedException('You cannot delete this entry.');
        }

        $this->entityManager->remove($tracking);
        $this->entityManager->flush();

        return $this->redirectToRoute('tracking_index');
    }

    /**
     * Multi-factor health risk assessment.
     * Returns an array with: level (ok|warning|urgent), message, specialty, score, reasons.
     */
    private function getSuggestion(array $stats): array
    {
        $score   = 0;
        $reasons = [];

        // ── Stress ───────────────────────────────────────────
        $stress = (float) ($stats['averageStress'] ?? 0);
        if ($stress >= 8) { $score += 3; $reasons[] = ['label' => 'Very high stress', 'icon' => 'fas fa-bolt']; }
        elseif ($stress >= 6) { $score += 1; $reasons[] = ['label' => 'Elevated stress', 'icon' => 'fas fa-bolt']; }

        // ── Mood ─────────────────────────────────────────────
        $mood = (float) ($stats['averageMood'] ?? 10);
        if ($mood <= 3) { $score += 3; $reasons[] = ['label' => 'Very low mood', 'icon' => 'fas fa-sad-tear']; }
        elseif ($mood <= 5) { $score += 1; $reasons[] = ['label' => 'Low mood', 'icon' => 'fas fa-meh']; }

        // ── Anxiety ──────────────────────────────────────────
        $anxiety = (float) ($stats['averageAnxiety'] ?? 0);
        if ($anxiety >= 7) { $score += 2; $reasons[] = ['label' => 'High anxiety', 'icon' => 'fas fa-brain']; }
        elseif ($anxiety >= 5) { $score += 1; $reasons[] = ['label' => 'Moderate anxiety', 'icon' => 'fas fa-brain']; }

        // ── Sleep ────────────────────────────────────────────
        $sleep = (float) ($stats['averageSleepHours'] ?? 8);
        if ($sleep > 0 && $sleep < 4) { $score += 2; $reasons[] = ['label' => 'Severely low sleep', 'icon' => 'fas fa-moon']; }
        elseif ($sleep >= 4 && $sleep < 6) { $score += 1; $reasons[] = ['label' => 'Insufficient sleep', 'icon' => 'fas fa-moon']; }

        // ── Energy ───────────────────────────────────────────
        $energy = (float) ($stats['averageEnergy'] ?? 10);
        if ($energy > 0 && $energy <= 2) { $score += 2; $reasons[] = ['label' => 'Critically low energy', 'icon' => 'fas fa-fire']; }
        elseif ($energy > 2 && $energy <= 4) { $score += 1; $reasons[] = ['label' => 'Low energy', 'icon' => 'fas fa-fire']; }

        // ── Motivation ───────────────────────────────────────
        $motivation = (float) ($stats['averageMotivation'] ?? 10);
        if ($motivation > 0 && $motivation <= 2) { $score += 1; $reasons[] = ['label' => 'Very low motivation', 'icon' => 'fas fa-star']; }

        // ── Determine specialty based on dominant issue ───────
        $specialty = 'General Practitioner';
        if ($anxiety >= 7 || $stress >= 8) {
            $specialty = 'Psychiatrist or Therapist';
        } elseif ($mood <= 3 || ($motivation > 0 && $motivation <= 2)) {
            $specialty = 'Psychologist';
        } elseif ($sleep < 5 || ($energy > 0 && $energy <= 3)) {
            $specialty = 'General Practitioner';
        }

        // ── Map score to urgency level ────────────────────────
        if ($score >= 5) {
            return [
                'level'     => 'urgent',
                'score'     => $score,
                'reasons'   => $reasons,
                'specialty' => $specialty,
                'message'   => 'Your recent health data shows several concerning patterns. We strongly recommend booking an appointment with a ' . $specialty . ' as soon as possible.',
                'cta'       => 'Book Appointment Now',
            ];
        }

        if ($score >= 2) {
            return [
                'level'     => 'warning',
                'score'     => $score,
                'reasons'   => $reasons,
                'specialty' => $specialty,
                'message'   => 'Some of your health indicators are below optimal. Consider consulting a ' . $specialty . ' if this persists.',
                'cta'       => 'Book a Consultation',
            ];
        }

        return [
            'level'     => 'ok',
            'score'     => $score,
            'reasons'   => [],
            'specialty' => null,
            'message'   => 'Your health metrics look good. Keep up the great work!',
            'cta'       => null,
        ];
    }
}