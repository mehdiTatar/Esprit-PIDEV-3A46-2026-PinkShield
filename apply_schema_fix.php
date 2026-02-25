<?php
// apply_schema_fix.php
try {
    $pdo = new PDO("mysql:host=127.0.0.1;dbname=pinkshield_db", "root", "");
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    $tables = ['user', 'doctor']; // admin already has it or we will check it too
    foreach ($tables as $table) {
        echo "Updating table: $table\n";
        try {
            $pdo->exec("ALTER TABLE `$table` ADD `auth_code` VARCHAR(10) DEFAULT NULL");
            echo "  Successfully added auth_code to $table\n";
        } catch (Exception $e) {
            echo "  Error: " . $e->getMessage() . "\n";
        }
    }
} catch (PDOException $e) {
    echo "Connection failed: " . $e->getMessage() . "\n";
}
