<?php
// check_db.php
require 'vendor/autoload.php';
use App\Kernel;
use Symfony\Bundle\FrameworkBundle\Console\Application;
use Symfony\Component\Console\Input\ArrayInput;
use Symfony\Component\Console\Output\BufferedOutput;

$kernel = new Kernel($_SERVER['APP_ENV'] ?? 'dev', (bool) ($_SERVER['APP_DEBUG'] ?? true));
$kernel->boot();
$container = $kernel->getContainer();
$entityManager = $container->get('doctrine')->getManager();
$connection = $entityManager->getConnection();

$tables = ['user', 'doctor', 'admin'];
foreach ($tables as $table) {
    echo "Processing table: $table\n";
    try {
        $columns = $connection->fetchAllAssociative("DESCRIBE `$table` ");
        foreach ($columns as $column) {
            echo "  Column: " . $column['Field'] . " (" . $column['Type'] . ")\n";
        }
    } catch (\Exception $e) {
        echo "  Error: " . $e->getMessage() . "\n";
    }
    echo "\n";
}
