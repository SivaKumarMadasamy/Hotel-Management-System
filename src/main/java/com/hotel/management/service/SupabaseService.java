package com.hotel.management.service;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.core.ParameterizedTypeReference;
import java.util.*;

@Service
@SuppressWarnings("null")
public class SupabaseService {
    private final String supabaseUrl;
    private final String supabaseKey;
    private final RestTemplate restTemplate;

    public SupabaseService(Dotenv dotenv) {
        this.supabaseUrl = dotenv.get("SUPABASE_URL");
        this.supabaseKey = dotenv.get("SUPABASE_PUBLISHABLE_KEY");
        this.restTemplate = new RestTemplate();
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", supabaseKey);
        headers.set("Authorization", "Bearer " + supabaseKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Prefer", "return=representation");
        return headers;
    }

    public List<Map<String, Object>> select(String table, Map<String, String> equals, String orderBy, boolean desc) {
        if (supabaseUrl == null) return new ArrayList<>();
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(supabaseUrl + "/rest/v1/" + table)
                .queryParam("select", "*");
            
            if (equals != null) {
                for (Map.Entry<String, String> entry : equals.entrySet()) {
                    builder.queryParam(entry.getKey(), "eq." + entry.getValue());
                }
            }
            if (orderBy != null) {
                builder.queryParam("order", orderBy + "." + (desc ? "desc" : "asc"));
            }
            
            HttpEntity<String> entity = new HttpEntity<>(getHeaders());
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                builder.toUriString(), HttpMethod.GET, entity,
                new ParameterizedTypeReference<List<Map<String, Object>>>() {}
            );
            return response.getBody();
        } catch(Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void insert(String table, Map<String, Object> data) throws Exception {
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(data, getHeaders());
        restTemplate.postForEntity(supabaseUrl + "/rest/v1/" + table, entity, String.class);
    }

    public void update(String table, String idCol, Object idVal, Map<String, Object> data) throws Exception {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(supabaseUrl + "/rest/v1/" + table)
            .queryParam(idCol, "eq." + idVal);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(data, getHeaders());
        restTemplate.exchange(builder.toUriString(), HttpMethod.PATCH, entity, String.class);
    }

    public void delete(String table, String idCol, Object idVal) throws Exception {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(supabaseUrl + "/rest/v1/" + table)
            .queryParam(idCol, "eq." + idVal);
        HttpEntity<String> entity = new HttpEntity<>(getHeaders());
        restTemplate.exchange(builder.toUriString(), HttpMethod.DELETE, entity, String.class);
    }
}
