<?php
$sourceDir = 'C:\Users\mahdi\.gemini\antigravity\brain\12bc7d9e-f7ce-4ea4-ba25-6c17417e9d97\\';
$targetDir = __DIR__ . '\\';

$files = [
    'symfony_app_running_apache_1772680356966.png' => 'login_screenshot.png',
    'pinkshield_blog_profiler_page_1772680539511.png' => 'blog_screenshot.png',
    'symfony_profiler_performance_metrics_1772680476927.png' => 'profiler_performance.png',
    'symfony_profiler_doctrine_queries_1772680674488.png' => 'doctrine_queries.png',
    'symfony_profiler_request_list_1772680488882.png' => 'profiler_requests.png',
];

foreach ($files as $source => $target) {
    if (copy($sourceDir . $source, $targetDir . $target)) {
        echo "Successfully copied $source to $target\n";
    } else {
        echo "Failed to copy $source to $target\n";
    }
}
?>
