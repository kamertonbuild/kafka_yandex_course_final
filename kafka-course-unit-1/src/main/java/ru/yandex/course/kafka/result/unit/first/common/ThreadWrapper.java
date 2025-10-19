package ru.yandex.course.kafka.result.unit.first.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Класс-обертка для управления потоком.
 */
@Slf4j
@RequiredArgsConstructor
public class ThreadWrapper extends Thread {

    private boolean isActive = true;
    private final Runnable runnable;
    private final long pause;

    /**
     * Запускает поток.
     */
    @Override
    public void run() {
        while (isActive) {
            try {
                runnable.run();
                Thread.sleep(pause);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Останавливает поток.
     */
    public synchronized void deactivate() {
        isActive = false;
    }
}
