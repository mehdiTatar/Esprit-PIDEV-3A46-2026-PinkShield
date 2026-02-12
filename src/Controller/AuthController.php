<?php

namespace App\Controller;

use App\Entity\User;
use App\Entity\Doctor;
use App\Form\UserFormType;
use App\Form\DoctorFormType;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\PasswordHasher\Hasher\UserPasswordHasherInterface;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Authentication\AuthenticationUtils;
use Symfony\Component\Validator\Validator\ValidatorInterface;
use Symfony\Component\DependencyInjection\ParameterBag\ParameterBagInterface;

class AuthController extends AbstractController
{
    private string $recaptchaSiteKey;
    private string $recaptchaSecretKey;

    public function __construct(ParameterBagInterface $params)
    {
        $this->recaptchaSiteKey = $params->get('recaptcha_site_key');
        $this->recaptchaSecretKey = $params->get('recaptcha_secret_key');
    }

    #[Route('/login', name: 'login')]
    public function login(AuthenticationUtils $authenticationUtils): Response
    {
        // if ($this->getUser()) {
        //     return $this->redirectToRoute('target_path');
        // }

        // get the login error if there is one
        $error = $authenticationUtils->getLastAuthenticationError();
        // last username entered by the user
        $lastUsername = $authenticationUtils->getLastUsername();

        return $this->render('auth/login.html.twig', [
            'last_username' => $lastUsername,
            'error' => $error,
            'captcha_error' => null,
            'recaptcha_site_key' => $this->recaptchaSiteKey,
        ]);
    }

    #[Route('/logout', name: 'logout')]
    public function logout(): void
    {
        throw new \LogicException('This method can be blank - it will be intercepted by the logout key on your firewall.');
    }

    #[Route('/register', name: 'register')]
    public function register(Request $request, EntityManagerInterface $entityManager, UserPasswordHasherInterface $passwordHasher, ValidatorInterface $validator): Response
    {
        // Check for user_type from either query params (links) or POST data (forms)
        $userType = $request->query->get('user_type') ?? $request->request->get('user_type');

        if ($userType === 'patient') {
            return $this->redirectToRoute('register_patient');
        } elseif ($userType === 'doctor') {
            return $this->redirectToRoute('register_doctor');
        }

        return $this->render('auth/register.html.twig');
    }

    #[Route('/register/patient', name: 'register_patient')]
    public function registerPatient(Request $request, EntityManagerInterface $entityManager, UserPasswordHasherInterface $passwordHasher, ValidatorInterface $validator): Response
    {
        $user = new User();
        $form = $this->createForm(UserFormType::class, $user);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $user->setRoles(['ROLE_USER']);
            // Set full name by combining first and last name
            $fullName = trim(($user->getFirstName() ?? '') . ' ' . ($user->getLastName() ?? ''));
            $user->setFullName($fullName ?: 'User');
            $user->setPassword($passwordHasher->hashPassword($user, $user->getPassword()));

            $entityManager->persist($user);
            $entityManager->flush();

            return $this->redirectToRoute('login');
        }

        return $this->render('auth/register_patient.html.twig', [
            'form' => $form,
        ]);
    }

    #[Route('/register/doctor', name: 'register_doctor')]
    public function registerDoctor(Request $request, EntityManagerInterface $entityManager, UserPasswordHasherInterface $passwordHasher, ValidatorInterface $validator): Response
    {
        $doctor = new Doctor();
        $form = $this->createForm(DoctorFormType::class, $doctor);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $doctor->setRoles(['ROLE_DOCTOR']);
            $doctor->setPassword($passwordHasher->hashPassword($doctor, $doctor->getPassword()));

            $entityManager->persist($doctor);
            $entityManager->flush();

            return $this->redirectToRoute('login');
        }

        return $this->render('auth/register_doctor.html.twig', [
            'form' => $form,
        ]);
    }
}
