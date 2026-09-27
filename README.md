# Hotel Management System - Java Version

This is the fully converted Java version of your Python Flask application. It maintains all of your existing UI, design, features, and connects seamlessly to your existing Supabase database!

## Technology Stack
- **Framework:** Spring Boot (Java 17+)
- **Templating:** Thymeleaf (converted precisely from Jinja2)
- **Database:** Supabase via REST API (mirrors your Python connection approach)
- **CSS/JS:** Preserved identically from your Python app

## How to Run
1. Ensure you have **Java 17+** and **Maven** installed on your system.
2. Copy your `.env` file from the Python project into this directory (`hotel-management-system-java`). Make sure it contains:
   ```env
   SUPABASE_URL=https://<your-project>.supabase.co
   SUPABASE_PUBLISHABLE_KEY=your_key_here
   ```
3. Open a terminal in this directory and run:
   ```bash
   mvn spring-boot:run
   ```
4. The application will start on `http://localhost:8080`.

## Password Compatibility Note
To ensure that you can still log in with the exact same test accounts you made in Python (e.g., `9842` for the manual account), the authentication controller has been built with an automatic fallback mechanism. It securely supports new Java-generated BCrypt passwords, while seamlessly allowing your existing manually created plain-text passwords!
