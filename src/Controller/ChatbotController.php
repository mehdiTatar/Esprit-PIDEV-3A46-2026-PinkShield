<?php

namespace App\Controller;

use App\Service\ChatbotService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\Routing\Attribute\Route;

class ChatbotController extends AbstractController
{
    public function __construct(private ChatbotService $chatbotService) {}

    #[Route('/chatbot/ask', name: 'chatbot_ask', methods: ['POST'])]
    public function ask(Request $request): JsonResponse
    {
        // Must be logged in
        if (!$this->getUser()) {
            return $this->json(['reply' => '🔒 Please log in to use the AI assistant.'], 403);
        }

        $data    = json_decode($request->getContent(), true);
        $message = trim($data['message'] ?? '');

        if ($message === '') {
            return $this->json(['reply' => 'Please type a message first!']);
        }

        if (mb_strlen($message) > 500) {
            return $this->json(['reply' => 'Your message is too long. Please keep it under 500 characters.']);
        }

        $reply = $this->chatbotService->getResponse($message);

        return $this->json(['reply' => $reply]);
    }
}
