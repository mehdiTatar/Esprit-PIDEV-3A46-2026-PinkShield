<?php

namespace App\Controller;

use App\Entity\Wishlist;
use App\Entity\Parapharmacie;
use App\Repository\WishlistRepository;
use App\Repository\ParapharmacieRepository;
use App\Repository\UserRepository;
use App\Service\NotificationService;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/wishlist')]
class WishlistController extends AbstractController
{
    public function __construct(
        private NotificationService $notificationService
    ) {}
    #[Route('/', name: 'wishlist_index')]
    public function index(WishlistRepository $wishlistRepository): Response
    {
        $this->denyAccessUnlessGranted('ROLE_USER');
        
        $user = $this->getUser();
        $wishlistItems = $wishlistRepository->findByUser($user);

        // Calculate total price of all wishlist items
        $totalPrice = 0.0;
        foreach ($wishlistItems as $item) {
            if ($item->getProduct() && $item->getProduct()->getPrice()) {
                $totalPrice += (float) $item->getProduct()->getPrice();
            }
        }

        return $this->render('wishlist/index.html.twig', [
            'wishlistItems' => $wishlistItems,
            'totalPrice' => round($totalPrice, 2),
        ]);
    }

    #[Route('/add/{productId}', name: 'wishlist_add', methods: ['POST'])]
    public function add(
        int $productId,
        ParapharmacieRepository $productRepository,
        WishlistRepository $wishlistRepository,
        EntityManagerInterface $entityManager
    ): JsonResponse {
        $this->denyAccessUnlessGranted('ROLE_USER');
        
        $user = $this->getUser();
        $product = $productRepository->find($productId);

        if (!$product) {
            return new JsonResponse(['success' => false, 'message' => 'Product not found'], 404);
        }

        // Check if already in wishlist
        $existing = $wishlistRepository->findByUserAndProduct($user, $product);
        if ($existing) {
            return new JsonResponse(['success' => false, 'message' => 'Already in wishlist'], 400);
        }

        $wishlist = new Wishlist();
        $wishlist->setUser($user);
        $wishlist->setProduct($product);

        $entityManager->persist($wishlist);
        $entityManager->flush();

        $this->notificationService->notifyAdmins(
            'New Wishlist Addition',
            $user->getFullName() . ' added "' . $product->getName() . '" to their wishlist',
            'info',
            'fas fa-heart'
        );

        return new JsonResponse(['success' => true, 'message' => 'Added to wishlist']);
    }

    #[Route('/remove/{wishlistId}', name: 'wishlist_remove', methods: ['POST'])]
    public function remove(
        int $wishlistId,
        WishlistRepository $wishlistRepository,
        EntityManagerInterface $entityManager
    ): JsonResponse {
        $this->denyAccessUnlessGranted('ROLE_USER');
        
        $user = $this->getUser();
        $wishlistItem = $wishlistRepository->find($wishlistId);

        if (!$wishlistItem || $wishlistItem->getUser() !== $user) {
            return new JsonResponse(['success' => false, 'message' => 'Not found'], 404);
        }

        $entityManager->remove($wishlistItem);
        $entityManager->flush();

        return new JsonResponse(['success' => true, 'message' => 'Removed from wishlist']);
    }

    #[Route('/remove-by-product/{productId}', name: 'wishlist_remove_by_product', methods: ['POST'])]
    public function removeByProduct(
        int $productId,
        ParapharmacieRepository $productRepository,
        WishlistRepository $wishlistRepository,
        EntityManagerInterface $entityManager
    ): JsonResponse {
        $this->denyAccessUnlessGranted('ROLE_USER');
        
        $user = $this->getUser();
        $product = $productRepository->find($productId);

        if (!$product) {
            return new JsonResponse(['success' => false, 'message' => 'Product not found'], 404);
        }

        $wishlistItem = $wishlistRepository->findByUserAndProduct($user, $product);

        if (!$wishlistItem) {
            return new JsonResponse(['success' => false, 'message' => 'Not in wishlist'], 404);
        }

        $entityManager->remove($wishlistItem);
        $entityManager->flush();

        return new JsonResponse(['success' => true, 'message' => 'Removed from wishlist']);
    }

    #[Route('/check/{productId}', name: 'wishlist_check')]
    public function check(
        int $productId,
        ParapharmacieRepository $productRepository,
        WishlistRepository $wishlistRepository
    ): JsonResponse {
        $this->denyAccessUnlessGranted('ROLE_USER');
        
        $user = $this->getUser();
        $product = $productRepository->find($productId);

        if (!$product) {
            return new JsonResponse(['inWishlist' => false]);
        }

        $exists = $wishlistRepository->findByUserAndProduct($user, $product) !== null;

        return new JsonResponse(['inWishlist' => $exists]);
    }

    #[Route('/api/total', name: 'wishlist_api_total', methods: ['GET'])]
    public function apiTotal(WishlistRepository $wishlistRepository): JsonResponse
    {
        $this->denyAccessUnlessGranted('ROLE_USER');

        $user = $this->getUser();
        $wishlistItems = $wishlistRepository->findByUser($user);

        $totalPrice = 0.0;
        $itemCount = 0;
        foreach ($wishlistItems as $item) {
            if ($item->getProduct() && $item->getProduct()->getPrice()) {
                $totalPrice += (float) $item->getProduct()->getPrice();
                $itemCount++;
            }
        }

        return new JsonResponse([
            'success' => true,
            'totalPrice' => round($totalPrice, 2),
            'itemCount' => $itemCount,
        ]);
    }
}
