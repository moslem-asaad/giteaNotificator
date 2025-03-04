package com.example.catalog.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class DiscordNotifier {

    @Value("${general.channel.url}")
    private String DISCORD_WEBHOOK_URL;
    @Value("${my.events.url}")
    private String MY_EVENTS_CHANNEL;

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

    public void setDISCORD_WEBHOOK_URL(String DISCORD_WEBHOOK_URL) {
        this.DISCORD_WEBHOOK_URL = DISCORD_WEBHOOK_URL;
    }

    public void setMY_EVENTS_CHANNEL(String MY_EVENTS_CHANNEL) {
        this.MY_EVENTS_CHANNEL = MY_EVENTS_CHANNEL;
    }
}
