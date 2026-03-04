<?php

namespace App\Controller;

use App\Entity\Doctor;
use App\Entity\Rating;
use App\Form\RatingFormType;
use App\Repository\DoctorRepository;
use App\Repository\RatingRepository;
use App\Service\NotificationService;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/rating')]
class RatingController extends AbstractController
{
    public function __construct(
        private NotificationService $notificationService
    ) {}
    #[Route('/doctors', name: 'rating_doctors_list')]
    public function doctorsList(DoctorRepository $doctorRepository, RatingRepository $ratingRepository): Response
    {
        $this->denyAccessUnlessGranted('ROLE_USER');

        // Only patients (non-doctors) can rate doctors
        if ($this->isGranted('ROLE_DOCTOR')) {
            $this->addFlash('warning', 'Doctors cannot rate other doctors.');
            return $this->redirectToRoute('dashboard');
        }

        $doctors = $doctorRepository->findAll();
        /** @var \App\Entity\User $user */
        $user = $this->getUser();

        // Get ratings for each doctor
        $doctorRatings = [];
        foreach ($doctors as $doctor) {
            $doctorRatings[$doctor->getId()] = [
                'average' => $ratingRepository->getAverageRating($doctor),
                'count' => $ratingRepository->getRatingCount($doctor),
                'userRating' => $ratingRepository->findByDoctorAndUser($doctor, $user),
            ];
        }

        return $this->render('rating/doctors_list.html.twig', [
            'doctors' => $doctors,
            'doctorRatings' => $doctorRatings,
        ]);
    }

    #[Route('/doctor/{id}/rate', name: 'rating_rate_doctor', methods: ['GET', 'POST'])]
    public function rateDoctor(
        Doctor $doctor,
        Request $request,
        EntityManagerInterface $entityManager,
        RatingRepository $ratingRepository
    ): Response {
        $this->denyAccessUnlessGranted('ROLE_USER');

        // Only patients (non-doctors) can rate doctors
        if ($this->isGranted('ROLE_DOCTOR')) {
            $this->addFlash('warning', 'Doctors cannot rate other doctors.');
            return $this->redirectToRoute('dashboard');
        }

        /** @var \App\Entity\User $user */
        $user = $this->getUser();

        // Check if user already rated this doctor
        $existingRating = $ratingRepository->findByDoctorAndUser($doctor, $user);

        $rating = $existingRating ?? new Rating();
        $form = $this->createForm(RatingFormType::class, $rating);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $rating->setDoctor($doctor);
            $rating->setUser($user);

            $entityManager->persist($rating);
            $entityManager->flush();

            $this->notificationService->notifyAdmins(
                'Doctor Rated',
                $user->getFullName() . ' rated Dr. ' . $doctor->getFullName() . ' (' . $rating->getRating() . '★)',
                'info',
                'fas fa-star'
            );

            $this->addFlash('success', 'Your rating has been saved successfully!');
            return $this->redirectToRoute('rating_doctors_list');
        }

        return $this->render('rating/rate_doctor.html.twig', [
            'form' => $form,
            'doctor' => $doctor,
            'isEdit' => (bool) $existingRating,
        ]);
    }
}
