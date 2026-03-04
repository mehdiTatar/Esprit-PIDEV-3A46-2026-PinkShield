<?php

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

final class Version20260211170000 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Add daily tracking for mood, stress, and activities';
    }

    public function up(Schema $schema): void
    {
        $this->addSql('CREATE TABLE daily_tracking (
            id INT AUTO_INCREMENT NOT NULL,
            user_id INT NOT NULL,
            date DATE NOT NULL,
            mood INT NOT NULL,
            stress INT NOT NULL,
            activities TEXT DEFAULT NULL,
            created_at DATETIME NOT NULL,
            updated_at DATETIME NOT NULL,
            INDEX IDX_DAILY_TRACKING_USER_ID (user_id),
            PRIMARY KEY(id)
        ) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');

        $this->addSql('ALTER TABLE daily_tracking ADD CONSTRAINT FK_DAILY_TRACKING_USER_ID FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE');
    }

    public function down(Schema $schema): void
    {
        $this->addSql('DROP TABLE daily_tracking');
    }
}