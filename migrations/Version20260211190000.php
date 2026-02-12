<?php

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

final class Version20260211190000 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Add additional health tracking fields to daily_tracking table';
    }

    public function up(Schema $schema): void
    {
        $this->addSql('ALTER TABLE daily_tracking ADD anxiety_level INT NULL');
        $this->addSql('ALTER TABLE daily_tracking ADD focus_level INT NULL');
        $this->addSql('ALTER TABLE daily_tracking ADD motivation_level INT NULL');
        $this->addSql('ALTER TABLE daily_tracking ADD social_interaction_level INT NULL');
        $this->addSql('ALTER TABLE daily_tracking ADD sleep_hours INT NULL');
        $this->addSql('ALTER TABLE daily_tracking ADD energy_level INT NULL');
        $this->addSql('ALTER TABLE daily_tracking ADD symptoms LONGTEXT NULL');
        $this->addSql('ALTER TABLE daily_tracking ADD medication_taken TINYINT(1) NULL');
        $this->addSql('ALTER TABLE daily_tracking ADD appetite_level INT NULL');
        $this->addSql('ALTER TABLE daily_tracking ADD water_intake INT NULL');
        $this->addSql('ALTER TABLE daily_tracking ADD physical_activity_level INT NULL');
    }

    public function down(Schema $schema): void
    {
        $this->addSql('ALTER TABLE daily_tracking DROP anxiety_level');
        $this->addSql('ALTER TABLE daily_tracking DROP focus_level');
        $this->addSql('ALTER TABLE daily_tracking DROP motivation_level');
        $this->addSql('ALTER TABLE daily_tracking DROP social_interaction_level');
        $this->addSql('ALTER TABLE daily_tracking DROP sleep_hours');
        $this->addSql('ALTER TABLE daily_tracking DROP energy_level');
        $this->addSql('ALTER TABLE daily_tracking DROP symptoms');
        $this->addSql('ALTER TABLE daily_tracking DROP medication_taken');
        $this->addSql('ALTER TABLE daily_tracking DROP appetite_level');
        $this->addSql('ALTER TABLE daily_tracking DROP water_intake');
        $this->addSql('ALTER TABLE daily_tracking DROP physical_activity_level');
    }
}
