<?php

namespace App\Service;

use Psr\Log\LoggerInterface;
use Symfony\Contracts\HttpClient\HttpClientInterface;

class CommentModerationService
{
    // Using Hugging Face router with a chat model for content moderation
    private const HUGGINGFACE_URL = 'https://router.huggingface.co/v1/chat/completions';
    
    // Common inappropriate words/phrases to filter
    private const BAD_WORDS = [
        
    ];

    public function __construct(
        private HttpClientInterface $httpClient,
        private ?string $apiKey = null,
        private ?LoggerInterface $logger = null,
    ) {
    }

    /**
     * Check if comment content passes moderation.
     * Returns true if approved, false if flagged as inappropriate.
     */
    public function isApproved(string $content): bool
    {
        if (trim($content) === '') {
            return false;
        }

        // First, check with local bad words filter (always runs)
        if (!$this->passesLocalFilter($content)) {
            $this->logger?->info('CommentModerationService: Content blocked by local filter.');
            return false;
        }

        // If API key is not configured, rely only on local filter
        if (empty($this->apiKey) || trim($this->apiKey) === '') {
            $this->logger?->warning('CommentModerationService: No API key configured. Using local filter only.');
            return true; // Already passed local filter
        }

        // Try Hugging Face moderation using chat completion
        try {
            $response = $this->httpClient->request('POST', self::HUGGINGFACE_URL, [
                'headers' => [
                    'Authorization' => 'Bearer ' . $this->apiKey,
                    'Content-Type'  => 'application/json',
                ],
                'json' => [
                    'model' => 'meta-llama/Llama-3.2-3B-Instruct:fastest',
                    'messages' => [
                        [
                            'role' => 'system',
                            'content' => 'You are a content moderation assistant. Analyze the following comment and respond with ONLY "APPROVED" if it is appropriate, or "BLOCKED" if it contains hate speech, profanity, threats, harassment, or inappropriate content. Do not provide any explanation, just the single word.'
                        ],
                        [
                            'role' => 'user',
                            'content' => $content
                        ]
                    ],
                    'max_tokens' => 10,
                    'temperature' => 0.1,
                ],
                'timeout' => 30,
            ]);

            $statusCode = $response->getStatusCode();
            $data = $response->toArray(false);

            $this->logger?->info('CommentModerationService: Hugging Face API response', [
                'status_code' => $statusCode,
                'data' => $data
            ]);

            // Check for API errors
            if ($statusCode !== 200) {
                $errorMsg = $data['error'] ?? 'Unknown error';
                $this->logger?->error('CommentModerationService: API returned error', [
                    'status' => $statusCode,
                    'error' => $errorMsg
                ]);
                throw new \RuntimeException("API Error: $errorMsg");
            }

            // Extract the response from chat completion
            if (isset($data['choices'][0]['message']['content'])) {
                $result = trim(strtoupper($data['choices'][0]['message']['content']));
                
                $this->logger?->info('CommentModerationService: Moderation result', [
                    'result' => $result,
                    'original_content' => $content
                ]);

                // Check if the response contains BLOCKED
                $isBlocked = str_contains($result, 'BLOCKED');
                
                return !$isBlocked;
            }

            $this->logger?->warning('CommentModerationService: Unexpected API response structure.', ['data' => $data]);
            return true; // Fallback to local filter result (already passed)

        } catch (\Exception $e) {
            $this->logger?->error('CommentModerationService: API call failed, using local filter only.', [
                'error' => $e->getMessage(),
                'trace' => $e->getTraceAsString(),
            ]);
            // If API fails, rely on local filter (already passed)
            return true;
        }
    }

    /**
     * Local bad words filter as fallback/first line of defense
     */
    private function passesLocalFilter(string $content): bool
    {
        $contentLower = strtolower($content);
        
        foreach (self::BAD_WORDS as $badWord) {
            // Check for whole word matches (with word boundaries)
            if (preg_match('/\b' . preg_quote($badWord, '/') . '\b/i', $contentLower)) {
                return false;
            }
        }
        
        return true;
    }
}
