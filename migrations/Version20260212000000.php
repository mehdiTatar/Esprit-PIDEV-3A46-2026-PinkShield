<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

final class Version20260212000000 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Add admin_id column to notification table';
    }

    public function up(Schema $schema): void
    {
        $this->addSql('ALTER TABLE notification ADD admin_id INT DEFAULT NULL');
        $this->addSql('ALTER TABLE notification ADD CONSTRAINT FK_BF5476CA642B8210 FOREIGN KEY (admin_id) REFERENCES admin (id) ON DELETE CASCADE');
        $this->addSql('CREATE INDEX IDX_BF5476CA642B8210 ON notification (admin_id)');
        $this->addSql('ALTER TABLE notification MODIFY user_id INT DEFAULT NULL');
    }

    public function down(Schema $schema): void
    {
        $this->addSql('ALTER TABLE notification DROP FOREIGN KEY FK_BF5476CA642B8210');
        $this->addSql('DROP INDEX IDX_BF5476CA642B8210 ON notification');
        $this->addSql('ALTER TABLE notification DROP admin_id');
    }
}
