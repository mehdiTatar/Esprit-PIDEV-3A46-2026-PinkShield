<?php

namespace App\Form;

use App\Entity\Doctor;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\TextType;
use Symfony\Component\Form\Extension\Core\Type\PasswordType;
use Symfony\Component\Form\Extension\Core\Type\ChoiceType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Validator\Constraints as Assert;

class DoctorFormType extends AbstractType
{
    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $isEdit = isset($options['data']) && $options['data']->getId();

        $builder
            ->add('email', TextType::class, [
                'label' => 'Email Address',
                'required' => true,
                'attr' => [
                    'class' => 'form-control',
                    'placeholder' => 'Enter email address',
                ],
                'constraints' => [
                    new Assert\NotBlank(['message' => 'Email cannot be empty']),
                    new Assert\Email(['message' => 'Invalid email format']),
                ],
            ])
            ->add('fullName', TextType::class, [
                'label' => 'Full Name',
                'required' => true,
                'attr' => [
                    'class' => 'form-control',
                    'placeholder' => 'Enter full name',
                ],
                'constraints' => [
                    new Assert\NotBlank(['message' => 'Full name cannot be empty']),
                    new Assert\Length([
                        'min' => 2,
                        'max' => 255,
                        'minMessage' => 'Full name must be at least 2 characters',
                        'maxMessage' => 'Full name must not exceed 255 characters',
                    ]),
                ],
            ])
            ->add('speciality', ChoiceType::class, [
                'label' => 'Speciality',
                'required' => true,
                'attr' => [
                    'class' => 'form-control',
                ],
                'choices' => [
                    'Cardiology' => 'Cardiology',
                    'Dermatology' => 'Dermatology',
                    'Orthopedics' => 'Orthopedics',
                    'Neurology' => 'Neurology',
                    'Psychiatry' => 'Psychiatry',
                    'Pediatrics' => 'Pediatrics',
                    'Oncology' => 'Oncology',
                    'Ophthalmology' => 'Ophthalmology',
                    'Gynecology' => 'Gynecology',
                    'General Surgery' => 'General Surgery',
                    'Urology' => 'Urology',
                    'ENT (Otolaryngology)' => 'ENT (Otolaryngology)',
                    'Gastroenterology' => 'Gastroenterology',
                    'Pulmonology' => 'Pulmonology',
                    'Dentistry' => 'Dentistry',
                ],
                'constraints' => [
                    new Assert\NotBlank(['message' => 'Please select a speciality']),
                ],
            ])
            ->add('address', TextType::class, [
                'label' => 'Address',
                'attr' => ['class' => 'form-control'],
            ])
            ->add('phone', TextType::class, [
                'label' => 'Phone Number',
                'attr' => ['class' => 'form-control'],
                'constraints' => [
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
                ],
                'constraints' => !$isEdit ? [
                    new Assert\NotBlank(['message' => 'Password cannot be empty']),
                    new Assert\Length([
                        'min' => 6,
                        'minMessage' => 'Password must be at least 6 characters',
                    ]),
                ] : [
                    new Assert\Length([
                        'min' => 6,
                        'minMessage' => 'Password must be at least 6 characters',
                    ]),
                ],
            ]);
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Doctor::class,
        ]);
    }
}
