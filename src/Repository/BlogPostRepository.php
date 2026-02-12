<?php

namespace App\Repository;

use App\Entity\BlogPost;
use Doctrine\Bundle\DoctrineBundle\Repository\ServiceEntityRepository;
use Doctrine\Persistence\ManagerRegistry;

/**
 * @extends ServiceEntityRepository<BlogPost>
 */
class BlogPostRepository extends ServiceEntityRepository
{
    public function __construct(ManagerRegistry $registry)
    {
        parent::__construct($registry, BlogPost::class);
    }

    /**
     * @return BlogPost[] Returns an array of BlogPost objects ordered by date
     */
    public function findAllLatest(): array
    {
        return $this->createQueryBuilder('b')
            ->orderBy('b.createdAt', 'DESC')
            ->getQuery()
            ->getResult();
    }

    /**
     * Search and filter blog posts
     */
    public function searchAndFilter(?string $search = null, ?string $sortBy = null): array
    {
        $qb = $this->createQueryBuilder('b');

        if ($search) {
            $qb->andWhere('b.title LIKE :search OR b.content LIKE :search OR b.authorName LIKE :search')
               ->setParameter('search', '%' . $search . '%');
        }

        // Default sort by creation date descending
        $orderBy = 'b.createdAt';
        $orderDir = 'DESC';

        if ($sortBy === 'title_asc') {
            $orderBy = 'b.title';
            $orderDir = 'ASC';
        } elseif ($sortBy === 'title_desc') {
            $orderBy = 'b.title';
            $orderDir = 'DESC';
        } elseif ($sortBy === 'date_newest') {
            $orderBy = 'b.createdAt';
            $orderDir = 'DESC';
        } elseif ($sortBy === 'date_oldest') {
            $orderBy = 'b.createdAt';
            $orderDir = 'ASC';
        } elseif ($sortBy === 'author') {
            $orderBy = 'b.authorName';
            $orderDir = 'ASC';
        }

        $qb->orderBy($orderBy, $orderDir);

        return $qb->getQuery()->getResult();
    }
}
