package com.hit.sse.controller;

import com.hit.sse.constant.CommonConstant.EmitterEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class SSEController {
  public final Map<String, SseEmitter> emitters = new HashMap<>();

  // Method for client subscription
  @GetMapping("/subscribe")
  public SseEmitter subscribe(@RequestParam("userId") String userId) {
    SseEmitter sseEmitter = new SseEmitter(Long.MAX_VALUE);
    try {
      sseEmitter.send(SseEmitter.event().name(EmitterEvent.INIT));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    emitters.put(userId, sseEmitter);

    sseEmitter.onCompletion(() -> emitters.remove(userId));
    sseEmitter.onTimeout(() -> emitters.remove(userId));
    sseEmitter.onError((e) -> emitters.remove(userId));

    return sseEmitter;
  }

  // Method for dispatching events to client
  @PostMapping("/dispatchEvent")
  public void dispatchEventToClients(@RequestParam("data") String data, @RequestParam("userId") String userId) {
    SseEmitter emitter = emitters.get(userId);

    if (emitter != null) {
      try {
        emitter.send(SseEmitter.event().name(EmitterEvent.NEW_DATA).data(data));
      } catch (IOException e) {
        emitters.remove(userId);
      }
    }

  }

}
