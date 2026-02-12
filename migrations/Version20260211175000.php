<?php

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

final class Version20260211175000 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Seed sample appointments data';
    }

    public function up(Schema $schema): void
    {
        $now = new \DateTime();
        $createdAt = $now->format('Y-m-d H:i:s');
        
        $appointmentDate1 = (new \DateTime('+5 days'))->format('Y-m-d H:i:s');
        $appointmentDate2 = (new \DateTime('+7 days'))->format('Y-m-d H:i:s');
        $appointmentDate3 = (new \DateTime('+10 days'))->format('Y-m-d H:i:s');

        $this->addSql("INSERT INTO appointment (patient_email, patient_name, doctor_email, doctor_name, appointment_date, status, notes, created_at) 
            VALUES ('patient@pinkshield.com', 'John Doe', 'doctor@pinkshield.com', 'Dr. Smith', '$appointmentDate1', 'confirmed', 'Regular checkup', '$createdAt')");
        
        $this->addSql("INSERT INTO appointment (patient_email, patient_name, doctor_email, doctor_name, appointment_date, status, notes, created_at) 
            VALUES ('patient@pinkshield.com', 'John Doe', 'doctor@pinkshield.com', 'Dr. Smith', '$appointmentDate2', 'pending', 'Follow-up consultation', '$createdAt')");
        
        $this->addSql("INSERT INTO appointment (patient_email, patient_name, doctor_email, doctor_name, appointment_date, status, notes, created_at) 
            VALUES ('patient@pinkshield.com', 'John Doe', 'doctor@pinkshield.com', 'Dr. Smith', '$appointmentDate3', 'confirmed', 'Lab results discussion', '$createdAt')");
    }

    public function down(Schema $schema): void
    {
        $this->addSql("DELETE FROM appointment WHERE doctor_email = 'doctor@pinkshield.com'");
    }
}
