<?php

namespace App\Controller;

use App\Entity\Wishlist;
use App\Entity\Parapharmacie;
use App\Entity\Notification;
use App\Entity\User;
use App\Repository\WishlistRepository;
use App\Repository\ParapharmacieRepository;
use App\Repository\UserRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\JsonResponse;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Attribute\Route;

#[Route('/wishlist')]
class WishlistController extends AbstractController
{
    #[Route('/', name: 'wishlist_index')]
    public function index(WishlistRepository $wishlistRepository, ParapharmacieRepository $parapharmacieRepository): Response
    {
        $this->denyAccessUnlessGranted('ROLE_USER');
        $user = $this->getUser();
        $wishlistItems = $wishlistRepository->findByUser($user);

        // Smart Bundle Deals logic
        $bundleSuggestions = [];
        if ($wishlistItems) {
            foreach ($wishlistItems as $item) {
                $product = $item->getProduct();
                $category = $product->getCategory();
                if ($category) {
                    // Find companion products by same category
                    $companions = $parapharmacieRepository->findBy(['category' => $category]);
                    foreach ($companions as $companion) {
                        if ($companion->getId() !== $product->getId()) {
                            // Bundle price: 15% off sum
                            $origPrice = (float)$product->getPrice() + (float)$companion->getPrice();
                            $bundlePrice = round($origPrice * 0.85, 2);
                            $bundleSuggestions[] = [
                                'wishlistItem' => $item,
                                'companion' => $companion,
                                'originalPrice' => $origPrice,
                                'bundlePrice' => $bundlePrice,
                            ]; 
                        }
                    }
                }
            }
        }

        return $this->render('wishlist/index.html.twig', [
            'wishlistItems' => $wishlistItems,
            'bundleSuggestions' => $bundleSuggestions,
        ]);
    }

    #[Route('/add/{productId}', name: 'wishlist_add', methods: ['POST'])]
    public function add(
        int $productId,
        ParapharmacieRepository $productRepository,
        WishlistRepository $wishlistRepository,
        UserRepository $userRepository,
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

        // Create notification for all admins
        $admins = $userRepository->findByRole('ROLE_ADMIN');
        $userName = ($user instanceof User) ? $user->getFullName() : $user->getUserIdentifier();
        foreach ($admins as $admin) {
            $notification = new Notification();
            $notification->setUser($admin);
            $notification->setTitle('New Wishlist Addition');
            $notification->setMessage($userName . ' added "' . $product->getName() . '" to their wishlist');
            $notification->setType('info');
            $notification->setIcon('fas fa-heart');
            $entityManager->persist($notification);
        }
        $entityManager->flush();

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
}
