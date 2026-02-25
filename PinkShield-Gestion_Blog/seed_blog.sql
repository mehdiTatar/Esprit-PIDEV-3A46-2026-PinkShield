-- Seed data for BlogPosts
INSERT INTO blog_post (title, content, author_email, author_name, author_role, created_at) VALUES
('Welcome to PinkShield Medical Forum', 'Welcome to our new medical discussion platform. Here you can find the latest news in healthcare, tips for maintaining a healthy lifestyle, and connect with our medical professionals.', 'admin@pinkshield.com', 'Admin', 'ROLE_ADMIN', NOW()),
('Understanding Heart Health', 'Heart disease is a leading cause of health issues worldwide. Regular exercise, a balanced diet, and routine checkups are essential for maintaining cardiovascular health. In this post, we discuss simple steps you can take today.', 'doctor@pinkshield.com', 'Dr. Smith', 'ROLE_DOCTOR', NOW()),
('The Importance of Mental Wellness', 'Mental health is just as important as physical health. Practicing mindfulness, getting enough sleep, and seeking professional help when needed are key components of overall well-being.', 'doctor@pinkshield.com', 'Dr. Smith', 'ROLE_DOCTOR', NOW());

-- Seed data for Comments (assuming the IDs are 1, 2, 3)
INSERT INTO comment (blog_post_id, content, author_email, author_name, created_at) VALUES
(1, 'This is a great initiative! Looking forward to the discussions.', 'patient@pinkshield.com', 'John Patient', NOW()),
(1, 'Glad to have a dedicated space for medical news.', 'doctor@pinkshield.com', 'Dr. Smith', NOW()),
(2, 'Very informative post. What kind of exercises do you recommend for beginners?', 'patient@pinkshield.com', 'John Patient', NOW());
