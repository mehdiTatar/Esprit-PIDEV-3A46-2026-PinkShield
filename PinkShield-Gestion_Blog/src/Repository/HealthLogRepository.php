<?php

namespace App\Repository;

use App\Entity\HealthLog;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<HealthLog>
 */
class HealthLogRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, HealthLog::class);
    }

    /**
     * @return HealthLog[] Returns an array of HealthLog objects for a user in the last N days
     */
    public function findLastLogs(string $email, int $days = 7): array
    {
        $date = new \DateTime();
        $date->modify("-{$days} days");

        return $this->createQueryBuilder('h')
            ->andWhere('h.userEmail = :email')
            ->andWhere('h.createdAt >= :date')
            ->setParameter('email', $email)
            ->setParameter('date', $date)
            ->orderBy('h.createdAt', 'ASC')
            ->getQuery()
            ->getResult();
    }
}
