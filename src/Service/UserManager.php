<?php

namespace App\Service;

use App\Entity\User;

class UserManager
{
    /**
     * Valide un utilisateur de base (vérifie l'email et le nom complet)
     * 
     * @param User $user
     * @return bool
     * @throws \InvalidArgumentException
     */
    public function validate(User $user): bool
    {
        if (!$user->getEmail() || !filter_var($user->getEmail(), FILTER_VALIDATE_EMAIL)) {
            throw new \InvalidArgumentException("L'email de l'utilisateur est obligatoire et doit être valide.");
        }

        if (!$user->getFullName() || strlen($user->getFullName()) < 2) {
            throw new \InvalidArgumentException("Le nom complet de l'utilisateur est obligatoire (min 2 caractères).");
        }

        return true;
    }
}
