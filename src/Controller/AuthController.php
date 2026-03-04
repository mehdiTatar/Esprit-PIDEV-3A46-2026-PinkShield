<?php

namespace App\Controller;

use App\Entity\User;
use App\Entity\Doctor;
use App\Form\UserFormType;
use App\Form\DoctorFormType;
use App\Repository\UserRepository;
use App\Service\FaceRecognitionService;
use App\Service\NotificationService;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Bundle\SecurityBundle\Security;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Mailer\MailerInterface;
use Symfony\Component\Mime\Email;
use Symfony\Component\PasswordHasher\Hasher\UserPasswordHasherInterface;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Routing\Generator\UrlGeneratorInterface;
use Symfony\Component\Security\Http\Authentication\AuthenticationUtils;
use Symfony\Component\Validator\Validator\ValidatorInterface;
use Symfony\Component\DependencyInjection\ParameterBag\ParameterBagInterface;

class AuthController extends AbstractController
{
    private string $recaptchaSiteKey;
    private string $recaptchaSecretKey;

    public function __construct(
        ParameterBagInterface $params,
        private NotificationService $notificationService,
        private FaceRecognitionService $faceRecognitionService
    ) {
        $this->recaptchaSiteKey  = $params->get('recaptcha_site_key');
        $this->recaptchaSecretKey = $params->get('recaptcha_secret_key');
    }

    #[Route('/login', name: 'login')]
    public function login(AuthenticationUtils $authenticationUtils): Response
    {
        if ($this->getUser()) {
            return $this->redirectToRoute('dashboard');
        }

        $error        = $authenticationUtils->getLastAuthenticationError();
        $lastUsername = $authenticationUtils->getLastUsername();

        return $this->render('auth/login.html.twig', [
            'last_username'      => $lastUsername,
            'error'              => $error,
            'captcha_error'      => null,
            'recaptcha_site_key' => $this->recaptchaSiteKey,
        ]);
    }

    // ─────────────────────────────────────────────────────────────
    //  FACE RECOGNITION LOGIN
    // ─────────────────────────────────────────────────────────────

    #[Route('/face-login', name: 'face_login', methods: ['POST'])]
    public function faceLogin(
        Request        $request,
        UserRepository $userRepository,
        Security       $security
    ): JsonResponse {
        $email     = trim((string) $request->request->get('email', ''));
        $facePhoto = $request->files->get('face_photo');

        // ── Basic validation ────────────────────────────────────
        if ($email === '' || !$facePhoto) {
            return new JsonResponse([
                'success' => false,
                'message' => 'Please enter your email and capture/upload your face photo.',
            ], 400);
        }

        // ── Find user ───────────────────────────────────────────
        $user = $userRepository->findOneBy(['email' => $email]);

        if (!$user) {
            return new JsonResponse([
                'success' => false,
                'message' => 'No account found with this email address.',
            ], 404);
        }

        if (!$user->getFaceImagePath()) {
            return new JsonResponse([
                'success' => false,
                'message' => 'This account does not have a registered face. Please use email & password.',
            ], 400);
        }

        // ── Compare faces via Face++ ────────────────────────────
        $confidence = $this->faceRecognitionService->compareFaces(
            $user->getFaceImagePath(),
            $facePhoto
        );

        if ($confidence < 70.0) {
            return new JsonResponse([
                'success'    => false,
                'message'    => 'Face not recognised. Please try again or use email & password.',
                'confidence' => $confidence,
            ], 401);
        }

        // ── Log the user in programmatically ───────────────────
        $security->login($user, 'form_login', 'main');

        // Determine redirect based on role
        if (in_array('ROLE_ADMIN', $user->getRoles(), true)) {
            $redirectRoute = 'admin_dashboard';
        } elseif (in_array('ROLE_DOCTOR', $user->getRoles(), true)) {
            $redirectRoute = 'doctor_dashboard';
        } else {
            $redirectRoute = 'user_dashboard';
        }

        return new JsonResponse([
            'success'      => true,
            'message'      => 'Face recognised! Signing you in…',
            'confidence'   => $confidence,
            'redirect_url' => $this->generateUrl($redirectRoute),
        ]);
    }

    #[Route('/2fa_skip', name: '2fa_skip')]
    public function skip2fa(
        \Symfony\Component\Security\Core\Authentication\Token\Storage\TokenStorageInterface $tokenStorage
    ): Response
    {
        $token = $tokenStorage->getToken();

        // The scheb bundle wraps the real token inside a TwoFactorToken.
        // Extract the original authenticated token and put it back.
        if ($token instanceof \Scheb\TwoFactorBundle\Security\Authentication\Token\TwoFactorToken) {
            $tokenStorage->setToken($token->getAuthenticatedToken());
        }

        return $this->redirectToRoute('home');
    }

    #[Route('/logout', name: 'logout')]
    public function logout(): void
    {
        throw new \LogicException('This method can be blank - it will be intercepted by the logout key on your firewall.');
    }

    // ─────────────────────────────────────────────────────────────
    //  AJAX FIELD VALIDATION
    // ─────────────────────────────────────────────────────────────

    #[Route('/validate-field', name: 'ajax_validate_field', methods: ['POST'])]
    public function validateField(Request $request): JsonResponse
    {
        $data  = json_decode($request->getContent(), true) ?? [];
        $field = $data['field']  ?? '';
        $value = trim($data['value'] ?? '');
        $extra = $data['extra']  ?? '';

        $error = match ($field) {
            'email' => $this->validateEmail($value),
            'firstName', 'lastName' => $this->validateName($value),
            'password'              => $this->validatePassword($value),
            'confirm_password'      => $this->validateConfirmPassword($value, $extra),
            'phone'                 => $this->validatePhone($value),
            'address'               => $this->validateAddress($value),
            'doctorEmail'           => $this->validateRequired($value, 'Please select a doctor.'),
            'appointmentDate'       => $this->validateFutureDate($value),
            'notes'                 => $this->validateNotes($value),
            default                 => null,
        };

        return new JsonResponse(['valid' => $error === null, 'error' => $error]);
    }

    private function validateEmail(string $value): ?string
    {
        if ($value === '') return 'Email address is required.';
        if (!filter_var($value, FILTER_VALIDATE_EMAIL)) return 'Please enter a valid email address.';
        return null;
    }

    private function validateName(string $value): ?string
    {
        if ($value === '') return 'This field is required.';
        if (mb_strlen($value) < 2) return 'Must be at least 2 characters.';
        if (!preg_match('/^[\p{L}\s\-\']+$/u', $value)) return 'Only letters, spaces, hyphens, and apostrophes are allowed.';
        return null;
    }

    private function validatePassword(string $value): ?string
    {
        if ($value === '') return 'Password is required.';
        if (mb_strlen($value) < 8) return 'Password must be at least 8 characters.';
        return null;
    }

    private function validateConfirmPassword(string $value, string $password): ?string
    {
        if ($value === '') return 'Please confirm your password.';
        if ($value !== $password) return 'Passwords do not match.';
        return null;
    }

    private function validatePhone(string $value): ?string
    {
        if ($value === '') return null; // optional
        if (!preg_match('/^[\+\d][\d\s\-\.]{6,19}$/', $value)) return 'Please enter a valid phone number.';
        return null;
    }

    private function validateAddress(string $value): ?string
    {
        if ($value === '') return null; // optional
        if (mb_strlen($value) < 5) return 'Address must be at least 5 characters.';
        return null;
    }

    private function validateRequired(string $value, string $msg): ?string
    {
        return $value === '' ? $msg : null;
    }

    private function validateFutureDate(string $value): ?string
    {
        if ($value === '') return 'Please select an appointment date.';
        try {
            $dt = new \DateTimeImmutable($value);
            if ($dt <= new \DateTimeImmutable()) return 'The appointment date must be in the future.';
        } catch (\Exception) {
            return 'Invalid date format.';
        }
        return null;
    }

    private function validateNotes(string $value): ?string
    {
        if (mb_strlen($value) > 500) return 'Notes must be 500 characters or fewer.';
        return null;
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

            // ── Face Recognition (optional, non-blocking) ────────────────
            $facePhoto = $request->files->get('face_photo');
            if ($facePhoto) {
                // Use a temporary unique ID before the entity has a real DB id
                $tempId = uniqid('user_', true);
                $result = $this->faceRecognitionService->processUploadedFace($facePhoto, $tempId);
                $user->setFaceId($result['face_token']);
                $user->setFaceImagePath($result['image_path']);
            }
            // ─────────────────────────────────────────────────────────────

            $entityManager->persist($user);
            $entityManager->flush();

            $this->notificationService->notifyAdmins(
                'New Patient Registration',
                $user->getFullName() . ' registered as a new patient',
                'info',
                'fas fa-user-plus'
            );

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

            $this->notificationService->notifyAdmins(
                'New Doctor Registration',
                $doctor->getFullName() . ' registered as a new doctor',
                'info',
                'fas fa-user-md'
            );

            return $this->redirectToRoute('login');
        }

        return $this->render('auth/register_doctor.html.twig', [
            'form' => $form,
        ]);
    }

    // ─────────────────────────────────────────────────────────────
    //  FORGOT PASSWORD
    // ─────────────────────────────────────────────────────────────

    #[Route('/forgot-password', name: 'forgot_password', methods: ['GET', 'POST'])]
    public function forgotPassword(
        Request $request,
        UserRepository $userRepository,
        EntityManagerInterface $entityManager,
        MailerInterface $mailer
    ): Response {
        if ($request->isMethod('POST')) {
            $emailAddress = $request->request->get('email');
            $user = $userRepository->findOneBy(['email' => $emailAddress]);

            // Always show success to prevent user enumeration
            if ($user) {
                $token = bin2hex(random_bytes(32));
                $user->setResetToken($token);
                $user->setResetTokenExpiresAt(new \DateTimeImmutable('+1 hour'));
                $entityManager->flush();

                $resetUrl = $this->generateUrl(
                    'reset_password',
                    ['token' => $token],
                    UrlGeneratorInterface::ABSOLUTE_URL
                );

                $email = (new Email())
                    ->from('mehditatarpidev7@gmail.com')
                    ->to($user->getEmail())
                    ->subject('PinkShield — Réinitialisation de votre mot de passe')
                    ->html($this->renderView('emails/reset_password.html.twig', [
                        'user'     => $user,
                        'resetUrl' => $resetUrl,
                    ]));

                $mailer->send($email);

                $this->notificationService->notifyAdmins(
                    'Password Reset Requested',
                    $user->getEmail() . ' requested a password reset',
                    'warning',
                    'fas fa-key'
                );
            }

            $this->addFlash('success', 'Si cet email existe, un lien de réinitialisation vous a été envoyé.');
            return $this->redirectToRoute('forgot_password');
        }

        return $this->render('auth/forgot_password.html.twig');
    }

    // ─────────────────────────────────────────────────────────────
    //  RESET PASSWORD
    // ─────────────────────────────────────────────────────────────

    #[Route('/reset-password/{token}', name: 'reset_password', methods: ['GET', 'POST'])]
    public function resetPassword(
        string $token,
        Request $request,
        UserRepository $userRepository,
        EntityManagerInterface $entityManager,
        UserPasswordHasherInterface $passwordHasher
    ): Response {
        $user = $userRepository->findOneBy(['resetToken' => $token]);

        if (!$user || $user->getResetTokenExpiresAt() < new \DateTimeImmutable()) {
            $this->addFlash('danger', 'Ce lien de réinitialisation est invalide ou a expiré.');
            return $this->redirectToRoute('forgot_password');
        }

        if ($request->isMethod('POST')) {
            $newPassword = $request->request->get('password');
            $confirmPassword = $request->request->get('confirm_password');

            if (strlen($newPassword) < 6) {
                $this->addFlash('danger', 'Le mot de passe doit contenir au moins 6 caractères.');
                return $this->redirectToRoute('reset_password', ['token' => $token]);
            }

            if ($newPassword !== $confirmPassword) {
                $this->addFlash('danger', 'Les mots de passe ne correspondent pas.');
                return $this->redirectToRoute('reset_password', ['token' => $token]);
            }

            $user->setPassword($passwordHasher->hashPassword($user, $newPassword));
            $user->setResetToken(null);
            $user->setResetTokenExpiresAt(null);
            $entityManager->flush();

            $this->notificationService->notifyAdmins(
                'Password Reset Completed',
                $user->getFullName() . ' (' . $user->getEmail() . ') reset their password',
                'success',
                'fas fa-lock-open'
            );

            $this->addFlash('success', 'Mot de passe mis à jour avec succès ! Vous pouvez maintenant vous connecter.');
            return $this->redirectToRoute('login');
        }

        return $this->render('auth/reset_password.html.twig', [
            'token' => $token,
        ]);
    }
}
