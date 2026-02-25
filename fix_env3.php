<?php
$content = <<<'ENV'
###> symfony/framework-bundle ###
APP_ENV=dev
APP_SECRET=
APP_SHARE_DIR=var/share
###< symfony/framework-bundle ###

###> symfony/routing ###
DEFAULT_URI=http://localhost
###< symfony/routing ###

###> doctrine/doctrine-bundle ###
DATABASE_URL="mysql://root:@127.0.0.1:3306/pinkshield2?serverVersion=10.4.32-MariaDB&charset=utf8mb4"
###< doctrine/doctrine-bundle ###

###> Google reCAPTCHA ###
RECAPTCHA_SITE_KEY=6Ld2ZGgsAAAAADdnL6aZUrkbCCqjZWnM162JWpBV
RECAPTCHA_SECRET_KEY=6Ld2ZGgsAAAAALKfyWR5V-JMFG6vyqSrlJ_-OcYR
###< Google reCAPTCHA ###

###> knplabs/knp-snappy-bundle ###
WKHTMLTOPDF_PATH=/usr/local/bin/wkhtmltopdf
WKHTMLTOIMAGE_PATH=/usr/local/bin/wkhtmltoimage
###< knplabs/knp-snappy-bundle ###

###> Twilio SMS Integration ###
TWILIO_ACCOUNT_SID=your_twilio_account_sid_here
TWILIO_AUTH_TOKEN=your_twilio_auth_token_here
TWILIO_PHONE_NUMBER=+1234567890
###< Twilio SMS Integration ###

###> AI Integration ###
AI_PROVIDER=openai
AI_API_KEY=sk-proj-QSj20x-sqPx6uGCAsfIcBU903tFG10vlBkVTYxAKc5VNzV-Jfbs90PkMMx0ctG-7glBKZpaI_0T3BlbkFJ5hDNVDrjNEznQ4Dz4XMmgckZadDiDCVkFx3MgPwUUaLC-ljGlKH6cPOm8nPTisUyEIjk_ZDd3AA
AI_MODEL=gpt-3.5-turbo
###< AI Integration ###

###> Stripe Integration ###
STRIPE_SECRET_KEY=sk_test_51T4kvDIz0mxh74grhkObvEJxQcfEG0tko3GCQ5zCUZ0dQqsPBRWlmFRGBxAF7ybQI8nWAZ08cO7NSO6ouzfUnudE00M2jadrte
STRIPE_PUBLIC_KEY=pk_test_51T4kvDIz0mxh74grB344hm4sCQdMRuMG7evNUG0a0bbTtk6yHZvSF1se9hLr8YHL5E675CGiBbFhMjvAvtOjCsIT00Xw0sC5kN
###< Stripe Integration ###

###> symfony/mailer ###
MAILER_DSN=null://null
###< symfony/mailer ###

###> HuggingFace AI ###
HUGGINGFACE_API_KEY=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
###< HuggingFace AI ###
ENV;

file_put_contents('.env', $content);
echo "Fixed .env file properly\n";
