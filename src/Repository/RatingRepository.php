<?php

namespace App\Repository;

use App\Entity\Rating;
use App\Entity\Doctor;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Rating>
 *
 * @method Rating|null find($id, $lockMode = null, $lockVersion = null)
 * @method Rating|null findOneBy(array $criteria, array $orderBy = null)
 * @method Rating[]    findAll()
 * @method Rating[]    findBy(array $criteria, array $orderBy = null, $limit = null, $offset = null)
 */
class RatingRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Rating::class);
    }

    /**
     * Get average rating for a doctor
     */
    public function getAverageRating(Doctor $doctor): ?float
    {
        $result = $this->createQueryBuilder('r')
            ->select('AVG(r.rating) as avg_rating')
            ->andWhere('r.doctor = :doctor')
            ->setParameter('doctor', $doctor)
            ->getQuery()
            ->getOneOrNullResult();

        return $result['avg_rating'] ? (float) $result['avg_rating'] : null;
    }

    /**
     * Get rating count for a doctor
     */
    public function getRatingCount(Doctor $doctor): int
    {
        return $this->createQueryBuilder('r')
            ->select('COUNT(r.id)')
            ->andWhere('r.doctor = :doctor')
            ->setParameter('doctor', $doctor)
            ->getQuery()
            ->getSingleScalarResult();
    }

    /**
     * Find rating by doctor and user
     */
    public function findByDoctorAndUser(Doctor $doctor, $user)
    {
        return $this->findOneBy([
            'doctor' => $doctor,
            'user' => $user,
        ]);
    }
}
