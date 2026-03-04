<?php

namespace App\Controller;

use App\Repository\NotificationRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Doctrine\ORM\EntityManagerInterface;

#[Route('/notifications')]
class NotificationController extends AbstractController
{
    public function __construct(
        private readonly NotificationRepository $notificationRepository,
        private readonly EntityManagerInterface $entityManager
    ) {}

    // ─── Index ──────────────────────────────────────────────
    #[Route('/', name: 'notifications_index')]
    public function index(Request $request): Response
    {
        $this->denyAccessUnlessGranted('ROLE_USER');

        $user          = $this->getUser();
        $notifications = $this->notificationRepository->findByUserOrAdmin($user);
        $unreadCount   = $this->notificationRepository->countUnreadByUser($user);

        // Compute type counts for filter badges
        $typeCounts = ['all' => count($notifications), 'info' => 0, 'success' => 0, 'warning' => 0, 'danger' => 0];
        foreach ($notifications as $n) {
            $t = $n->getType();
            if (isset($typeCounts[$t])) {
                $typeCounts[$t]++;
            }
        }

        return $this->render('notification/index.html.twig', [
            'notifications' => $notifications,
            'unreadCount'   => $unreadCount,
            'typeCounts'    => $typeCounts,
        ]);
    }

    // ─── Detail ─────────────────────────────────────────────
    #[Route('/{id}/show', name: 'notification_show', requirements: ['id' => '\d+'])]
    public function show(int $id): Response
    {
        $this->denyAccessUnlessGranted('ROLE_USER');

        $notification = $this->notificationRepository->find($id);

        if (!$notification) {
            throw $this->createNotFoundException('Notification not found.');
        }

        $this->assertOwnership($notification);

        // Auto-mark as read
        if (!$notification->isRead()) {
            $notification->setIsRead(true);
            $this->entityManager->flush();
        }

        return $this->render('notification/show.html.twig', [
            'notification' => $notification,
        ]);
    }

    // ─── API: List ──────────────────────────────────────────
    #[Route('/api/list', name: 'notifications_api_list')]
    public function apiList(): JsonResponse
    {
        $this->denyAccessUnlessGranted('ROLE_USER');

        $user          = $this->getUser();
        $notifications = $this->notificationRepository->findByUserOrAdmin($user);
        $unreadCount   = $this->notificationRepository->countUnreadByUser($user);

        $data = array_map(fn($n) => [
            'id'        => $n->getId(),
            'title'     => $n->getTitle(),
            'message'   => $n->getMessage(),
            'icon'      => $n->getIcon(),
            'type'      => $n->getType(),
            'isRead'    => $n->isRead(),
            'createdAt' => $n->getCreatedAt()->format('M d, Y H:i'),
            'timestamp' => $n->getCreatedAt()->getTimestamp(),
        ], $notifications);

        return new JsonResponse([
            'notifications' => $data,
            'unreadCount'   => $unreadCount,
        ]);
    }

    // ─── API: Mark Single Read ──────────────────────────────
    #[Route('/api/mark-read/{id}', name: 'notification_mark_read', methods: ['POST'])]
    public function markRead(int $id): JsonResponse
    {
        $this->denyAccessUnlessGranted('ROLE_USER');

        $notification = $this->notificationRepository->find($id);

        if (!$notification) {
            return new JsonResponse(['success' => false, 'error' => 'Not found'], 404);
        }

        try {
            $this->assertOwnership($notification);
        } catch (\Exception) {
            return new JsonResponse(['success' => false, 'error' => 'Forbidden'], 403);
        }

        $notification->setIsRead(true);
        $this->entityManager->flush();

        return new JsonResponse(['success' => true]);
    }

    // ─── API: Mark All Read ─────────────────────────────────
    #[Route('/api/mark-all-read', name: 'notification_mark_all_read', methods: ['POST'])]
    public function markAllRead(): JsonResponse
    {
        $this->denyAccessUnlessGranted('ROLE_USER');

        $unread = $this->notificationRepository->findUnreadByUser($this->getUser());

        foreach ($unread as $notification) {
            $notification->setIsRead(true);
        }

        $this->entityManager->flush();

        return new JsonResponse(['success' => true, 'count' => count($unread)]);
    }

    // ─── API: Delete Single ─────────────────────────────────
    #[Route('/api/delete/{id}', name: 'notification_delete', methods: ['POST'])]
    public function delete(int $id): JsonResponse
    {
        $this->denyAccessUnlessGranted('ROLE_USER');

        $notification = $this->notificationRepository->find($id);

        if (!$notification) {
            return new JsonResponse(['success' => false, 'error' => 'Not found'], 404);
        }

        try {
            $this->assertOwnership($notification);
        } catch (\Exception) {
            return new JsonResponse(['success' => false, 'error' => 'Forbidden'], 403);
        }

        $this->entityManager->remove($notification);
        $this->entityManager->flush();

        return new JsonResponse(['success' => true]);
    }

    // ─── Helper: ownership guard ────────────────────────────
    private function assertOwnership(object $notification): void
    {
        $user = $this->getUser();

        if ($user instanceof \App\Entity\Admin) {
            if ($notification->getAdmin() !== $user) {
                throw $this->createAccessDeniedException();
            }
        } else {
            if ($notification->getUser() !== $user) {
                throw $this->createAccessDeniedException();
            }
        }
    }
}
