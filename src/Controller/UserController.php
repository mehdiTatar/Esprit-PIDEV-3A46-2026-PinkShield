<?php

namespace App\Controller;

use App\Entity\User;
use App\Form\UserFormType;
use App\Repository\UserRepository;
use App\Service\NotificationService;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\PasswordHasher\Hasher\UserPasswordHasherInterface;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Core\Authentication\Token\Storage\TokenStorageInterface;

#[Route('/user')]
class UserController extends AbstractController
{
    public function __construct(
        private NotificationService $notificationService
    ) {}
    #[Route('/', name: 'user_index')]
    public function index(Request $request, UserRepository $userRepository): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        
        $searchId = $request->query->get('search_id');

        if ($searchId) {
            $found = $userRepository->findById((int) $searchId);
            $users = $found ? [$found] : [];
        } else {
            $users = $userRepository->findAll();
        }

        return $this->render('user/index.html.twig', [
            'users'    => $users,
            'searchId' => $searchId,
        ]);
    }

    #[Route('/new', name: 'user_new')]
    public function new(Request $request, EntityManagerInterface $entityManager, UserPasswordHasherInterface $passwordHasher): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        
        $user = new User();
        $form = $this->createForm(UserFormType::class, $user);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            // Build fullName from first and last name
            $user->setFullName(trim(($user->getFirstName() ?? '') . ' ' . ($user->getLastName() ?? '')));

            $user->setRoles(['ROLE_USER']);
            $user->setPassword($passwordHasher->hashPassword($user, $user->getPassword()));

            $entityManager->persist($user);
            $entityManager->flush();

            return $this->redirectToRoute('user_index');
        }

        return $this->render('user/form.html.twig', [
            'form' => $form,
            'title' => 'Create User',
        ]);
    }

    #[Route('/{id}/edit', name: 'user_edit')]
    public function edit(Request $request, User $user, EntityManagerInterface $entityManager, UserPasswordHasherInterface $passwordHasher, TokenStorageInterface $tokenStorage): Response
    {
        // Allow admin to edit any user; users can edit their own profile regardless of role
        $currentUser = $this->getUser();
        $currentUserId = is_object($currentUser) && method_exists($currentUser, 'getId') ? $currentUser->getId() : null;
        if (!$this->isGranted('ROLE_ADMIN') && $currentUserId !== $user->getId()) {
            throw $this->createAccessDeniedException('You do not have permission to edit this profile.');
        }

        $form = $this->createForm(UserFormType::class, $user);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            // Update fullName from first/last
            $user->setFullName(trim(($user->getFirstName() ?? '') . ' ' . ($user->getLastName() ?? '')));

            if ($form->has('password') && $form->get('password')->getData()) {
                $user->setPassword($passwordHasher->hashPassword($user, $form->get('password')->getData()));
            }

            $entityManager->flush();
            
            $this->notificationService->notifyAdmins(
                'User Profile Updated',
                $user->getFullName() . ' updated their profile',
                'info',
                'fas fa-user-edit'
            );
            
            $this->addFlash('success', 'Profile updated successfully!');

            // Redirect back to appropriate dashboard if available
            if ($this->isGranted('ROLE_ADMIN')) {
                return $this->redirectToRoute('user_index');
            }
            return $this->redirectToRoute('user_dashboard');
        }

        return $this->render('user/form.html.twig', [
            'form' => $form,
            'title' => 'Edit Profile',
        ]);
    }

    #[Route('/{id}/delete', name: 'user_delete', methods: ['POST'])]
    public function delete(Request $request, User $user, EntityManagerInterface $entityManager): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        $entityManager->remove($user);
        $entityManager->flush();

        $this->addFlash('success', 'User deleted successfully!');
        return $this->redirectToRoute('user_index');
    }

    #[Route('/{id}/activate', name: 'user_activate', methods: ['POST'])]
    public function activate(User $user, EntityManagerInterface $entityManager): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        $user->setStatus('active');
        $entityManager->flush();

        $this->addFlash('success', 'User account activated successfully!');
        return $this->redirectToRoute('user_index');
    }

    #[Route('/{id}/deactivate', name: 'user_deactivate', methods: ['POST'])]
    public function deactivate(User $user, EntityManagerInterface $entityManager): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        $user->setStatus('inactive');
        $entityManager->flush();

        $this->addFlash('success', 'User account deactivated successfully!');
        return $this->redirectToRoute('user_index');
    }

    #[Route('/{id}', name: 'user_show')]
    public function show(User $user): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        return $this->render('user/show.html.twig', [
            'user' => $user,
        ]);
    }
}
