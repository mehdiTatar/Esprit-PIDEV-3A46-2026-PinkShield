<?php

namespace App\Repository;

use App\Entity\DailyTracking;
use App\Entity\User;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<DailyTracking>
 *
 * @method DailyTracking|null find($id, $lockMode = null, $lockVersion = null)
 * @method DailyTracking|null findOneBy(array $criteria, array $orderBy = null)
 * @method DailyTracking[]    findAll()
 * @method DailyTracking[]    findBy(array $criteria, array $orderBy = null, $limit = null, $offset = null)
 */
class DailyTrackingRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, DailyTracking::class);
    }

    public function getStatistics(User $user): array
    {
        $qb = $this->createQueryBuilder('d')
            ->select('
                AVG(d.mood) as averageMood,
                AVG(d.stress) as averageStress,
                AVG(d.anxietyLevel) as averageAnxiety,
                AVG(d.focusLevel) as averageFocus,
                AVG(d.motivationLevel) as averageMotivation,
                AVG(d.socialInteractionLevel) as averageSocialInteraction,
                AVG(d.sleepHours) as averageSleepHours,
                AVG(d.energyLevel) as averageEnergy,
                AVG(d.appetiteLevel) as averageAppetite,
                AVG(d.waterIntake) as averageWaterIntake,
                AVG(d.physicalActivityLevel) as averagePhysicalActivity
            ')
            ->where('d.user = :user')
            ->setParameter('user', $user);

        return $qb->getQuery()->getSingleResult();
    }

    public function findRecentByUser(User $user, int $limit = 10): array
    {
        return $this->createQueryBuilder('d')
            ->where('d.user = :user')
            ->setParameter('user', $user)
            ->orderBy('d.date', 'DESC')
            ->setMaxResults($limit)
            ->getQuery()
            ->getResult();
    }

    public function getWeeklyStatistics(User $user): array
    {
        $startDate = new \DateTime('-7 days');
        $qb = $this->createQueryBuilder('d')
            ->select('
                d.date as date,
                AVG(d.mood) as mood,
                AVG(d.stress) as stress,
                AVG(d.energyLevel) as energy,
                AVG(d.sleepHours) as sleep,
                AVG(d.anxietyLevel) as anxiety,
                AVG(d.motivationLevel) as motivation
            ')
            ->where('d.user = :user')
            ->andWhere('d.date >= :startDate')
            ->setParameter('user', $user)
            ->setParameter('startDate', $startDate)
            ->groupBy('d.date')
            ->orderBy('d.date', 'ASC');

        return $qb->getQuery()->getResult();
    }

    public function getMonthlyStatistics(User $user): array
    {
        $startDate = new \DateTime('-30 days');
        $qb = $this->createQueryBuilder('d')
            ->select('
                d.date as date,
                AVG(d.mood) as mood,
                AVG(d.stress) as stress,
                AVG(d.energyLevel) as energy,
                AVG(d.sleepHours) as sleep
            ')
            ->where('d.user = :user')
            ->andWhere('d.date >= :startDate')
            ->setParameter('user', $user)
            ->setParameter('startDate', $startDate)
            ->groupBy('d.date')
            ->orderBy('d.date', 'ASC');

        return $qb->getQuery()->getResult();
    }

    public function getTrendData(User $user, int $days = 30): array
    {
        $startDate = new \DateTime(-$days . ' days');
        $qb = $this->createQueryBuilder('d')
            ->select('d.date, d.mood, d.stress, d.energyLevel')
            ->where('d.user = :user')
            ->andWhere('d.date >= :startDate')
            ->setParameter('user', $user)
            ->setParameter('startDate', $startDate)
            ->orderBy('d.date', 'ASC');

        return $qb->getQuery()->getResult();
    }

    public function getAdvancedStatistics(User $user): array
    {
        $allTime = $this->getStatistics($user);
        $lastWeek = $this->getWeeklyStatistics($user);
        
        // Calculate trends
        $mood_trend = null;
        $stress_trend = null;
        if (count($lastWeek) >= 2) {
            $values = array_map(fn($item) => (float)$item['mood'], $lastWeek);
            if (count($values) > 1) {
                $mood_trend = end($values) - reset($values);
            }
            
            $stress_values = array_map(fn($item) => (float)$item['stress'], $lastWeek);
            if (count($stress_values) > 1) {
                $stress_trend = end($stress_values) - reset($stress_values);
            }
        }

        return [
            'allTime' => $allTime,
            'weeklyData' => $lastWeek,
            'moodTrend' => $mood_trend,
            'stressTrend' => $stress_trend,
            'totalEntries' => $this->count(['user' => $user]),
        ];
    }
}
