<?php

namespace App\Controller;

use App\Service\CommentModerationService;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

class ModerationTestController extends AbstractController
{
    #[Route('/test/moderation', name: 'test_moderation')]
    public function testModeration(Request $request, CommentModerationService $moderationService): Response
    {
        $result = null;
        $testText = '';
        $error = null;

        if ($request->isMethod('POST')) {
            $testText = $request->request->get('test_text', '');
            
            if (!empty($testText)) {
                try {
                    $isApproved = $moderationService->isApproved($testText);
                    $result = [
                        'approved' => $isApproved,
                        'message' => $isApproved 
                            ? 'Comment APPROVED ✓' 
                            : 'Comment BLOCKED ✗ (Contains inappropriate content)'
                    ];
                } catch (\Exception $e) {
                    $error = $e->getMessage();
                }
            }
        }

        return $this->render('moderation_test.html.twig', [
            'result' => $result,
            'test_text' => $testText,
            'error' => $error,
        ]);
    }
}
