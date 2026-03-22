package com.udiims.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class SupabaseService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-key}")
    private String serviceKey;

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private String restUrl() {
        return supabaseUrl + "/rest/v1";
    }

    private HttpRequest.Builder base(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("apikey", serviceKey)
                .header("Authorization", "Bearer " + serviceKey)
                .header("Content-Type", "application/json");
    }

    public String getRaw(String table, String query) throws Exception {
        String url = restUrl() + "/" + table + (query != null && !query.isEmpty() ? "?" + query : "");
        HttpRequest req = base(url).GET().build();
        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 400) throw new RuntimeException(res.body());
        return res.body();
    }

    public List<Map<String, Object>> getList(String table, String query) throws Exception {
        String json = getRaw(table, query);
        return mapper.readValue(json, new TypeReference<>() {});
    }

    public Map<String, Object> getSingle(String table, String query) throws Exception {
        String q = (query != null && !query.isEmpty()) ? query + "&limit=1" : "limit=1";
        List<Map<String, Object>> list = getList(table, q);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<Map<String, Object>> post(String table, Map<String, Object> body) throws Exception {
        String url = restUrl() + "/" + table;
        String json = mapper.writeValueAsString(body);
        HttpRequest req = base(url)
                .header("Prefer", "return=representation")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 400) throw new RuntimeException(res.body());
        return mapper.readValue(res.body(), new TypeReference<>() {});
    }

    public List<Map<String, Object>> patch(String table, String filter, Map<String, Object> body) throws Exception {
        String url = restUrl() + "/" + table + "?" + filter;
        String json = mapper.writeValueAsString(body);
        HttpRequest req = base(url)
                .header("Prefer", "return=representation")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 400) throw new RuntimeException(res.body());
        return mapper.readValue(res.body(), new TypeReference<>() {});
    }

    public void delete(String table, String filter) throws Exception {
        String url = restUrl() + "/" + table + "?" + filter;
        HttpRequest req = base(url).DELETE().build();
        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() >= 400) throw new RuntimeException(res.body());
    }

    public ObjectMapper getMapper() {
        return mapper;
    }
}
