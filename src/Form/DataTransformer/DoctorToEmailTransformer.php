<?php

namespace App\Form\DataTransformer;

use App\Entity\Doctor;
use App\Repository\DoctorRepository;
use Symfony\Component\Form\DataTransformerInterface;
use Symfony\Component\Form\Exception\TransformationFailedException;

class DoctorToEmailTransformer implements DataTransformerInterface
{
    public function __construct(private DoctorRepository $doctorRepository)
    {
    }

    /**
     * Transforms a Doctor entity to a string (email) for displaying in the form
     * This is called when rendering the form with existing data
     */
    public function transform(mixed $value): string
    {
        if ($value === null) {
            return '';
        }

        if ($value instanceof Doctor) {
            return $value->getEmail();
        }

        if (is_string($value)) {
            return $value;
        }

        throw new TransformationFailedException('Expected a Doctor object or email string.');
    }

    /**
     * Transforms submitted form data back to a string (email)
     * This is called when the form is submitted
     * EntityType with choice_value='email' returns the email string directly
     */
    public function reverseTransform(mixed $value): ?string
    {
        if (!$value) {
            return null;
        }

        // The value should already be a string (email) from EntityType
        if (is_string($value)) {
            return $value;
        }

        // Handle if it comes as a Doctor object
        if ($value instanceof Doctor) {
            return $value->getEmail();
        }

        throw new TransformationFailedException('Expected an email string or Doctor object.');
    }
}
