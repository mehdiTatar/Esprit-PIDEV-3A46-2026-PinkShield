<?php

namespace App\Controller;

use App\Entity\BlogPost;
use App\Entity\Comment;
use App\Entity\Admin;
use App\Entity\Doctor;
use App\Entity\User;
use App\Form\BlogPostFormType;
use App\Repository\BlogPostRepository;
use App\Service\CommentModerationService;
use App\Service\EmailNotificationService;
use App\Service\NotificationService;
use App\Repository\CommentRepository;
use Knp\Component\Pager\PaginatorInterface;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;
use Symfony\Component\Security\Http\Attribute\IsGranted;

#[Route('/blog')]
class BlogController extends AbstractController
{
    public function __construct(
        private NotificationService $notificationService
    ) {}

    #[Route('/', name: 'blog_index')]
    public function index(Request $request, BlogPostRepository $blogPostRepository, PaginatorInterface $paginator): Response
    {
        $q = $request->query->get('q');
        $sortBy = $request->query->get('sortBy');

        // Get query builder for paginator
        $queryBuilder = $blogPostRepository->searchAndSortQueryBuilder($q, $sortBy);

        // Paginate the results
        $pagination = $paginator->paginate(
            $queryBuilder,
            $request->query->getInt('page', 1),
            4 // Items per page
        );

        return $this->render('blog/index.html.twig', [
            'pagination' => $pagination,
            'q' => $q,
            'sortBy' => $sortBy,
        ]);
    }

    #[Route('/new', name: 'blog_new')]
    public function new(Request $request, EntityManagerInterface $entityManager): Response
    {
        $this->denyAccessUnlessGranted('IS_AUTHENTICATED_FULLY');

        $post = new BlogPost();
        $form = $this->createForm(BlogPostFormType::class, $post);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $user = $this->getUser();
            $post->setAuthorEmail($user->getUserIdentifier());

            $name = 'Anonymous';
            if ($user instanceof Admin) {
                $name = 'Admin';
            } elseif ($user instanceof Doctor || $user instanceof User) {
                $name = $user->getFullName();
            }

            $post->setAuthorName($name);
            $post->setAuthorRole($user->getRoles()[0]);

            $uploadedFile = $form->get('image')->getData();
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

            $this->notificationService->notifyAdmins(
                'New Blog Post Created',
                $post->getAuthorName() . ' published a new blog post: "' . $post->getTitle() . '"',
                'info',
                'fas fa-newspaper'
            );

            $this->addFlash('success', 'Post created successfully!');
            return $this->redirectToRoute('blog_index');
        }

        return $this->render('blog/form.html.twig', [
            'post' => $post,
            'form' => $form,
            'action' => 'Create',
        ]);
    }

    #[Route('/{id}', name: 'blog_show', methods: ['GET', 'POST'], requirements: ['id' => '\d+'])]
    public function show(
        int $id,
        BlogPostRepository $blogPostRepository,
        Request $request,
        EntityManagerInterface $entityManager,
        CommentModerationService $commentModerationService,
        EmailNotificationService $emailNotificationService,
        CommentRepository $commentRepository,
        PaginatorInterface $paginator
    ): Response
    {
        $post = $blogPostRepository->find($id);
        if (!$post) {
            throw $this->createNotFoundException('Post not found');
        }

        if ($request->isMethod('POST') && $this->getUser()) {
            $commentContent = $request->request->get('comment');
            $parentCommentId = $request->request->get('parent_comment_id');

            if ($commentContent) {
                // Check moderation
                if (!$commentModerationService->isApproved($commentContent)) {
                    $this->addFlash('error', 'Your comment contains inappropriate content and was not posted.');
                    return $this->redirectToRoute('blog_show', ['id' => $post->getId()]);
                }

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

                // Handle reply to another comment
                if ($parentCommentId) {
                    $parentComment = $commentRepository->find($parentCommentId);
                    if ($parentComment) {
                        $comment->setParentComment($parentComment);

                        // Send email notification to the parent comment author
                        if ($parentComment->getAuthorEmail() !== $user->getUserIdentifier()) {
                            $emailNotificationService->notifyCommentReply($parentComment, $comment);
                        }
                    }
                }

                $entityManager->persist($comment);
                $entityManager->flush();

                $this->notificationService->notifyAdmins(
                    'New Blog Comment',
                    $name . ' commented on "' . $post->getTitle() . '"',
                    'info',
                    'fas fa-comment'
                );

                $this->addFlash('success', 'Comment added!');
                return $this->redirectToRoute('blog_show', ['id' => $post->getId()]);
            }
        }

        // Get only top-level comments (no parent) for pagination
        $queryBuilder = $commentRepository->createQueryBuilder('c')
            ->leftJoin('c.replies', 'r')
            ->addSelect('r')
            ->where('c.blogPost = :post')
            ->andWhere('c.parentComment IS NULL')
            ->setParameter('post', $post)
            ->orderBy('c.createdAt', 'DESC');

        $pagination = $paginator->paginate(
            $queryBuilder,
            $request->query->getInt('page', 1),
            10 // Comments per page
        );

        return $this->render('blog/show.html.twig', [
            'post' => $post,
            'pagination' => $pagination,
        ]);
    }

    #[Route('/{id}/edit', name: 'blog_edit', requirements: ['id' => '\d+'])]
    public function edit(int $id, BlogPostRepository $blogPostRepository, Request $request, EntityManagerInterface $entityManager): Response
    {
        $post = $blogPostRepository->find($id);
        if (!$post) {
            throw $this->createNotFoundException('Post not found');
        }

        $user = $this->getUser();
        if (!$user || ($user->getUserIdentifier() !== $post->getAuthorEmail() && !$this->isGranted('ROLE_ADMIN'))) {
            throw $this->createAccessDeniedException('You cannot edit this post.');
        }

        $form = $this->createForm(BlogPostFormType::class, $post);
        $form->handleRequest($request);

        if ($form->isSubmitted() && $form->isValid()) {
            $uploadedFile = $form->get('image')->getData();
            if ($uploadedFile) {
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

            $this->notificationService->notifyAdmins(
                'Blog Post Edited',
                $user->getUserIdentifier() . ' edited blog post: "' . $post->getTitle() . '"',
                'info',
                'fas fa-edit'
            );

            $this->addFlash('success', 'Post updated successfully!');
            return $this->redirectToRoute('blog_show', ['id' => $post->getId()]);
        }

        return $this->render('blog/form.html.twig', [
            'post' => $post,
            'form' => $form,
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

        $title = $post->getTitle();
        $entityManager->remove($post);
        $entityManager->flush();

        $this->notificationService->notifyAdmins(
            'Blog Post Deleted',
            $user->getUserIdentifier() . ' deleted blog post: "' . $title . '"',
            'warning',
            'fas fa-trash'
        );

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
