<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Add face_id and face_image_path columns to the user table
 * for Face++ face recognition integration.
 */
final class Version20260222200000 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Add face_id and face_image_path columns to user table for face recognition';
    }

    public function up(Schema $schema): void
    {
        $this->addSql("ALTER TABLE `user` ADD face_id VARCHAR(255) DEFAULT NULL, ADD face_image_path VARCHAR(255) DEFAULT NULL");
    }

    public function down(Schema $schema): void
    {
        $this->addSql("ALTER TABLE `user` DROP face_id, DROP face_image_path");
    }
}
