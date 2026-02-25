<?php

namespace App\Form;

use App\Entity\Appointment;
use App\Entity\Doctor;
use App\Repository\DoctorRepository;
use App\Form\DataTransformer\DoctorToEmailTransformer;
use Symfony\Bridge\Doctrine\Form\Type\EntityType;
use Symfony\Component\Form\AbstractType;
use Symfony\Component\Form\Extension\Core\Type\DateTimeType;
use Symfony\Component\Form\Extension\Core\Type\TextareaType;
use Symfony\Component\Form\FormBuilderInterface;
use Symfony\Component\Form\FormEvent;
use Symfony\Component\Form\FormEvents;
use Symfony\Component\OptionsResolver\OptionsResolver;
use Symfony\Component\Validator\Constraints as Assert;

class AppointmentFormType extends AbstractType
{
    public function __construct(private DoctorRepository $doctorRepository)
    {
    }

    public function buildForm(FormBuilderInterface $builder, array $options): void
    {
        $builder
            ->add('doctorEmail', EntityType::class, [
                'class' => Doctor::class,
                'choice_label' => function (Doctor $doctor) {
                    $status = $doctor->getStatus() === 'active' ? ' (Available)' : ' (Inactive)';
                    return $doctor->getFullName() . ' - ' . $doctor->getSpeciality() . $status;
                },
                'choice_value' => 'email',
                'placeholder' => 'Choose a doctor...',
                'label' => 'Select Doctor',
                'attr' => [
                    'class' => 'form-select',
                ],
                'query_builder' => function (DoctorRepository $repo) {
                    return $repo->createQueryBuilder('d')
                        ->orderBy('d.fullName', 'ASC');
                },
                'constraints' => [
                    new Assert\NotBlank([
                        'message' => 'Please select a doctor',
                    ]),
                ],
            ])
            ->add('appointmentDate', DateTimeType::class, [
                'label' => 'Date & Time',
                'widget' => 'single_text',
                'input' => 'datetime',
                'required' => true,
                'attr' => [
                    'class' => 'form-control',
                    'placeholder' => 'Select appointment date and time',
                ],
                'constraints' => [
                    new Assert\NotBlank([
                        'message' => 'Appointment date is required',
                    ]),
                    new Assert\GreaterThan([
                        'value' => 'now',
                        'message' => 'Appointment date must be in the future',
                    ]),
                ],
            ])
            ->add('notes', TextareaType::class, [
                'label' => 'Notes (Optional)',
                'required' => false,
                'attr' => [
                    'class' => 'form-control',
                    'rows' => 3,
                    'placeholder' => 'Symptoms or reasons for visit...',
                ],
            ]);

        // Add a model transformer to convert Doctor object to email string
        $builder->get('doctorEmail')->addModelTransformer(
            new DoctorToEmailTransformer($this->doctorRepository)
        );

        // Handle empty datetime values before transformation
        $builder->addEventListener(FormEvents::PRE_SUBMIT, function (FormEvent $event) {
            $data = $event->getData();
            if (is_array($data) && (empty($data['appointmentDate']) || $data['appointmentDate'] === '')) {
                $data['appointmentDate'] = null;
                $event->setData($data);
            }
        });
    }

    public function configureOptions(OptionsResolver $resolver): void
    {
        $resolver->setDefaults([
            'data_class' => Appointment::class,
            'csrf_protection' => true,
            'csrf_field_name' => '_token',
        ]);
    }
}

