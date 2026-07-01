package com.example.chatter.listener;

import com.example.chatter.entity.UserEntity;
import com.example.chatter.io.StatusResponse;
import com.example.chatter.repository.MessageRepository;
import com.example.chatter.repository.UserRepository;
import com.example.chatter.service.PresenceService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {
    private final PresenceService presenceService;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;


    @EventListener
    public void handleSessionConnected(
            SessionConnectEvent event) {
        StompHeaderAccessor accessor =
                StompHeaderAccessor.wrap(
                        event.getMessage());

        String id = accessor.getFirstNativeHeader("id");

        if (id == null) {
            return;
        }

        presenceService.addSession(
                accessor.getSessionId(),
                id
        );

        UserEntity user =
                userRepository.findById(id);

        if (user != null) {

            user.setOnline(true);

            userRepository.save(user);

            messageRepository.updateStatus(
                    id,
                    "online"
            );
        }

        messagingTemplate.convertAndSend(
                "/topic/status",
                new StatusResponse(
                        id,
                        true
                )
        );
    }

    @EventListener
    public void handleSessionDisconnected(
            SessionDisconnectEvent event) {
        StompHeaderAccessor accessor =
                StompHeaderAccessor.wrap(
                        event.getMessage());

        String id =
                presenceService.removeSession(
                        accessor.getSessionId()
                );

        if (id == null) {
            return;
        }

        markOfflineAfterGracePeriod(id);
    }

    @Async
    public void markOfflineAfterGracePeriod(
            String id) {

        try {
            Thread.sleep(5000);
        }
        catch (InterruptedException e) {
            return;
        }

        if (presenceService.isOnline(id)) {
            messagingTemplate.convertAndSend(
                    "/topic/status",
                    new StatusResponse(
                            id,
                            true
                    )
            );
            return;
        }

        UserEntity user =
                userRepository.findById(id);

        if (user != null) {

            user.setOnline(false);

            user.setLastSeen(
                    System.currentTimeMillis()
            );

            userRepository.save(user);
        }

        messagingTemplate.convertAndSend(
                "/topic/status",
                new StatusResponse(
                        id,
                        false
                )
        );
    }
}