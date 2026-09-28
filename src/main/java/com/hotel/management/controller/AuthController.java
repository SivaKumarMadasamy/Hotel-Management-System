package com.hotel.management.controller;

import com.hotel.management.PasswordUtils;
import com.hotel.management.service.SupabaseService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.*;

@Controller
public class AuthController {
    private final SupabaseService supabase;

    public AuthController(SupabaseService supabase) {
        this.supabase = supabase;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password, 
                        HttpSession session, RedirectAttributes redirect) {
        try {
            email = email.trim().toLowerCase();
            
            // --- Auto-provision Admin User ---
            if ("sivakumar".equals(email)) {
                List<Map<String, Object>> adminCheck = supabase.select("users", Map.of("email", "sivakumar"), null, false);
                if (adminCheck == null || adminCheck.isEmpty()) {
                    Map<String, Object> newAdmin = new HashMap<>();
                    newAdmin.put("username", "Sivakumar");
                    newAdmin.put("email", "sivakumar");
                    newAdmin.put("password", PasswordUtils.hashPassword("984231"));
                    newAdmin.put("role", "admin");
                    supabase.insert("users", newAdmin);
                }
            }
            // ---------------------------------
            
            List<Map<String, Object>> users = supabase.select("users", Map.of("email", email), null, false);
            
            if (users != null && !users.isEmpty()) {
                Map<String, Object> user = users.get(0);
                String storedPassword = String.valueOf(user.get("password"));
                
                if (PasswordUtils.checkPassword(storedPassword, password)) {
                    session.setAttribute("user_id", user.get("id"));
                    session.setAttribute("username", user.get("username"));
                    session.setAttribute("email", user.get("email"));
                    session.setAttribute("role", user.getOrDefault("role", "customer"));
                    redirect.addFlashAttribute("success", "Login successful!");
                    return "redirect:/dashboard";
                }
            }
            redirect.addFlashAttribute("danger", "Invalid email or password!");
        } catch (Exception e) {
            redirect.addFlashAttribute("danger", "Login error: " + e.getMessage());
        }
        return "redirect:/login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username, @RequestParam String email, 
                           @RequestParam String password, RedirectAttributes redirect) {
        try {
            username = username.trim();
            email = email.trim().toLowerCase();
            
            List<Map<String, Object>> emailCheck = supabase.select("users", Map.of("email", email), null, false);
            List<Map<String, Object>> userCheck = supabase.select("users", Map.of("username", username), null, false);
            
            boolean hasEmail = emailCheck != null && !emailCheck.isEmpty();
            boolean hasUser = userCheck != null && !userCheck.isEmpty();
            
            if (hasEmail && hasUser) {
                redirect.addFlashAttribute("danger", "Account already exists!");
                return "redirect:/register";
            } else if (hasEmail) {
                redirect.addFlashAttribute("danger", "Email already exists!");
                return "redirect:/register";
            } else if (hasUser) {
                redirect.addFlashAttribute("danger", "Username already exists!");
                return "redirect:/register";
            }
            
            Map<String, Object> newUser = new HashMap<>();
            newUser.put("username", username);
            newUser.put("email", email);
            newUser.put("password", PasswordUtils.hashPassword(password));
            
            supabase.insert("users", newUser);
            redirect.addFlashAttribute("success", "Registration successful! Please log in.");
            return "redirect:/login";
        } catch (Exception e) {
            redirect.addFlashAttribute("danger", "Registration Error: " + e.getMessage());
            return "redirect:/register";
        }
    }


    @GetMapping("/logout")

    public String logout(HttpSession session, RedirectAttributes redirect) {
        session.invalidate();
        redirect.addFlashAttribute("success", "You have been logged out.");
        return "redirect:/login";
    }
}
