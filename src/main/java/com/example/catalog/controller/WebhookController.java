package com.example.catalog.controller;

import com.example.catalog.services.DiscordNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/gitea")
public class WebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);
    private final DiscordNotifier discordNotifier;
    private static final String TARGET_USER = "moslem";
    private static final String COMMON_REPO_NAME = "giteaFinalProject";

    private final ConcurrentHashMap<String, Long> recentWebhookEvents = new ConcurrentHashMap<>();

    public WebhookController(DiscordNotifier discordNotifier) {
        this.discordNotifier = discordNotifier;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody Map<String, Object> payload) {
        try {
            if (payload == null || payload.isEmpty()) {
                logger.warn("Received empty webhook payload.");
                return ResponseEntity.badRequest().body("Invalid payload: Request body is empty.");
            }

            String deliveryId = getDeliveryId(payload);
            long currentTime = System.currentTimeMillis();

            if (recentWebhookEvents.containsKey(deliveryId) &&
                    (currentTime - recentWebhookEvents.get(deliveryId)) < TimeUnit.SECONDS.toMillis(5)) {
                logger.info("Duplicate webhook detected and ignored: {}", deliveryId);
                return ResponseEntity.ok("Duplicate webhook ignored");
            }

            recentWebhookEvents.put(deliveryId, currentTime);

            String actor = getActor(payload);
            String repoName = getRepoName(payload);
            String eventType = getEventType(payload);

            if (actor.equals("Unknown User") || repoName.equals("Unknown Repo")) {
                logger.warn("Invalid webhook payload: Missing actor or repository name.");
                return ResponseEntity.badRequest().body("Invalid webhook payload: Missing required fields.");
            }

            String message = formatWebhookMessage(payload, eventType);
            boolean sendToCommonRepo = COMMON_REPO_NAME.equals(repoName);
            boolean sendToMyEvents = TARGET_USER.equals(actor);

            discordNotifier.sendNotification(message, sendToCommonRepo, sendToMyEvents);
            logger.info("Webhook processed successfully for event: {}", eventType);

            return ResponseEntity.ok("Webhook received and processed");

        } catch (Exception e) {
            logger.error("Error processing webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing webhook");
        }
    }

    private String getDeliveryId(Map<String, Object> payload) {
        return String.valueOf(payload.hashCode());
    }

    private String getActor(Map<String, Object> payload) {
        try {
            if (payload.get("sender") instanceof Map) {
                Map<String, Object> sender = (Map<String, Object>) payload.get("sender");
                return sender.getOrDefault("login", "Unknown User").toString();
            }
        } catch (Exception e) {
            logger.warn("Error extracting actor from webhook payload", e);
        }
        return "Unknown User";
    }

    private String getRepoName(Map<String, Object> payload) {
        try {
            if (payload.get("repository") instanceof Map) {
                Map<String, Object> repository = (Map<String, Object>) payload.get("repository");
                return repository.getOrDefault("name", "Unknown Repo").toString();
            }
        } catch (Exception e) {
            logger.warn("Error extracting repository name from webhook payload", e);
        }
        return "Unknown Repo";
    }

    private String getEventType(Map<String, Object> payload) {
        try {
            if (payload.containsKey("before") && payload.containsKey("after")) {
                return "push";
            }
            if ("branch".equals(payload.get("ref_type"))) {
                return payload.containsKey("pusher_type") && "user".equals(payload.get("pusher_type")) ? "delete_branch" : "create_branch";
            }
            if ("tag".equals(payload.get("ref_type"))) {
                return payload.containsKey("pusher_type") && "user".equals(payload.get("pusher_type")) ? "delete_tag" : "create_tag";
            }
            if (payload.containsKey("action") && payload.containsKey("repository")) {
                String action = payload.get("action").toString();
                if ("created".equals(action)) {
                    return "repo_created";
                } else if ("deleted".equals(action)) {
                    return "repo_deleted";
                }
            }
        } catch (Exception e) {
            logger.warn("Error determining event type from webhook payload", e);
        }
        return "unknown";
    }

    private String formatWebhookMessage(Map<String, Object> payload, String eventType) {
        String repoName = getRepoName(payload);
        String actor = getActor(payload);
        String ref = payload.getOrDefault("ref", "Unknown Ref").toString();
        String message = "⚡ **Unhandled Event** in **" + repoName + "** by **" + actor + "**.";

        switch (eventType) {
            case "push":
                message = "🚀 **Push Event** by **" + actor + "** in **" + repoName + "**\n"
                        + "🔹 **Branch:** " + ref;
                break;
            case "create_branch":
                message = "🌿 **New Branch Created** by **" + actor + "** in **" + repoName + "**\n"
                        + "🔹 **Branch:** " + ref;
                break;
            case "delete_branch":
                message = "❌ **Branch Deleted** by **" + actor + "** in **" + repoName + "**\n"
                        + "🗑️ **Branch:** " + ref;
                break;
            case "create_tag":
                message = "🏷️ **New Tag Created** by **" + actor + "** in **" + repoName + "**\n"
                        + "🔹 **Tag:** " + ref;
                break;
            case "delete_tag":
                message = "🗑️ **Tag Deleted** by **" + actor + "** in **" + repoName + "**\n"
                        + "🚫 **Tag:** " + ref;
                break;
            case "repo_created":
                message = "📁 **New Repository Created** by **" + actor + "**\n"
                        + "🔹 **Repository:** " + repoName;
                break;
            case "repo_deleted":
                message = "🚨 **Repository Deleted** by **" + actor + "**\n"
                        + "🗑️ **Repository:** " + repoName;
                break;
        }

        return message;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGlobalException(Exception e) {
        logger.error("Unhandled exception in WebhookController", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error occurred");
    }
}
