<?php

namespace App\Form;

use App\Entity\User;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\Extension\Core\Type\PasswordType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Validator\Constraints as Assert;

class UserFormType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $isEdit = isset($options['data']) && $options['data']->getId();

        $builder
            ->add('email', TextType::class, [
                'label' => 'Email Address',
                'required' => false,
                'attr' => [
                    'class' => 'form-control',
                    'placeholder' => 'Enter email address',
                    'data-validate' => 'email',
                ],
                'constraints' => [
                    new Assert\NotBlank(['message' => 'Email cannot be empty']),
                    new Assert\Email(['message' => 'Invalid email format']),
                ],
            ])
            ->add('firstName', TextType::class, [
                'label' => 'Prénom',
                'required' => false,
                'attr' => [
                    'class' => 'form-control',
                    'placeholder' => 'Enter first name',
                    'data-validate' => 'name',
                ],
                'constraints' => [
                    new Assert\NotBlank(['message' => 'First name cannot be empty']),
                    new Assert\Length(['min' => 2, 'max' => 100, 'minMessage' => 'First name must be at least 2 characters', 'maxMessage' => 'First name must not exceed 100 characters']),
                    new Assert\Regex(['pattern' => "/^[a-zA-ZÀ-ÿ\s\-']+$/", 'message' => 'First name can only contain letters, spaces, hyphens and apostrophes']),
                ],
            ])
            ->add('lastName', TextType::class, [
                'label' => 'Nom',
                'required' => false,
                'attr' => [
                    'class' => 'form-control',
                    'placeholder' => 'Enter last name',
                    'data-validate' => 'name',
                ],
                'constraints' => [
                    new Assert\NotBlank(['message' => 'Last name cannot be empty']),
                    new Assert\Length(['min' => 2, 'max' => 100, 'minMessage' => 'Last name must be at least 2 characters', 'maxMessage' => 'Last name must not exceed 100 characters']),
                    new Assert\Regex(['pattern' => "/^[a-zA-ZÀ-ÿ\s\-']+$/", 'message' => 'Last name can only contain letters, spaces, hyphens and apostrophes']),
                ],
            ])
            ->add('address', TextType::class, [
                'label' => 'Address',
                'required' => false,
                'attr' => [
                    'class' => 'form-control',
                    'placeholder' => 'Enter address',
                    'data-validate' => 'address',
                ],
                'constraints' => [
                    new Assert\NotBlank(['message' => 'Address cannot be empty']),
                    new Assert\Length(['min' => 2, 'max' => 255, 'minMessage' => 'Address must be at least 2 characters', 'maxMessage' => 'Address must not exceed 255 characters']),
                ],
            ])
            ->add('phone', TextType::class, [
                'label' => 'Phone Number',
                'required' => false,
                'attr' => [
                    'class' => 'form-control',
                    'placeholder' => 'Enter phone number',
                    'data-validate' => 'phone',
                ],
                'constraints' => [
                    new Assert\NotBlank(['message' => 'Phone cannot be empty']),
                    new Assert\Length([
                        'max' => 20,
                        'maxMessage' => 'Phone must not exceed 20 characters',
                    ]),
                    new Assert\Regex([
                        'pattern' => '/^[\d\+\-\(\)\s]*$/',
                        'message' => 'Invalid phone number format',
                    ]),
                ],
            ])
            ->add('password', PasswordType::class, [
                'label' => $isEdit ? 'Password (leave blank to keep current)' : 'Password',
                'required' => false,
                'attr' => [
                    'class' => 'form-control',
                    'placeholder' => 'Enter password',
                    'data-validate' => 'password',
                ],
                'constraints' => !$isEdit ? [
                    new Assert\NotBlank(['message' => 'Password cannot be empty']),
                    new Assert\Length([
                        'min' => 6,
                        'minMessage' => 'Password must be at least 6 characters',
                    ]),
                ] : [],
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => User::class,
        ]);
    }
}
