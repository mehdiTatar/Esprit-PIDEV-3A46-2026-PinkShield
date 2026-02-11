<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

final class Version20260211162000 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Create parapharmacie table and seed sample appointments/items';
    }

    public function up(Schema $schema): void
    {
        $this->addSql('CREATE TABLE parapharmacie (id INT AUTO_INCREMENT NOT NULL, appointment_id INT DEFAULT NULL, name VARCHAR(150) NOT NULL, description LONGTEXT DEFAULT NULL, price NUMERIC(10, 2) NOT NULL, INDEX IDX_PARAPH_APPOINTMENT (appointment_id), PRIMARY KEY(id)) DEFAULT CHARACTER SET utf8mb4 COLLATE `utf8mb4_unicode_ci` ENGINE = InnoDB');
        $this->addSql('ALTER TABLE parapharmacie ADD CONSTRAINT FK_PARAPH_APPOINTMENT FOREIGN KEY (appointment_id) REFERENCES `user` (id)');

        // Insert sample appointments if none exist
        $this->addSql("INSERT INTO `appointment` (patient_email, patient_name, doctor_email, doctor_name, appointment_date, status, notes, created_at) VALUES ('patient1@example.com','John Doe','doctor@pinkshield.com','Dr. Smith', DATE_ADD(NOW(), INTERVAL 3 DAY),'pending','Routine checkup', NOW())");
        $this->addSql("INSERT INTO `appointment` (patient_email, patient_name, doctor_email, doctor_name, appointment_date, status, notes, created_at) VALUES ('patient2@example.com','Jane Roe','doctor@pinkshield.com','Dr. Smith', DATE_ADD(NOW(), INTERVAL 5 DAY),'pending','Follow-up visit', NOW())");

        // Seed parapharmacie linked to the appointments we just created (IDs assumed auto-increment recent)
        $this->addSql("INSERT INTO parapharmacie (appointment_id, name, description, price) VALUES ((SELECT id FROM appointment ORDER BY id DESC LIMIT 1), 'Vitamin C 500mg', 'Immune support supplements', 9.99)");
        $this->addSql("INSERT INTO parapharmacie (appointment_id, name, description, price) VALUES ((SELECT id FROM appointment ORDER BY id DESC LIMIT 1 OFFSET 1), 'Pain Relief Gel', 'Topical analgesic', 12.50)");
    }

    public function down(Schema $schema): void
    {
        $this->addSql('DROP TABLE parapharmacie');
        $this->addSql('DELETE FROM appointment WHERE patient_email IN (\'patient1@example.com\', \'patient2@example.com\')');
    }
}
