<?php
// check_db_direct.php
try {
    $pdo = new PDO("mysql:host=127.0.0.1;dbname=pinkshield_db", "root", "");
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

    $tables = ['user', 'doctor', 'admin'];
    foreach ($tables as $table) {
        echo "Processing table: $table\n";
        try {
            $stmt = $pdo->query("DESC $table");
            while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
                echo "  Column: " . $row['Field'] . " (" . $row['Type'] . ")\n";
            }
        } catch (Exception $e) {
            echo "  Error: " . $e->getMessage() . "\n";
        }
        echo "\n";
    }
} catch (PDOException $e) {
    echo "Connection failed: " . $e->getMessage() . "\n";
}
