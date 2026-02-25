<?php
/**
 * Script to create sample medical blog post images
 * Using FFmpeg or simple file creation
 */

$uploadDir = __DIR__ . '/public/uploads/blog';

// Ensure directory exists
if (!is_dir($uploadDir)) {
    mkdir($uploadDir, 0755, true);
}

// Create simple placeholder images using FFmpeg
$samples = [
    ['name' => 'heart-health.jpg', 'color' => 'FF6464', 'title' => 'Heart%20Health'],
    ['name' => 'mental-wellness.jpg', 'color' => '6496C8', 'title' => 'Mental%20Wellness'],
    ['name' => 'fitness-guide.jpg', 'color' => '64C864', 'title' => 'Fitness%20Guide'],
    ['name' => 'nutrition-tips.jpg', 'color' => 'FFC864', 'title' => 'Nutrition%20Tips'],
    ['name' => 'sleep-matters.jpg', 'color' => '9664C8', 'title' => 'Sleep%20Matters'],
];

foreach ($samples as $sample) {
    $filePath = $uploadDir . '/' . $sample['name'];
    
    // Use FFmpeg to create a colored image with text
    $cmd = sprintf(
        'ffmpeg -f lavfi -i color=c=#%s:s=800x400:d=1 -vf "drawtext=text=%s:fontsize=60:x=(w-tw)/2:y=(h-th)/2:fontcolor=white" -y "%s" 2>nul',
        $sample['color'],
        $sample['title'],
        $filePath
    );
    
    exec($cmd);
    
    if (file_exists($filePath)) {
        echo "✓ Created: {$sample['name']}\n";
    } else {
        echo "✗ Failed to create: {$sample['name']}\n";
    }
}

echo "\nImage creation completed.\n";
?>
