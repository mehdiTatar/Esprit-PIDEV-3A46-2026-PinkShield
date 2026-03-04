<?php

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

final class Version20260211185000 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Seed sample daily tracking data for patient user';
    }

    public function up(Schema $schema): void
    {
        $now = new \DateTime();
        $createdAtTime = $now->format('Y-m-d H:i:s');
        
        // Get patient user ID (assuming ID = 1 for patient@pinkshield.com)
        $this->addSql("INSERT INTO daily_tracking (user_id, date, mood, stress, activities, created_at, updated_at) 
            VALUES (1, DATE_SUB(NOW(), INTERVAL 5 DAY), 7, 4, 'Good day, went for a walk and relaxed', '$createdAtTime', '$createdAtTime')");
        
        $this->addSql("INSERT INTO daily_tracking (user_id, date, mood, stress, activities, created_at, updated_at) 
            VALUES (1, DATE_SUB(NOW(), INTERVAL 4 DAY), 6, 5, 'Worked from home, felt productive', '$createdAtTime', '$createdAtTime')");
        
        $this->addSql("INSERT INTO daily_tracking (user_id, date, mood, stress, activities, created_at, updated_at) 
            VALUES (1, DATE_SUB(NOW(), INTERVAL 3 DAY), 8, 3, 'Great day! Exercised and meditated', '$createdAtTime', '$createdAtTime')");
        
        $this->addSql("INSERT INTO daily_tracking (user_id, date, mood, stress, activities, created_at, updated_at) 
            VALUES (1, DATE_SUB(NOW(), INTERVAL 2 DAY), 5, 6, 'Stressful work day but got a massage', '$createdAtTime', '$createdAtTime')");
        
        $this->addSql("INSERT INTO daily_tracking (user_id, date, mood, stress, activities, created_at, updated_at) 
            VALUES (1, DATE_SUB(NOW(), INTERVAL 1 DAY), 7, 4, 'Better today, got good sleep and sun', '$createdAtTime', '$createdAtTime')");
        
        $this->addSql("INSERT INTO daily_tracking (user_id, date, mood, stress, activities, created_at, updated_at) 
            VALUES (1, NOW(), 8, 2, 'Excellent morning, feeling energetic', '$createdAtTime', '$createdAtTime')");
    }

    public function down(Schema $schema): void
    {
        $this->addSql("DELETE FROM daily_tracking WHERE user_id = 1");
    }
}
