<?php
// fix_2fa.php
echo "Starting 2FA Bundle Installation...\n";

function run($cmd) {
    echo "Executing: $cmd\n";
    $output = [];
    $returnValue = 0;
    exec($cmd . ' 2>&1', $output, $returnValue);
    echo implode("\n", $output) . "\n";
    return $returnValue === 0;
}

// 1. Force update the composer.lock with the new requirements
echo "Step 1: Updating composer.lock...\n";
if (run('composer update scheb/2fa-bundle scheb/2fa-email --no-scripts --no-plugins')) {
    echo "Success!\n";
} else {
    echo "Failed to update bundles. Trying a general install...\n";
    run('composer install --no-scripts --no-plugins');
}

// 2. Try to update schema
echo "Step 2: Updating database schema...\n";
run('php bin/console doctrine:schema:update --force');

echo "\nDone. If Step 1 succeeded, your site should now be working.\n";
