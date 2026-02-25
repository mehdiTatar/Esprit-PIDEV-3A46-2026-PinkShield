<?php

namespace App\Service;

use Symfony\Contracts\HttpClient\HttpClientInterface;

class AiPharmacistService
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
     * Suggest parapharmacie products based on appointment notes.
     * @return array<array{name: string, description: string, price: float}>
     */
    public function suggestProducts(string $notes): array
    {
        if (empty($this->apiKey)) {
            return $this->getFallbackSuggestions($notes);
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
                            'content' => 'You are a pharmacist AI assistant. Given medical appointment notes, suggest relevant over-the-counter pharmaceutical products. Return a JSON array of objects with "name", "description", and "price" (in USD). Maximum 3 products. Only return the JSON array, no other text.',
                        ],
                        [
                            'role'    => 'user',
                            'content' => 'Appointment notes: ' . $notes,
                        ],
                    ],
                    'temperature' => 0.7,
                    'max_tokens'  => 500,
                ],
            ]);

            $data = $response->toArray();
            $content = $data['choices'][0]['message']['content'] ?? '';

            // Parse JSON from response
            $content = trim($content);
            if (str_starts_with($content, '```')) {
                $content = preg_replace('/^```(?:json)?\s*/', '', $content);
                $content = preg_replace('/\s*```$/', '', $content);
            }

            $products = json_decode($content, true);

            if (is_array($products) && count($products) > 0) {
                return array_map(function ($p) {
                    return [
                        'name'        => $p['name'] ?? 'Product',
                        'description' => $p['description'] ?? '',
                        'price'       => (float)($p['price'] ?? 9.99),
                    ];
                }, $products);
            }

            return $this->getFallbackSuggestions($notes);
        } catch (\Exception $e) {
            error_log('AiPharmacistService error: ' . $e->getMessage());
            return $this->getFallbackSuggestions($notes);
        }
    }

    private function getFallbackSuggestions(string $notes): array
    {
        $lower = strtolower($notes);
        $suggestions = [];

        if (str_contains($lower, 'headache') || str_contains($lower, 'pain') || str_contains($lower, 'fever')) {
            $suggestions[] = ['name' => 'Paracetamol 500mg', 'description' => 'Pain relief and fever reducer', 'price' => 5.99];
        }
        if (str_contains($lower, 'cough') || str_contains($lower, 'cold') || str_contains($lower, 'flu')) {
            $suggestions[] = ['name' => 'Cough Syrup', 'description' => 'Relieves cough and cold symptoms', 'price' => 8.99];
        }
        if (str_contains($lower, 'allergy') || str_contains($lower, 'rash') || str_contains($lower, 'itch')) {
            $suggestions[] = ['name' => 'Antihistamine Tablets', 'description' => 'Allergy relief', 'price' => 7.49];
        }
        if (str_contains($lower, 'stress') || str_contains($lower, 'anxiety') || str_contains($lower, 'sleep')) {
            $suggestions[] = ['name' => 'Herbal Calm Supplement', 'description' => 'Natural stress & sleep support', 'price' => 12.99];
        }
        if (str_contains($lower, 'vitamin') || str_contains($lower, 'fatigue') || str_contains($lower, 'tired')) {
            $suggestions[] = ['name' => 'Multivitamin Complex', 'description' => 'Daily vitamins and minerals', 'price' => 14.99];
        }

        if (empty($suggestions)) {
            $suggestions[] = ['name' => 'Vitamin C 1000mg', 'description' => 'Immune system support', 'price' => 6.99];
            $suggestions[] = ['name' => 'Omega-3 Fish Oil', 'description' => 'Heart and brain health', 'price' => 11.99];
        }

        return array_slice($suggestions, 0, 3);
    }
}
