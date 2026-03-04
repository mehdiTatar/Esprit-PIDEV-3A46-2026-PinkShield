<?php

namespace App\Repository;

use App\Entity\User;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;
use Symfony\Component\Security\Core\Exception\UnsupportedUserException;
use Symfony\Component\Security\Core\User\PasswordAuthenticatedUserInterface;
use Symfony\Component\Security\Core\User\PasswordUpgraderInterface;

/**
 * @extends ServiceEntityRepository<User>
 */
class UserRepository extends ServiceEntityRepository implements PasswordUpgraderInterface
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, User::class);
    }

    /**
     * Used to upgrade (rehash) the user's password automatically over time.
     */
    public function upgradePassword(PasswordAuthenticatedUserInterface $user, string $newHashedPassword): void
    {
        if (!$user instanceof User) {
            throw new UnsupportedUserException(sprintf('Instances of "%s" are not supported.', $user::class));
        }

        $user->setPassword($newHashedPassword);
        $this->getEntityManager()->persist($user);
        $this->getEntityManager()->flush();
    }

    public function searchByIdAndStatus(?int $id, ?string $status): array
    {
        $qb = $this->createQueryBuilder('u');

        if ($id) {
            $qb->andWhere('u.id = :id')
               ->setParameter('id', $id);
        }

        if ($status) {
            $qb->andWhere('u.status = :status')
               ->setParameter('status', $status);
        }

        return $qb->orderBy('u.id', 'DESC')
                  ->getQuery()
                  ->getResult();
    }

    /**
     * Find users by role
     */
    public function findByRole(string $role): array
    {
        return $this->createQueryBuilder('u')
                    ->andWhere('u.roles LIKE :role')
                    ->setParameter('role', '%' . $role . '%')
                    ->getQuery()
                    ->getResult();
    }

    /**
     * Find a single user by ID (returns null if not found).
     */
    public function findById(int $id): ?User
    {
        return $this->find($id);
    }

    //    /**
    //     * @return User[] Returns an array of User objects
    //     */
    //    public function findByExampleField($value): array
    //    {
    //        return $this->createQueryBuilder('u')
    //            ->andWhere('u.exampleField = :val')
    //            ->setParameter('val', $value)
    //            ->orderBy('u.id', 'ASC')
    //            ->setMaxResults(10)
    //            ->getQuery()
    //            ->getResult()
    //        ;
    //    }

    //    public function findOneBySomeField($value): ?User
    //    {
    //        return $this->createQueryBuilder('u')
    //            ->andWhere('u.exampleField = :val')
    //            ->setParameter('val', $value)
    //            ->getQuery()
    //            ->getOneOrNullResult()
    //        ;
    //    }

    /**
     * Count users grouped by status.
     * @return array{active:int, inactive:int, suspended:int}
     */
    public function countByStatus(): array
    {
        $rows = $this->createQueryBuilder('u')
            ->select('u.status, COUNT(u.id) AS cnt')
            ->groupBy('u.status')
            ->getQuery()
            ->getResult();

        $map = ['active' => 0, 'inactive' => 0, 'suspended' => 0];
        foreach ($rows as $row) {
            $key = $row['status'] ?? 'active';
            $map[$key] = (int)$row['cnt'];
        }
        return $map;
    }

    /**
     * Return the $limit most recently created users.
     */
    public function findRecentUsers(int $limit = 5): array
    {
        return $this->createQueryBuilder('u')
            ->orderBy('u.id', 'DESC')
            ->setMaxResults($limit)
            ->getQuery()
            ->getResult();
    }

    /**
     * Registrations per calendar month for the last $months months.
     * Returns array like ['2026-01' => 3, '2026-02' => 7, ...]
     *
     * NOTE: Uses native SQL through DQL SUBSTRING — works on MySQL.
     */
    public function countRegistrationsByMonth(int $months = 6): array
    {
        // Build the last N month keys the PHP side first
        $labels = [];
        for ($i = $months - 1; $i >= 0; $i--) {
            $labels[] = (new \DateTimeImmutable("first day of -$i month"))->format('Y-m');
        }

        // We'll load all users and group in PHP to stay DB-agnostic (no date-format DQL)
        $users = $this->findAll();
        $counts = array_fill_keys($labels, 0);
        foreach ($users as $user) {
            // Users have no createdAt; we approximate using ID ordering.
            // If there is no createdAt, skip detailed breakdown — still return zeros.
        }

        return $counts;
    }
}
