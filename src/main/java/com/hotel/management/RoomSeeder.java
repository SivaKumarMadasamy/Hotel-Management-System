package com.hotel.management;

import com.hotel.management.service.SupabaseService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class RoomSeeder implements CommandLineRunner {

    private final SupabaseService supabase;

    public RoomSeeder(SupabaseService supabase) {
        this.supabase = supabase;
    }

    @Override
    public void run(String... args) {
        try {
            // Check and seed Rooms No. 1 to 20 if not already present
            for (int i = 1; i <= 20; i++) {
                String roomNum = String.valueOf(i);
                List<Map<String, Object>> existing = supabase.select("rooms", Map.of("room_number", roomNum), null, false);
                if (existing == null || existing.isEmpty()) {
                    String type;
                    double price;

                    if (i <= 5) {
                        type = "Single";
                        price = 2500.0;
                    } else if (i <= 10) {
                        type = "Double";
                        price = 4500.0;
                    } else if (i <= 16) {
                        type = "Deluxe";
                        price = 7500.0;
                    } else {
                        type = "Suite";
                        price = 15000.0;
                    }

                    Map<String, Object> room = new HashMap<>();
                    room.put("room_number", roomNum);
                    room.put("room_type", type);
                    room.put("price", price);
                    room.put("status", "Available");

                    supabase.insert("rooms", room);
                    System.out.println("RoomSeeder: Inserted Room " + roomNum + " (" + type + ")");
                }
            }
        } catch (Exception e) {
            System.err.println("RoomSeeder notice: " + e.getMessage());
        }
    }
}
