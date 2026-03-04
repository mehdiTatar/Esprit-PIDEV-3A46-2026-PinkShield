<?php

namespace App\Form;

use App\Entity\Parapharmacie;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\MoneyType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Validator\Constraints as Assert;

class ParapharmacieType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('name', TextType::class, [
                'label' => 'Item name',
                'attr' => [
                    'class' => 'form-control',
                    'placeholder' => 'Enter product name',
                    'data-validate' => 'product-name',
                ],
                'constraints' => [
                    new Assert\NotBlank(['message' => 'Product name cannot be empty']),
                    new Assert\Length(['min' => 2, 'max' => 150, 'minMessage' => 'Product name must be at least 2 characters', 'maxMessage' => 'Product name must not exceed 150 characters']),
                ],
            ])
            ->add('description', TextareaType::class, [
                'label' => 'Description',
                'required' => false,
                'attr' => [
                    'class' => 'form-control',
                    'rows' => 3,
                    'placeholder' => 'Enter product description (optional)',
                    'data-validate' => 'description',
                ],
                'constraints' => [
                    new Assert\Length(['max' => 1000, 'maxMessage' => 'Description cannot exceed 1000 characters']),
                ],
            ])
            ->add('price', MoneyType::class, [
                'label' => 'Price',
                'currency' => 'USD',
                'attr' => ['class' => 'form-control'],
                'constraints' => [
                    new Assert\NotBlank(['message' => 'Price is required']),
                    new Assert\PositiveOrZero(['message' => 'Price must be a positive number']),
                ],
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults(['data_class' => Parapharmacie::class]);
    }
}
