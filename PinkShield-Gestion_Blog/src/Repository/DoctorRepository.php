<?php

namespace App\Repository;

use App\Entity\Doctor;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Doctor>
 */
class DoctorRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Doctor::class);
    }

    /**
     * Find a doctor by email
     */
    public function findByEmail(string $email): ?Doctor
    {
        return $this->findOneBy(['email' => $email]);
    }

    /**
     * Find a doctor by ID
     */
    public function findById(int $id): ?Doctor
    {
        return $this->find($id);
    }

    /**
     * Find doctors by full name (partial match)
     */
    public function findByFullName(string $name): array
    {
        return $this->createQueryBuilder('d')
            ->where('d.fullName LIKE :name')
            ->setParameter('name', '%' . $name . '%')
            ->orderBy('d.fullName', 'ASC')
            ->getQuery()
            ->getResult();
    }
}
