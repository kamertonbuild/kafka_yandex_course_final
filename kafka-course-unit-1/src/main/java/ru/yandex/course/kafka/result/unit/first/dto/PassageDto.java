package ru.yandex.course.kafka.result.unit.first.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;

/**
 * Сообщение для отправки kafka-брокеру.
 */
@Value
@Builder
@RequiredArgsConstructor
public class PassageDto {

    /**
     * Идентификатор прохода.
     */
    long id;

    /**
     * ФИО ФЛ.
     */
    String fio;

    /**
     * Дата/время прохода.
     */
    LocalDateTime date;
}
