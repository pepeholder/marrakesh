package org.example.marrakech.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    // Клиенты будут подписываться на топики, начинающиеся с /topic
    config.enableSimpleBroker("/topic");
    // Сообщения, отправляемые с клиента, будут иметь префикс /app
    config.setApplicationDestinationPrefixes("/app");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    // Точка подключения для WebSocket
    registry.addEndpoint("/ws")
        .setAllowedOrigins("*")
        .withSockJS();
  }
}
