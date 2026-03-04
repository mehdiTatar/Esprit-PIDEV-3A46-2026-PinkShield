<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Add address and phone columns to doctor table
 */
final class Version20260211201000 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Add address and phone columns to doctor table';
    }

    public function up(Schema $schema): void
    {
        $this->addSql('ALTER TABLE doctor ADD address VARCHAR(255) DEFAULT NULL');
        $this->addSql('ALTER TABLE doctor ADD phone VARCHAR(20) DEFAULT NULL');
    }

    public function down(Schema $schema): void
    {
        $this->addSql('ALTER TABLE doctor DROP COLUMN address');
        $this->addSql('ALTER TABLE doctor DROP COLUMN phone');
    }
}
