<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

final class Version20260211160000 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Add first_name, last_name, address columns to user table';
    }

    public function up(Schema $schema): void
    {
        $this->addSql('ALTER TABLE `user` ADD `first_name` VARCHAR(100) DEFAULT NULL, ADD `last_name` VARCHAR(100) DEFAULT NULL, ADD `address` VARCHAR(255) DEFAULT NULL');
    }

    public function down(Schema $schema): void
    {
        $this->addSql('ALTER TABLE `user` DROP COLUMN `first_name`, DROP COLUMN `last_name`, DROP COLUMN `address`');
    }
}
