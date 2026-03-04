<?php

namespace App\Service;

class ChatbotService
{
    // ── Knowledge Base ────────────────────────────────────────────────────────
    private array $knowledgeBase = [

        // ── Greetings ────────────────────────────────────────────────────────
        'greetings' => [
            'keywords' => ['hello', 'hi', 'hey', 'bonjour', 'salut', 'good morning', 'good evening', 'howdy', 'yo', 'greetings'],
            'responses' => [
                "👋 Hello! I'm **PinkShield AI Assistant**, your personal health guide. How can I help you today?",
                "Hi there! 😊 Welcome to PinkShield. I'm here to help you with appointments, health tips, or anything about our platform!",
                "Hey! Great to see you 🩺 I'm your PinkShield AI assistant. Ask me anything about health or our services!",
            ],
        ],

        // ── Platform — Appointments ──────────────────────────────────────────
        'appointments' => [
            'keywords' => ['appointment', 'book', 'booking', 'schedule', 'rendez-vous', 'reserve', 'visit', 'consultation', 'meet doctor'],
            'responses' => [
                "📅 To **book an appointment**, go to the **Appointments** section in your sidebar, click **New Appointment**, choose your doctor and preferred time slot, then confirm. It's that easy!",
                "You can schedule a consultation by navigating to **Appointments → New Appointment**. Choose your specialist, select an available slot, and submit. You'll receive a confirmation notification!",
            ],
        ],

        // ── Platform — Doctors ───────────────────────────────────────────────
        'doctors' => [
            'keywords' => ['doctor', 'physician', 'specialist', 'médecin', 'docteur', 'cardiologist', 'dermatologist', 'find doctor', 'who treats'],
            'responses' => [
                "👨‍⚕️ PinkShield has a team of qualified specialists. You can browse doctors from the **Appointments** booking page. Each doctor profile shows their specialty, availability, and patient ratings.",
                "Our medical team includes specialists in cardiology, dermatology, general medicine, and more. Head to **New Appointment** to see available doctors and their profiles.",
            ],
        ],

        // ── Platform — Profile ───────────────────────────────────────────────
        'profile' => [
            'keywords' => ['profile', 'account', 'settings', 'update info', 'change email', 'change password', 'photo', 'edit profile', 'my info'],
            'responses' => [
                "👤 To update your profile, click your **avatar/name** in the top-right corner and select **My Profile**. You can update your photo, personal info, and change your password there.",
                "Your account settings are accessible via the **Profile** link in the navigation dropdown. Update your personal details, profile picture, or password anytime.",
            ],
        ],

        // ── Platform — Blog ──────────────────────────────────────────────────
        'blog' => [
            'keywords' => ['blog', 'article', 'news', 'health tips', 'read', 'post', 'health news'],
            'responses' => [
                "📰 Our **Health Blog** is packed with articles on wellness, nutrition, and medical news. Access it via the **Blog** link in the navigation bar. New articles are posted regularly by our medical team!",
                "You can find health articles and expert tips in the **Blog** section. It's a great resource for staying informed about your health!",
            ],
        ],

        // ── Platform — Notifications ─────────────────────────────────────────
        'notifications' => [
            'keywords' => ['notification', 'alert', 'reminder', 'message', 'bell', 'updates'],
            'responses' => [
                "🔔 Your **notifications** appear in the bell icon at the top of the page. They include appointment reminders, system updates, and messages from doctors. Click the bell to view all.",
            ],
        ],

        // ── Health — General ─────────────────────────────────────────────────
        'health_general' => [
            'keywords' => ['health', 'healthy', 'wellness', 'wellbeing', 'lifestyle', 'fit', 'fitness', 'exercise', 'sport', 'active'],
            'responses' => [
                "💪 Great question about health! Key pillars of good health are:\n\n• **Exercise**: 30 min/day of moderate activity\n• **Sleep**: 7-9 hours per night\n• **Hydration**: 8 glasses of water daily\n• **Nutrition**: Balanced meals with fruits & veggies\n• **Stress**: Regular meditation or relaxation\n\nWant tips on a specific area?",
                "🌿 Maintaining wellness involves consistent habits. Regular check-ups with your doctor are also crucial — would you like to **book an appointment** to discuss your health with one of our specialists?",
            ],
        ],

        // ── Health — Nutrition ───────────────────────────────────────────────
        'nutrition' => [
            'keywords' => ['nutrition', 'diet', 'food', 'eat', 'eating', 'meal', 'calories', 'weight', 'vegetable', 'fruit', 'protein', 'vitamin'],
            'responses' => [
                "🥗 Good nutrition is the foundation of health! Here are key principles:\n\n• Eat **colorful vegetables** at every meal\n• Choose **lean proteins**: chicken, fish, legumes\n• Limit processed foods and added sugars\n• Include **healthy fats**: avocado, nuts, olive oil\n• Stay hydrated with water throughout the day\n\n_For personalized dietary advice, consult one of our nutritionist specialists._",
                "🍎 A balanced diet should include:\n\n• **50%** vegetables & fruits\n• **25%** whole grains\n• **25%** lean protein\n• Plus healthy fats in moderation\n\nNeed more personalized guidance? Book a consultation with our dietitian!",
            ],
        ],

        // ── Health — Sleep ───────────────────────────────────────────────────
        'sleep' => [
            'keywords' => ['sleep', 'insomnia', 'tired', 'fatigue', 'rest', 'nap', 'bedtime', 'wake up', 'sleep quality'],
            'responses' => [
                "😴 Quality sleep is essential for recovery and health. Tips for better sleep:\n\n• **Consistent schedule**: Sleep and wake at the same time\n• **Dark & cool room**: Ideal bedroom is ~18°C\n• **No screens** 1 hour before bed\n• **Limit caffeine** after 2pm\n• **Relaxation routine**: Reading, light stretching, or meditation\n\nIf insomnia persists, speak with one of our doctors.",
            ],
        ],

        // ── Health — Stress ──────────────────────────────────────────────────
        'stress' => [
            'keywords' => ['stress', 'anxiety', 'anxious', 'worried', 'overwhelmed', 'burnout', 'mental health', 'depressed', 'depression', 'panic', 'nervous'],
            'responses' => [
                "🧘 It's important to take care of your mental health. Here are some strategies:\n\n• **Breathing exercises**: 4-7-8 breathing technique\n• **Physical activity**: Even a short walk helps\n• **Journaling**: Write down thoughts and feelings\n• **Connect socially**: Talk to someone you trust\n• **Limit news/social media** if overwhelming\n\n_If you're experiencing persistent anxiety or depression, please don't hesitate to book an appointment with a mental health specialist on PinkShield._",
            ],
        ],

        // ── Health — Heart ───────────────────────────────────────────────────
        'heart' => [
            'keywords' => ['heart', 'cardiac', 'cardiology', 'blood pressure', 'hypertension', 'cholesterol', 'chest pain', 'palpitation', 'cardiovascular'],
            'responses' => [
                "❤️ Heart health is critical. Key indicators to monitor:\n\n• **Blood pressure**: Should be below 120/80 mmHg\n• **Cholesterol**: Check levels annually\n• **Heart rate**: Normal resting is 60-100 bpm\n• **Weight**: Maintain a healthy BMI\n\n⚠️ If you experience **chest pain, shortness of breath, or palpitations**, seek immediate medical attention.\n\n_For a proactive heart check-up, book a cardiology consultation on PinkShield._",
            ],
        ],

        // ── Health — Diabetes ────────────────────────────────────────────────
        'diabetes' => [
            'keywords' => ['diabetes', 'diabetic', 'blood sugar', 'glucose', 'insulin', 'glycemia', 'a1c'],
            'responses' => [
                "🩸 Diabetes management involves:\n\n• **Monitoring blood glucose** regularly\n• Following a **low-glycemic diet** (whole grains, vegetables, lean protein)\n• **Regular exercise** to improve insulin sensitivity\n• Taking prescribed medication consistently\n• **Annual eye, kidney, and foot exams**\n\n_Our endocrinology specialists can provide comprehensive diabetes care. Book your consultation today!_",
            ],
        ],

        // ── Health — Headache/Pain ───────────────────────────────────────────
        'pain' => [
            'keywords' => ['headache', 'migraine', 'pain', 'ache', 'back pain', 'joint pain', 'fever', 'temperature', 'nausea', 'vomiting', 'dizzy', 'dizziness'],
            'responses' => [
                "🤕 For common headaches:\n• Rest in a dark, quiet room\n• Stay hydrated\n• Apply a cold/warm compress\n• Over-the-counter pain relief if needed\n\n⚠️ **See a doctor if**: Pain is sudden and severe, accompanied by fever, stiff neck, confusion, or vision changes.\n\n_Book an appointment on PinkShield for a proper diagnosis._",
                "💊 Pain can have many causes. General tips: rest, ice/heat therapy, and hydration. However, persistent or severe pain should always be evaluated by a doctor. Would you like to **book a consultation**?",
            ],
        ],

        // ── Platform — Password/Login ────────────────────────────────────────
        'auth' => [
            'keywords' => ['password', 'forgot password', 'login', 'sign in', 'log in', 'reset password', 'cant login', 'access', 'locked out'],
            'responses' => [
                "🔑 **Forgot your password?** On the login page, click **\"Forgot password?\"** and enter your email. We'll send you a secure reset link.\n\nIf you're having other login issues, contact the admin through the system.",
            ],
        ],

        // ── Platform — Registration ──────────────────────────────────────────
        'register' => [
            'keywords' => ['register', 'sign up', 'create account', 'new account', 'inscription', 'join'],
            'responses' => [
                "📝 To create a PinkShield account, click **Register** on the login page. You can sign up as:\n\n• **Patient** – to book appointments and track your health\n• **Doctor** – to manage your practice and patients\n\nRegistration is quick and free!",
            ],
        ],

        // ── What is PinkShield ───────────────────────────────────────────────
        'about' => [
            'keywords' => ['what is pinkshield', 'about', 'platform', 'pinkshield', 'who are you', 'what can you do', 'features', 'services'],
            'responses' => [
                "🏥 **PinkShield** is a modern medical management platform that connects patients with healthcare professionals. Features include:\n\n• 📅 **Online appointment booking**\n• 👨‍⚕️ **Doctor profiles & specialty search**\n• 📊 **Personal health tracking**\n• 📰 **Health blog & wellness tips**\n• 🔔 **Smart notifications & reminders**\n• 🔒 **Secure medical records**\n\nHow can I assist you today?",
            ],
        ],

        // ── Help ─────────────────────────────────────────────────────────────
        'help' => [
            'keywords' => ['help', 'what can you do', 'commands', 'topics', 'questions', 'assist', 'support', 'guide'],
            'responses' => [
                "🤖 I'm PinkShield AI Assistant! Here's what I can help with:\n\n**Platform**\n• 📅 Booking & managing appointments\n• 👤 Account & profile settings\n• 🔑 Password reset\n• 📰 Blog & health articles\n\n**Health Topics**\n• 🥗 Nutrition & diet\n• 😴 Sleep quality\n• ❤️ Heart & cardiovascular health\n• 🧘 Mental health & stress\n• 🩸 Diabetes management\n• 💊 Pain & symptoms\n\nJust type your question naturally and I'll do my best to help!",
            ],
        ],

        // ── Thanks ───────────────────────────────────────────────────────────
        'thanks' => [
            'keywords' => ['thank', 'thanks', 'thank you', 'merci', 'thx', 'ty', 'appreciate', 'great', 'perfect', 'awesome', 'wonderful'],
            'responses' => [
                "You're very welcome! 😊 Is there anything else I can help you with?",
                "Happy to help! 🩺 Feel free to ask me anything else about your health or the PinkShield platform.",
                "Glad I could assist! Take care of your health 💪 Let me know if you have more questions.",
            ],
        ],

        // ── Goodbye ──────────────────────────────────────────────────────────
        'goodbye' => [
            'keywords' => ['bye', 'goodbye', 'see you', 'later', 'ciao', 'au revoir', 'take care', 'exit'],
            'responses' => [
                "Goodbye! 👋 Take care of yourself and stay healthy. PinkShield is always here when you need us!",
                "See you later! 🌸 Remember: your health is your greatest wealth. Stay well!",
            ],
        ],
    ];

    // ── Fallback responses ────────────────────────────────────────────────────
    private array $fallbacks = [
        "🤔 I'm not sure I fully understand that. Could you rephrase? You can ask me about:\n\n• **Appointments** – booking, scheduling\n• **Health topics** – nutrition, sleep, stress, heart health\n• **Platform features** – profile, notifications, blog\n• Or type **'help'** for a full list!",
        "💭 That's an interesting question! I'm still learning. For specific medical concerns, I strongly recommend booking an appointment with one of our qualified doctors on PinkShield.\n\nIs there something else I can help you with?",
        "🩺 I didn't quite catch that. I'm best at answering questions about PinkShield features or general health topics. Type **'help'** to see what I can assist with!",
    ];

    /**
     * Process user message and return an AI response.
     */
    public function getResponse(string $userMessage): string
    {
        $message = mb_strtolower(trim($userMessage));

        // Remove punctuation for better matching
        $cleaned = preg_replace('/[^\w\s]/u', ' ', $message);
        $cleaned = preg_replace('/\s+/', ' ', $cleaned);

        $bestMatch     = null;
        $bestScore     = 0;

        foreach ($this->knowledgeBase as $category => $data) {
            $score = $this->calculateScore($cleaned, $data['keywords']);
            if ($score > $bestScore) {
                $bestScore  = $score;
                $bestMatch  = $data;
            }
        }

        // Threshold: at least 1 keyword must match
        if ($bestMatch !== null && $bestScore >= 1) {
            $responses = $bestMatch['responses'];
            return $responses[array_rand($responses)];
        }

        // Fallback
        return $this->fallbacks[array_rand($this->fallbacks)];
    }

    /**
     * Score a message against a list of keywords using partial and exact matching.
     */
    private function calculateScore(string $message, array $keywords): int
    {
        $score = 0;
        foreach ($keywords as $keyword) {
            if (str_contains($message, $keyword)) {
                // Exact keyword match scores more
                $score += (str_word_count($keyword) > 1) ? 3 : 1;
            }
        }
        return $score;
    }
}
