<?php

namespace App\Tests\Entity;

use App\Entity\Parapharmacie;
use PHPUnit\Framework\TestCase;
use Symfony\Component\Validator\Validation;
use Symfony\Component\Validator\Constraints\PositiveOrZero;

class ParapharmacieTest extends TestCase
{
    private function createValidator()
    {
        return Validation::createValidatorBuilder()
            ->enableAnnotationMapping()
            ->getValidator();
    }

    public function testStockCanBePositiveOrZero(): void
    {
        $validator = $this->createValidator();
        $product = new Parapharmacie();

        // setting a valid stock should not produce violations
        $product->setStock(25);

        $violations = $validator->validate($product);
        $this->assertCount(0, $violations, "Expected no validation errors for non-negative stock");
    }

    public function testStockCannotBeNegative(): void
    {
        $validator = $this->createValidator();
        $product = new Parapharmacie();

        // negative stock is against business rule
        $product->setStock(-3);

        $violations = $validator->validate($product);
        $this->assertGreaterThan(0, $violations->count(), "Expected at least one violation for negative stock");

        // optionally check message
        $this->assertStringContainsString('Stock cannot be negative', (string) $violations);
    }
}
