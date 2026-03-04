<?php
/**
 * TEMPORARY DIAGNOSTIC - DELETE AFTER USE
 * Shows what database Symfony is actually connecting to during a live request.
 */

require_once __DIR__ . '/../vendor/autoload.php';

use Symfony\Component\Dotenv\Dotenv;

// Load all env files exactly as Symfony does
$dotenv = new Dotenv();
$dotenv->loadEnv(__DIR__ . '/../.env');

$dbUrl = $_ENV['DATABASE_URL'] ?? getenv('DATABASE_URL') ?? 'NOT FOUND';
$appEnv = $_ENV['APP_ENV'] ?? getenv('APP_ENV') ?? 'NOT FOUND';

echo '<pre style="font-family:monospace;padding:20px;background:#f5f5f5;">';
echo "APP_ENV: $appEnv\n";
echo "DATABASE_URL: $dbUrl\n\n";

// Parse and connect
if (preg_match('!mysql://([^:]*):([^@]*)@([^:]+):(\d+)/([^?]+)!', $dbUrl, $m)) {
    [, $dbuser, $dbpass, $dbhost, $dbport, $dbname] = $m;
    $dbname = preg_replace('/\?.*/', '', $dbname);
    
    echo "Connecting to: $dbhost:$dbport / $dbname as $dbuser\n\n";
    
    try {
        $pdo = new PDO("mysql:host=$dbhost;port=$dbport;dbname=$dbname;charset=utf8mb4", $dbuser, $dbpass);
        $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
        
        $cols = array_column($pdo->query("SHOW COLUMNS FROM `user`")->fetchAll(PDO::FETCH_ASSOC), 'Field');
        echo "User table columns:\n";
        echo implode(', ', $cols) . "\n\n";
        
        echo in_array('face_id', $cols) ? "✅ face_id EXISTS\n" : "❌ face_id MISSING\n";
        echo in_array('face_image_path', $cols) ? "✅ face_image_path EXISTS\n" : "❌ face_image_path MISSING\n";
        
        // Show all databases
        $dbs = $pdo->query("SHOW DATABASES")->fetchAll(PDO::FETCH_COLUMN);
        echo "\nAll databases: " . implode(', ', $dbs) . "\n";
        
    } catch (Exception $e) {
        echo "ERROR: " . $e->getMessage() . "\n";
    }
} else {
    echo "Could not parse DATABASE_URL!\n";
}

echo '</pre>';
echo '<p style="color:red;font-family:monospace;">DELETE this file: public/fix_db.php</p>';
