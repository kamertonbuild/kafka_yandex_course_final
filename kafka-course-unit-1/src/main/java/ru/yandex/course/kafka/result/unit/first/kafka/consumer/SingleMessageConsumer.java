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
 * Consumer для обработки единичного сообщения о проходе.
 * <p>
 * Читает и обрабатывает одно сообщения о проходе {@link PassageDto}.
 * </p>
 */
@Slf4j
public class SingleMessageConsumer {

    private final Consumer<Long, PassageDto> consumer;

    /**
     * Конструктор.
     *
     * @param topicName    имя topic'а
     * @param bootstrapUrl адрес kafka-брокера
     */
    public SingleMessageConsumer(String topicName, String bootstrapUrl) {
        Map<String, Object> settings =
            ImmutableMap.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapUrl,
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName(),
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, KafkaJsonDeserializer.class.getName(),
                ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1,
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true,
                ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 100,
                ConsumerConfig.GROUP_ID_CONFIG, getClass().getSimpleName()
            );

        try {
            consumer = new KafkaConsumer<>(settings);
            consumer.subscribe(Collections.singletonList(topicName));
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Читает и обрабатывает сообщение о проходе.
     */
    public void poll() {
        try {
            ConsumerRecords<Long, PassageDto> poll = consumer.poll(Duration.ofMillis(1000));

            int pollCount = poll.count();
            if (pollCount > 1) {
                log.error("{} - poll count > 1", getClass().getSimpleName());
            }

            log.info("<<<< {} - POLL STARTED <<<<", getClass().getSimpleName());
            AtomicInteger count = new AtomicInteger();
            poll.forEach(passage -> {
                log.info("<<<< {} - {}: {}, key: {}, value: {}, offset: {}",
                    count.get(), getClass().getSimpleName(), passage.value(), passage.key(), passage.value(),
                    passage.offset());
                count.getAndIncrement();
            });
            log.info("<<<< {} - POLL FINISHED: {} <<<<\n", this.getClass().getSimpleName(), count);
        } catch (Exception e) {
            log.error("{} - error poll messages: {}", getClass().getSimpleName(), e);
        }
    }
}
