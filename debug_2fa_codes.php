<?php
require 'vendor/autoload.php';

use App\Kernel;
use Symfony\Component\Dotenv\Dotenv;

$dotenv = new Dotenv();
$dotenv->load(__DIR__.'/.env', __DIR__.'/.env.local');

$dbUrl = $_ENV['DATABASE_URL'];
if (preg_match('/mysql:\/\/([^:]+):([^@]+)@([^:]+):(\d+)\/(.+)/', $dbUrl, $matches)) {
    $user = $matches[1];
    $pass = $matches[2];
    $host = $matches[3];
    $port = $matches[4];
    $name = $matches[5];
    $dsn = "mysql:host=$host;port=$port;dbname=$name;charset=utf8mb4";
} else {
    echo "Could not parse DATABASE_URL\n";
    exit(1);
}

try {
    $pdo = new PDO($dsn, $user, $pass, [PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION]);
    
    echo "--- User Table ---\n";
    $stmt = $pdo->query("SELECT email, auth_code FROM user WHERE auth_code IS NOT NULL");
    while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
        printf("Email: %s | Code: %s\n", $row['email'], $row['auth_code']);
    }

    echo "\n--- Doctor Table ---\n";
    $stmt = $pdo->query("SELECT email, auth_code FROM doctor WHERE auth_code IS NOT NULL");
    while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
        printf("Email: %s | Code: %s\n", $row['email'], $row['auth_code']);
    }

    echo "\n--- Admin Table ---\n";
    $stmt = $pdo->query("SELECT email, auth_code FROM admin WHERE auth_code IS NOT NULL");
    while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
        printf("Email: %s | Code: %s\n", $row['email'], $row['auth_code']);
    }

} catch (PDOException $e) {
    echo "Error: " . $e->getMessage() . "\n";
}
