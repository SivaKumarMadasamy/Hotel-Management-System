package com.hotel.management.controller;

import com.hotel.management.service.SupabaseService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Controller
public class MainController {
    private final SupabaseService supabase;

    public MainController(SupabaseService supabase) {
        this.supabase = supabase;
    }

    private boolean isLoggedIn(HttpSession session) {
        return session.getAttribute("user_id") != null;
    }

    @GetMapping("/")
    public String index(HttpSession session) {
        return "index";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model, RedirectAttributes redirect) {
        if (!isLoggedIn(session)) return "redirect:/login";
        if (!"admin".equals(session.getAttribute("role"))) return "redirect:/";
        
        try {
            List<Map<String, Object>> rooms = supabase.select("rooms", null, null, false);
            int availableRooms = 0;
            int bookedRooms = 0;
            for (Map<String, Object> room : rooms) {
                if ("Available".equals(room.get("status"))) availableRooms++;
                else bookedRooms++;
            }
            
            List<Map<String, Object>> bookings = supabase.select("bookings", null, null, false);
            List<Map<String, Object>> complaints = supabase.select("complaints", Map.of("status", "Pending"), null, false);
            
            model.addAttribute("total_rooms", rooms != null ? rooms.size() : 0);
            model.addAttribute("available_rooms", availableRooms);
            model.addAttribute("booked_rooms", bookedRooms);
            model.addAttribute("total_bookings", bookings.size());
            model.addAttribute("pending_complaints", complaints.size());
            return "dashboard";
        } catch (Exception e) {
            redirect.addFlashAttribute("danger", "Error fetching dashboard data: " + e.getMessage());
            return "redirect:/";
        }
    }

    // --- Room Management ---
    @GetMapping("/rooms")
    public String rooms(HttpSession session, Model model) {
        if (!isLoggedIn(session)) return "redirect:/login";
        model.addAttribute("rooms", supabase.select("rooms", null, "room_number", false));
        return "rooms";
    }

    @GetMapping("/add_room")
    public String addRoomPage(HttpSession session) {
        if (!isLoggedIn(session)) return "redirect:/login";
        return "add_room";
    }

    @PostMapping("/add_room")
    public String addRoom(@RequestParam String room_number, @RequestParam String room_type, 
                          @RequestParam double price, HttpSession session, RedirectAttributes redirect) {
        if (!isLoggedIn(session)) return "redirect:/login";
        try {
            List<Map<String, Object>> existing = supabase.select("rooms", Map.of("room_number", room_number), null, false);
            if (existing != null && !existing.isEmpty()) {
                redirect.addFlashAttribute("danger", "Room number already exists!");
                return "redirect:/add_room";
            }
            Map<String, Object> newRoom = new HashMap<>();
            newRoom.put("room_number", room_number);
            newRoom.put("room_type", room_type);
            newRoom.put("price", price);
            newRoom.put("status", "Available");
            supabase.insert("rooms", newRoom);
            redirect.addFlashAttribute("success", "Room added successfully!");
            return "redirect:/rooms";
        } catch (Exception e) {
            redirect.addFlashAttribute("danger", "Error: " + e.getMessage());
            return "redirect:/add_room";
        }
    }

    @GetMapping("/edit_room/{id}")
    public String editRoomPage(@PathVariable int id, HttpSession session, Model model, RedirectAttributes redirect) {
        if (!isLoggedIn(session)) return "redirect:/login";
        List<Map<String, Object>> rooms = supabase.select("rooms", Map.of("id", String.valueOf(id)), null, false);
        if (rooms == null || rooms.isEmpty()) {
            redirect.addFlashAttribute("danger", "Room not found!");
            return "redirect:/rooms";
        }
        model.addAttribute("room", rooms.get(0));
        return "edit_room";
    }

    @PostMapping("/edit_room/{id}")
    public String editRoom(@PathVariable int id, @RequestParam String room_number, @RequestParam String room_type, 
                           @RequestParam double price, @RequestParam String status, 
                           HttpSession session, RedirectAttributes redirect) {
        if (!isLoggedIn(session)) return "redirect:/login";
        try {
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("room_number", room_number);
            updateData.put("room_type", room_type);
            updateData.put("price", price);
            updateData.put("status", status);
            supabase.update("rooms", "id", id, updateData);
            redirect.addFlashAttribute("success", "Room updated successfully!");
        } catch (Exception e) {
            redirect.addFlashAttribute("danger", "Error: " + e.getMessage());
        }
        return "redirect:/rooms";
    }

    @PostMapping("/delete_room/{id}")
    public String deleteRoom(@PathVariable int id, HttpSession session, RedirectAttributes redirect) {
        if (!isLoggedIn(session)) return "redirect:/login";
        try {
            Map<String, String> query = new HashMap<>();
            query.put("room_id", String.valueOf(id));
            query.put("status", "Active");
            List<Map<String, Object>> activeBookings = supabase.select("bookings", query, null, false);
            
            if (activeBookings != null && !activeBookings.isEmpty()) {
                redirect.addFlashAttribute("danger", "Cannot delete room with active bookings!");
            } else {
                supabase.delete("rooms", "id", id);
                redirect.addFlashAttribute("success", "Room deleted successfully!");
            }
        } catch (Exception e) {
            redirect.addFlashAttribute("danger", "Error: " + e.getMessage());
        }
        return "redirect:/rooms";
    }

    // --- Booking Management ---
    @GetMapping("/bookings")
    public String bookings(HttpSession session, Model model) {
        if (!isLoggedIn(session)) return "redirect:/login";
        // NOTE: In Java without ORM, fetching joined data manually or relying on DB view.
        // PostgREST supports select=*,rooms(*).
        // Since we didn't implement complex parsing, we fetch bookings and rooms separately and join.
        List<Map<String, Object>> bookings = supabase.select("bookings?select=*,rooms(*)", null, "check_in", true);
        
        List<Map<String, Object>> filtered = new ArrayList<>();
        String role = (String) session.getAttribute("role");
        String email = (String) session.getAttribute("email");
        
        if (bookings != null) {
            for (Map<String, Object> b : bookings) {
                if ("admin".equals(role) || email.equals(b.get("email"))) {
                    filtered.add(b);
                }
            }
        }
        model.addAttribute("bookings", filtered);
        return "bookings";
    }

    @GetMapping("/book_room")
    public String bookRoomPage(HttpSession session, Model model) {
        if (!isLoggedIn(session)) return "redirect:/login";
        List<Map<String, Object>> availableRooms = supabase.select("rooms", Map.of("status", "Available"), null, false);
        model.addAttribute("available_rooms", availableRooms);
        
        Map<String, Object> userInfo = new HashMap<>();
        List<Map<String, Object>> users = supabase.select("users", Map.of("id", String.valueOf(session.getAttribute("user_id"))), null, false);
        if (users != null && !users.isEmpty()) {
            userInfo = users.get(0);
        }
        model.addAttribute("user_info", userInfo);
        return "book_room";
    }

    @PostMapping("/book_room")
    public String bookRoom(@RequestParam int room_id, @RequestParam String customer_name,
                           @RequestParam String phone, @RequestParam String email,
                           @RequestParam String check_in, @RequestParam String check_out,
                           HttpSession session, RedirectAttributes redirect) {
        if (!isLoggedIn(session)) return "redirect:/login";
        try {
            LocalDate inDate = LocalDate.parse(check_in);
            LocalDate outDate = LocalDate.parse(check_out);
            
            if (!outDate.isAfter(inDate)) {
                redirect.addFlashAttribute("danger", "Check-out date must be after check-in date!");
                return "redirect:/book_room";
            }
            
            long days = ChronoUnit.DAYS.between(inDate, outDate);
            
            List<Map<String, Object>> rooms = supabase.select("rooms", Map.of("id", String.valueOf(room_id)), null, false);
            if (rooms == null || rooms.isEmpty()) {
                redirect.addFlashAttribute("danger", "Invalid room.");
                return "redirect:/book_room";
            }
            
            double price = Double.parseDouble(rooms.get(0).get("price").toString());
            double totalPrice = days * price;
            
            Map<String, Object> booking = new HashMap<>();
            booking.put("room_id", room_id);
            booking.put("customer_name", customer_name);
            booking.put("phone", phone);
            booking.put("email", email);
            booking.put("check_in", check_in);
            booking.put("check_out", check_out);
            booking.put("total_price", totalPrice);
            booking.put("status", "Active");
            
            supabase.insert("bookings", booking);
            supabase.update("rooms", "id", room_id, Map.of("status", "Booked"));
            
            redirect.addFlashAttribute("success", String.format("Room booked successfully! Total price: Rs.%.2f", totalPrice));
            return "redirect:/dashboard";
        } catch (Exception e) {
            redirect.addFlashAttribute("danger", "Error booking room: " + e.getMessage());
            return "redirect:/book_room";
        }
    }

    @PostMapping("/cancel_booking/{id}")
    public String cancelBooking(@PathVariable int id, HttpSession session, RedirectAttributes redirect) {
        if (!isLoggedIn(session)) return "redirect:/login";
        try {
            List<Map<String, Object>> bookings = supabase.select("bookings", Map.of("id", String.valueOf(id)), null, false);
            if (bookings != null && !bookings.isEmpty()) {
                Map<String, Object> booking = bookings.get(0);
                if ("Active".equals(booking.get("status"))) {
                    supabase.update("bookings", "id", id, Map.of("status", "Cancelled"));
                    supabase.update("rooms", "id", booking.get("room_id"), Map.of("status", "Available"));
                    redirect.addFlashAttribute("success", "Booking cancelled successfully!");
                }
            }
        } catch (Exception e) {
            redirect.addFlashAttribute("danger", "Error: " + e.getMessage());
        }
        return "redirect:/bookings";
    }

    // --- Complaint Management ---
    @GetMapping("/complaints")
    public String complaints(HttpSession session, Model model) {
        if (!isLoggedIn(session)) return "redirect:/login";
        if (!"admin".equals(session.getAttribute("role"))) return "redirect:/";
        model.addAttribute("complaints", supabase.select("complaints", null, "created_at", true));
        return "complaints";
    }

    @GetMapping("/add_complaint")
    public String addComplaintPage(HttpSession session) {
        if (!isLoggedIn(session)) return "redirect:/login";
        return "add_complaint";
    }

    @PostMapping("/add_complaint")
    public String addComplaint(@RequestParam String customer_name, @RequestParam String room_number,
                               @RequestParam String complaint, HttpSession session, RedirectAttributes redirect) {
        if (!isLoggedIn(session)) return "redirect:/login";
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("customer_name", customer_name);
            data.put("room_number", room_number);
            data.put("complaint", complaint);
            data.put("status", "Pending");
            supabase.insert("complaints", data);
            redirect.addFlashAttribute("success", "Complaint submitted successfully! We will look into it.");
            return "redirect:/rooms";
        } catch (Exception e) {
            redirect.addFlashAttribute("danger", "Error submitting complaint: " + e.getMessage());
            return "redirect:/add_complaint";
        }
    }

    @PostMapping("/update_complaint/{id}")
    public String updateComplaint(@PathVariable int id, @RequestParam String status, 
                                  HttpSession session, RedirectAttributes redirect) {
        if (!isLoggedIn(session)) return "redirect:/login";
        if (!"admin".equals(session.getAttribute("role"))) return "redirect:/";
        try {
            supabase.update("complaints", "id", id, Map.of("status", status));
            redirect.addFlashAttribute("success", "Complaint status updated!");
        } catch (Exception e) {
            redirect.addFlashAttribute("danger", "Error: " + e.getMessage());
        }
        return "redirect:/complaints";
    }
}
