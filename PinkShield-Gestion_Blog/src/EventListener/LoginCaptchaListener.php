<?php

namespace App\EventListener;

use Symfony\Component\EventDispatcher\Attribute\AsEventListener;
use Symfony\Component\HttpKernel\KernelInterface;
use Symfony\Component\Security\Http\Event\CheckPassportEvent;
use Symfony\Component\Security\Core\Exception\AuthenticationException;
use Symfony\Contracts\HttpClient\HttpClientInterface;
use Symfony\Component\HttpFoundation\RequestStack;

#[AsEventListener(event: CheckPassportEvent::class)]
class LoginCaptchaListener
{
    private const RECAPTCHA_VERIFY_URL = 'https://www.google.com/recaptcha/api/siteverify';

    public function __construct(
        private HttpClientInterface $httpClient,
        private RequestStack $requestStack,
        private string $recaptchaSecretKey,
        private KernelInterface $kernel,
    ) {
    }

    public function __invoke(CheckPassportEvent $event): void
    {
        // Skip reCAPTCHA in dev environment (avoids "invalid credentials" when keys are invalid/test)
        if ($this->kernel->getEnvironment() === 'dev') {
            return;
        }

        $request = $this->requestStack->getCurrentRequest();
        
        if (!$request) {
            return;
        }
        
        // Only check on login page form submissions
        if ($request->getPathInfo() !== '/login' || !$request->isMethod('POST')) {
            return;
        }

        // Check for reCAPTCHA token
        $recaptchaToken = $request->request->get('g-recaptcha-response', '');
        
        if (!$recaptchaToken) {
            throw new AuthenticationException('Please verify that you are not a robot.');
        }

        // Verify reCAPTCHA token with Google
        try {
            $response = $this->httpClient->request('POST', self::RECAPTCHA_VERIFY_URL, [
                'body' => [
                    'secret' => $this->recaptchaSecretKey,
                    'response' => $recaptchaToken,
                ],
            ]);

            $data = $response->toArray();

            if (!isset($data['success']) || !$data['success']) {
                throw new AuthenticationException('reCAPTCHA verification failed. Please try again.');
            }
        } catch (\Exception $e) {
            throw new AuthenticationException('reCAPTCHA verification error. Please try again.');
        }
    }
}
