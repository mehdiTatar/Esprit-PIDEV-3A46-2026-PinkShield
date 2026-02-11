<?php

namespace App\Entity;

use App\Repository\DailyTrackingRepository;
use Doctrine\DBAL\Types\Types;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity(repositoryClass: DailyTrackingRepository::class)]
class DailyTracking
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\ManyToOne(targetEntity: User::class, inversedBy: "dailyTrackings")]
    #[ORM\JoinColumn(nullable: false, onDelete: "CASCADE")]
    private ?User $user = null;

    #[ORM\Column(type: Types::DATE_MUTABLE)]
    #[Assert\NotBlank]
    private ?\DateTimeInterface $date = null;

    #[ORM\Column]
    #[Assert\Range(min: 1, max: 10)]
    private int $mood;

    #[ORM\Column]
    #[Assert\Range(min: 1, max: 10)]
    private int $stress;

    #[ORM\Column(type: Types::TEXT, nullable: true)]
    private ?string $activities = null;

    #[ORM\Column(nullable: true)]
    #[Assert\Range(min: 1, max: 10)]
    private ?int $anxietyLevel = null;

    #[ORM\Column(nullable: true)]
    #[Assert\Range(min: 1, max: 10)]
    private ?int $focusLevel = null;

    #[ORM\Column(nullable: true)]
    #[Assert\Range(min: 1, max: 10)]
    private ?int $motivationLevel = null;

    #[ORM\Column(nullable: true)]
    #[Assert\Range(min: 1, max: 10)]
    private ?int $socialInteractionLevel = null;

    #[ORM\Column(nullable: true)]
    #[Assert\Range(min: 0, max: 24)]
    private ?int $sleepHours = null;

    #[ORM\Column(nullable: true)]
    #[Assert\Range(min: 1, max: 10)]
    private ?int $energyLevel = null;

    #[ORM\Column(type: Types::TEXT, nullable: true)]
    private ?string $symptoms = null;

    #[ORM\Column(type: Types::BOOLEAN, nullable: true)]
    private ?bool $medicationTaken = null;

    #[ORM\Column(nullable: true)]
    #[Assert\Range(min: 1, max: 10)]
    private ?int $appetiteLevel = null;

    #[ORM\Column(nullable: true)]
    #[Assert\Range(min: 0, max: 100)]
    private ?int $waterIntake = null;

    #[ORM\Column(nullable: true)]
    #[Assert\Range(min: 1, max: 10)]
    private ?int $physicalActivityLevel = null;

    #[ORM\Column(type: Types::DATETIME_MUTABLE)]
    private \DateTimeInterface $createdAt;

    #[ORM\Column(type: Types::DATETIME_MUTABLE)]
    private \DateTimeInterface $updatedAt;

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getUser(): ?User
    {
        return $this->user;
    }

    public function setUser(?User $user): static
    {
        $this->user = $user;
        return $this;
    }

    public function getDate(): ?\DateTimeInterface
    {
        return $this->date;
    }

    public function setDate(?\DateTimeInterface $date): static
    {
        $this->date = $date;
        return $this;
    }

    public function getMood(): int
    {
        return $this->mood;
    }

    public function setMood(int $mood): static
    {
        $this->mood = $mood;
        return $this;
    }

    public function getStress(): int
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

    public function getAnxietyLevel(): ?int
    {
        return $this->anxietyLevel;
    }

    public function setAnxietyLevel(?int $anxietyLevel): static
    {
        $this->anxietyLevel = $anxietyLevel;
        return $this;
    }

    public function getFocusLevel(): ?int
    {
        return $this->focusLevel;
    }

    public function setFocusLevel(?int $focusLevel): static
    {
        $this->focusLevel = $focusLevel;
        return $this;
    }

    public function getMotivationLevel(): ?int
    {
        return $this->motivationLevel;
    }

    public function setMotivationLevel(?int $motivationLevel): static
    {
        $this->motivationLevel = $motivationLevel;
        return $this;
    }

    public function getSocialInteractionLevel(): ?int
    {
        return $this->socialInteractionLevel;
    }

    public function setSocialInteractionLevel(?int $socialInteractionLevel): static
    {
        $this->socialInteractionLevel = $socialInteractionLevel;
        return $this;
    }

    public function getSleepHours(): ?int
    {
        return $this->sleepHours;
    }

    public function setSleepHours(?int $sleepHours): static
    {
        $this->sleepHours = $sleepHours;
        return $this;
    }

    public function getEnergyLevel(): ?int
    {
        return $this->energyLevel;
    }

    public function setEnergyLevel(?int $energyLevel): static
    {
        $this->energyLevel = $energyLevel;
        return $this;
    }

    public function getSymptoms(): ?string
    {
        return $this->symptoms;
    }

    public function setSymptoms(?string $symptoms): static
    {
        $this->symptoms = $symptoms;
        return $this;
    }

    public function isMedicationTaken(): ?bool
    {
        return $this->medicationTaken;
    }

    public function setMedicationTaken(?bool $medicationTaken): static
    {
        $this->medicationTaken = $medicationTaken;
        return $this;
    }

    public function getAppetiteLevel(): ?int
    {
        return $this->appetiteLevel;
    }

    public function setAppetiteLevel(?int $appetiteLevel): static
    {
        $this->appetiteLevel = $appetiteLevel;
        return $this;
    }

    public function getWaterIntake(): ?int
    {
        return $this->waterIntake;
    }

    public function setWaterIntake(?int $waterIntake): static
    {
        $this->waterIntake = $waterIntake;
        return $this;
    }

    public function getPhysicalActivityLevel(): ?int
    {
        return $this->physicalActivityLevel;
    }

    public function setPhysicalActivityLevel(?int $physicalActivityLevel): static
    {
        $this->physicalActivityLevel = $physicalActivityLevel;
        return $this;
    }

    public function getCreatedAt(): \DateTimeInterface
    {
        return $this->createdAt;
    }

    public function setCreatedAt(\DateTimeInterface $createdAt): static
    {
        $this->createdAt = $createdAt;
        return $this;
    }

    public function getUpdatedAt(): \DateTimeInterface
    {
        return $this->updatedAt;
    }

    public function setUpdatedAt(\DateTimeInterface $updatedAt): static
    {
        $this->updatedAt = $updatedAt;
        return $this;
    }
}