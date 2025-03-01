package com.example.catalog;

import com.example.catalog.controller.WebhookController;
import com.example.catalog.services.DiscordNotifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WebhookControllerTest {

    private WebhookController webhookController;
    private DiscordNotifier discordNotifier;

    @BeforeEach
    void setUp() {
        discordNotifier = mock(DiscordNotifier.class);
        webhookController = new WebhookController(discordNotifier);
    }

    @Test
    void testHandleWebhook_PushEvent() {
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> sender = new HashMap<>();
        sender.put("login", "moslem");
        payload.put("sender", sender);
        Map<String, Object> repository = new HashMap<>();
        repository.put("name", "giteaFinalProject");
        payload.put("repository", repository);
        payload.put("before", "abc123");
        payload.put("after", "def456");

        ResponseEntity<String> response = webhookController.handleWebhook(payload);

        assertEquals("Webhook received and processed", response.getBody());
        verify(discordNotifier, times(1)).sendNotification(contains("Push Event"), eq(true), eq(true));
    }

    @Test
    void testHandleWebhook_DuplicateEventIgnored() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("before", "abc123");
        payload.put("after", "def456");

        webhookController.handleWebhook(payload);
        ResponseEntity<String> response = webhookController.handleWebhook(payload);

        assertEquals("Duplicate webhook ignored", response.getBody());
        verify(discordNotifier, times(1)).sendNotification(anyString(), anyBoolean(), anyBoolean());
    }


    @Test
    void testHandleWebhook_CreateBranchEvent() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("ref_type", "branch");
        //payload.put("pusher_type", "user");
        Map<String, Object> sender = new HashMap<>();
        sender.put("login", "moslem");
        payload.put("sender", sender);
        Map<String, Object> repository = new HashMap<>();
        repository.put("name", "giteaFinalProject");
        payload.put("repository", repository);

        ResponseEntity<String> response = webhookController.handleWebhook(payload);

        assertEquals("Webhook received and processed", response.getBody());
        verify(discordNotifier, times(1)).sendNotification(contains("New Branch Created"), eq(true), eq(true));
    }

    @Test
    void testHandleWebhook_DeleteBranchEvent() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("ref_type", "branch");
        payload.put("pusher_type", "user");
        Map<String, Object> sender = new HashMap<>();
        sender.put("login", "moslem");
        payload.put("sender", sender);
        Map<String, Object> repository = new HashMap<>();
        repository.put("name", "giteaFinalProject");
        payload.put("repository", repository);
        payload.put("action", "deleted");

        ResponseEntity<String> response = webhookController.handleWebhook(payload);

        assertEquals("Webhook received and processed", response.getBody());
        verify(discordNotifier, times(1)).sendNotification(contains("Branch Deleted"), eq(true), eq(true));
    }

    @Test
    void testHandleWebhook_CreateRepositoryEvent() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("action", "created");
        Map<String, Object> sender = new HashMap<>();
        sender.put("login", "moslem");
        payload.put("sender", sender);
        Map<String, Object> repository = new HashMap<>();
        repository.put("name", "testRepo");
        payload.put("repository", repository);

        ResponseEntity<String> response = webhookController.handleWebhook(payload);

        assertEquals("Webhook received and processed", response.getBody());
        verify(discordNotifier, times(1)).sendNotification(contains("New Repository Created"), eq(false), eq(true));
    }

    @Test
    void testHandleWebhook_DeleteRepositoryEvent() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("action", "deleted");
        Map<String, Object> sender = new HashMap<>();
        sender.put("login", "moslem");
        payload.put("sender", sender);
        Map<String, Object> repository = new HashMap<>();
        repository.put("name", "testRepo");
        payload.put("repository", repository);

        ResponseEntity<String> response = webhookController.handleWebhook(payload);

        assertEquals("Webhook received and processed", response.getBody());
        verify(discordNotifier, times(1)).sendNotification(contains("Repository Deleted"), eq(false), eq(true));
    }
}