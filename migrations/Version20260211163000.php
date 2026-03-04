<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

final class Version20260211163000 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Fix parapharmacie foreign key to reference appointment';
    }

    public function up(Schema $schema): void
    {
        // Drop incorrect FK if exists and add correct one
        $this->addSql('SET FOREIGN_KEY_CHECKS=0');
        // Try to drop old FK names (may vary)
        $this->addSql('ALTER TABLE parapharmacie DROP FOREIGN KEY IF EXISTS FK_PARAPH_APPOINTMENT');
        $this->addSql('ALTER TABLE parapharmacie DROP FOREIGN KEY IF EXISTS `FK_27D41E87E5B533F9`');
        $this->addSql('ALTER TABLE parapharmacie ADD CONSTRAINT FK_PARAPH_APPOINTMENT FOREIGN KEY (appointment_id) REFERENCES appointment (id) ON DELETE CASCADE');
        $this->addSql('SET FOREIGN_KEY_CHECKS=1');
    }

    public function down(Schema $schema): void
    {
        $this->addSql('SET FOREIGN_KEY_CHECKS=0');
        $this->addSql('ALTER TABLE parapharmacie DROP FOREIGN KEY IF EXISTS FK_PARAPH_APPOINTMENT');
        $this->addSql('SET FOREIGN_KEY_CHECKS=1');
    }
}
