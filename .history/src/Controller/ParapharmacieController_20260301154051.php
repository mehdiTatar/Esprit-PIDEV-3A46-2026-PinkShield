<?php

namespace App\Controller;

use App\Repository\ParapharmacieRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/parapharmacy')]
class ParapharmacieController extends AbstractController
{
    #[Route('/', name: 'parapharmacy_index')]
    public function index(ParapharmacieRepository $parapharmacieRepository, \Symfony\Component\HttpFoundation\Request $request): Response
    {
        // Allow both patients (ROLE_USER) and doctors (ROLE_DOCTOR) to access parapharmacy
        $user = $this->getUser();
        if (!$user || (!$this->isGranted('ROLE_USER') && !$this->isGranted('ROLE_DOCTOR'))) {
            throw $this->createAccessDeniedException('Access denied');
        }

        // Determine sorting parameter from query string
        $sort = $request->query->get('sort');

        $qb = $parapharmacieRepository->createQueryBuilder('p');
        switch ($sort) {
            case 'name_asc':
                $qb->orderBy('p.name', 'ASC');
                break;
            case 'name_desc':
                $qb->orderBy('p.name', 'DESC');
                break;
            case 'price_asc':
                $qb->orderBy('p.price', 'ASC');
                break;
            case 'price_desc':
                $qb->orderBy('p.price', 'DESC');
                break;
            default:
                // no explicit order - database default
                break;
        }

        $products = $qb->getQuery()->getResult();

        return $this->render('parapharmacy/index.html.twig', [
            'products' => $products,
            'currentSort' => $sort,
        ]);
    }
}
