<?php

namespace App\Controller;

use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

class ForumController extends AbstractController
{
    #[Route('/forum', name: 'forum')]
    public function index(): Response
    {
        $this->denyAccessUnlessGranted('ROLE_USER');

        return $this->render('forum/index.html.twig');
    }

    #[Route('/forum/post', name: 'forum_post')]
    public function post(Request $request): Response
    {
        $this->denyAccessUnlessGranted('ROLE_USER');

        if ($request->isMethod('POST')) {
            $this->addFlash('success', 'Votre message a été publié (placeholder).');
            return $this->redirectToRoute('forum');
        }

        return $this->render('forum/post.html.twig');
    }
}
