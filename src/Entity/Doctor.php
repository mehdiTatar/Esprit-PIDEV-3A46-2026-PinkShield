<?php

namespace App\Entity;

use App\Repository\DoctorRepository;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Security\Core\User\PasswordAuthenticatedUserInterface;
use Symfony\Component\Security\Core\User\UserInterface;
use Symfony\Component\Validator\Constraints as Assert;

use Scheb\TwoFactorBundle\Model\Email\TwoFactorInterface;

#[ORM\Entity(repositoryClass: DoctorRepository::class)]
#[ORM\UniqueConstraint(name: 'UNIQ_IDENTIFIER_EMAIL_DOCTOR', fields: ['email'])]
class Doctor implements UserInterface, PasswordAuthenticatedUserInterface, TwoFactorInterface
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
    #[Assert\NotBlank(message: 'Password cannot be empty')]
    #[Assert\Length(min: 6, minMessage: 'Password must be at least 6 characters')]
    private ?string $password = null;

    #[ORM\Column(length: 100)]
    #[Assert\NotBlank(message: 'First name cannot be empty')]
    #[Assert\Length(min: 2, max: 100, minMessage: 'First name must be at least 2 characters', maxMessage: 'First name must not exceed 100 characters')]
    #[Assert\Regex(pattern: "/^[a-zA-ZÀ-ÿ\s\-']+$/", message: 'First name can only contain letters, spaces, hyphens and apostrophes')]
    private ?string $firstName = null;

    #[ORM\Column(length: 100)]
    #[Assert\NotBlank(message: 'Last name cannot be empty')]
    #[Assert\Length(min: 2, max: 100, minMessage: 'Last name must be at least 2 characters', maxMessage: 'Last name must not exceed 100 characters')]
    #[Assert\Regex(pattern: "/^[a-zA-ZÀ-ÿ\s\-']+$/", message: 'Last name can only contain letters, spaces, hyphens and apostrophes')]
    private ?string $lastName = null;

    #[ORM\Column(length: 255)]
    #[Assert\NotBlank(message: 'Speciality cannot be empty')]
    #[Assert\Length(min: 2, max: 255, minMessage: 'Speciality must be at least 2 characters', maxMessage: 'Speciality must not exceed 255 characters')]
    #[Assert\Regex(pattern: "/^[a-zA-ZÀ-ÿ\s\-'()]+$/", message: 'Speciality can only contain letters, spaces, hyphens and apostrophes')]
    private ?string $speciality = null;

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
    #[Assert\Choice(choices: ['active', 'inactive', 'suspended'], message: 'Invalid doctor status')]
    private ?string $status = 'active'; // active, inactive, suspended

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
        // guarantee every doctor at least has ROLE_DOCTOR
        if (!in_array('ROLE_DOCTOR', $roles)) {
            $roles[] = 'ROLE_DOCTOR';
        }

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

    public function setPassword(?string $password): static
    {
        $this->password = $password;

        return $this;
    }

    public function getFirstName(): ?string
    {
        return $this->firstName;
    }

    public function setFirstName(string $firstName): static
    {
        $this->firstName = $firstName;

        return $this;
    }

    public function getLastName(): ?string
    {
        return $this->lastName;
    }

    public function setLastName(string $lastName): static
    {
        $this->lastName = $lastName;

        return $this;
    }

    public function getFullName(): ?string
    {
        return $this->firstName . ' ' . $this->lastName;
    }

    public function getSpeciality(): ?string
    {
        return $this->speciality;
    }

    public function setSpeciality(string $speciality): static
    {
        $this->speciality = $speciality;

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

    public function getStatus(): ?string
    {
        return $this->status;
    }

    public function setStatus(?string $status): static
    {
        $this->status = $status;

        return $this;
    }

    public function eraseCredentials(): void
    {
        // nothing sensitive stored in plain text
    }

    public function isEmailAuthEnabled(): bool
    {
        return true;
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
