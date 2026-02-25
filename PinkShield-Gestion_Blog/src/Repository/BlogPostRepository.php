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
     * @return BlogPost[]
     */
    public function searchAndSort(?string $q, ?string $sort): array
    {
        return $this->searchAndSortQueryBuilder($q, $sort)
            ->getQuery()
            ->getResult();
    }

    /**
     * Returns query builder for pagination
     */
    public function searchAndSortQueryBuilder(?string $q, ?string $sort)
    {
        $qb = $this->createQueryBuilder('b');

        if ($q && trim($q) !== '') {
            $qb->andWhere('b.title LIKE :q OR b.content LIKE :q OR b.authorName LIKE :q')
                ->setParameter('q', '%' . trim($q) . '%');
        }

        switch ($sort) {
            case 'oldest':
                $qb->orderBy('b.createdAt', 'ASC');
                break;
            case 'title_asc':
                $qb->orderBy('b.title', 'ASC');
                break;
            case 'title_desc':
                $qb->orderBy('b.title', 'DESC');
                break;
            case 'comments_desc':
                $qb->orderBy('SIZE(b.comments)', 'DESC');
                break;
            case 'comments_asc':
                $qb->orderBy('SIZE(b.comments)', 'ASC');
                break;
            case 'author':
                $qb->orderBy('b.authorName', 'ASC');
                break;
            default:
                $qb->orderBy('b.createdAt', 'DESC');
        }

        return $qb;
    }
}
