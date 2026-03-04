<?php

namespace App\Entity;

use App\Repository\CommentRepository;
use Doctrine\Common\Collections\ArrayCollection;
use Doctrine\Common\Collections\Collection;
use Doctrine\DBAL\Types\Types;
use Doctrine\ORM\Mapping as ORM;
use Symfony\Component\Validator\Constraints as Assert;

#[ORM\Entity(repositoryClass: CommentRepository::class)]
class Comment
{
    #[ORM\Id]
    #[ORM\GeneratedValue]
    #[ORM\Column]
    private ?int $id = null;

    #[ORM\Column(type: Types::TEXT)]
    #[Assert\NotBlank(message: 'Comment cannot be empty')]
    #[Assert\Length(min: 2, max: 5000, minMessage: 'Comment must be at least 2 characters', maxMessage: 'Comment cannot exceed 5000 characters')]
    private ?string $content = null;

    #[ORM\Column(length: 180)]
    #[Assert\NotBlank(message: 'Email cannot be empty')]
    #[Assert\Email(message: 'Invalid email format')]
    private ?string $authorEmail = null;

    #[ORM\Column(length: 255)]
    #[Assert\NotBlank(message: 'Author name cannot be empty')]
    #[Assert\Length(min: 2, max: 255, minMessage: 'Author name must be at least 2 characters', maxMessage: 'Author name must not exceed 255 characters')]
    #[Assert\Regex(pattern: "/^[a-zA-ZÀ-ÿ\s\-']+$/", message: 'Author name can only contain letters, spaces, hyphens and apostrophes')]
    private ?string $authorName = null;

    #[ORM\Column(type: Types::DATETIME_MUTABLE)]
    private ?\DateTimeInterface $createdAt = null;

    #[ORM\ManyToOne(inversedBy: 'comments')]
    #[ORM\JoinColumn(nullable: false)]
    private ?BlogPost $blogPost = null;

    #[ORM\ManyToOne(targetEntity: self::class, inversedBy: 'replies')]
    #[ORM\JoinColumn(nullable: true, onDelete: 'CASCADE')]
    private ?self $parentComment = null;

    #[ORM\OneToMany(mappedBy: 'parentComment', targetEntity: self::class, orphanRemoval: true)]
    private Collection $replies;

    public function __construct()
    {
        $this->createdAt = new \DateTime();
        $this->replies = new ArrayCollection();
    }

    public function getId(): ?int
    {
        return $this->id;
    }

    public function getContent(): ?string
    {
        return $this->content;
    }

    public function setContent(string $content): static
    {
        $this->content = $content;
        return $this;
    }

    public function getAuthorEmail(): ?string
    {
        return $this->authorEmail;
    }

    public function setAuthorEmail(string $authorEmail): static
    {
        $this->authorEmail = $authorEmail;
        return $this;
    }

    public function getAuthorName(): ?string
    {
        return $this->authorName;
    }

    public function setAuthorName(string $authorName): static
    {
        $this->authorName = $authorName;
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

    public function getBlogPost(): ?BlogPost
    {
        return $this->blogPost;
    }

    public function setBlogPost(?BlogPost $blogPost): static
    {
        $this->blogPost = $blogPost;
        return $this;
    }

    public function getParentComment(): ?self
    {
        return $this->parentComment;
    }

    public function setParentComment(?self $parentComment): static
    {
        $this->parentComment = $parentComment;
        return $this;
    }

    /**
     * @return Collection<int, self>
     */
    public function getReplies(): Collection
    {
        return $this->replies;
    }

    public function addReply(self $reply): static
    {
        if (!$this->replies->contains($reply)) {
            $this->replies->add($reply);
            $reply->setParentComment($this);
        }
        return $this;
    }

    public function removeReply(self $reply): static
    {
        if ($this->replies->removeElement($reply)) {
            if ($reply->getParentComment() === $this) {
                $reply->setParentComment(null);
            }
        }
        return $this;
    }
}

