# Email Notifications for Comment Replies

## Overview
Users now receive email notifications when someone replies to their comments on blog posts.

## Features
- ✅ Email sent when someone replies to your comment
- ✅ No email sent if you reply to your own comment
- ✅ Beautiful HTML email template with PinkShield branding
- ✅ Direct link to view the reply in the email
- ✅ Content moderation applied to all comments and replies

## How It Works

### For Users
1. Post a comment on any blog post
2. When someone replies to your comment, you'll receive an email notification
3. Click the link in the email to view the full discussion

### Reply to a Comment
1. Go to any blog post with comments
2. Click the "Reply" button under a comment
3. Write your reply and click "Send Reply"
4. The original comment author will receive an email notification

## Email Configuration

### Development/Testing (Current Setup)
The system is currently using `null://null` transport, which means emails are logged but not actually sent. Check the Symfony profiler to see the email content.

### Production Setup

#### Option 1: Gmail
1. Create a Gmail App Password:
   - Go to Google Account settings
   - Security → 2-Step Verification → App passwords
   - Generate a new app password

2. Update `.env`:
```env
MAILER_DSN=gmail+smtp://your-email@gmail.com:your-app-password@default
```

#### Option 2: SMTP Server
Update `.env` with your SMTP credentials:
```env
MAILER_DSN=smtp://username:password@smtp.example.com:587
```

#### Option 3: Third-party Email Services
- **SendGrid**: `MAILER_DSN=sendgrid://KEY@default`
- **Mailgun**: `MAILER_DSN=mailgun://KEY:DOMAIN@default`
- **Amazon SES**: `MAILER_DSN=ses+smtp://ACCESS_KEY:SECRET_KEY@default?region=us-east-1`

## Customization

### Change Email Sender
Edit `src/Service/EmailNotificationService.php`:
```php
->from('noreply@pinkshield.com')  // Change this
```

### Customize Email Template
The email template is in `src/Service/EmailNotificationService.php` in the `getEmailTemplate()` method.

### Change Link URL
Update the link in `getEmailTemplate()`:
```php
'http://localhost:8000/blog/' . $blogPost->getId() . '#comment-' . $replyComment->getId()
```
Change `http://localhost:8000` to your production domain.

## Testing

### Test in Development
1. Start your Symfony server: `php -S localhost:8000 -t public`
2. Login and post a comment on a blog post
3. Login as a different user and reply to that comment
4. Check the Symfony profiler (toolbar at bottom) to see the email that would be sent

### Test Email Sending
To test actual email sending, configure a real MAILER_DSN and:
1. Reply to a comment
2. Check the recipient's email inbox
3. Verify the email looks correct and the link works

## Database Changes
A new migration was created to add the `parent_comment_id` field to the `comment` table. This allows comments to have replies (parent-child relationship).

## Troubleshooting

### Emails not being sent
- Check `MAILER_DSN` in `.env`
- Check Symfony logs: `var/log/dev.log`
- Verify email credentials are correct

### Reply button not showing
- Make sure you're logged in
- Check browser console for JavaScript errors

### Email links not working
- Update the base URL in `EmailNotificationService.php`
- Ensure your server is accessible at that URL
