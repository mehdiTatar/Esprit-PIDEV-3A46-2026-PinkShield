<?php

namespace App\Form;

use App\Entity\Rating;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Validator\Constraints as Assert;

class RatingFormType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('rating', ChoiceType::class, [
                'label' => 'Your Rating',
                'choices' => [
                    '⭐ Poor' => 1,
                    '⭐⭐ Fair' => 2,
                    '⭐⭐⭐ Good' => 3,
                    '⭐⭐⭐⭐ Very Good' => 4,
                    '⭐⭐⭐⭐⭐ Excellent' => 5,
                ],
                'attr' => [
                    'class' => 'form-control',
                ],
                'constraints' => [
                    new Assert\NotBlank(['message' => 'Please select a rating']),
                    new Assert\Range(['min' => 1, 'max' => 5, 'notInRangeMessage' => 'Rating must be between 1 and 5']),
                ],
            ])
            ->add('comment', TextareaType::class, [
                'label' => 'Your Review (Optional)',
                'required' => false,
                'attr' => [
                    'class' => 'form-control',
                    'rows' => 4,
                    'placeholder' => 'Share your experience with this doctor (optional)',
                ],
                'constraints' => [
                    new Assert\Length(['max' => 1000, 'maxMessage' => 'Comment cannot exceed 1000 characters']),
                ],
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Rating::class,
        ]);
    }
}
