<?php

namespace App\Service;

use Symfony\Contracts\HttpClient\HttpClientInterface;

/**
 * AI-powered comment moderation.
 * Uses the OpenAI API to detect inappropriate content.
 * Falls back to a keyword-based filter if the API call fails.
 */
class CommentModerationService
{
    private array $bannedWords = [
        'spam', 'scam', 'viagra', 'casino', 'porn', 'xxx',
        'fuck', 'shit', 'asshole', 'bitch', 'dick', 'bastard',
        'hate', 'kill', 'die', 'stupid idiot', 'dumbass',
    ];

    public function __construct(
        private HttpClientInterface $httpClient,
        private string $aiApiKey,
        private string $aiModel = 'gpt-3.5-turbo',
    ) {}

    /**
     * Returns true if the comment is safe to publish.
     */
    public function isApproved(string $content): bool
    {
        // Quick keyword check first (free, no API call)
        if ($this->containsBannedWords($content)) {
            return false;
        }

        // Try AI moderation if an API key is configured
        if ($this->aiApiKey && $this->aiApiKey !== 'your_api_key_here') {
            try {
                return $this->aiModerate($content);
            } catch (\Throwable) {
                // API error — fall through to keyword-only check
            }
        }

        return true;
    }

    private function containsBannedWords(string $content): bool
    {
        $lower = mb_strtolower($content);
        foreach ($this->bannedWords as $word) {
            if (str_contains($lower, $word)) {
                return true;
            }
        }
        return false;
    }

    private function aiModerate(string $content): bool
    {
        $response = $this->httpClient->request('POST', 'https://api.openai.com/v1/chat/completions', [
            'headers' => [
                'Authorization' => 'Bearer ' . $this->aiApiKey,
                'Content-Type'  => 'application/json',
            ],
            'json' => [
                'model'    => $this->aiModel,
                'messages' => [
                    [
                        'role'    => 'system',
                        'content' => 'You are a content moderator. Reply ONLY with "SAFE" or "UNSAFE". '
                            . 'A comment is UNSAFE if it contains hate speech, harassment, threats, '
                            . 'explicit sexual content, spam, or scam links. Otherwise it is SAFE.',
                    ],
                    [
                        'role'    => 'user',
                        'content' => 'Moderate this comment: "' . mb_substr($content, 0, 500) . '"',
                    ],
                ],
                'max_tokens'  => 5,
                'temperature' => 0,
            ],
            'timeout' => 5,
        ]);

        $data  = $response->toArray(false);
        $reply = strtoupper(trim($data['choices'][0]['message']['content'] ?? 'SAFE'));

        return str_contains($reply, 'SAFE') && !str_contains($reply, 'UNSAFE');
    }
}
