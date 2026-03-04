<?php

namespace App\Repository;

use App\Entity\Notification;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Notification>
 */
class NotificationRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Notification::class);
    }

    public function findByUser($user)
    {
        return $this->createQueryBuilder('n')
            ->andWhere('n.user = :user')
            ->setParameter('user', $user)
            ->orderBy('n.createdAt', 'DESC')
            ->getQuery()
            ->getResult();
    }

    public function findByAdmin($admin)
    {
        return $this->createQueryBuilder('n')
            ->andWhere('n.admin = :admin')
            ->setParameter('admin', $admin)
            ->orderBy('n.createdAt', 'DESC')
            ->getQuery()
            ->getResult();
    }

    public function findByUserOrAdmin($userOrAdmin)
    {
        if (get_class($userOrAdmin) === 'App\\Entity\\Admin') {
            return $this->findByAdmin($userOrAdmin);
        }
        return $this->findByUser($userOrAdmin);
    }

    public function findUnreadByUser($user)
    {
        if (get_class($user) === 'App\\Entity\\Admin') {
            return $this->createQueryBuilder('n')
                ->andWhere('n.admin = :admin')
                ->andWhere('n.isRead = false')
                ->setParameter('admin', $user)
                ->orderBy('n.createdAt', 'DESC')
                ->getQuery()
                ->getResult();
        }
        
        return $this->createQueryBuilder('n')
            ->andWhere('n.user = :user')
            ->andWhere('n.isRead = false')
            ->setParameter('user', $user)
            ->orderBy('n.createdAt', 'DESC')
            ->getQuery()
            ->getResult();
    }

    public function countUnreadByUser($user)
    {
        if (get_class($user) === 'App\\Entity\\Admin') {
            return $this->createQueryBuilder('n')
                ->select('COUNT(n.id)')
                ->andWhere('n.admin = :admin')
                ->andWhere('n.isRead = false')
                ->setParameter('admin', $user)
                ->getQuery()
                ->getSingleScalarResult();
        }
        
        return $this->createQueryBuilder('n')
            ->select('COUNT(n.id)')
            ->andWhere('n.user = :user')
            ->andWhere('n.isRead = false')
            ->setParameter('user', $user)
            ->getQuery()
            ->getSingleScalarResult();
    }
}
