<?php

namespace App\Service;

use App\Entity\Comment;
use Psr\Log\LoggerInterface;
use Symfony\Component\Mailer\MailerInterface;
use Symfony\Component\Mime\Email;

class EmailNotificationService
{
    public function __construct(
        private MailerInterface $mailer,
        private ?LoggerInterface $logger = null,
    ) {
    }

    /**
     * Send email notification when someone replies to a comment
     */
    public function notifyCommentReply(Comment $originalComment, Comment $replyComment): void
    {
        try {
            $email = (new Email())
                ->from('noreply@pinkshield.com')
                ->to($originalComment->getAuthorEmail())
                ->subject('New reply to your comment on PinkShield')
                ->html($this->getEmailTemplate($originalComment, $replyComment));

            $this->mailer->send($email);

            $this->logger?->info('Email notification sent', [
                'to' => $originalComment->getAuthorEmail(),
                'original_comment_id' => $originalComment->getId(),
                'reply_comment_id' => $replyComment->getId(),
            ]);
        } catch (\Exception $e) {
            $this->logger?->error('Failed to send email notification', [
                'error' => $e->getMessage(),
                'to' => $originalComment->getAuthorEmail(),
            ]);
        }
    }

    private function getEmailTemplate(Comment $originalComment, Comment $replyComment): string
    {
        $blogPost = $originalComment->getBlogPost();
        
        return sprintf('
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #e91e63;">New Reply to Your Comment</h2>
                    
                    <p>Hi %s,</p>
                    
                    <p><strong>%s</strong> replied to your comment on the blog post "<strong>%s</strong>":</p>
                    
                    <div style="background-color: #f5f5f5; padding: 15px; border-left: 4px solid #e91e63; margin: 20px 0;">
                        <p style="margin: 0;"><strong>Your comment:</strong></p>
                        <p style="margin: 10px 0 0 0;">%s</p>
                    </div>
                    
                    <div style="background-color: #fff3f8; padding: 15px; border-left: 4px solid #e91e63; margin: 20px 0;">
                        <p style="margin: 0;"><strong>Reply:</strong></p>
                        <p style="margin: 10px 0 0 0;">%s</p>
                    </div>
                    
                    <p>
                        <a href="%s" style="display: inline-block; padding: 10px 20px; background-color: #e91e63; color: white; text-decoration: none; border-radius: 5px;">View Full Discussion</a>
                    </p>
                    
                    <hr style="border: none; border-top: 1px solid #ddd; margin: 30px 0;">
                    
                    <p style="font-size: 12px; color: #666;">
                        This is an automated notification from PinkShield. You received this email because someone replied to your comment.
                    </p>
                </div>
            </body>
            </html>
        ',
            htmlspecialchars($originalComment->getAuthorName()),
            htmlspecialchars($replyComment->getAuthorName()),
            htmlspecialchars($blogPost->getTitle()),
            htmlspecialchars(substr($originalComment->getContent(), 0, 200) . (strlen($originalComment->getContent()) > 200 ? '...' : '')),
            htmlspecialchars(substr($replyComment->getContent(), 0, 200) . (strlen($replyComment->getContent()) > 200 ? '...' : '')),
            'http://localhost:8000/blog/' . $blogPost->getId() . '#comment-' . $replyComment->getId()
        );
    }
}
