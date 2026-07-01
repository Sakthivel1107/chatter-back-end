package com.example.chatter.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class PresenceService {

    private final ConcurrentMap<String, String> sessions =
            new ConcurrentHashMap<>();

    public void addSession(
            String sessionId,
            String id) {

        sessions.put(sessionId, id);
    }

    public String removeSession(
            String sessionId) {

        return sessions.remove(sessionId);
    }

    public boolean isOnline(
            String id) {

        return sessions.containsValue(id);
    }
}