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
    public function index(ParapharmacieRepository $parapharmacieRepository): Response
    {
        // Allow both patients (ROLE_USER) and doctors (ROLE_DOCTOR) to access parapharmacy
        $user = $this->getUser();
        if (!$user || (!$this->isGranted('ROLE_USER') && !$this->isGranted('ROLE_DOCTOR'))) {
            throw $this->createAccessDeniedException('Access denied');
        }
        
        $products = $parapharmacieRepository->findAll();

        return $this->render('parapharmacy/index.html.twig', [
            'products' => $products,
        ]);
    }
}
