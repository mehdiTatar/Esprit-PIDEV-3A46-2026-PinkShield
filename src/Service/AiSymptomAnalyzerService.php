<?php

namespace App\Service;

use Symfony\Contracts\HttpClient\HttpClientInterface;

class AiSymptomAnalyzerService
{
    private HttpClientInterface $httpClient;
    private string $apiKey;
    private string $model;

    public function __construct(HttpClientInterface $httpClient)
    {
        $this->httpClient = $httpClient;
        $this->apiKey = $_ENV['AI_API_KEY'] ?? '';
        $this->model  = $_ENV['AI_MODEL'] ?? 'gpt-3.5-turbo';
    }

    /**
     * Analyze appointment notes and return health suggestions.
     * @return array{success: bool, suggestions?: array, error?: string}
     */
    public function analyzeNotes(string $notes): array
    {
        if (empty($this->apiKey)) {
            return $this->getFallbackAnalysis($notes);
        }

        try {
            $response = $this->httpClient->request('POST', 'https://api.openai.com/v1/chat/completions', [
                'headers' => [
                    'Authorization' => 'Bearer ' . $this->apiKey,
                    'Content-Type'  => 'application/json',
                ],
                'json' => [
                    'model'    => $this->model,
                    'messages' => [
                        [
                            'role'    => 'system',
                            'content' => 'You are a medical AI assistant. Analyze the given appointment notes and provide health recommendations. Return a JSON array of objects with "symptom", "recommendation", and "severity" (low/medium/high). Maximum 3 items. Only return the JSON array, no other text.',
                        ],
                        [
                            'role'    => 'user',
                            'content' => 'Appointment notes: ' . $notes,
                        ],
                    ],
                    'temperature' => 0.5,
                    'max_tokens'  => 500,
                ],
            ]);

            $data = $response->toArray();
            $content = $data['choices'][0]['message']['content'] ?? '';

            $content = trim($content);
            if (str_starts_with($content, '```')) {
                $content = preg_replace('/^```(?:json)?\s*/', '', $content);
                $content = preg_replace('/\s*```$/', '', $content);
            }

            $suggestions = json_decode($content, true);

            if (is_array($suggestions) && count($suggestions) > 0) {
                return [
                    'success'     => true,
                    'suggestions' => array_map(function ($s) {
                        return [
                            'symptom'        => $s['symptom'] ?? 'General',
                            'recommendation' => $s['recommendation'] ?? 'Consult your doctor.',
                            'severity'       => $s['severity'] ?? 'low',
                        ];
                    }, $suggestions),
                ];
            }

            return $this->getFallbackAnalysis($notes);
        } catch (\Exception $e) {
            error_log('AiSymptomAnalyzerService error: ' . $e->getMessage());
            return $this->getFallbackAnalysis($notes);
        }
    }

    private function getFallbackAnalysis(string $notes): array
    {
        return [
            'success'     => true,
            'suggestions' => [
                [
                    'symptom'        => 'General consultation',
                    'recommendation' => 'Based on the notes provided, please follow your doctor\'s prescribed treatment plan.',
                    'severity'       => 'low',
                ],
            ],
        ];
    }
}
