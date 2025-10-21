package ru.yandex.course.kafka.result.unit.first.kafka.consumer;

import com.google.common.collect.ImmutableMap;
import io.confluent.kafka.serializers.KafkaJsonDeserializer;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.LongDeserializer;
import ru.yandex.course.kafka.result.unit.first.dto.PassageDto;

/**
 * Consumer для пакетной обработки сообщений о проходе.
 * <p>
 * Читает и обрабатывает не менее 10 сообщений о проходе {@link PassageDto}
 * </p>
 */
@Slf4j
public class BatchMessageConsumer {

    private static final int FETCH_MIN_BYTES = 1300;
    private static final int FETCH_MAX_WAIT_MS = 10 * 60000;
    private static final int MAX_POLL_INTERVAL_MS = 10 * 60000;

    private final Consumer<Long, PassageDto> consumer;

    /**
     * Конструктор.
     *
     * @param topicName    имя topic'а
     * @param bootstrapUrl адрес kafka-брокера
     */
    public BatchMessageConsumer(String topicName, String bootstrapUrl) {
        Map<String, Object> settings =
            ImmutableMap.of(
                // Адрес kafka-брокера.
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapUrl,
                // Класс десериализатора для ключа.
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName(),
                // Класс десериализатора для сообщения.
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaJsonDeserializer.class.getName(),
                // Идентификатор группы consumer'ов.
                ConsumerConfig.GROUP_ID_CONFIG, this.getClass().getSimpleName(),
                // Отключена автоматическая фиксация смещения.
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false,
                // Минимальный размер данных, возвращаемых за один вызов poll().
                ConsumerConfig.FETCH_MIN_BYTES_CONFIG, FETCH_MIN_BYTES,
                // Максимальное время ожидания для получения данных.
                ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, FETCH_MAX_WAIT_MS,
                // Максимальная задержка между вызовами poll().
                ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, MAX_POLL_INTERVAL_MS);

        try {
            this.consumer = new KafkaConsumer<>(settings);
            consumer.subscribe(Collections.singletonList(topicName));
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Читает и обрабатывает сообщения о проходах.
     */
    public void poll() {
        try {
            ConsumerRecords<Long, PassageDto> poll = consumer.poll(Duration.ofMillis(1000));

            int pollCount = poll.count();
            if (pollCount < 10) {
                log.error("{} - poll count > 1", this.getClass().getSimpleName());
            }

            log.info("<<<<<< {} - POLL STARTED <<<<", this.getClass().getSimpleName());
            AtomicInteger count = new AtomicInteger();
            poll.forEach(passage -> {
                log.info("<<<<<< {} - {}: {}, key: {}, offset: {}",
                    count.get(), this.getClass().getSimpleName(), passage.value(), passage.key(), passage.offset());
                count.getAndIncrement();
            });
            consumer.commitSync();
            log.info("<<<<<< {} - POLL FINISHED: {} <<<<\n", this.getClass().getSimpleName(), count);
        } catch (Exception e) {
            log.error("{} - error poll messages: {}", getClass().getSimpleName(), e);
        }
    }
}
