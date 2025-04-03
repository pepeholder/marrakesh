package org.example.marrakech.controller;

import org.example.marrakech.dto.MoveUpdateMessage;
import org.example.marrakech.entity.Move;
import org.example.marrakech.service.MoveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class MoveWebSocketController {

  private final SimpMessagingTemplate messagingTemplate;
  private final MoveService moveService;

  @Autowired
  public MoveWebSocketController(SimpMessagingTemplate messagingTemplate, MoveService moveService) {
    this.messagingTemplate = messagingTemplate;
    this.moveService = moveService;
  }

  /**
   * Клиенты отправляют сообщения на /app/move с данными хода.
   * Контроллер сохраняет ход и рассылает обновление всем подписанным на топик /topic/game/{gameId}/moves.
   */
  @MessageMapping("/move")
  public void processMove(Move move) {
    // Сохраняем ход через сервис
    Move savedMove = moveService.saveMove(move);

    // Формируем сообщение обновления
    MoveUpdateMessage updateMessage = new MoveUpdateMessage(
        savedMove.getMoveId(),
        savedMove.getGame().getId(),
        savedMove.getUser().getId(),
        savedMove.getTurnNumber(),
        savedMove.getMoveDescription(),
        savedMove.getTimestamp().toString()
    );

    // Отправляем сообщение на топик конкретной игры
    messagingTemplate.convertAndSend("/topic/game/" + savedMove.getGame().getId() + "/moves", updateMessage);
  }
}
