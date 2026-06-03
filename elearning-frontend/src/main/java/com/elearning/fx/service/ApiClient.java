package com.elearning.fx.service;

import com.elearning.fx.util.SessionManager;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Client HTTP REST pour communiquer avec le backend Spring Boot.
 */
public class ApiClient {

    private static final String BASE_URL = "http://localhost:8080/api";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>)
                    (src, typeOfSrc, context) -> new JsonPrimitive(src.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>)
                    (json, typeOfT, context) -> {
                        String dateStr = json.getAsString();
                        try {
                            return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        } catch (Exception e) {
                            return LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                        }
                    })
            .create();

    // ==================== AUTH ====================

    public static JsonObject login(String email, String motDePasse) throws IOException, InterruptedException {
        JsonObject body = new JsonObject();
        body.addProperty("email", email);
        body.addProperty("motDePasse", motDePasse);
        return post("/auth/login", body);
    }

    public static JsonObject register(String nom, String prenom, String email,
                                       String motDePasse, String role) throws IOException, InterruptedException {
        JsonObject body = new JsonObject();
        body.addProperty("nom", nom);
        body.addProperty("prenom", prenom);
        body.addProperty("email", email);
        body.addProperty("motDePasse", motDePasse);
        if (role != null) body.addProperty("role", role);
        return post("/auth/register", body);
    }

    // ==================== COURSES ====================

    public static List<JsonObject> getCourses() throws IOException, InterruptedException {
        return getList("/courses");
    }

    public static JsonObject getCourseById(Long id) throws IOException, InterruptedException {
        return get("/courses/" + id);
    }

    public static List<JsonObject> getCoursesByTeacher(Long teacherId) throws IOException, InterruptedException {
        return getList("/courses/teacher/" + teacherId);
    }

    public static List<JsonObject> searchCourses(String query) throws IOException, InterruptedException {
        return getList("/courses/search?q=" + query);
    }

    public static JsonObject createCourse(JsonObject course) throws IOException, InterruptedException {
        return post("/courses", course);
    }

    public static JsonObject updateCourse(Long id, JsonObject course) throws IOException, InterruptedException {
        return put("/courses/" + id, course);
    }

    public static void deleteCourse(Long id) throws IOException, InterruptedException {
        delete("/courses/" + id);
    }

    // ==================== ENROLLMENTS ====================

    public static JsonObject enroll(Long courseId) throws IOException, InterruptedException {
        return post("/enrollments/course/" + courseId, new JsonObject());
    }

    public static void unenroll(Long courseId) throws IOException, InterruptedException {
        delete("/enrollments/course/" + courseId);
    }

    public static List<JsonObject> getMyEnrollments() throws IOException, InterruptedException {
        return getList("/enrollments/my");
    }

    public static List<JsonObject> getEnrollmentsByCourse(Long courseId) throws IOException, InterruptedException {
        return getList("/enrollments/course/" + courseId);
    }

    // ==================== RESOURCES ====================

    public static List<JsonObject> getResourcesByCourse(Long courseId) throws IOException, InterruptedException {
        return getList("/courses/" + courseId + "/resources");
    }

    public static JsonObject uploadResource(Long courseId, Path filePath) throws IOException, InterruptedException {
        String boundary = UUID.randomUUID().toString();
        String fileName = filePath.getFileName().toString();
        byte[] fileBytes = java.nio.file.Files.readAllBytes(filePath);

        // Construire le multipart
        String lineEnd = "\r\n";
        StringBuilder sb = new StringBuilder();
        sb.append("--").append(boundary).append(lineEnd);
        sb.append("Content-Disposition: form-data; name=\"file\"; filename=\"")
                .append(fileName).append("\"").append(lineEnd);
        sb.append("Content-Type: application/octet-stream").append(lineEnd);
        sb.append(lineEnd);

        byte[] header = sb.toString().getBytes();
        byte[] footer = (lineEnd + "--" + boundary + "--" + lineEnd).getBytes();

        byte[] requestBody = new byte[header.length + fileBytes.length + footer.length];
        System.arraycopy(header, 0, requestBody, 0, header.length);
        System.arraycopy(fileBytes, 0, requestBody, header.length, fileBytes.length);
        System.arraycopy(footer, 0, requestBody, header.length + fileBytes.length, footer.length);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/courses/" + courseId + "/resources"))
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(BodyPublishers.ofByteArray(requestBody));

        if (SessionManager.isLoggedIn()) {
            builder.header("Authorization", "Bearer " + SessionManager.getToken());
        }

        HttpResponse<String> response = httpClient.send(builder.build(), BodyHandlers.ofString());
        return gson.fromJson(response.body(), JsonObject.class);
    }

    public static void deleteResource(Long resourceId) throws IOException, InterruptedException {
        delete("/resources/" + resourceId);
    }

    public static byte[] downloadResource(Long resourceId) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/resources/" + resourceId + "/download"))
                .GET();

        if (SessionManager.isLoggedIn()) {
            builder.header("Authorization", "Bearer " + SessionManager.getToken());
        }

        HttpResponse<byte[]> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
        return response.body();
    }

    // ==================== EVENTS ====================

    public static List<JsonObject> getEventsByMonth(int year, int month) throws IOException, InterruptedException {
        return getList("/events/month?year=" + year + "&month=" + month);
    }

    public static List<JsonObject> getEventsByDay(String date) throws IOException, InterruptedException {
        return getList("/events/day?date=" + date);
    }

    public static List<JsonObject> getUpcomingEvents() throws IOException, InterruptedException {
        return getList("/events/upcoming");
    }

    public static JsonObject createEvent(JsonObject event) throws IOException, InterruptedException {
        return post("/events", event);
    }

    public static void deleteEvent(Long id) throws IOException, InterruptedException {
        delete("/events/" + id);
    }

    // ==================== LIVE SESSIONS ====================

    public static List<JsonObject> getUpcomingSessions() throws IOException, InterruptedException {
        return getList("/live-sessions/upcoming");
    }

    public static List<JsonObject> getLiveSessions() throws IOException, InterruptedException {
        return getList("/live-sessions/live");
    }

    public static List<JsonObject> getSessionsByCourse(Long courseId) throws IOException, InterruptedException {
        return getList("/live-sessions/course/" + courseId);
    }

    public static JsonObject createSession(JsonObject session) throws IOException, InterruptedException {
        return post("/live-sessions", session);
    }

    public static JsonObject startSession(Long id) throws IOException, InterruptedException {
        return put("/live-sessions/" + id + "/start", new JsonObject());
    }

    public static JsonObject endSession(Long id) throws IOException, InterruptedException {
        return put("/live-sessions/" + id + "/end", new JsonObject());
    }

    public static void deleteSession(Long id) throws IOException, InterruptedException {
        delete("/live-sessions/" + id);
    }

    // ==================== DASHBOARD ====================

    public static JsonObject getTeacherDashboard() throws IOException, InterruptedException {
        return get("/dashboard/teacher/stats");
    }

    // ==================== AUDIT ====================

    public static JsonObject getAuditLogs(int page, int size) throws IOException, InterruptedException {
        return get("/audit?page=" + page + "&size=" + size);
    }

    // ==================== HELPERS ====================

    private static JsonObject get(String endpoint) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .GET();
        addAuthHeader(builder);

        HttpResponse<String> response = httpClient.send(builder.build(), BodyHandlers.ofString());
        return gson.fromJson(response.body(), JsonObject.class);
    }

    private static List<JsonObject> getList(String endpoint) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .GET();
        addAuthHeader(builder);

        HttpResponse<String> response = httpClient.send(builder.build(), BodyHandlers.ofString());
        Type listType = new TypeToken<List<JsonObject>>() {}.getType();

        // Gérer le cas où la réponse est un objet paginé
        String body = response.body();
        try {
            return gson.fromJson(body, listType);
        } catch (JsonSyntaxException e) {
            // Si c'est un objet paginé, extraire le content
            JsonObject obj = gson.fromJson(body, JsonObject.class);
            if (obj.has("content")) {
                return gson.fromJson(obj.get("content"), listType);
            }
            return List.of();
        }
    }

    private static JsonObject post(String endpoint, JsonObject body) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .POST(BodyPublishers.ofString(gson.toJson(body)));
        addAuthHeader(builder);

        HttpResponse<String> response = httpClient.send(builder.build(), BodyHandlers.ofString());
        return gson.fromJson(response.body(), JsonObject.class);
    }

    private static JsonObject put(String endpoint, JsonObject body) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .header("Content-Type", "application/json")
                .PUT(BodyPublishers.ofString(gson.toJson(body)));
        addAuthHeader(builder);

        HttpResponse<String> response = httpClient.send(builder.build(), BodyHandlers.ofString());
        return gson.fromJson(response.body(), JsonObject.class);
    }

    private static void delete(String endpoint) throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + endpoint))
                .DELETE();
        addAuthHeader(builder);

        httpClient.send(builder.build(), BodyHandlers.ofString());
    }

    private static void addAuthHeader(HttpRequest.Builder builder) {
        if (SessionManager.isLoggedIn()) {
            builder.header("Authorization", "Bearer " + SessionManager.getToken());
        }
    }

    public static Gson getGson() {
        return gson;
    }
}
