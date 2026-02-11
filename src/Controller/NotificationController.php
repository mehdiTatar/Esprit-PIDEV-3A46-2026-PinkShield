<?php

namespace App\Controller;

use App\Repository\NotificationRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/notifications')]
class NotificationController extends AbstractController
{
    #[Route('/', name: 'notifications_index')]
    public function index(NotificationRepository $notificationRepository): Response
    {
        $this->denyAccessUnlessGranted('ROLE_USER');
        
        $user = $this->getUser();
        $notifications = $notificationRepository->findByUser($user);

        return $this->render('notification/index.html.twig', [
            'notifications' => $notifications,
        ]);
    }

    #[Route('/api/list', name: 'notifications_api_list')]
    public function apiList(NotificationRepository $notificationRepository): JsonResponse
    {
        $this->denyAccessUnlessGranted('ROLE_USER');
        
        $user = $this->getUser();
        $notifications = $notificationRepository->findByUser($user);
        $unreadCount = $notificationRepository->countUnreadByUser($user);

        $data = [];
        foreach ($notifications as $notification) {
            $data[] = [
                'id' => $notification->getId(),
                'title' => $notification->getTitle(),
                'message' => $notification->getMessage(),
                'icon' => $notification->getIcon(),
                'type' => $notification->getType(),
                'isRead' => $notification->isRead(),
                'createdAt' => $notification->getCreatedAt()->format('M d, Y H:i'),
            ];
        }

        return new JsonResponse([
            'notifications' => $data,
            'unreadCount' => $unreadCount,
        ]);
    }

    #[Route('/api/mark-read/{id}', name: 'notification_mark_read', methods: ['POST'])]
    public function markRead(
        int $id,
        NotificationRepository $notificationRepository,
        \Doctrine\ORM\EntityManagerInterface $entityManager
    ): JsonResponse {
        $this->denyAccessUnlessGranted('ROLE_USER');
        
        $user = $this->getUser();
        $notification = $notificationRepository->find($id);

        if (!$notification || $notification->getUser() !== $user) {
            return new JsonResponse(['success' => false], 404);
        }

        $notification->setIsRead(true);
        $entityManager->flush();

        return new JsonResponse(['success' => true]);
    }
}
