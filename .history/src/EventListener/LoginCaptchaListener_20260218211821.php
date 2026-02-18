<?php

namespace App\EventListener;

use Symfony\Component\EventDispatcher\Attribute\AsEventListener;
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
    ) {
    }

    public function __invoke(CheckPassportEvent $event): void
    {
        $request = $this->requestStack->getCurrentRequest();
        
        if (!$request) {
            return;
        }

        // Skip captcha verification for non-login requests
        if ($request->getPathInfo() !== '/login' || $request->getMethod() !== 'POST') {
            return;
        }

        // Get the reCAPTCHA token from the form
        $recaptchaToken = $request->request->get('g-recaptcha-response');

        if (!$recaptchaToken) {
            throw new AuthenticationException('reCAPTCHA verification failed: No token provided');
        }

        try {
            // Verify the token with Google's reCAPTCHA API
            $response = $this->httpClient->request('POST', self::RECAPTCHA_VERIFY_URL, [
                'body' => [
                    'secret' => $this->recaptchaSecretKey,
                    'response' => $recaptchaToken,
                ],
            ]);

            $data = $response->toArray();

            // Check if verification was successful
            if (!$data['success'] ?? false) {
                throw new AuthenticationException('reCAPTCHA verification failed: Invalid token');
            }

            // Optionally check the score for reCAPTCHA v3
            if (isset($data['score']) && $data['score'] < 0.5) {
                throw new AuthenticationException('reCAPTCHA verification failed: Suspicious activity detected');
            }
        } catch (AuthenticationException $e) {
            throw $e;
        } catch (\Exception $e) {
            throw new AuthenticationException('reCAPTCHA verification error: ' . $e->getMessage());
        }
    }
}

