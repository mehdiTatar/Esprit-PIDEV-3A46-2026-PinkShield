<?php
/**
 * TEMPORARY CACHE CLEANER - DELETE AFTER USE
 */

$cacheDir = __DIR__ . '/../var/cache/dev';

function deleteDir($dir) {
    if (!is_dir($dir)) return 0;
    $count = 0;
    $items = new RecursiveIteratorIterator(
        new RecursiveDirectoryIterator($dir, FilesystemIterator::SKIP_DOTS),
        RecursiveIteratorIterator::CHILD_FIRST
    );
    foreach ($items as $item) {
        if ($item->isDir()) {
            @rmdir($item->getRealPath());
        } else {
            @unlink($item->getRealPath());
            $count++;
        }
    }
    @rmdir($dir);
    return $count;
}

$count = deleteDir($cacheDir);

echo '<div style="font-family:monospace;padding:20px;background:#f0fff0;border:2px solid green;">';
echo '<h2 style="color:green">✅ Cache Cleared</h2>';
echo "<p>Deleted <strong>$count</strong> files from <code>var/cache/dev/</code></p>";
echo '<p>Symfony will rebuild the cache on the next page load (may take a moment).</p>';
echo '<p style="margin-top:16px;color:red;font-weight:bold;">⚠️ DELETE this file now: <code>public/clear_cache.php</code></p>';
echo '</div>';
