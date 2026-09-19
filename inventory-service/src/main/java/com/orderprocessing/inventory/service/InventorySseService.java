package com.orderprocessing.inventory.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class InventorySseService {

    private final List<SseEmitter> emitters =
            new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {

        SseEmitter emitter = new SseEmitter(0L);

        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(error -> emitters.remove(emitter));

        try {
            emitter.send(
                    SseEmitter.event()
                            .name("CONNECTED")
                            .data("Inventory SSE connected")
            );
        } catch (IOException exception) {
            emitters.remove(emitter);
        }
        return emitter;
    }

    public void publish(String eventName, Object data) {

        for (SseEmitter emitter : emitters) {

            try {
                emitter.send(
                        SseEmitter.event()
                                .name(eventName)
                                .data(data)
                );

            } catch (IOException exception) {

                emitters.remove(emitter);
            }
        }
    }
}