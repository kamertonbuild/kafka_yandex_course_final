package ru.yandex.course.kafka.result.unit.first.message;

import java.time.LocalDateTime;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.course.kafka.result.unit.first.dto.PassageDto;
import ru.yandex.course.kafka.result.unit.first.kafka.producer.MessageProducer;

/**
 * Генератор сообщений о проходе.
 */
@Slf4j
@RequiredArgsConstructor
public class MessageGenerator {

    private final MessageProducer messageProducer;
    private final Random random = new Random();

    /**
     * Создает сообщение о проходе и передает в producer {@link MessageProducer}.
     */
    public void generate() {
        long id = random.nextLong(1000L);
        PassageDto passageDto = PassageDto.builder()
            .id(id)
            .fio("fio" + id)
            .date(LocalDateTime.now())
            .build();

        messageProducer.send(passageDto);
    }
}
