<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

final class Version20260218120000 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Replace fullName with firstName and lastName in doctor table';
    }

    public function up(Schema $schema): void
    {
        // Add new columns
        $this->addSql('ALTER TABLE doctor ADD first_name VARCHAR(100) DEFAULT NULL, ADD last_name VARCHAR(100) DEFAULT NULL');
        
        // Migrate existing data - split fullName into firstName and lastName
        $this->addSql("UPDATE doctor SET first_name = SUBSTRING_INDEX(full_name, ' ', 1), last_name = SUBSTRING_INDEX(full_name, ' ', -1)");
        
        // Set NOT NULL constraints
        $this->addSql('ALTER TABLE doctor MODIFY first_name VARCHAR(100) NOT NULL, MODIFY last_name VARCHAR(100) NOT NULL');
        
        // Drop old column
        $this->addSql('ALTER TABLE doctor DROP full_name');
    }

    public function down(Schema $schema): void
    {
        // Add back the fullName column
        $this->addSql('ALTER TABLE doctor ADD full_name VARCHAR(255) NOT NULL');
        
        // Migrate data back
        $this->addSql("UPDATE doctor SET full_name = CONCAT(first_name, ' ', last_name)");
        
        // Drop new columns
        $this->addSql('ALTER TABLE doctor DROP first_name, DROP last_name');
    }
}
