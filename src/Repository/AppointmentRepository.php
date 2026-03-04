<?php

namespace App\Repository;

use App\Entity\Appointment;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<Appointment>
 */
class AppointmentRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, Appointment::class);
    }

    /**
     * @return Appointment[] Returns an array of Appointment objects for a patient
     */
    public function findByPatient(string $email): array
    {
        return $this->createQueryBuilder('a')
            ->andWhere('a.patientEmail = :email')
            ->setParameter('email', $email)
            ->orderBy('a.appointmentDate', 'DESC')
            ->getQuery()
            ->getResult();
    }

    /**
     * @return Appointment[] Returns an array of Appointment objects for a doctor
     */
    public function findByDoctor(string $email): array
    {
        return $this->createQueryBuilder('a')
            ->andWhere('a.doctorEmail = :email')
            ->setParameter('email', $email)
            ->orderBy('a.appointmentDate', 'DESC')
            ->getQuery()
            ->getResult();
    }

    /**
     * Count scheduled appointments for a doctor
     */
    public function countScheduledByDoctor(string $email): int
    {
        return $this->createQueryBuilder('a')
            ->select('COUNT(a.id)')
            ->andWhere('a.doctorEmail = :email')
            ->andWhere("a.status != 'cancelled'")
            ->setParameter('email', $email)
            ->getQuery()
            ->getSingleScalarResult();
    }

    /**
     * Count unique patients for a doctor
     */
    public function countUniquePatientsByDoctor(string $email): int
    {
        return $this->createQueryBuilder('a')
            ->select('COUNT(DISTINCT a.patientEmail)')
            ->andWhere('a.doctorEmail = :email')
            ->setParameter('email', $email)
            ->getQuery()
            ->getSingleScalarResult();
    }

    /**
     * Return unique patients for a doctor with aggregated stats.
     * Each row: patientEmail, patientName, totalAppointments, lastAppointment, statuses
     */
    public function findPatientsByDoctor(string $doctorEmail): array
    {
        $rows = $this->createQueryBuilder('a')
            ->select(
                'a.patientEmail',
                'a.patientName',
                'COUNT(a.id) AS totalAppointments',
                'MAX(a.appointmentDate) AS lastAppointment',
                "SUM(CASE WHEN a.status = 'confirmed' THEN 1 ELSE 0 END) AS confirmed",
                "SUM(CASE WHEN a.status = 'pending'   THEN 1 ELSE 0 END) AS pending",
                "SUM(CASE WHEN a.status = 'cancelled' THEN 1 ELSE 0 END) AS cancelled"
            )
            ->andWhere('a.doctorEmail = :email')
            ->setParameter('email', $doctorEmail)
            ->groupBy('a.patientEmail')
            ->addGroupBy('a.patientName')
            ->orderBy('lastAppointment', 'DESC')
            ->getQuery()
            ->getResult();

        return $rows;
    }

    /**
     * Count appointments grouped by status.
     * @return array{pending:int, confirmed:int, cancelled:int}
     */
    public function countByStatus(): array
    {
        $rows = $this->createQueryBuilder('a')
            ->select('a.status, COUNT(a.id) AS cnt')
            ->groupBy('a.status')
            ->getQuery()
            ->getResult();

        $map = ['pending' => 0, 'confirmed' => 0, 'cancelled' => 0];
        foreach ($rows as $row) {
            $map[$row['status']] = (int)$row['cnt'];
        }
        return $map;
    }

    /**
     * Return top $limit doctors ranked by total appointment count.
     * Each row: doctorName, doctorEmail, total
     */
    public function findTopDoctors(int $limit = 5): array
    {
        return $this->createQueryBuilder('a')
            ->select('a.doctorName, a.doctorEmail, COUNT(a.id) AS total')
            ->groupBy('a.doctorEmail')
            ->addGroupBy('a.doctorName')
            ->orderBy('total', 'DESC')
            ->setMaxResults($limit)
            ->getQuery()
            ->getResult();
    }

    /**
     * Count total appointments.
     */
    public function countTotal(): int
    {
        return (int) $this->createQueryBuilder('a')
            ->select('COUNT(a.id)')
            ->getQuery()
            ->getSingleScalarResult();
    }

    /**
     * Bulk update to cancel appointments that are in the past.
     * This ensures the UI always shows correct status without manual intervention.
     */
    public function autoCancelPastAppointments(): int
    {
        return $this->createQueryBuilder('a')
            ->update()
            ->set('a.status', ':status')
            ->where('a.appointmentDate < :now')
            ->andWhere("a.status != 'cancelled'")
            ->setParameter('status', 'cancelled')
            ->setParameter('now', new \DateTime())
            ->getQuery()
            ->execute();
    }
}