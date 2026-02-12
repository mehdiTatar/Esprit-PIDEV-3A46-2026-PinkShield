<?php

namespace App\Controller;

use App\Entity\BlogPost;
use App\Entity\Comment;
use App\Entity\Admin;
use App\Entity\Doctor;
use App\Entity\User;
use App\Repository\BlogPostRepository;
use App\Repository\CommentRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;

#[Route('/blog')]
class BlogController extends AbstractController
{
    #[Route('/', name: 'blog_index')]
    public function index(Request $request, BlogPostRepository $blogPostRepository): Response
    {
        $search = $request->query->get('search', '');
        $sortBy = $request->query->get('sortBy', 'date_newest');
        
        $posts = $blogPostRepository->searchAndFilter($search ?: null, $sortBy);

        return $this->render('blog/index.html.twig', [
            'posts' => $posts,
            'search' => $search,
            'sortBy' => $sortBy,
        ]);
    }

    #[Route('/new', name: 'blog_new')]
    public function new(Request $request, EntityManagerInterface $entityManager): Response
    {
        $this->denyAccessUnlessGranted('IS_AUTHENTICATED_FULLY');
        $post = new BlogPost();
        
        if ($request->isMethod('POST')) {
            $title = $request->request->get('title');
            $content = $request->request->get('content');
            
            if ($title && $content) {
                $user = $this->getUser();
                $post->setTitle($title);
                $post->setContent($content);
                $post->setAuthorEmail($user->getUserIdentifier());
                
                // Get name based on user type
                $name = 'Anonymous';
                if ($user instanceof Admin) {
                    $name = 'Admin';
                } elseif ($user instanceof Doctor || $user instanceof User) {
                    $name = $user->getFullName();
                }
                
                $post->setAuthorName($name);
                $post->setAuthorRole($user->getRoles()[0]);
                
                // Handle image upload
                $uploadedFile = $request->files->get('image');
                if ($uploadedFile) {
                    $filename = uniqid() . '_' . str_replace(' ', '_', $uploadedFile->getClientOriginalName());
                    $uploadedFile->move(
                        $this->getParameter('kernel.project_dir') . '/public/uploads/blog',
                        $filename
                    );
                    $post->setImagePath('/uploads/blog/' . $filename);
                }
                
                $entityManager->persist($post);
                $entityManager->flush();
                
                $this->addFlash('success', 'Post created successfully!');
                return $this->redirectToRoute('blog_index');
            }
        }

        return $this->render('blog/form.html.twig', [
            'post' => $post,
            'action' => 'Create',
        ]);
    }

    #[Route('/{id}', name: 'blog_show', methods: ['GET', 'POST'], requirements: ['id' => '\d+'])]
    public function show(int $id, BlogPostRepository $blogPostRepository, Request $request, EntityManagerInterface $entityManager): Response
    {
        $post = $blogPostRepository->find($id);
        if (!$post) {
            throw $this->createNotFoundException('Post not found');
        }

        if ($request->isMethod('POST') && $this->getUser()) {
            $commentContent = $request->request->get('comment');
            if ($commentContent) {
                $comment = new Comment();
                $comment->setContent($commentContent);
                $comment->setBlogPost($post);
                
                $user = $this->getUser();
                $comment->setAuthorEmail($user->getUserIdentifier());
                
                $name = 'Anonymous';
                if ($user instanceof Admin) {
                    $name = 'Admin';
                } elseif ($user instanceof Doctor || $user instanceof User) {
                    $name = $user->getFullName();
                }
                $comment->setAuthorName($name);
                
                $entityManager->persist($comment);
                $entityManager->flush();
                
                $this->addFlash('success', 'Comment added!');
                return $this->redirectToRoute('blog_show', ['id' => $post->getId()]);
            }
        }

        return $this->render('blog/show.html.twig', [
            'post' => $post,
        ]);
    }

    #[Route('/{id}/edit', name: 'blog_edit', requirements: ['id' => '\d+'])]
    public function edit(int $id, BlogPostRepository $blogPostRepository, Request $request, EntityManagerInterface $entityManager): Response
    {
        $post = $blogPostRepository->find($id);
        if (!$post) {
            throw $this->createNotFoundException('Post not found');
        }

        // Check if user is author or admin
        $user = $this->getUser();
        if (!$user || ($user->getUserIdentifier() !== $post->getAuthorEmail() && !$this->isGranted('ROLE_ADMIN'))) {
            throw $this->createAccessDeniedException('You cannot edit this post.');
        }

        if ($request->isMethod('POST')) {
            $title = $request->request->get('title');
            $content = $request->request->get('content');
            
            if ($title && $content) {
                $post->setTitle($title);
                $post->setContent($content);
                
                // Handle image upload
                $uploadedFile = $request->files->get('image');
                if ($uploadedFile) {
                    // Delete old image if exists
                    if ($post->getImagePath()) {
                        $oldImagePath = $this->getParameter('kernel.project_dir') . '/public' . $post->getImagePath();
                        if (file_exists($oldImagePath)) {
                            unlink($oldImagePath);
                        }
                    }
                    
                    $filename = uniqid() . '_' . str_replace(' ', '_', $uploadedFile->getClientOriginalName());
                    $uploadedFile->move(
                        $this->getParameter('kernel.project_dir') . '/public/uploads/blog',
                        $filename
                    );
                    $post->setImagePath('/uploads/blog/' . $filename);
                }
                
                $entityManager->flush();
                
                $this->addFlash('success', 'Post updated successfully!');
                return $this->redirectToRoute('blog_show', ['id' => $post->getId()]);
            }
        }

        return $this->render('blog/form.html.twig', [
            'post' => $post,
            'action' => 'Edit',
        ]);
    }

    #[Route('/{id}/delete', name: 'blog_delete', requirements: ['id' => '\d+'])]
    public function delete(int $id, BlogPostRepository $blogPostRepository, EntityManagerInterface $entityManager): Response
    {
        $post = $blogPostRepository->find($id);
        if (!$post) {
            throw $this->createNotFoundException('Post not found');
        }

        $user = $this->getUser();
        if (!$user || ($user->getUserIdentifier() !== $post->getAuthorEmail() && !$this->isGranted('ROLE_ADMIN'))) {
            throw $this->createAccessDeniedException('You cannot delete this post.');
        }

        $entityManager->remove($post);
        $entityManager->flush();
        
        $this->addFlash('success', 'Post deleted!');
        return $this->redirectToRoute('blog_index');
    }

    #[Route('/comment/{id}/edit', name: 'comment_edit', requirements: ['id' => '\d+'], methods: ['GET', 'POST'])]
    public function editComment(int $id, CommentRepository $commentRepository, Request $request, EntityManagerInterface $entityManager): Response
    {
        $comment = $commentRepository->find($id);
        if (!$comment) {
            throw $this->createNotFoundException('Comment not found');
        }

        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        if ($request->isMethod('POST')) {
            $content = $request->request->get('content');
            if ($content) {
                $comment->setContent($content);
                $entityManager->flush();
                
                $this->addFlash('success', 'Comment updated successfully!');
                return $this->redirectToRoute('blog_show', ['id' => $comment->getBlogPost()->getId()]);
            }
        }

        return $this->render('blog/comment_edit.html.twig', [
            'comment' => $comment,
        ]);
    }

    #[Route('/comment/{id}/delete', name: 'comment_delete', requirements: ['id' => '\d+'])]
    public function deleteComment(int $id, CommentRepository $commentRepository, EntityManagerInterface $entityManager): Response
    {
        $comment = $commentRepository->find($id);
        if (!$comment) {
            throw $this->createNotFoundException('Comment not found');
        }

        $this->denyAccessUnlessGranted('ROLE_ADMIN');

        $postId = $comment->getBlogPost()->getId();
        $entityManager->remove($comment);
        $entityManager->flush();
        
        $this->addFlash('success', 'Comment deleted successfully!');
        return $this->redirectToRoute('blog_show', ['id' => $postId]);
    }
}
