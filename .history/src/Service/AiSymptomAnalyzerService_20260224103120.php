<?php

namespace App\Service;

use Symfony\Contracts\HttpClient\HttpClientInterface;

class AiSymptomAnalyzerService
{
    private HttpClientInterface $httpClient;
    private string $apiKey;
    private string $apiProvider; // 'openai' or 'gemini'
    private string $model;

    public function __construct(
        HttpClientInterface $httpClient,
        string $aiApiKey,
        string $aiProvider = 'openai',
        string $aiModel = 'gpt-3.5-turbo'
    ) {
        $this->httpClient = $httpClient;
        $this->apiKey = $aiApiKey;
        $this->apiProvider = $aiProvider;
        $this->model = $aiModel;
    }

    /**
     * Analyze medical notes and suggest parapharmacy products
     *
     * @param string $notes The consultation notes from the appointment
     * @return array An array containing 'success' (bool) and 'suggestions' (string) or 'error' (string)
     */
    public function analyzeNotes(string $notes): array
    {
        // Don't process empty notes
        if (empty(trim($notes))) {
            return [
                'success' => false,
                'error' => 'No notes provided for analysis',
            ];
        }

        try {
            if ($this->apiProvider === 'gemini') {
                return $this->analyzeWithGemini($notes);
            } else {
                return $this->analyzeWithOpenAi($notes);
            }
        } catch (\Exception $e) {
            return [
                'success' => false,
                'error' => 'AI analysis failed: ' . $e->getMessage(),
            ];
        }
    }

    /**
     * Analyze notes using OpenAI API
     *
     * @param string $notes The consultation notes
     * @return array The analysis result
     */
    private function analyzeWithOpenAi(string $notes): array
    {
        $prompt = "Act as a pharmacist. Read these consultation notes: {$notes}. Suggest 2 generic parapharmacy product types or active ingredients that could help. Return ONLY a comma-separated list of the 2 items, nothing else.";

        $response = $this->httpClient->request('POST', 'https://api.openai.com/v1/chat/completions', [
            'headers' => [
                'Authorization' => "Bearer {$this->apiKey}",
                'Content-Type' => 'application/json',
            ],
            'json' => [
                'model' => $this->model,
                'messages' => [
                    [
                        'role' => 'user',
                        'content' => $prompt,
                    ],
                ],
                'temperature' => 0.7,
                'max_tokens' => 100,
            ],
        ]);

        $statusCode = $response->getStatusCode();

        if ($statusCode !== 200) {
            $error = $response->toArray(false);
            throw new \Exception('OpenAI API error: ' . ($error['error']['message'] ?? 'Unknown error'));
        }

        $data = $response->toArray();
        $suggestions = $data['choices'][0]['message']['content'] ?? '';

        return [
            'success' => true,
            'suggestions' => trim($suggestions),
        ];
    }

    /**
     * Analyze notes using Google Gemini API
     *
     * @param string $notes The consultation notes
     * @return array The analysis result
     */
    private function analyzeWithGemini(string $notes): array
    {
        $prompt = "Act as a pharmacist. Read these consultation notes: {$notes}. Suggest 2 generic parapharmacy product types or active ingredients that could help. Return ONLY a comma-separated list of the 2 items, nothing else.";

        $response = $this->httpClient->request('POST', 'https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent', [
            'query' => [
                'key' => $this->apiKey,
            ],
            'json' => [
                'contents' => [
                    [
                        'parts' => [
                            [
                                'text' => $prompt,
                            ],
                        ],
                    ],
                ],
                'generationConfig' => [
                    'temperature' => 0.7,
                    'maxOutputTokens' => 100,
                ],
            ],
        ]);

        $statusCode = $response->getStatusCode();

        if ($statusCode !== 200) {
            throw new \Exception('Gemini API error: Status ' . $statusCode);
        }

        $data = $response->toArray();

        // Extract text from Gemini response
        $suggestions = '';
        if (isset($data['candidates'][0]['content']['parts'][0]['text'])) {
            $suggestions = $data['candidates'][0]['content']['parts'][0]['text'];
        }

        if (empty($suggestions)) {
            throw new \Exception('No response from Gemini API');
        }

        return [
            'success' => true,
            'suggestions' => trim($suggestions),
        ];
    }
}
