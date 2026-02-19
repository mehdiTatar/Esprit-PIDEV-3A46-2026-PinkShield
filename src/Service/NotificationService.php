<?php

namespace App\Service;

use App\Entity\Admin;
use App\Entity\Notification;
use App\Entity\User;
use App\Repository\AdminRepository;
use Doctrine\ORM\EntityManagerInterface;

/**
 * Centralized notification service for creating and dispatching
 * notifications to admins and users throughout the application.
 */
class NotificationService
{
    // ── Notification Types ──────────────────────────────────
    public const TYPE_INFO    = 'info';
    public const TYPE_SUCCESS = 'success';
    public const TYPE_WARNING = 'warning';
    public const TYPE_DANGER  = 'danger';

    // ── Standard Icons (Font Awesome) ───────────────────────
    public const ICON_USER_PLUS    = 'fas fa-user-plus';
    public const ICON_USER_MD      = 'fas fa-user-md';
    public const ICON_USER_EDIT    = 'fas fa-user-edit';
    public const ICON_KEY          = 'fas fa-key';
    public const ICON_LOCK_OPEN    = 'fas fa-lock-open';
    public const ICON_CALENDAR     = 'fas fa-calendar-check';
    public const ICON_CHECK        = 'fas fa-check-circle';
    public const ICON_CANCEL       = 'fas fa-times-circle';
    public const ICON_PILLS        = 'fas fa-pills';
    public const ICON_HEART        = 'fas fa-heart';
    public const ICON_NEWSPAPER    = 'fas fa-newspaper';
    public const ICON_COMMENT      = 'fas fa-comment';
    public const ICON_EDIT         = 'fas fa-edit';
    public const ICON_TRASH        = 'fas fa-trash';
    public const ICON_STAR         = 'fas fa-star';
    public const ICON_HEARTBEAT    = 'fas fa-heartbeat';
    public const ICON_BELL         = 'fas fa-bell';

    public function __construct(
        private readonly AdminRepository $adminRepository,
        private readonly EntityManagerInterface $entityManager
    ) {}

    /**
     * Dispatch a notification to ALL administrators.
     *
     * @param string $title   Short notification headline
     * @param string $message Detailed notification body
     * @param string $type    One of the TYPE_* constants
     * @param string $icon    Font Awesome class string
     */
    public function notifyAdmins(
        string $title,
        string $message,
        string $type = self::TYPE_INFO,
        string $icon = self::ICON_BELL
    ): void {
        $admins = $this->adminRepository->findByRole('ROLE_ADMIN');

        foreach ($admins as $admin) {
            $this->createNotification($title, $message, $type, $icon, admin: $admin);
        }

        $this->entityManager->flush();
    }

    /**
     * Dispatch a notification to a specific user.
     */
    public function notifyUser(
        User $user,
        string $title,
        string $message,
        string $type = self::TYPE_INFO,
        string $icon = self::ICON_BELL
    ): void {
        $this->createNotification($title, $message, $type, $icon, user: $user);
        $this->entityManager->flush();
    }

    /**
     * Internal factory — builds and persists a single Notification entity.
     */
    private function createNotification(
        string $title,
        string $message,
        string $type,
        string $icon,
        ?User $user = null,
        ?Admin $admin = null
    ): Notification {
        $notification = new Notification();
        $notification->setTitle($title);
        $notification->setMessage($message);
        $notification->setType($type);
        $notification->setIcon($icon);

        if ($user) {
            $notification->setUser($user);
        }
        if ($admin) {
            $notification->setAdmin($admin);
        }

        $this->entityManager->persist($notification);

        return $notification;
    }
}
