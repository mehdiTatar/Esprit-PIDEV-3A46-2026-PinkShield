<?php
require 'vendor/autoload.php';
$dotenv = new Symfony\Component\Dotenv\Dotenv();
$dotenv->load('.env');
echo 'Via getenv: ' . getenv('HUGGINGFACE_API_KEY') . "\n";
echo 'Via \$_ENV: ' . ($_ENV['HUGGINGFACE_API_KEY'] ?? 'NOT SET') . "\n";
echo 'Via \$_SERVER: ' . ($_SERVER['HUGGINGFACE_API_KEY'] ?? 'NOT SET') . "\n";
