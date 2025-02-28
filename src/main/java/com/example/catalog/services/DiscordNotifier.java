package com.example.catalog.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class DiscordNotifier {
    private final String DISCORD_WEBHOOK_URL = "https://discord.com/api/webhooks/1340961632670453830/bhapSzwsAgO9DR7KcQHNUWjc5-5QPZn7m_dwtECloq2sIr0STbuWjOYHNHQeCr7d-PBX";  // Common repo webhook
    private final String MY_EVENTS_CHANNEL = "https://discord.com/api/webhooks/1345056655255273513/ZD92m_4yrfc5A8RCHiryW6lgjEE2ulBkhGrxUeIBqjpsThM9Awiuc4IWvqgR0K5fDmnQ"; // Moslem's personal channel

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendNotification(String message, boolean sendToCommonRepo, boolean sendToMyEvents) {
        Map<String, String> body = new HashMap<>();
        body.put("content", message);

        if (sendToCommonRepo) {
            restTemplate.postForObject(DISCORD_WEBHOOK_URL, body, String.class);
        }
        if (sendToMyEvents) {
            restTemplate.postForObject(MY_EVENTS_CHANNEL, body, String.class);
        }
    }
}
