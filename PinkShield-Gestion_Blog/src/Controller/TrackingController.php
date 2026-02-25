<?php

namespace App\Controller;

use App\Entity\DailyTracking;
use App\Repository\DailyTrackingRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

class TrackingController extends AbstractController
{
    private EntityManagerInterface $entityManager;

    public function __construct(EntityManagerInterface $entityManager)
    {
        $this->entityManager = $entityManager;
    }

    #[Route('/tracking', name: 'tracking_index', methods: ['GET', 'POST'])]
    public function index(Request $request, DailyTrackingRepository $repository): Response
    {
        $this->denyAccessUnlessGranted('ROLE_USER');

        if ($request->isMethod('POST')) {
            $data = $request->request->all();

            $tracking = new DailyTracking();
            $tracking->setUser($this->getUser());
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

            return $this->redirectToRoute('tracking_index');
        }

        $stats = $repository->getStatistics($this->getUser());
        $suggestion = $this->getSuggestion($stats);
        $recentEntries = $repository->findRecentByUser($this->getUser(), 10);
        $advancedStats = $repository->getAdvancedStatistics($this->getUser());
        $weeklyData = $repository->getWeeklyStatistics($this->getUser());

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

    private function getSuggestion(array $stats): string
    {
        if ($stats['averageStress'] > 7) {
            return 'Consider booking an appointment with a therapist.';
        }

        if ($stats['averageMood'] < 4) {
            return 'You might benefit from a consultation with a psychologist.';
        }

        return 'Keep up the good work! No appointments needed at the moment.';
    }
}