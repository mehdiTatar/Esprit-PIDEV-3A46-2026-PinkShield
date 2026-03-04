<?php

use Symfony\Component\Dotenv\Dotenv;

require dirname(__DIR__).'/vendor/autoload.php';

if (method_exists(Dotenv::class, 'bootEnv')) {
    (new Dotenv())->bootEnv(dirname(__DIR__).'/.env');
}
<<<<<<< HEAD
=======

if ($_SERVER['APP_DEBUG']) {
    umask(0000);
}
>>>>>>> f6cc000b0612f83d55ba4325b4872374266fe173
