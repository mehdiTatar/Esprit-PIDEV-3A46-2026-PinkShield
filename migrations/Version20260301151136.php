<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Auto-generated Migration: Please modify to your needs!
 */
final class Version20260301151136 extends AbstractMigration
{
    public function getDescription(): string
    {
        return '';
    }

    public function up(Schema $schema): void
    {
        // this up() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE `admin` CHANGE auth_code auth_code VARCHAR(10) DEFAULT NULL, CHANGE roles roles JSON NOT NULL');
        $this->addSql('ALTER TABLE blog_post CHANGE image_path image_path VARCHAR(255) DEFAULT NULL');
        $this->addSql('ALTER TABLE comment ADD parent_comment_id INT DEFAULT NULL');
        $this->addSql('ALTER TABLE comment ADD CONSTRAINT FK_9474526CBF2AF943 FOREIGN KEY (parent_comment_id) REFERENCES comment (id) ON DELETE CASCADE');
        $this->addSql('CREATE INDEX IDX_9474526CBF2AF943 ON comment (parent_comment_id)');
        $this->addSql('ALTER TABLE doctor CHANGE auth_code auth_code VARCHAR(10) DEFAULT NULL, CHANGE roles roles JSON NOT NULL, CHANGE address address VARCHAR(255) DEFAULT NULL, CHANGE phone phone VARCHAR(20) DEFAULT NULL, CHANGE status status VARCHAR(20) DEFAULT NULL');
        $this->addSql('ALTER TABLE notification CHANGE icon icon VARCHAR(255) DEFAULT NULL');
        $this->addSql('ALTER TABLE parapharmacie ADD stock INT NOT NULL');
        $this->addSql('ALTER TABLE user CHANGE auth_code auth_code VARCHAR(10) DEFAULT NULL, CHANGE roles roles JSON NOT NULL, CHANGE full_name full_name VARCHAR(255) DEFAULT NULL, CHANGE first_name first_name VARCHAR(100) DEFAULT NULL, CHANGE last_name last_name VARCHAR(100) DEFAULT NULL, CHANGE address address VARCHAR(255) DEFAULT NULL, CHANGE phone phone VARCHAR(20) DEFAULT NULL, CHANGE status status VARCHAR(20) DEFAULT NULL, CHANGE reset_token reset_token VARCHAR(100) DEFAULT NULL, CHANGE reset_token_expires_at reset_token_expires_at DATETIME DEFAULT NULL COMMENT \'(DC2Type:datetime_immutable)\', CHANGE face_id face_id VARCHAR(255) DEFAULT NULL, CHANGE face_image_path face_image_path VARCHAR(255) DEFAULT NULL');
    }

    public function down(Schema $schema): void
    {
        // this down() migration is auto-generated, please modify it to your needs
        $this->addSql('ALTER TABLE `admin` CHANGE auth_code auth_code VARCHAR(10) DEFAULT \'NULL\', CHANGE roles roles LONGTEXT NOT NULL COLLATE `utf8mb4_bin`');
        $this->addSql('ALTER TABLE blog_post CHANGE image_path image_path VARCHAR(255) DEFAULT \'NULL\'');
        $this->addSql('ALTER TABLE comment DROP FOREIGN KEY FK_9474526CBF2AF943');
        $this->addSql('DROP INDEX IDX_9474526CBF2AF943 ON comment');
        $this->addSql('ALTER TABLE comment DROP parent_comment_id');
        $this->addSql('ALTER TABLE doctor CHANGE auth_code auth_code VARCHAR(10) DEFAULT \'NULL\', CHANGE roles roles LONGTEXT NOT NULL COLLATE `utf8mb4_bin`, CHANGE address address VARCHAR(255) DEFAULT \'NULL\', CHANGE phone phone VARCHAR(20) DEFAULT \'NULL\', CHANGE status status VARCHAR(20) DEFAULT \'NULL\'');
        $this->addSql('ALTER TABLE notification CHANGE icon icon VARCHAR(255) DEFAULT \'NULL\'');
        $this->addSql('ALTER TABLE parapharmacie DROP stock');
        $this->addSql('ALTER TABLE user CHANGE auth_code auth_code VARCHAR(10) DEFAULT \'NULL\', CHANGE roles roles LONGTEXT NOT NULL COLLATE `utf8mb4_bin`, CHANGE full_name full_name VARCHAR(255) DEFAULT \'NULL\', CHANGE first_name first_name VARCHAR(100) DEFAULT \'NULL\', CHANGE last_name last_name VARCHAR(100) DEFAULT \'NULL\', CHANGE address address VARCHAR(255) DEFAULT \'NULL\', CHANGE phone phone VARCHAR(20) DEFAULT \'NULL\', CHANGE status status VARCHAR(20) DEFAULT \'NULL\', CHANGE reset_token reset_token VARCHAR(100) DEFAULT \'NULL\', CHANGE reset_token_expires_at reset_token_expires_at DATETIME DEFAULT \'NULL\' COMMENT \'(DC2Type:datetime_immutable)\', CHANGE face_id face_id VARCHAR(255) DEFAULT \'NULL\', CHANGE face_image_path face_image_path VARCHAR(255) DEFAULT \'NULL\'');
    }
}
