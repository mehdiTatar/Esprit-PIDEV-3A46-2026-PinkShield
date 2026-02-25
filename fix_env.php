<?php
$content = file_get_contents('.env');
$content = str_replace("\r\n", "\n", $content);
file_put_contents('.env', $content);
echo "Fixed line endings in .env\n";
