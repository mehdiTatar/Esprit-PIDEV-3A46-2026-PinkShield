<?php

namespace App\Entity;

use App\Repository\AppointmentRepository;
use Doctrine\DBAL\Types\Types;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;

#[ORM\Entity(repositoryClass: AppointmentRepository::class)]
class Appointment
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\Column(length: 180)]
    #[Assert\NotBlank(message: 'Patient email is required')]
    #[Assert\Email(message: 'Invalid patient email format')]
    private ?string $patientEmail = null;

    #[ORM\Column(length: 255)]
    #[Assert\NotBlank(message: 'Patient name is required')]
    #[Assert\Length(min: 2, max: 255, minMessage: 'Patient name must be at least 2 characters', maxMessage: 'Patient name must not exceed 255 characters')]
    private ?string $patientName = null;

    #[ORM\Column(length: 180)]
    #[Assert\NotBlank(message: 'Doctor email is required')]
    #[Assert\Email(message: 'Invalid doctor email format')]
    private ?string $doctorEmail = null;

    #[ORM\Column(length: 255)]
    #[Assert\Length(min: 2, max: 255, minMessage: 'Doctor name must be at least 2 characters', maxMessage: 'Doctor name must not exceed 255 characters')]
    private ?string $doctorName = null;

    #[ORM\Column(type: Types::DATETIME_MUTABLE)]
    #[Assert\NotBlank(message: 'Please select an appointment date')]
    #[Assert\GreaterThan('now', message: 'Appointment date must be in the future')]
    private ?\DateTimeInterface $appointmentDate = null;

    #[ORM\Column(length: 20)]
    #[Assert\NotBlank(message: 'Status is required')]
    #[Assert\Choice(choices: ['pending', 'confirmed', 'cancelled'], message: 'Invalid appointment status')]
    private ?string $status = 'pending'; // pending, confirmed, cancelled

    #[ORM\Column(type: Types::TEXT, nullable: true)]
    #[Assert\Length(max: 2000, maxMessage: 'Notes cannot exceed 2000 characters')]
    private ?string $notes = null;

    #[ORM\Column(type: Types::DATETIME_MUTABLE)]
    private ?\DateTimeInterface $createdAt = null;

    #[ORM\OneToMany(mappedBy: 'appointment', targetEntity: \App\Entity\Parapharmacie::class, cascade: ['persist','remove'])]
    private Collection $parapharmacies;

    public function __construct()
    {
        $this->createdAt = new \DateTime();
        $this->parapharmacies = new ArrayCollection();
    }

    /**
     * @return Collection<int, \App\Entity\Parapharmacie>
     */
    public function getParapharmacies(): Collection
    {
        return $this->parapharmacies;
    }

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getPatientEmail(): ?string
    {
        return $this->patientEmail;
    }

    public function setPatientEmail(string $patientEmail): static
    {
        $this->patientEmail = $patientEmail;
        return $this;
    }

    public function getPatientName(): ?string
    {
        return $this->patientName;
    }

    public function setPatientName(string $patientName): static
    {
        $this->patientName = $patientName;
        return $this;
    }

    public function getDoctorEmail(): ?string
    {
        return $this->doctorEmail;
    }

    public function setDoctorEmail(string $doctorEmail): static
    {
        $this->doctorEmail = $doctorEmail;
        return $this;
    }

    public function getDoctorName(): ?string
    {
        return $this->doctorName;
    }

    public function setDoctorName(string $doctorName): static
    {
        $this->doctorName = $doctorName;
        return $this;
    }

    public function getAppointmentDate(): ?\DateTimeInterface
    {
        return $this->appointmentDate;
    }

    public function setAppointmentDate(?\DateTimeInterface $appointmentDate): static
    {
        $this->appointmentDate = $appointmentDate;
        return $this;
    }

    public function getStatus(): ?string
    {
        return $this->status;
    }

    public function setStatus(string $status): static
    {
        $this->status = $status;
        return $this;
    }

    public function getNotes(): ?string
    {
        return $this->notes;
    }

    public function setNotes(?string $notes): static
    {
        $this->notes = $notes;
        return $this;
    }

    public function getCreatedAt(): ?\DateTimeInterface
    {
        return $this->createdAt;
    }

    public function setCreatedAt(\DateTimeInterface $createdAt): static
    {
        $this->createdAt = $createdAt;
        return $this;
    }
}
