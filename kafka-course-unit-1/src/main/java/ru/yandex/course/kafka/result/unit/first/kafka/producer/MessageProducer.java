package ru.yandex.course.kafka.result.unit.first.kafka.producer;

import com.google.common.collect.ImmutableMap;
import io.confluent.kafka.serializers.KafkaJsonSerializer;
import java.util.Map;
import java.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import ru.yandex.course.kafka.result.unit.first.dto.PassageDto;

/**
 * Отправляет сообщение о проходе {@link PassageDto} в topic.
 */
@Slf4j
public class MessageProducer {

    private static final String ACKS_ALL = "all";
    private static final int RETRIES_NUMBER = 3;
    private static final String MIN_IN_SYNC_REPLICAS_NUMBER = "2";

    private final String topicName;
    private final Producer<Long, PassageDto> producer;

    /**
     * Конструктор.
     *
     * @param topicName    имя topic'а
     * @param bootstrapUrl адрес kafka-брокера
     */
    public MessageProducer(String topicName, String bootstrapUrl) {
        Map<String, Object> settings =
            ImmutableMap.of(
                // Адрес kafka-брокера.
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapUrl,

                // Режим At Least Once.
                // Ожидание синхронизации коли-ва реплик, заданных в параметре MIN_IN_SYNC_REPLICAS_CONFIG.
                ProducerConfig.ACKS_CONFIG, ACKS_ALL,
                // Количество повторных попыток выполнить запрос, который завершился неудачей.
                ProducerConfig.RETRIES_CONFIG, RETRIES_NUMBER,
                // Минимальное количество синхронизированных реплик.
                TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, MIN_IN_SYNC_REPLICAS_NUMBER,
                // Класс сериализатора для ключа.
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName(),
                // Класс сериализатора для сообщения.
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaJsonSerializer.class.getName()
            );

        this.producer = new KafkaProducer<>(settings);
        this.topicName = topicName;
    }

    /**
     * Отправляет сообщение о проходе в topic'а.
     *
     * @param passage сообщение о проходе
     */
    public void send(PassageDto passage) {
        try {
            ProducerRecord<Long, PassageDto> record = new ProducerRecord<>(topicName, passage.getId(), passage);
            Future<RecordMetadata> send = producer.send(record);
            RecordMetadata metadata = send.get();
            log.info(">>>> Message {} SENT, partition: {}, offset: {}\n", passage, metadata.partition(),
                metadata.offset());
        } catch (Exception e) {
            log.error("{} - error sending message: {}", getClass().getSimpleName(), passage, e);
        }
    }

    /**
     * Закрывает продюсер.
     */
    public void close() {
        producer.close();
    }
}
