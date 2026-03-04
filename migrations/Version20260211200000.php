<?php

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

final class Version20260211200000 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Add status column to doctor table';
    }

    public function up(Schema $schema): void
    {
        $this->addSql('ALTER TABLE doctor ADD status VARCHAR(20) DEFAULT \'active\'');
    }

    public function down(Schema $schema): void
    {
        $this->addSql('ALTER TABLE doctor DROP status');
    }
}
