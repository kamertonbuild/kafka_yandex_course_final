# Apache Kafka для разработки и архитектуры

## Модуль 1. Базовая эксплуатация Apache Kafka. Финальное задание

### Предварительные требования

1. Docker
2. Docker-Compose
3. Maven

### Запуск кластера

Для создания и запуска кластера необходимо выполнить [cluster-start.bat](cluster/cluster-start.bat).
Описание работы сценария:

1. Остановка и удаление контейнеров кластера.
2. Компиляция и сборка приложения.
3. Создание docker-образа приложения.
4. Запуск kafka-кластера:
    1. 3 экземпляра kafka-брокера.
    2. Zookeeper.
    3. Kafka UI.
5. Пауза (30 сек) для запуска кластера.
6. Создание топика passage (см. [add-passage-topic](cluster/topic/add-passage-topic.bat)).
7. Вывод информации о топике (см. [passages-topic-info](cluster/topic/passages-topic-info.bat)).
8. Запуск приложения.

### Принцип работы приложения

1. В главном классе
   приложения [Main](kafka-course-unit-1%2Fsrc%2Fmain%2Fjava%2Fru%2Fyandex%2Fcourse%2Fkafka%2Fresult%2Funit%2Ffirst%2FMain.java)
   читаются переменные окружения **KC_UNIT1_BOOTSTRAP_URL** / **KC_UNIT1_TOPIC_NAME** с адресом брокера и именем топика.
2. В
   классе [MessageProducer](kafka-course-unit-1%2Fsrc%2Fmain%2Fjava%2Fru%2Fyandex%2Fcourse%2Fkafka%2Fresult%2Funit%2Ffirst%2Fkafka%2Fproducer%2FMessageProducer.java)
   реализован producer. В
   классах [SingleMessageConsumer](kafka-course-unit-1%2Fsrc%2Fmain%2Fjava%2Fru%2Fyandex%2Fcourse%2Fkafka%2Fresult%2Funit%2Ffirst%2Fkafka%2Fconsumer%2FSingleMessageConsumer.java)
   и [BatchMessageConsumer](kafka-course-unit-1%2Fsrc%2Fmain%2Fjava%2Fru%2Fyandex%2Fcourse%2Fkafka%2Fresult%2Funit%2Ffirst%2Fkafka%2Fconsumer%2FBatchMessageConsumer.java)
   consumer'ы.
3. В
   классе [MessageGenerator](kafka-course-unit-1%2Fsrc%2Fmain%2Fjava%2Fru%2Fyandex%2Fcourse%2Fkafka%2Fresult%2Funit%2Ffirst%2Fmessage%2FMessageGenerator.java)
   генерируются случайные сообщения и отправляются в topic с
   помощью [MessageProducer](kafka-course-unit-1%2Fsrc%2Fmain%2Fjava%2Fru%2Fyandex%2Fcourse%2Fkafka%2Fresult%2Funit%2Ffirst%2Fkafka%2Fproducer%2FMessageProducer.java).
4. Отправленные сообщения читаются
   consumer'ами [SingleMessageConsumer](kafka-course-unit-1%2Fsrc%2Fmain%2Fjava%2Fru%2Fyandex%2Fcourse%2Fkafka%2Fresult%2Funit%2Ffirst%2Fkafka%2Fconsumer%2FSingleMessageConsumer.java)
   и [BatchMessageConsumer](kafka-course-unit-1%2Fsrc%2Fmain%2Fjava%2Fru%2Fyandex%2Fcourse%2Fkafka%2Fresult%2Funit%2Ffirst%2Fkafka%2Fconsumer%2FBatchMessageConsumer.java).
5. Producer и consumer'ы запускаются в отдельных потоках.
6. Перед завершением работы приложения потоки завершаются, producer закрывается.

### Проверка правильности работы

1. Файл [topic.txt](cluster/topic/topic.txt)
2. Для проверки работы producer'а / consumer'ов необходимо подключится к одному из экземпляров приложения:
   ```bash
   docker container logs -f cluster-kc-unit-1-1 >> cluster-kc-unit-1-1.log
   ```
   или

   ```bash 
   docker container logs -f cluster-kc-unit-1-2 >> cluster-kc-unit-1-2.log
   ```

   #### [MessageProducer](kafka-course-unit-1/src/main/java/ru/yandex/course/kafka/result/unit/first/kafka/producer/MessageProducer.java)
   Для проверки работы producer'а найти следующую строку в файле лога:
   ``` 
   >>>> Message
   ```
   Пример лога:
   ```
   01:07:55.563 [Thread-0] INFO ru.yandex.course.kafka.result.unit.first.kafka.producer.MessageProducer - >>>> Message PassageDto(id=333, fio=fio333, date=2025-10-21T01:07:55.555470500) SENT, partition: 1, offset: 8027
   ```

   #### [SingleMessageConsumer](kafka-course-unit-1/src/main/java/ru/yandex/course/kafka/result/unit/first/kafka/consumer/SingleMessageConsumer.java)
   Для проверки работы одиночного consumer'а найти следующую строку в файле лога:
   ```
   <<<< SingleMessageConsumer - POLL FINISHED
   ```
   Убедиться в том что сообщение правильно десериализовано, кол-во сообщений равно 1.
   Пример лога:
   ```
   01:07:55.543 [Thread-1] INFO ru.yandex.course.kafka.result.unit.first.kafka.consumer.SingleMessageConsumer - <<<< SingleMessageConsumer - POLL STARTED <<<<
   01:07:55.543 [Thread-1] INFO ru.yandex.course.kafka.result.unit.first.kafka.consumer.SingleMessageConsumer - <<<< 0 - SingleMessageConsumer: {id=878, fio=fio878, date=[2025, 10, 21, 0, 48, 56, 523804800]}, key: 878, value: {id=878, fio=fio878, date=[2025, 10, 21, 0, 48, 56, 523804800]}, offset: 625
   01:07:55.543 [Thread-1] INFO ru.yandex.course.kafka.result.unit.first.kafka.consumer.SingleMessageConsumer - <<<< SingleMessageConsumer - POLL FINISHED: 1 <<<<
   ```

   #### [BatchMessageConsumer](kafka-course-unit-1/src/main/java/ru/yandex/course/kafka/result/unit/first/kafka/consumer/BatchMessageConsumer.java)
   Для проверки работы пакетного consumer'а найти следующую строку в файле лога:
   ```
   <<<<<< BatchMessageConsumer - POLL FINISHED
   ```
   Убедиться в том что сообщения правильно десериализованы, кол-во сообщений не меньше 10.
   Пример лога:
   ```
   01:07:54.695 [Thread-2] INFO ru.yandex.course.kafka.result.unit.first.kafka.consumer.BatchMessageConsumer - <<<<<< BatchMessageConsumer - POLL STARTED <<<<
   01:07:54.695 [Thread-2] INFO ru.yandex.course.kafka.result.unit.first.kafka.consumer.BatchMessageConsumer - <<<<<< 0 - BatchMessageConsumer: {id=746, fio=fio746, date=[2025, 10, 21, 1, 7, 52, 940360400]}, key: 746, offset: 7550
   01:07:54.695 [Thread-2] INFO ru.yandex.course.kafka.result.unit.first.kafka.consumer.BatchMessageConsumer - <<<<<< 1 - BatchMessageConsumer: {id=379, fio=fio379, date=[2025, 10, 21, 1, 7, 52, 942357200]}, key: 379, offset: 7551
   01:07:54.695 [Thread-2] INFO ru.yandex.course.kafka.result.unit.first.kafka.consumer.BatchMessageConsumer - <<<<<< 2 - BatchMessageConsumer: {id=543, fio=fio543, date=[2025, 10, 21, 1, 7, 53, 487301200]}, key: 543, offset: 7552
   01:07:54.695 [Thread-2] INFO ru.yandex.course.kafka.result.unit.first.kafka.consumer.BatchMessageConsumer - <<<<<< 3 - BatchMessageConsumer: {id=820, fio=fio820, date=[2025, 10, 21, 1, 7, 53, 596321100]}, key: 820, offset: 7553
   01:07:54.695 [Thread-2] INFO ru.yandex.course.kafka.result.unit.first.kafka.consumer.BatchMessageConsumer - <<<<<< 4 - BatchMessageConsumer: {id=594, fio=fio594, date=[2025, 10, 21, 1, 7, 53, 814317700]}, key: 594, offset: 7554
   01:07:54.695 [Thread-2] INFO ru.yandex.course.kafka.result.unit.first.kafka.consumer.BatchMessageConsumer - <<<<<< 5 - BatchMessageConsumer: {id=64, fio=fio64, date=[2025, 10, 21, 1, 7, 53, 814294600]}, key: 64, offset: 7555
   01:07:54.695 [Thread-2] INFO ru.yandex.course.kafka.result.unit.first.kafka.consumer.BatchMessageConsumer - <<<<<< 6 - BatchMessageConsumer: {id=806, fio=fio806, date=[2025, 10, 21, 1, 7, 53, 922568700]}, key: 806, offset: 7556
   01:07:54.695 [Thread-2] INFO ru.yandex.course.kafka.result.unit.first.kafka.consumer.BatchMessageConsumer - <<<<<< 7 - BatchMessageConsumer: {id=556, fio=fio556, date=[2025, 10, 21, 1, 7, 54, 251543800]}, key: 556, offset: 7557
   01:07:54.695 [Thread-2] INFO ru.yandex.course.kafka.result.unit.first.kafka.consumer.BatchMessageConsumer - <<<<<< 8 - BatchMessageConsumer: {id=722, fio=fio722, date=[2025, 10, 21, 1, 7, 54, 468325000]}, key: 722, offset: 7558
   01:07:54.695 [Thread-2] INFO ru.yandex.course.kafka.result.unit.first.kafka.consumer.BatchMessageConsumer - <<<<<< 9 - BatchMessageConsumer: {id=270, fio=fio270, date=[2025, 10, 21, 1, 7, 54, 686543500]}, key: 270, offset: 7559
   ...
   01:07:54.696 [Thread-2] INFO ru.yandex.course.kafka.result.unit.first.kafka.consumer.BatchMessageConsumer - <<<<<< BatchMessageConsumer - POLL FINISHED: 10 <<<<
   ```