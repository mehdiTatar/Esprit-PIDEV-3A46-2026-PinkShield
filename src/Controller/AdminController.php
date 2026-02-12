<?php

namespace App\Controller;

use App\Entity\Admin;
use App\Entity\HealthLog;
use App\Entity\BlogPost;
use App\Form\AdminFormType;
use App\Repository\AdminRepository;
use App\Repository\HealthLogRepository;
use App\Repository\BlogPostRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\PasswordHasher\Hasher\UserPasswordHasherInterface;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/admin')]
class AdminController extends AbstractController
{
    #[Route('/manage-admins', name: 'admin_index')]
    public function index(AdminRepository $adminRepository): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        
        $admins = $adminRepository->findAll();

        return $this->render('admin/index.html.twig', [
            'admins' => $admins,
        ]);
    }

    #[Route('/health-logs', name: 'admin_health_logs')]
    public function healthLogs(HealthLogRepository $healthLogRepository): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        $logs = $healthLogRepository->findBy([], ['createdAt' => 'DESC']);

        return $this->render('dashboard/admin_health_logs.html.twig', [
            'logs' => $logs,
        ]);
    }

    #[Route('/health-logs/{id}/delete', name: 'admin_health_log_delete', requirements: ['id' => '\\d+'])]
    public function healthLogDelete(int $id, HealthLogRepository $healthLogRepository, EntityManagerInterface $entityManager): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        $log = $healthLogRepository->find($id);
        if (!$log) {
            throw $this->createNotFoundException('Health log not found');
        }

        $entityManager->remove($log);
        $entityManager->flush();

        $this->addFlash('success', 'Health log deleted successfully');
        return $this->redirectToRoute('admin_health_logs');
    }

    #[Route('/health-stats', name: 'admin_health_stats')]
    public function healthStats(HealthLogRepository $healthLogRepository): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        $all = $healthLogRepository->findAll();
        $total = count($all);
        $avgMood = $total ? array_sum(array_map(fn($l) => $l->getMood(), $all)) / $total : 0;
        $avgStress = $total ? array_sum(array_map(fn($l) => $l->getStress(), $all)) / $total : 0;

        return $this->render('dashboard/admin_health_stats.html.twig', [
            'total' => $total,
            'avgMood' => round($avgMood, 2),
            'avgStress' => round($avgStress, 2),
        ]);
    }

    #[Route('/manage-blog', name: 'admin_manage_blog')]
    public function manageBlog(Request $request, BlogPostRepository $blogPostRepository, EntityManagerInterface $entityManager): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        $search = $request->query->get('search', '');
        $sortBy = $request->query->get('sortBy', 'date_newest');
        
        $posts = $blogPostRepository->searchAndFilter($search ?: null, $sortBy);

        // Handle delete requests
        if ($request->isMethod('POST') && $request->request->get('deleteId')) {
            $deleteId = $request->request->get('deleteId');
            $post = $blogPostRepository->find($deleteId);
            if ($post) {
                $entityManager->remove($post);
                $entityManager->flush();
                $this->addFlash('success', 'Blog post deleted successfully');
                return $this->redirectToRoute('admin_manage_blog', [
                    'search' => $search,
                    'sortBy' => $sortBy
                ]);
            }
        }

        return $this->render('admin/manage_blog.html.twig', [
            'posts' => $posts,
            'search' => $search,
            'sortBy' => $sortBy,
        ]);
    }

    #[Route('/new', name: 'admin_new')]
    public function new(Request $request, EntityManagerInterface $entityManager, UserPasswordHasherInterface $passwordHasher): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        
        $admin = new Admin();
        $form = $this->createForm(AdminFormType::class, $admin);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $admin->setRoles(['ROLE_ADMIN']);
            $admin->setPassword($passwordHasher->hashPassword($admin, $admin->getPassword()));

            $entityManager->persist($admin);
            $entityManager->flush();

            return $this->redirectToRoute('admin_index');
        }

        return $this->render('admin/form.html.twig', [
            'form' => $form,
            'title' => 'Create Admin',
        ]);
    }

    #[Route('/{id}/edit', name: 'admin_edit', requirements: ['id' => '\d+'])]
    public function edit(Request $request, int $id, AdminRepository $adminRepository, EntityManagerInterface $entityManager, UserPasswordHasherInterface $passwordHasher): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        
        $admin = $adminRepository->find($id);
        if (!$admin) {
            throw $this->createNotFoundException('Admin not found');
        }

        $form = $this->createForm(AdminFormType::class, $admin);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            if ($form->get('password')->getData()) {
                $admin->setPassword($passwordHasher->hashPassword($admin, $form->get('password')->getData()));
            }

            $entityManager->flush();
            $this->addFlash('success', 'Admin profile updated successfully!');

            return $this->redirectToRoute('admin_dashboard');
        }

        return $this->render('admin/form.html.twig', [
            'form' => $form,
            'title' => 'Edit Admin',
        ]);
    }

    #[Route('/{id}/delete', name: 'admin_delete', requirements: ['id' => '\d+'])]
    public function delete(Request $request, int $id, AdminRepository $adminRepository, EntityManagerInterface $entityManager): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        
        $admin = $adminRepository->find($id);
        if (!$admin) {
            throw $this->createNotFoundException('Admin not found');
        }

        $entityManager->remove($admin);
        $entityManager->flush();

        $this->addFlash('success', 'Admin deleted successfully!');
        return $this->redirectToRoute('admin_index');
    }

    #[Route('/{id}', name: 'admin_show', requirements: ['id' => '\d+'])]
    public function show(int $id, AdminRepository $adminRepository): Response
    {
        $this->denyAccessUnlessGranted('ROLE_ADMIN');
        
        $admin = $adminRepository->find($id);
        if (!$admin) {
            throw $this->createNotFoundException('Admin not found');
        }

        return $this->render('admin/show.html.twig', [
            'admin' => $admin,
        ]);
    }
}
