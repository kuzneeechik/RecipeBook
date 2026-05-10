package ru.hits.recipe_book.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class ExternalApiTestSupport {

    protected static final String BASE_URL =
            System.getProperty("api.baseUrl", "http://localhost:8081");

    protected final ObjectMapper objectMapper = new ObjectMapper();
    protected final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final Set<String> createdProductIds = new LinkedHashSet<>();
    private final Set<String> createdDishIds = new LinkedHashSet<>();

    @BeforeAll
    void ensureBackendIsRunning() throws Exception {
        HttpResponse<String> response = get("/actuator/health");
        assertEquals(
                200,
                response.statusCode(),
                "Backend is not available at " + BASE_URL + ". Start the application before running API tests."
        );
    }

    @AfterEach
    void cleanupTrackedEntities() throws Exception {
        for (String dishId : List.copyOf(createdDishIds)) {
            cleanupByDelete("/api/dishes/" + dishId);
        }
        createdDishIds.clear();

        for (String productId : List.copyOf(createdProductIds)) {
            cleanupByDelete("/api/products/" + productId);
        }
        createdProductIds.clear();
    }

    protected HttpResponse<String> get(String path) throws IOException, InterruptedException {
        return send("GET", path, null);
    }

    protected HttpResponse<String> post(String path, String body) throws IOException, InterruptedException {
        HttpResponse<String> response = send("POST", path, body);
        trackCreatedEntity(path, response);
        return response;
    }

    protected HttpResponse<String> put(String path, String body) throws IOException, InterruptedException {
        return send("PUT", path, body);
    }

    protected HttpResponse<String> delete(String path) throws IOException, InterruptedException {
        return send("DELETE", path, null);
    }

    protected JsonNode readBody(HttpResponse<String> response) throws JsonProcessingException {
        return objectMapper.readTree(response.body());
    }

    protected String createProductAndReturnId() throws Exception {
        HttpResponse<String> response = post("/api/products", buildProductRequest(
                "Свёкла",
                List.of("https://example.com/photo-1.jpg"),
                43.0,
                0.1,
                1.0,
                1.5,
                "Единица продукта",
                "VEGETABLES",
                "REQUIRES_COOKING",
                List.of("VEGAN", "GLUTEN_FREE", "SUGAR_FREE")
        ));
        assertEquals(201, response.statusCode());
        return readBody(response).get("id").asText();
    }

    protected void createDishWithProduct(String productId, String dishName) throws Exception {
        List<Map<String, Object>> ingredients = List.of(Map.of(
                "productId", productId,
                "quantityGrams", 100.0
        ));

        HttpResponse<String> response = post("/api/dishes", buildDishRequest(
                dishName,
                List.of("https://example.com/photo-1.jpg"),
                43.0,
                0.1,
                1.0,
                1.5,
                ingredients,
                400.0,
                "SOUP",
                List.of("VEGAN")
        ));
        assertEquals(201, response.statusCode());
    }

    protected String buildProductRequest(
            String name,
            List<String> photos,
            double calories,
            double proteins,
            double fats,
            double carbohydrates,
            String composition,
            String category,
            String cookingRequirement,
            List<String> flags
    ) throws JsonProcessingException {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("photos", photos);
        body.put("calories", calories);
        body.put("proteins", proteins);
        body.put("fats", fats);
        body.put("carbohydrates", carbohydrates);
        body.put("composition", composition);
        body.put("category", category);
        body.put("cookingRequirement", cookingRequirement);
        body.put("flags", flags);
        return objectMapper.writeValueAsString(body);
    }

    protected String buildDishRequest(
            String name,
            List<String> photos,
            double calories,
            double proteins,
            double fats,
            double carbohydrates,
            List<Map<String, Object>> ingredients,
            double servingSizeGrams,
            String category,
            List<String> flags
    ) throws JsonProcessingException {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("photos", photos);
        body.put("calories", calories);
        body.put("proteins", proteins);
        body.put("fats", fats);
        body.put("carbohydrates", carbohydrates);
        body.put("ingredients", ingredients);
        body.put("servingSizeGrams", servingSizeGrams);
        body.put("category", category);
        body.put("flags", flags);
        return objectMapper.writeValueAsString(body);
    }

    protected List<String> buildPhotosList(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(index -> "https://example.com/photo-" + index + ".jpg")
                .toList();
    }

    protected void assertContainsExactly(JsonNode jsonArray, List<String> expectedValues) {
        assertEquals(expectedValues.size(), jsonArray.size());
        for (int i = 0; i < expectedValues.size(); i++) {
            assertEquals(expectedValues.get(i), jsonArray.get(i).asText());
        }
    }

    protected void assertContainsInAnyOrder(JsonNode jsonArray, List<String> expectedValues) {
        assertEquals(expectedValues.size(), jsonArray.size());
        Set<String> actualValues = new LinkedHashSet<>();
        jsonArray.forEach(node -> actualValues.add(node.asText()));
        assertEquals(Set.copyOf(expectedValues), Set.copyOf(actualValues));
    }

    private void trackCreatedEntity(String path, HttpResponse<String> response) throws JsonProcessingException {
        if (response.statusCode() != 201) {
            return;
        }
        JsonNode body = readBody(response);
        JsonNode idNode = body.get("id");
        if (idNode == null || idNode.isNull()) {
            return;
        }

        if ("/api/products".equals(path)) {
            createdProductIds.add(idNode.asText());
        } else if ("/api/dishes".equals(path)) {
            createdDishIds.add(idNode.asText());
        }
    }

    private void cleanupByDelete(String path) throws IOException, InterruptedException {
        HttpResponse<String> response = send("DELETE", path, null);
        int status = response.statusCode();
        if (status != 204 && status != 404) {
            throw new IllegalStateException("Failed to cleanup test data via " + path + ": HTTP " + status);
        }
    }

    private HttpResponse<String> send(String method, String path, String body) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json; charset=UTF-8");

        switch (method) {
            case "GET" -> builder.GET();
            case "DELETE" -> builder.DELETE();
            case "POST" -> builder.POST(HttpRequest.BodyPublishers.ofString(body == null ? "" : body, StandardCharsets.UTF_8));
            case "PUT" -> builder.PUT(HttpRequest.BodyPublishers.ofString(body == null ? "" : body, StandardCharsets.UTF_8));
            default -> throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }

        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }
}
