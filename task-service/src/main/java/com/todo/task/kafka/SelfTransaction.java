package com.todo.task.kafka;

import com.todo.task.entity.OutboxEvent;
import com.todo.task.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class SelfTransaction {
private final OutboxRepository outboxRepository;
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void markAsPublished(OutboxEvent event) {
        event.setPublishedAt(Instant.now());
        outboxRepository.save(event);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void incrementRetry(OutboxEvent event) {
        event.setRetryCount(event.getRetryCount() + 1);
        // Если превысили лимит, помечаем как опубликованное (но записываем ошибку)
        if (event.getRetryCount() > 10) {
            log.error("🔥 Event {} failed after 10 retries. Giving up.", event.getId());
            event.setPublishedAt(Instant.now());
        }
        outboxRepository.save(event);
    }
}
