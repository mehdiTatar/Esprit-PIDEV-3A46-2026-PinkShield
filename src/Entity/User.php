<?php

namespace App\Entity;

use App\Repository\UserRepository;
use Doctrine\ORM\Mapping as ORM;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Symfony\Component\Security\Core\User\PasswordAuthenticatedUserInterface;
use Symfony\Component\Security\Core\User\UserInterface;
use Symfony\Component\Validator\Constraints as Assert;

use Scheb\TwoFactorBundle\Model\Email\TwoFactorInterface;

#[ORM\Entity(repositoryClass: UserRepository::class)]
#[ORM\UniqueConstraint(name: 'UNIQ_IDENTIFIER_EMAIL', fields: ['email'])]
class User implements UserInterface, PasswordAuthenticatedUserInterface, TwoFactorInterface
{
    #[ORM\Column(length: 10, nullable: true)]
    private ?string $authCode = null;
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\Column(length: 180)]
    #[Assert\NotBlank(message: 'Email cannot be empty')]
    #[Assert\Email(message: 'Invalid email format')]
    private ?string $email = null;

    /**
     * @var list<string> The user roles
     */
    #[ORM\Column]
    private array $roles = [];

    /**
     * @var string The hashed password
     */
    #[ORM\Column]
    #[Assert\Length(min: 6, minMessage: 'Password must be at least 6 characters')]
    private ?string $password = null;

    #[ORM\Column(length: 255, nullable: true)]
    #[Assert\NotBlank(message: 'Full name is required', groups: ['profile'])]
    #[Assert\Length(min: 2, max: 255, minMessage: 'Full name must be at least 2 characters', maxMessage: 'Full name must not exceed 255 characters')]
    #[Assert\Regex(pattern: "/^[a-zA-ZÀ-ÿ\s\-']+$/", message: 'Full name can only contain letters, spaces, hyphens and apostrophes')]
    private ?string $fullName = null;

    #[ORM\Column(length: 100, nullable: true)]
    #[Assert\NotBlank(message: 'First name cannot be empty')]
    #[Assert\Length(min: 2, max: 100, minMessage: 'First name must be at least 2 characters', maxMessage: 'First name must not exceed 100 characters')]
    #[Assert\Regex(pattern: "/^[a-zA-ZÀ-ÿ\s\-']+$/", message: 'First name can only contain letters, spaces, hyphens and apostrophes')]
    private ?string $firstName = null;

    #[ORM\Column(length: 100, nullable: true)]
    #[Assert\NotBlank(message: 'Last name cannot be empty')]
    #[Assert\Length(min: 2, max: 100, minMessage: 'Last name must be at least 2 characters', maxMessage: 'Last name must not exceed 100 characters')]
    #[Assert\Regex(pattern: "/^[a-zA-ZÀ-ÿ\s\-']+$/", message: 'Last name can only contain letters, spaces, hyphens and apostrophes')]
    private ?string $lastName = null;

    #[ORM\Column(length: 255, nullable: true)]
    #[Assert\NotBlank(message: 'Address cannot be empty')]
    #[Assert\Length(min: 2, max: 255, minMessage: 'Address must be at least 2 characters', maxMessage: 'Address must not exceed 255 characters')]
    private ?string $address = null;

    #[ORM\Column(length: 20, nullable: true)]
    #[Assert\NotBlank(message: 'Phone cannot be empty')]
    #[Assert\Length(max: 20, maxMessage: 'Phone must not exceed 20 characters')]
    #[Assert\Regex(pattern: '/^[\d\+\-\(\)\s]*$/', message: 'Invalid phone number format')]
    private ?string $phone = null;

    #[ORM\Column(length: 20, nullable: true)]
    #[Assert\Choice(choices: ['active', 'inactive', 'suspended'], message: 'Invalid user status')]
    private ?string $status = 'active'; // active, inactive, suspended

    #[ORM\Column(length: 100, nullable: true)]
    private ?string $resetToken = null;

    #[ORM\Column(nullable: true)]
    private ?\DateTimeImmutable $resetTokenExpiresAt = null;

    #[ORM\OneToMany(targetEntity: DailyTracking::class, mappedBy: 'user', cascade: ['persist', 'remove'])]
    private Collection $dailyTrackings;

    #[ORM\Column(length: 255, nullable: true)]
    private ?string $faceId = null;

    #[ORM\Column(length: 255, nullable: true)]
    private ?string $faceImagePath = null;

    public function __construct()
    {
        $this->dailyTrackings = new ArrayCollection();
    }

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getEmail(): ?string
    {
        return $this->email;
    }

    public function setEmail(string $email): static
    {
        $this->email = $email;

        return $this;
    }

    /**
     * A visual identifier that represents this user.
     *
     * @see UserInterface
     */
    public function getUserIdentifier(): string
    {
        return (string) $this->email;
    }

    /**
     * @see UserInterface
     */
    public function getRoles(): array
    {
        $roles = $this->roles;
        // guarantee every user at least has ROLE_USER
        $roles[] = 'ROLE_USER';

        return array_unique($roles);
    }

    /**
     * @param list<string> $roles
     */
    public function setRoles(array $roles): static
    {
        $this->roles = $roles;

        return $this;
    }

    /**
     * @see PasswordAuthenticatedUserInterface
     */
    public function getPassword(): ?string
    {
        return $this->password;
    }

    public function setPassword(string $password): static
    {
        $this->password = $password;

        return $this;
    }

    public function getFullName(): ?string
    {
        return $this->fullName;
    }

    public function setFullName(string $fullName): static
    {
        $this->fullName = $fullName;

        return $this;
    }

    public function getFirstName(): ?string
    {
        return $this->firstName;
    }

    public function setFirstName(?string $firstName): static
    {
        $this->firstName = $firstName;

        return $this;
    }

    public function getLastName(): ?string
    {
        return $this->lastName;
    }

    public function setLastName(?string $lastName): static
    {
        $this->lastName = $lastName;

        return $this;
    }

    public function getAddress(): ?string
    {
        return $this->address;
    }

    public function setAddress(?string $address): static
    {
        $this->address = $address;

        return $this;
    }

    public function getPhone(): ?string
    {
        return $this->phone;
    }

    public function setPhone(?string $phone): static
    {
        $this->phone = $phone;

        return $this;
    }

    public function eraseCredentials(): void
    {
        // nothing sensitive stored in plain text
    }

    public function getStatus(): ?string
    {
        return $this->status;
    }

    public function setStatus(?string $status): static
    {
        $this->status = $status;
        return $this;
    }

    public function getResetToken(): ?string
    {
        return $this->resetToken;
    }

    public function setResetToken(?string $resetToken): static
    {
        $this->resetToken = $resetToken;
        return $this;
    }

    public function getResetTokenExpiresAt(): ?\DateTimeImmutable
    {
        return $this->resetTokenExpiresAt;
    }

    public function setResetTokenExpiresAt(?\DateTimeImmutable $resetTokenExpiresAt): static
    {
        $this->resetTokenExpiresAt = $resetTokenExpiresAt;
        return $this;
    }

    /**
     * @return Collection<int, DailyTracking>
     */
    public function getDailyTrackings(): Collection
    {
        return $this->dailyTrackings;
    }

    public function addDailyTracking(DailyTracking $dailyTracking): self
    {
        if (!$this->dailyTrackings->contains($dailyTracking)) {
            $this->dailyTrackings[] = $dailyTracking;
            $dailyTracking->setUser($this);
        }

        return $this;
    }

    public function removeDailyTracking(DailyTracking $dailyTracking): self
    {
        if ($this->dailyTrackings->removeElement($dailyTracking)) {
            // set the owning side to null (unless already changed)
            if ($dailyTracking->getUser() === $this) {
                $dailyTracking->setUser(null);
            }
        }

        return $this;
    }

    public function getFaceId(): ?string
    {
        return $this->faceId;
    }

    public function setFaceId(?string $faceId): static
    {
        $this->faceId = $faceId;

        return $this;
    }

    public function getFaceImagePath(): ?string
    {
        return $this->faceImagePath;
    }

    public function setFaceImagePath(?string $faceImagePath): static
    {
        $this->faceImagePath = $faceImagePath;

        return $this;
    }

    public function isEmailAuthEnabled(): bool
    {
        return true; // Always enabled for now
    }

    public function getEmailAuthRecipient(): string
    {
        return $this->email;
    }

    public function getEmailAuthCode(): string
    {
        return (string) $this->authCode;
    }

    public function setEmailAuthCode(?string $authCode): void
    {
        $this->authCode = $authCode;
    }
}
