package ru.yandex.course.kafka.result.unit.first;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import ru.yandex.course.kafka.result.unit.first.common.ThreadWrapper;
import ru.yandex.course.kafka.result.unit.first.kafka.consumer.BatchMessageConsumer;
import ru.yandex.course.kafka.result.unit.first.kafka.consumer.SingleMessageConsumer;
import ru.yandex.course.kafka.result.unit.first.kafka.producer.MessageProducer;
import ru.yandex.course.kafka.result.unit.first.message.MessageGenerator;

/**
 * Главный класс приложения.
 */
@Slf4j
public class Main {

    private static final String BOOTSTRAP_URL_ENV_NAME = "KC_UNIT1_BOOTSTRAP_URL";
    private static final String TOPIC_NAME_ENV_NAME = "KC_UNIT1_TOPIC_NAME";

    private static final int MESSAGE_DELAY_PRODUCER_MS = 100;
    private static final int PERIOD_POLL_OF_SINGLETON_CONSUMER_MS = 1500;
    private static final int PERIOD_POLL_OF_BATCH_CONSUMER_MS = 1000;

    private static final int TIMEOUT_THREAD_MS = 60000;

    /**
     * Точка входа.
     *
     * @param args параметры командной строки
     */
    public static void main(String[] args) {
        Map<String, String> environment = System.getenv();
        String bootstrapUrl = environment.get(BOOTSTRAP_URL_ENV_NAME);
        log.info("BOOTSTRAP_URL_ENV_NAME: {}", bootstrapUrl);

        String topicName = environment.get(TOPIC_NAME_ENV_NAME);
        log.info("TOPIC_NAME_ENV_NAME: {}", topicName);
        if (Strings.isBlank(bootstrapUrl) || Strings.isBlank(topicName)) {
            log.error("Environment variables: {}, {} were not initialized", BOOTSTRAP_URL_ENV_NAME,
                TOPIC_NAME_ENV_NAME);
            System.exit(1);
        }

        MessageProducer messageProducer = new MessageProducer(topicName, bootstrapUrl);
        MessageGenerator messageGenerator = new MessageGenerator(messageProducer);
        ThreadWrapper producerThread = new ThreadWrapper(messageGenerator::generate, MESSAGE_DELAY_PRODUCER_MS);

        SingleMessageConsumer singleMessageConsumer = new SingleMessageConsumer(topicName, bootstrapUrl);
        ThreadWrapper singleConsumerThread =
            new ThreadWrapper(singleMessageConsumer::poll, PERIOD_POLL_OF_SINGLETON_CONSUMER_MS);

        BatchMessageConsumer batchMessageConsumer = new BatchMessageConsumer(topicName, bootstrapUrl);
        ThreadWrapper batchConsumerThread =
            new ThreadWrapper(batchMessageConsumer::poll, PERIOD_POLL_OF_BATCH_CONSUMER_MS);

        Thread shutdownHook =
            new Thread(() -> shutdownHook(messageProducer, producerThread, singleConsumerThread, batchConsumerThread));
        Runtime.getRuntime().addShutdownHook(shutdownHook);

        producerThread.start();
        singleConsumerThread.start();
        batchConsumerThread.start();
    }

    /**
     * Завешает потоки producer'а и consumer'ов. Закрывает producer.
     *
     * @param messageProducer      producer сообщений о проходе
     * @param producerThread       поток producer'а сообщений о проходе
     * @param singleConsumerThread поток consumer'а для обработки сообщений о проходе
     * @param batchConsumerThread  поток consumer'а для пакетной обработки сообщений о проходе
     */
    private static void shutdownHook(MessageProducer messageProducer,
                                     ThreadWrapper producerThread,
                                     ThreadWrapper singleConsumerThread,
                                     ThreadWrapper batchConsumerThread) {
        producerThread.deactivate();
        singleConsumerThread.deactivate();
        batchConsumerThread.deactivate();
        try {
            producerThread.join(TIMEOUT_THREAD_MS);
            singleConsumerThread.join(TIMEOUT_THREAD_MS);
            batchConsumerThread.join(TIMEOUT_THREAD_MS);
        } catch (InterruptedException e) {
            log.info(e.getMessage());
        } finally {
            messageProducer.close();
            log.info("MessageProducer closed.");
        }
    }
}