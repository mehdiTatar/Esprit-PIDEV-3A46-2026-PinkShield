<?php

namespace App\Tests\Entity;

use App\Entity\User;
use PHPUnit\Framework\TestCase;
use Symfony\Component\Validator\Validation;

class UserTest extends TestCase
{
    private function createValidator()
    {
        return Validation::createValidatorBuilder()
            ->enableAttributeMapping()
            ->getValidator();
    }

    public function testUserEmailValidation(): void
    {
        $validator = $this->createValidator();
        $user = new User();

        // Test email invalide
        $user->setEmail('invalid-email');
        $violations = $validator->validateProperty($user, 'email');
        $this->assertGreaterThan(0, count($violations), "L'email devrait être invalide");

        // Test email valide
        $user->setEmail('test@example.com');
        $violations = $validator->validateProperty($user, 'email');
        $this->assertCount(0, $violations, "L'email devrait être valide");
    }

    public function testUserFullNameValidation(): void
    {
        $validator = $this->createValidator();
        $user = new User();

        // Test nom trop court
        $user->setFullName('A');
        $violations = $validator->validateProperty($user, 'fullName');
        $this->assertGreaterThan(0, count($violations), "Le nom complet devrait être trop court");

        // Test nom valide
        $user->setFullName('Jean Dupont');
        $violations = $validator->validateProperty($user, 'fullName');
        $this->assertCount(0, $violations, "Le nom complet devrait être valide");
    }
}
