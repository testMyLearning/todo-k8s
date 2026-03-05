package com.todo.task.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.todo.task.entity.OutboxEvent;
import com.todo.task.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.PessimisticLockException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String,String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final SelfTransaction selfTransaction;




    @Retryable(retryFor = {
            PessimisticLockException.class,
            CannotAcquireLockException.class
}, maxAttempts =3, backoff = @Backoff(delay = 500,multiplier = 2))
    @Scheduled(fixedDelay = 15000)
    @Transactional(timeout = 30)
    public void publishEvents(){
        List<OutboxEvent> events = outboxRepository.findUnpublishedEvents(100);
         if(events.isEmpty()){
             return;
         }
        log.info("📤 Publishing {} events to Kafka", events.size());

        for (OutboxEvent event : events) {
            try {
                // Отправляем в Kafka
                CompletableFuture<SendResult<String, String>> future =
                        kafkaTemplate.send(event.getEventType(), event.getPayload());

                // Асинхронно обрабатываем результат
                future.whenComplete((result, ex) -> {
                    if (ex == null) {
                        // Успешно отправили
                        selfTransaction.markAsPublished(event);
                        log.debug("✅ Event {} published to Kafka", event.getId());
                    } else {
                        // Ошибка отправки
                        log.error("❌ Failed to publish event {}", event.getId(), ex);
                        selfTransaction.incrementRetry(event);
                    }
                });

            } catch (Exception e) {
                log.error("❌ Exception publishing event {}", event.getId(), e);
                selfTransaction.incrementRetry(event);
            }
        }
    }


    }



