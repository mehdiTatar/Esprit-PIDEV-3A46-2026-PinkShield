<?php

namespace App\Entity;

use App\Repository\HealthLogRepository;
use Doctrine\DBAL\Types\Types;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity(repositoryClass: HealthLogRepository::class)]
class HealthLog
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\Column(length: 180)]
    #[Assert\NotBlank(message: 'User email is required')]
    #[Assert\Email(message: 'Invalid user email format')]
    private ?string $userEmail = null;

    #[ORM\Column]
    #[Assert\NotBlank(message: 'Mood is required')]
    #[Assert\Range(min: 1, max: 5, notInRangeMessage: 'Mood must be between 1 and 5')]
    private ?int $mood = null;

    #[ORM\Column]
    #[Assert\NotBlank(message: 'Stress level is required')]
    #[Assert\Range(min: 1, max: 5, notInRangeMessage: 'Stress level must be between 1 and 5')]
    private ?int $stress = null;

    #[ORM\Column(type: Types::TEXT, nullable: true)]
    #[Assert\Length(max: 1000, maxMessage: 'Activities cannot exceed 1000 characters')]
    private ?string $activities = null;

    #[ORM\Column(type: Types::DATETIME_MUTABLE)]
    private ?\DateTimeInterface $createdAt = null;

    public function __construct()
    {
        $this->createdAt = new \DateTime();
    }

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getUserEmail(): ?string
    {
        return $this->userEmail;
    }

    public function setUserEmail(string $userEmail): static
    {
        $this->userEmail = $userEmail;
        return $this;
    }

    public function getMood(): ?int
    {
        return $this->mood;
    }

    public function setMood(int $mood): static
    {
        $this->mood = $mood;
        return $this;
    }

    public function getStress(): ?int
    {
        return $this->stress;
    }

    public function setStress(int $stress): static
    {
        $this->stress = $stress;
        return $this;
    }

    public function getActivities(): ?string
    {
        return $this->activities;
    }

    public function setActivities(?string $activities): static
    {
        $this->activities = $activities;
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
