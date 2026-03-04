<?php

namespace App\Service;

use App\Entity\Comment;
use Symfony\Component\Mailer\MailerInterface;
use Symfony\Component\Mime\Email;

/**
 * Sends email notifications when someone replies to a comment.
 */
class EmailNotificationService
{
    public function __construct(
        private MailerInterface $mailer,
        private string $senderEmail = 'no-reply@pinkshield.com',
    ) {}

    /**
     * Notify the author of $parentComment that $reply was posted.
     */
    public function notifyCommentReply(Comment $parentComment, Comment $reply): void
    {
        $recipientEmail = $parentComment->getAuthorEmail();
        if (!$recipientEmail) {
            return;
        }

        $postTitle = $parentComment->getBlogPost()?->getTitle() ?? 'a blog post';

        $email = (new Email())
            ->from($this->senderEmail)
            ->to($recipientEmail)
            ->subject('New reply to your comment on "' . $postTitle . '"')
            ->html(
                '<div style="font-family:Arial,sans-serif;max-width:560px;margin:auto;">'
                . '<div style="background:linear-gradient(135deg,#c0396b,#e8557a);padding:24px;border-radius:12px 12px 0 0;">'
                . '<h2 style="color:#fff;margin:0;">💬 New Reply on PinkShield</h2>'
                . '</div>'
                . '<div style="padding:24px;background:#fff;border:1px solid #f0e4eb;border-top:none;border-radius:0 0 12px 12px;">'
                . '<p>Hi <strong>' . htmlspecialchars($parentComment->getAuthorName()) . '</strong>,</p>'
                . '<p><strong>' . htmlspecialchars($reply->getAuthorName()) . '</strong> replied to your comment on "<em>' . htmlspecialchars($postTitle) . '</em>":</p>'
                . '<blockquote style="border-left:3px solid #c0396b;padding:8px 16px;margin:16px 0;color:#555;background:#fdf2f6;border-radius:4px;">'
                . htmlspecialchars(mb_substr($reply->getContent(), 0, 300))
                . '</blockquote>'
                . '<p style="color:#888;font-size:0.85rem;">— PinkShield Medical Platform</p>'
                . '</div></div>'
            );

        try {
            $this->mailer->send($email);
        } catch (\Throwable) {
            // Silently fail — email notification should not block the user flow
        }
    }
}
