<?php

namespace App\Tests\Service;

use App\Entity\User;
use App\Service\UserManager;
use PHPUnit\Framework\TestCase;

class UserManagerTest extends TestCase
{
    /**
     * Teste la validation d'un utilisateur valide
     */
    public function testValidUser()
    {
        $user = new User();
        $user->setEmail('user@gmail.com');
        $user->setFullName('Jean Dupont');
        
        $manager = new UserManager();
        $this->assertTrue($manager->validate($user));
    }

    /**
     * Teste l'erreur si l'email de l'utilisateur est vide ou invalide
     */
    public function testUserWithInvalidEmail()
    {
        $this->expectException(\InvalidArgumentException::class);
        
        $user = new User();
        $user->setEmail('email_invalide');
        $user->setFullName('Jean Dupont');
        
        $manager = new UserManager();
        $manager->validate($user);
    }

    /**
     * Teste l'erreur si le nom complet de l'utilisateur est manquant
     */
    public function testUserWithoutFullName()
    {
        $this->expectException(\InvalidArgumentException::class);
        
        $user = new User();
        $user->setEmail('user@gmail.com');
        // fullName non défini
        
        $manager = new UserManager();
        $manager->validate($user);
    }
}
