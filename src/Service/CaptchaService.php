<?php

namespace App\Service;

use Symfony\Component\HttpFoundation\RequestStack;

class CaptchaService
{
    private const SESSION_ANSWER_KEY = 'captcha_answer';
    private const SESSION_QUESTION_KEY = 'captcha_question';

    public function __construct(private RequestStack $requestStack)
    {
    }

    private function getSession()
    {
        return $this->requestStack->getSession();
    }

    /**
     * Generate a random math CAPTCHA question
     */
    public function generateCaptcha(): array
    {
        $num1 = random_int(1, 20);
        $num2 = random_int(1, 20);
        $operators = ['+', '-', '*'];
        $operator = $operators[array_rand($operators)];

        match ($operator) {
            '+' => $answer = $num1 + $num2,
            '-' => $answer = $num1 - $num2,
            '*' => $answer = $num1 * $num2,
        };

        $question = "{$num1} {$operator} {$num2}";

        // Store answer in session
        $this->getSession()->set(self::SESSION_ANSWER_KEY, (string)$answer);
        $this->getSession()->set(self::SESSION_QUESTION_KEY, $question);

        return [
            'question' => $question,
            'answer' => $answer, // For testing only
        ];
    }

    /**
     * Verify CAPTCHA answer
     */
    public function verifyCaptcha(string $userAnswer): bool
    {
        $correctAnswer = $this->getSession()->get(self::SESSION_ANSWER_KEY);

        if (!$correctAnswer) {
            return false;
        }

        // Trim and convert both to strings for comparison
        $userAnswer = trim((string)$userAnswer);
        $correctAnswer = trim((string)$correctAnswer);

        if ($userAnswer === $correctAnswer) {
            // Clear CAPTCHA from session after successful verification
            $this->getSession()->remove(self::SESSION_ANSWER_KEY);
            $this->getSession()->remove(self::SESSION_QUESTION_KEY);
            return true;
        }

        return false;
    }

    /**
     * Get current CAPTCHA question
     */
    public function getQuestion(): ?string
    {
        return $this->getSession()->get(self::SESSION_QUESTION_KEY);
    }

    /**
     * Clear CAPTCHA from session
     */
    public function clear(): void
    {
        $this->getSession()->remove(self::SESSION_ANSWER_KEY);
        $this->getSession()->remove(self::SESSION_QUESTION_KEY);
    }
}
