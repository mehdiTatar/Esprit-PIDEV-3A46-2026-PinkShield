<?php
$lines = file('.env');
foreach ($lines as $i => $line) {
    if ($i >= 70) {
        echo ($i + 1) . ': ' . json_encode($line) . "\n";
    }
}
