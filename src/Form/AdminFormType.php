<?php

namespace App\Form;

use App\Entity\Admin;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\Extension\Core\Type\PasswordType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Validator\Constraints as Assert;

class AdminFormType extends AbstractType
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
            'data_class' => Admin::class,
        ]);
    }
}
