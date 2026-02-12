<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Create rating table for patient doctor ratings
 */
final class Version20260211202000 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Create rating table for patient doctor ratings';
    }

    public function up(Schema $schema): void
    {
        $this->addSql('CREATE TABLE rating (
            id INT AUTO_INCREMENT NOT NULL,
            doctor_id INT NOT NULL,
            user_id INT NOT NULL,
            rating TINYINT NOT NULL,
            comment LONGTEXT DEFAULT NULL,
            created_at DATETIME NOT NULL COMMENT \'(DC2Type:datetime_immutable)\',
            PRIMARY KEY(id),
            UNIQUE KEY UNIQ_DOCTOR_USER (doctor_id, user_id),
            FOREIGN KEY (doctor_id) REFERENCES doctor(id) ON DELETE CASCADE,
            FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
            INDEX idx_doctor (doctor_id),
            INDEX idx_user (user_id)
        ) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');
    }

    public function down(Schema $schema): void
    {
        $this->addSql('DROP TABLE rating');
    }
}
