package com.foodapp.food4ufrontend.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

public class ApiClient {
    private static final String BASE_URL = "http://localhost:8080";
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public static Optional<HttpResponse<String>> post(String path, String jsonBody, String token) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody));

        if (token != null && !token.isEmpty()) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }

        HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        return Optional.of(response);
    }

    public static Optional<HttpResponse<String>> get(String path, String token) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .GET();

        if (token != null && !token.isEmpty()) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }

        HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        return Optional.of(response);
    }

    public static Optional<HttpResponse<String>> put(String path, String jsonBody, String token) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody));

        if (token != null && !token.isEmpty()) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }

        HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        return Optional.of(response);
    }

    public static Optional<HttpResponse<String>> patch(String path, String jsonBody, String token) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(jsonBody));

        if (token != null && !token.isEmpty()) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }

        HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        return Optional.of(response);
    }

    public static Optional<HttpResponse<String>> delete(String path, String token) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .DELETE();

        if (token != null && !token.isEmpty()) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }

        HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        return Optional.of(response);
    }

    public static Optional<JsonNode> parseJson(String jsonString) {
        try {
            return Optional.of(objectMapper.readTree(jsonString));
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public static String toJson(Object obj) throws com.fasterxml.jackson.core.JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }
}