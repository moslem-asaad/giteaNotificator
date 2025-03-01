package com.example.catalog;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@TestPropertySource(locations = "classpath:application-test.properties")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebhookControllerApiTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Test
    void testWebhookEndpoint_SuccessfulProcessing() {
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> sender = new HashMap<>();
        sender.put("login", "moslem");
        payload.put("sender", sender);
        Map<String, Object> repository = new HashMap<>();
        repository.put("name", "giteaFinalProject");
        payload.put("repository", repository);
        payload.put("before", "abc123");
        payload.put("after", "def456");
        payload.put("ref", "refs/heads/main");

        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/gitea/webhook")
                .then()
                .statusCode(200)
                .body(equalTo("Webhook received and processed"));
    }

    @Test
    void testWebhookEndpoint_DuplicateRequestIgnored() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("before", "abc123");
        payload.put("after", "def456");
        payload.put("ref", "refs/heads/main");

        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/gitea/webhook")
                .then()
                .statusCode(200)
                .body(equalTo("Webhook received and processed"));

        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/gitea/webhook")
                .then()
                .statusCode(200)
                .body(equalTo("Duplicate webhook ignored"));
    }

    @Test
    void testWebhookEndpoint_CreateRepositoryEvent() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("action", "created");
        payload.put("ref", "refs/heads/main");
        Map<String, Object> sender = new HashMap<>();
        sender.put("login", "moslem");
        payload.put("sender", sender);
        Map<String, Object> repository = new HashMap<>();
        repository.put("name", "giteaFinalProject");
        payload.put("repository", repository);

        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/gitea/webhook")
                .then()
                .statusCode(200)
                .body(equalTo("Webhook received and processed"));
    }

    @Test
    void testWebhookEndpoint_DeleteRepositoryEvent() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("action", "deleted");
        payload.put("ref", "refs/heads/main");
        Map<String, Object> sender = new HashMap<>();
        sender.put("login", "moslem");
        payload.put("sender", sender);
        Map<String, Object> repository = new HashMap<>();
        repository.put("name", "giteaFinalProject");
        payload.put("repository", repository);

        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/gitea/webhook")
                .then()
                .statusCode(200)
                .body(equalTo("Webhook received and processed"));
    }

    @Test
    void testWebhookEndpoint_CreateBranchEvent() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("ref_type", "branch");
        payload.put("ref", "refs/heads/feature-branch");
        Map<String, Object> sender = new HashMap<>();
        sender.put("login", "moslem");
        payload.put("sender", sender);
        Map<String, Object> repository = new HashMap<>();
        repository.put("name", "giteaFinalProject");
        payload.put("repository", repository);

        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/gitea/webhook")
                .then()
                .statusCode(200)
                .body(equalTo("Webhook received and processed"));
    }

    @Test
    void testWebhookEndpoint_DeleteBranchEvent() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("ref_type", "branch");
        payload.put("action", "deleted");
        payload.put("ref", "refs/heads/feature-branch");
        Map<String, Object> sender = new HashMap<>();
        sender.put("login", "moslem");
        payload.put("sender", sender);
        Map<String, Object> repository = new HashMap<>();
        repository.put("name", "giteaFinalProject");
        payload.put("repository", repository);

        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                .post("/gitea/webhook")
                .then()
                .statusCode(200)
                .body(equalTo("Webhook received and processed"));
    }
}